/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.scan.TableEntriesScan;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.HiveLocationUtil;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.ArcticTableUtil;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableSet;
import org.apache.iceberg.relocated.com.google.common.primitives.Longs;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Table maintainer for mixed-iceberg and mixed-hive tables. */
public class MixedTableMaintainer implements TableMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(MixedTableMaintainer.class);

  private final ArcticTable arcticTable;

  private ChangeTableMaintainer changeMaintainer;

  private final BaseTableMaintainer baseMaintainer;

  private final Set<String> changeFiles;

  private final Set<String> baseFiles;

  private final Set<String> hiveFiles;

  public MixedTableMaintainer(ArcticTable arcticTable) {
    this.arcticTable = arcticTable;
    if (arcticTable.isKeyedTable()) {
      ChangeTable changeTable = arcticTable.asKeyedTable().changeTable();
      BaseTable baseTable = arcticTable.asKeyedTable().baseTable();
      changeMaintainer = new ChangeTableMaintainer(changeTable);
      baseMaintainer = new BaseTableMaintainer(baseTable);
      changeFiles =
          ImmutableSet.<String>builder()
              .addAll(IcebergTableUtil.getAllContentFilePath(changeTable))
              .addAll(IcebergTableUtil.getAllStatisticsFilePath(changeTable))
              .build();
      baseFiles =
          ImmutableSet.<String>builder()
              .addAll(IcebergTableUtil.getAllContentFilePath(baseTable))
              .addAll(IcebergTableUtil.getAllStatisticsFilePath(baseTable))
              .build();
    } else {
      baseMaintainer = new BaseTableMaintainer(arcticTable.asUnkeyedTable());
      changeFiles = new HashSet<>();
      baseFiles =
          ImmutableSet.<String>builder()
              .addAll(IcebergTableUtil.getAllContentFilePath(arcticTable.asUnkeyedTable()))
              .addAll(IcebergTableUtil.getAllStatisticsFilePath(arcticTable.asUnkeyedTable()))
              .build();
    }

    if (TableTypeUtil.isHive(arcticTable)) {
      hiveFiles = HiveLocationUtil.getHiveLocation(arcticTable);
    } else {
      hiveFiles = new HashSet<>();
    }
  }

  @Override
  public void cleanOrphanFiles(TableRuntime tableRuntime) {
    if (changeMaintainer != null) {
      changeMaintainer.cleanOrphanFiles(tableRuntime);
    }
    baseMaintainer.cleanOrphanFiles(tableRuntime);
  }

  @Override
  public void expireSnapshots(TableRuntime tableRuntime) {
    if (changeMaintainer != null) {
      changeMaintainer.expireSnapshots(tableRuntime);
    }
    baseMaintainer.expireSnapshots(tableRuntime);
  }

  protected void expireSnapshots(long mustOlderThan) {
    if (changeMaintainer != null) {
      changeMaintainer.expireSnapshots(mustOlderThan);
    }
    baseMaintainer.expireSnapshots(mustOlderThan);
  }

  protected void cleanContentFiles(long lastTime) {
    if (changeMaintainer != null) {
      changeMaintainer.cleanContentFiles(lastTime);
    }
    baseMaintainer.cleanContentFiles(lastTime);
  }

  protected void cleanMetadata(long lastTime) {
    if (changeMaintainer != null) {
      changeMaintainer.cleanMetadata(lastTime);
    }
    baseMaintainer.cleanMetadata(lastTime);
  }

  protected void cleanDanglingDeleteFiles() {
    if (changeMaintainer != null) {
      changeMaintainer.cleanDanglingDeleteFiles();
    }
    baseMaintainer.cleanDanglingDeleteFiles();
  }

  public ChangeTableMaintainer getChangeMaintainer() {
    return changeMaintainer;
  }

  public BaseTableMaintainer getBaseMaintainer() {
    return baseMaintainer;
  }

  public class ChangeTableMaintainer extends IcebergTableMaintainer {

    private static final int DATA_FILE_LIST_SPLIT = 3000;

    private final UnkeyedTable unkeyedTable;

    public ChangeTableMaintainer(UnkeyedTable unkeyedTable) {
      super(unkeyedTable);
      this.unkeyedTable = unkeyedTable;
    }

    @Override
    public Set<String> orphanFileCleanNeedToExcludeFiles() {
      return ImmutableSet.<String>builder()
          .addAll(changeFiles)
          .addAll(baseFiles)
          .addAll(hiveFiles)
          .build();
    }

    @Override
    public void expireSnapshots(TableRuntime tableRuntime) {
      expireSnapshots(Long.MAX_VALUE);
    }

    @Override
    public void expireSnapshots(long mustOlderThan) {
      long changeTTLPoint = getChangeTTLPoint();
      expireFiles(Longs.min(getChangeTTLPoint(), mustOlderThan));
      super.expireSnapshots(Longs.min(changeTTLPoint, mustOlderThan));
    }

    @Override
    protected long olderThanSnapshotNeedToExpire(long mustOlderThan) {
      long latestChangeFlinkCommitTime = fetchLatestFlinkCommittedSnapshotTime(unkeyedTable);
      return Longs.min(latestChangeFlinkCommitTime, mustOlderThan);
    }

    @Override
    protected Set<String> expireSnapshotNeedToExcludeFiles() {
      return ImmutableSet.<String>builder().addAll(baseFiles).addAll(hiveFiles).build();
    }

    public void expireFiles(long ttlPoint) {
      List<IcebergFileEntry> expiredDataFileEntries = getExpiredDataFileEntries(ttlPoint);
      deleteChangeFile(expiredDataFileEntries);
    }

    private long getChangeTTLPoint() {
      return System.currentTimeMillis()
          - CompatiblePropertyUtil.propertyAsLong(
                  unkeyedTable.properties(),
                  TableProperties.CHANGE_DATA_TTL,
                  TableProperties.CHANGE_DATA_TTL_DEFAULT)
              * 60
              * 1000;
    }

    private List<IcebergFileEntry> getExpiredDataFileEntries(long ttlPoint) {
      TableEntriesScan entriesScan =
          TableEntriesScan.builder(unkeyedTable).includeFileContent(FileContent.DATA).build();
      List<IcebergFileEntry> changeTTLFileEntries = new ArrayList<>();

      try (CloseableIterable<IcebergFileEntry> entries = entriesScan.entries()) {
        entries.forEach(
            entry -> {
              Snapshot snapshot = unkeyedTable.snapshot(entry.getSnapshotId());
              if (snapshot == null || snapshot.timestampMillis() < ttlPoint) {
                changeTTLFileEntries.add(entry);
              }
            });
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to close manifest entry scan of " + table.name(), e);
      }
      return changeTTLFileEntries;
    }

    private void deleteChangeFile(List<IcebergFileEntry> expiredDataFileEntries) {
      KeyedTable keyedTable = arcticTable.asKeyedTable();
      if (CollectionUtils.isEmpty(expiredDataFileEntries)) {
        return;
      }

      StructLikeMap<Long> optimizedSequences = ArcticTableUtil.readOptimizedSequence(keyedTable);
      if (MapUtils.isEmpty(optimizedSequences)) {
        LOG.info("table {} not contains max transaction id", keyedTable.id());
        return;
      }

      Map<String, List<IcebergFileEntry>> partitionDataFileMap =
          expiredDataFileEntries.stream()
              .collect(
                  Collectors.groupingBy(
                      entry -> keyedTable.spec().partitionToPath(entry.getFile().partition()),
                      Collectors.toList()));

      List<DataFile> changeDeleteFiles = new ArrayList<>();
      if (keyedTable.spec().isUnpartitioned()) {
        List<IcebergFileEntry> partitionDataFiles =
            partitionDataFileMap.get(
                keyedTable
                    .spec()
                    .partitionToPath(expiredDataFileEntries.get(0).getFile().partition()));

        Long optimizedSequence = optimizedSequences.get(TablePropertyUtil.EMPTY_STRUCT);
        if (optimizedSequence != null && CollectionUtils.isNotEmpty(partitionDataFiles)) {
          changeDeleteFiles.addAll(
              partitionDataFiles.stream()
                  .filter(
                      entry ->
                          FileNameRules.parseChangeTransactionId(
                                  entry.getFile().path().toString(), entry.getSequenceNumber())
                              <= optimizedSequence)
                  .map(entry -> (DataFile) entry.getFile())
                  .collect(Collectors.toList()));
        }
      } else {
        optimizedSequences.forEach(
            (key, value) -> {
              List<IcebergFileEntry> partitionDataFiles =
                  partitionDataFileMap.get(keyedTable.spec().partitionToPath(key));

              if (CollectionUtils.isNotEmpty(partitionDataFiles)) {
                changeDeleteFiles.addAll(
                    partitionDataFiles.stream()
                        .filter(
                            entry ->
                                FileNameRules.parseChangeTransactionId(
                                        entry.getFile().path().toString(),
                                        entry.getSequenceNumber())
                                    <= value)
                        .map(entry -> (DataFile) entry.getFile())
                        .collect(Collectors.toList()));
              }
            });
      }
      tryClearChangeFiles(changeDeleteFiles);
    }

    private void tryClearChangeFiles(List<DataFile> changeFiles) {
      if (CollectionUtils.isEmpty(changeFiles)) {
        return;
      }
      try {
        for (int startIndex = 0;
            startIndex < changeFiles.size();
            startIndex += DATA_FILE_LIST_SPLIT) {
          int end = Math.min(startIndex + DATA_FILE_LIST_SPLIT, changeFiles.size());
          List<DataFile> tableFiles = changeFiles.subList(startIndex, end);
          LOG.info("{} delete {} change files", unkeyedTable.name(), tableFiles.size());
          if (!tableFiles.isEmpty()) {
            DeleteFiles changeDelete = unkeyedTable.newDelete();
            changeFiles.forEach(changeDelete::deleteFile);
            changeDelete.commit();
          }
          LOG.info(
              "{} change committed, delete {} files, complete {}/{}",
              unkeyedTable.name(),
              tableFiles.size(),
              end,
              changeFiles.size());
        }
      } catch (Throwable t) {
        LOG.error(unkeyedTable.name() + " failed to delete change files, ignore", t);
      }
    }
  }

  public class BaseTableMaintainer extends IcebergTableMaintainer {

    public BaseTableMaintainer(UnkeyedTable unkeyedTable) {
      super(unkeyedTable);
    }

    @Override
    public Set<String> orphanFileCleanNeedToExcludeFiles() {
      return ImmutableSet.<String>builder()
          .addAll(changeFiles)
          .addAll(baseFiles)
          .addAll(hiveFiles)
          .build();
    }

    @Override
    protected Set<String> expireSnapshotNeedToExcludeFiles() {
      return ImmutableSet.<String>builder().addAll(changeFiles).addAll(hiveFiles).build();
    }
  }
}
