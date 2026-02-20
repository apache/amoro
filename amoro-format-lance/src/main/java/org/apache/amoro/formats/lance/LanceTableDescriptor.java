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

package org.apache.amoro.formats.lance;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TableSummary;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.amoro.utils.CommonUtil;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;
import org.apache.commons.lang3.tuple.Pair;
import org.lance.Branch;
import org.lance.Dataset;
import org.lance.Fragment;
import org.lance.FragmentMetadata;
import org.lance.ManifestSummary;
import org.lance.Tag;
import org.lance.Version;
import org.lance.fragment.DataFile;
import org.lance.fragment.DeletionFile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/** Table descriptor for Lance tables. */
public class LanceTableDescriptor implements FormatTableDescriptor {

  private ExecutorService ioExecutor;

  @Override
  public void withIoExecutor(ExecutorService ioExecutor) {
    this.ioExecutor = ioExecutor;
  }

  @Override
  public List<TableFormat> supportFormat() {
    return Collections.singletonList(LanceCatalogFactory.LANCE);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    Dataset dataset = (Dataset) amoroTable.originalTable();

    ServerTableMeta meta = new ServerTableMeta();
    meta.setTableIdentifier(amoroTable.id());
    meta.setTableType(LanceCatalogFactory.LANCE.name());
    meta.setBaseLocation(dataset.uri());

    // Schema
    List<AMSColumnInfo> columns = new ArrayList<>();
    Schema schema = dataset.getSchema();
    for (Field field : schema.getFields()) {
      AMSColumnInfo columnInfo = new AMSColumnInfo();
      columnInfo.setField(field.getName());
      columnInfo.setType(field.getType().toString());
      columnInfo.setRequired(!field.isNullable());
      columnInfo.setComment(null);
      columns.add(columnInfo);
    }
    meta.setSchema(columns);
    meta.setPkList(Collections.emptyList());
    meta.setPartitionColumnList(Collections.emptyList());
    meta.setProperties(amoroTable.properties());

    Version version = dataset.getVersion();
    ManifestSummary summary = version.getManifestSummary();

    long totalFilesSize = summary.getTotalFilesSize();
    long totalDataFiles = summary.getTotalDataFiles();
    long totalDeletionFiles = summary.getTotalDeletionFiles();
    long totalFileCount = totalDataFiles + totalDeletionFiles;
    long totalRows = summary.getTotalRows();

    String totalSizeHuman = CommonUtil.byteToXB(totalFilesSize);
    String averageFileSizeHuman =
        CommonUtil.byteToXB(totalFileCount == 0 ? 0 : totalFilesSize / totalFileCount);

    // Table summary
    String tableFormatDisplay = "Lance";
    TableSummary tableSummary =
        new TableSummary(
            totalFileCount, totalSizeHuman, averageFileSizeHuman, totalRows, tableFormatDisplay);
    meta.setTableSummary(tableSummary);

    // Base metrics (data files only)
    Map<String, Object> baseMetrics = new HashMap<>();
    baseMetrics.put("totalSize", CommonUtil.byteToXB(totalFilesSize));
    baseMetrics.put("fileCount", totalDataFiles);
    baseMetrics.put(
        "averageFileSize",
        CommonUtil.byteToXB(totalDataFiles == 0 ? 0 : totalFilesSize / totalDataFiles));
    meta.setBaseMetrics(baseMetrics);

    // Change metrics (deletion files if present)
    Map<String, Object> changeMetrics = new HashMap<>();
    if (totalDeletionFiles > 0) {
      changeMetrics.put("deletionFileCount", totalDeletionFiles);
      changeMetrics.put("deletedRows", summary.getTotalDeletionFileRows());
    }
    meta.setChangeMetrics(changeMetrics);

    return meta;
  }

