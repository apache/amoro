/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.optimizing.maintainer;

import static org.apache.amoro.shade.guava32.com.google.common.primitives.Longs.min;
import static org.apache.amoro.utils.MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST;

import org.apache.amoro.IcebergFileEntry;
import org.apache.amoro.TableFormat;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.scan.TableEntriesScan;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.server.table.TableOrphanFilesCleaningMetrics;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.utils.HiveLocationUtil;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Literal;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

/** Table maintainer for mixed-iceberg and mixed-hive tables. */
public class MixedTableMaintainer implements TableMaintainer {

  private static final Logger LOG = LoggerFactory.getLogger(MixedTableMaintainer.class);

  private final MixedTable mixedTable;

  private ChangeTableMaintainer changeMaintainer;

  private final BaseTableMaintainer baseMaintainer;

  public MixedTableMaintainer(MixedTable mixedTable) {
    this.mixedTable = mixedTable;
    if (mixedTable.isKeyedTable()) {
      changeMaintainer = new ChangeTableMaintainer(mixedTable.asKeyedTable().changeTable());
      baseMaintainer = new BaseTableMaintainer(mixedTable.asKeyedTable().baseTable());
    } else {
      baseMaintainer = new BaseTableMaintainer(mixedTable.asUnkeyedTable());
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

  @VisibleForTesting
  protected void expireSnapshots(long mustOlderThan, int minCount) {
    if (changeMaintainer != null) {
      changeMaintainer.expireSnapshots(mustOlderThan, minCount);
    }
    baseMaintainer.expireSnapshots(mustOlderThan, minCount);
  }

  @Override
  public void expireData(TableRuntime tableRuntime) {
    try {
      DataExpirationConfig expirationConfig =
          tableRuntime.getTableConfiguration().getExpiringDataConfig();
      Types.NestedField field =
          mixedTable.schema().findField(expirationConfig.getExpirationField());
      if (!TableConfigurations.isValidDataExpirationField(
          expirationConfig, field, mixedTable.name())) {
        return;
      }

      expireDataFrom(expirationConfig, expireMixedBaseOnRule(expirationConfig, field));
    } catch (Throwable t) {
      LOG.error("Unexpected purge error for table {} ", tableRuntime.getTableIdentifier(), t);
    }
  }

  protected Instant expireMixedBaseOnRule(
      DataExpirationConfig expirationConfig, Types.NestedField field) {
    Instant changeInstant =
        Optional.ofNullable(changeMaintainer).isPresent()
            ? changeMaintainer.expireBaseOnRule(expirationConfig, field)
            : Instant.MIN;
    Instant baseInstant = baseMaintainer.expireBaseOnRule(expirationConfig, field);
    if (changeInstant.compareTo(baseInstant) >= 0) {
      return changeInstant;
    } else {
      return baseInstant;
    }
  }

  @VisibleForTesting
  public void expireDataFrom(DataExpirationConfig expirationConfig, Instant instant) {
    if (instant.equals(Instant.MIN)) {
      return;
    }

    long expireTimestamp = instant.minusMillis(expirationConfig.getRetentionTime()).toEpochMilli();
    Types.NestedField field = mixedTable.schema().findField(expirationConfig.getExpirationField());
    LOG.info(
        "Expiring data older than {} in mixed table {} ",
        Instant.ofEpochMilli(expireTimestamp)
            .atZone(IcebergTableMaintainer.getDefaultZoneId(field))
            .toLocalDateTime(),
        mixedTable.name());

    Expression dataFilter =
        IcebergTableMaintainer.getDataExpression(
            mixedTable.schema(), expirationConfig, expireTimestamp);

    Pair<IcebergTableMaintainer.ExpireFiles, IcebergTableMaintainer.ExpireFiles> mixedExpiredFiles =
        mixedExpiredFileScan(expirationConfig, dataFilter, expireTimestamp);

    expireMixedFiles(mixedExpiredFiles.getLeft(), mixedExpiredFiles.getRight(), expireTimestamp);
  }

  private Pair<IcebergTableMaintainer.ExpireFiles, IcebergTableMaintainer.ExpireFiles>
      mixedExpiredFileScan(
          DataExpirationConfig expirationConfig, Expression dataFilter, long expireTimestamp) {
    return mixedTable.isKeyedTable()
        ? keyedExpiredFileScan(expirationConfig, dataFilter, expireTimestamp)
        : Pair.of(
            new IcebergTableMaintainer.ExpireFiles(),
            getBaseMaintainer().expiredFileScan(expirationConfig, dataFilter, expireTimestamp));
  }

  private Pair<IcebergTableMaintainer.ExpireFiles, IcebergTableMaintainer.ExpireFiles>
      keyedExpiredFileScan(
          DataExpirationConfig expirationConfig, Expression dataFilter, long expireTimestamp) {
    Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness =
        Maps.newConcurrentMap();

    KeyedTable keyedTable = mixedTable.asKeyedTable();
    ChangeTable changeTable = keyedTable.changeTable();
    BaseTable baseTable = keyedTable.baseTable();

    CloseableIterable<MixedFileEntry> changeEntries =
        CloseableIterable.transform(
            changeMaintainer.fileScan(changeTable, dataFilter, expirationConfig),
            e -> new MixedFileEntry(e.getFile(), e.getTsBound(), true));
    CloseableIterable<MixedFileEntry> baseEntries =
        CloseableIterable.transform(
            baseMaintainer.fileScan(baseTable, dataFilter, expirationConfig),
            e -> new MixedFileEntry(e.getFile(), e.getTsBound(), false));
    IcebergTableMaintainer.ExpireFiles changeExpiredFiles =
        new IcebergTableMaintainer.ExpireFiles();
    IcebergTableMaintainer.ExpireFiles baseExpiredFiles = new IcebergTableMaintainer.ExpireFiles();

    try (CloseableIterable<MixedFileEntry> entries =
        CloseableIterable.withNoopClose(Iterables.concat(changeEntries, baseEntries))) {
      Queue<MixedFileEntry> fileEntries = new LinkedTransferQueue<>();
      entries.forEach(
          e -> {
            if (IcebergTableMaintainer.mayExpired(e, partitionFreshness, expireTimestamp)) {
              fileEntries.add(e);
            }
          });
      fileEntries
          .parallelStream()
          .filter(
              e -> IcebergTableMaintainer.willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(
              e -> {
                if (e.isChange()) {
                  changeExpiredFiles.addFile(e);
                } else {
                  baseExpiredFiles.addFile(e);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Pair.of(changeExpiredFiles, baseExpiredFiles);
  }

  private void expireMixedFiles(
      IcebergTableMaintainer.ExpireFiles changeFiles,
      IcebergTableMaintainer.ExpireFiles baseFiles,
      long expireTimestamp) {
    Optional.ofNullable(changeMaintainer)
        .ifPresent(c -> c.expireFiles(changeFiles, expireTimestamp));
    Optional.ofNullable(baseMaintainer).ifPresent(c -> c.expireFiles(baseFiles, expireTimestamp));
  }

  @Override
  public void autoCreateTags(TableRuntime tableRuntime) {
    throw new UnsupportedOperationException("Mixed table doesn't support auto create tags");
  }

  protected void cleanContentFiles(
      long lastTime, TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics) {
    if (changeMaintainer != null) {
      changeMaintainer.cleanContentFiles(lastTime, orphanFilesCleaningMetrics);
    }
    baseMaintainer.cleanContentFiles(lastTime, orphanFilesCleaningMetrics);
  }

  protected void cleanMetadata(
      long lastTime, TableOrphanFilesCleaningMetrics orphanFilesCleaningMetrics) {
    if (changeMaintainer != null) {
      changeMaintainer.cleanMetadata(lastTime, orphanFilesCleaningMetrics);
    }
    baseMaintainer.cleanMetadata(lastTime, orphanFilesCleaningMetrics);
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
    @VisibleForTesting
    void expireSnapshots(long mustOlderThan, int minCount) {
      expireFiles(mustOlderThan);
      super.expireSnapshots(mustOlderThan, minCount);
    }

    @Override
    public void expireSnapshots(TableRuntime tableRuntime) {
      if (!expireSnapshotEnabled(tableRuntime)) {
        return;
      }
      long now = System.currentTimeMillis();
      expireFiles(now - snapshotsKeepTime(tableRuntime));
      expireSnapshots(
          mustOlderThan(tableRuntime, now),
          tableRuntime.getTableConfiguration().getSnapshotMinCount());
    }

    @Override
    protected long mustOlderThan(TableRuntime tableRuntime, long now) {
      return min(
          // The change data ttl time
          now - snapshotsKeepTime(tableRuntime),
          // The latest flink committed snapshot should not be expired for recovering flink job
          fetchLatestFlinkCommittedSnapshotTime(table));
    }

    @Override
    protected long snapshotsKeepTime(TableRuntime tableRuntime) {
      return tableRuntime.getTableConfiguration().getChangeDataTTLMinutes() * 60 * 1000;
    }

    public void expireFiles(long ttlPoint) {
      List<IcebergFileEntry> expiredDataFileEntries = getExpiredDataFileEntries(ttlPoint);
      deleteChangeFile(expiredDataFileEntries);
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
      KeyedTable keyedTable = mixedTable.asKeyedTable();
      if (CollectionUtils.isEmpty(expiredDataFileEntries)) {
        return;
      }

      StructLikeMap<Long> optimizedSequences = MixedTableUtil.readOptimizedSequence(keyedTable);
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
            tableFiles.forEach(changeDelete::deleteFile);
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
        LOG.error("{} failed to delete change files, ignore", unkeyedTable.name(), t);
      }
    }
  }

  public class BaseTableMaintainer extends IcebergTableMaintainer {
    private final Set<String> hiveFiles = Sets.newHashSet();

    public BaseTableMaintainer(UnkeyedTable unkeyedTable) {
      super(unkeyedTable);
      if (unkeyedTable.format() == TableFormat.MIXED_HIVE) {
        hiveFiles.addAll(HiveLocationUtil.getHiveLocation(mixedTable));
      }
    }

    @Override
    public Set<String> orphanFileCleanNeedToExcludeFiles() {
      Set<String> baseFiles = super.orphanFileCleanNeedToExcludeFiles();
      return Sets.union(baseFiles, hiveFiles);
    }

    @Override
    protected Set<String> expireSnapshotNeedToExcludeFiles() {
      return hiveFiles;
    }

    @Override
    protected long mustOlderThan(TableRuntime tableRuntime, long now) {
      return min(
          // The snapshots keep time for base store
          now - snapshotsKeepTime(tableRuntime),
          // The snapshot optimizing plan based should not be expired for committing
          fetchOptimizingPlanSnapshotTime(table, tableRuntime),
          // The latest non-optimized snapshot should not be expired for data expiring
          fetchLatestNonOptimizedSnapshotTime(table),
          // The latest flink committed snapshot should not be expired for recovering flink job
          fetchLatestFlinkCommittedSnapshotTime(table),
          // The latest snapshot contains the optimized sequence should not be expired for MOR
          fetchLatestOptimizedSequenceSnapshotTime(table));
    }

    /**
     * When committing a snapshot to the base store of mixed format keyed table, it will store the
     * optimized sequence to the snapshot, and will set a flag to the summary of this snapshot.
     *
     * <p>The optimized sequence will affect the correctness of MOR.
     *
     * @param table table
     * @return time of the latest snapshot with the optimized sequence flag in summary
     */
    private long fetchLatestOptimizedSequenceSnapshotTime(Table table) {
      if (mixedTable.isKeyedTable()) {
        Snapshot snapshot =
            findLatestSnapshotContainsKey(table, BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST);
        return snapshot == null ? Long.MAX_VALUE : snapshot.timestampMillis();
      } else {
        return Long.MAX_VALUE;
      }
    }
  }

  public static class MixedFileEntry extends IcebergTableMaintainer.FileEntry {

    private final boolean isChange;

    MixedFileEntry(ContentFile<?> file, Literal<Long> tsBound, boolean isChange) {
      super(file, tsBound);
      this.isChange = isChange;
    }

    public boolean isChange() {
      return isChange;
    }
  }
}