  @Override
  public List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType) {
    Dataset dataset = (Dataset) amoroTable.originalTable();
    List<Version> versions = dataset.listVersions();

    if (ioExecutor == null) {
      List<AmoroSnapshotsOfTable> snapshots = new ArrayList<>();
      for (Version version : versions) {
        snapshots.add(buildSnapshot(version));
      }
      return snapshots;
    }

    List<CompletableFuture<AmoroSnapshotsOfTable>> futures = new ArrayList<>();
    for (Version version : versions) {
      futures.add(CompletableFuture.supplyAsync(() -> buildSnapshot(version), ioExecutor));
    }

    List<AmoroSnapshotsOfTable> snapshots = new ArrayList<>();
    for (CompletableFuture<AmoroSnapshotsOfTable> future : futures) {
      try {
        snapshots.add(future.get());
      } catch (InterruptedException e) {
        // ignore
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
    return snapshots;
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(
      AmoroTable<?> amoroTable, String snapshotId, String ref) {
    Dataset dataset = (Dataset) amoroTable.originalTable();
    long versionId;
    try {
      versionId = Long.parseLong(snapshotId);
    } catch (NumberFormatException e) {
      return Collections.emptyList();
    }

    try (Dataset versionDataset = dataset.checkoutVersion(versionId)) {
      Version version = versionDataset.getVersion();
      long commitTime = toMillis(version.getDataTime());
      List<PartitionFileBaseInfo> files = new ArrayList<>();

      for (Fragment fragment : versionDataset.getFragments()) {
        FragmentMetadata metadata = fragment.metadata();
        for (DataFile dataFile : metadata.getFiles()) {
          String path = dataFile.getPath();
          long size = dataFile.getFileSizeBytes() == null ? 0L : dataFile.getFileSizeBytes();
          files.add(
              new PartitionFileBaseInfo(snapshotId, "DATA_FILE", commitTime, "", 0, path, size));
        }

        DeletionFile deletionFile = metadata.getDeletionFile();
        if (deletionFile != null) {
          files.add(
              new PartitionFileBaseInfo(snapshotId, "DELETION_FILE", commitTime, "", 0, "", 0L));
        }
      }

      return files;
    }
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    // Lance tables are treated as non-partitioned for now.
    return Collections.emptyList();
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    Dataset dataset = (Dataset) amoroTable.originalTable();
    Version version = dataset.getVersion();
    long commitTime = toMillis(version.getDataTime());

    List<PartitionFileBaseInfo> files = new ArrayList<>();
    for (Fragment fragment : dataset.getFragments()) {
      FragmentMetadata metadata = fragment.metadata();
      for (DataFile dataFile : metadata.getFiles()) {
        String path = dataFile.getPath();
        long size = dataFile.getFileSizeBytes() == null ? 0L : dataFile.getFileSizeBytes();
        files.add(
            new PartitionFileBaseInfo(
                String.valueOf(version.getId()), "DATA_FILE", commitTime, "", 0, path, size));
      }
    }
    return files;
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, String type, ProcessStatus status, int limit, int offset) {
    return Pair.of(Collections.emptyList(), 0);
  }

  @Override
  public Map<String, String> getTableOptimizingTypes(AmoroTable<?> amoroTable) {
    return Collections.emptyMap();
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(
      AmoroTable<?> amoroTable, String processId) {
    return Collections.emptyList();
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    Dataset dataset = (Dataset) amoroTable.originalTable();
    List<Tag> tags = dataset.tags().list();

    List<TagOrBranchInfo> infos = new ArrayList<>();
    for (Tag tag : tags) {
      TagOrBranchInfo info =
          new TagOrBranchInfo(tag.getName(), tag.getVersion(), -1, 0L, 0L, TagOrBranchInfo.TAG);
      infos.add(info);
    }
    return infos;
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    Dataset dataset = (Dataset) amoroTable.originalTable();
    List<Branch> branches = dataset.branches().list();

    List<TagOrBranchInfo> branchInfos = new ArrayList<>();
    for (Branch branch : branches) {
      TagOrBranchInfo info =
          new TagOrBranchInfo(
              branch.getName(), branch.getParentVersion() + 1, -1, 0L, 0L, TagOrBranchInfo.BRANCH);
      branchInfos.add(info);
    }
    branchInfos.add(TagOrBranchInfo.MAIN_BRANCH);
    return branchInfos;
  }

  @Override
  public List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  private AmoroSnapshotsOfTable buildSnapshot(Version version) {
    ManifestSummary summary = version.getManifestSummary();
    AmoroSnapshotsOfTable snapshot = new AmoroSnapshotsOfTable();
    snapshot.setSnapshotId(String.valueOf(version.getId()));
    snapshot.setCommitTime(toMillis(version.getDataTime()));
    snapshot.setFileCount((int) summary.getTotalDataFiles());
    snapshot.setFileSize(summary.getTotalFilesSize());
    snapshot.setRecords(summary.getTotalRows());
    snapshot.setOperation("WRITE");

    Map<String, String> filesSummary = new HashMap<>();
    filesSummary.put("data-files", String.valueOf(summary.getTotalDataFiles()));
    filesSummary.put("delta-files", String.valueOf(summary.getTotalDeletionFiles()));
    filesSummary.put("changelogs", "0");
    snapshot.setFilesSummaryForChart(filesSummary);

    return snapshot;
  }

  private long toMillis(ZonedDateTime time) {
    if (time == null) {
      return 0L;
    }
    return time.toInstant().toEpochMilli();
  }
}
