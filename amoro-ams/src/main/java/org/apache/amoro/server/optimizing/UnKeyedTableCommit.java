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

package org.apache.amoro.server.optimizing;

import static org.apache.amoro.hive.op.UpdateHiveFiles.DELETE_UNTRACKED_HIVE_FILE;
import static org.apache.amoro.hive.op.UpdateHiveFiles.SYNC_DATA_TO_HIVE;

import org.apache.amoro.api.CommitMetaProducer;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.HivePartitionUtil;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.exception.OptimizingCommitException;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.ContentFiles;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.SnapshotUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UnKeyedTableCommit {
  private static final Logger LOG = LoggerFactory.getLogger(UnKeyedTableCommit.class);

  private final Long targetSnapshotId;
  private final MixedTable table;
  private final Collection<TaskRuntime> tasks;

  public UnKeyedTableCommit(
      Long targetSnapshotId, MixedTable table, Collection<TaskRuntime> tasks) {
    this.targetSnapshotId = targetSnapshotId;
    this.table = table;
    this.tasks = tasks;
  }

  protected List<DataFile> moveFile2HiveIfNeed() {
    if (!needMoveFile2Hive()) {
      return null;
    }

    HMSClientPool hiveClient = ((SupportHive) table).getHMSClient();
    Map<String, String> partitionPathMap = new HashMap<>();
    Types.StructType partitionSchema =
        table.isUnkeyedTable()
            ? table.asUnkeyedTable().spec().partitionType()
            : table.asKeyedTable().baseTable().spec().partitionType();

    List<DataFile> newTargetFiles = new ArrayList<>();
    for (TaskRuntime taskRuntime : tasks) {
      RewriteFilesOutput output = taskRuntime.getOutput();
      DataFile[] dataFiles = output.getDataFiles();
      if (dataFiles == null) {
        continue;
      }

      List<DataFile> targetFiles =
          Arrays.stream(output.getDataFiles()).collect(Collectors.toList());

      long maxTransactionId =
          targetFiles.stream()
              .mapToLong(dataFile -> FileNameRules.parseTransactionId(dataFile.path().toString()))
              .max()
              .orElse(0L);

      for (DataFile targetFile : targetFiles) {
        String partitionPath =
            partitionPathMap.computeIfAbsent(
                taskRuntime.getPartition(),
                key -> getPartitionPath(hiveClient, maxTransactionId, targetFile, partitionSchema));

        DataFile finalDataFile = moveTargetFiles(targetFile, partitionPath);
        newTargetFiles.add(finalDataFile);
      }
    }
    return newTargetFiles;
  }

  private String getPartitionPath(
      HMSClientPool hiveClient,
      long maxTransactionId,
      DataFile targetFile,
      Types.StructType partitionSchema) {
    // get iceberg partition path
    String icebergPartitionLocation = getIcebergPartitionLocation(targetFile.partition());
    if (icebergPartitionLocation != null) {
      return icebergPartitionLocation;
    }
    // get hive partition path
    if (table.spec().isUnpartitioned()) {
      try {
        Table hiveTable =
            ((SupportHive) table)
                .getHMSClient()
                .run(
                    client -> client.getTable(table.id().getDatabase(), table.id().getTableName()));
        return hiveTable.getSd().getLocation();
      } catch (Exception e) {
        LOG.error("Get hive table failed", e);
        throw new RuntimeException("Get hive table failed", e);
      }
    } else {
      List<String> partitionValues =
          HivePartitionUtil.partitionValuesAsList(targetFile.partition(), partitionSchema);
      String hiveSubdirectory =
          table.isKeyedTable()
              ? HiveTableUtil.newHiveSubdirectory(maxTransactionId)
              : HiveTableUtil.newHiveSubdirectory();

      Partition p = HivePartitionUtil.getPartition(hiveClient, table, partitionValues);
      if (p == null) {
        return HiveTableUtil.newHiveDataLocation(
            ((SupportHive) table).hiveLocation(),
            table.spec(),
            targetFile.partition(),
            hiveSubdirectory);
      } else {
        return p.getSd().getLocation();
      }
    }
  }

  private String getIcebergPartitionLocation(StructLike partitionData) {
    UnkeyedTable baseTable =
        table.isKeyedTable() ? table.asKeyedTable().baseTable() : table.asUnkeyedTable();
    StructLikeMap<Map<String, String>> partitionProperty = baseTable.partitionProperty();
    Map<String, String> property =
        partitionProperty.get(
            table.spec().isUnpartitioned() ? TablePropertyUtil.EMPTY_STRUCT : partitionData);
    if (property == null) {
      return null;
    }
    return property.get(HiveTableProperties.PARTITION_PROPERTIES_KEY_HIVE_LOCATION);
  }

  public void commit() throws OptimizingCommitException {
    LOG.info("{} get tasks to commit {}", table.id(), tasks);

    List<DataFile> hiveNewDataFiles = moveFile2HiveIfNeed();
    // collect files
    Set<DataFile> addedDataFiles = Sets.newHashSet();
    Set<DataFile> removedDataFiles = Sets.newHashSet();
    Set<DeleteFile> addedDeleteFiles = Sets.newHashSet();
    Set<DeleteFile> removedDeleteFiles = Sets.newHashSet();
    for (TaskRuntime task : tasks) {
      if (CollectionUtils.isNotEmpty(hiveNewDataFiles)) {
        addedDataFiles.addAll(hiveNewDataFiles);
      } else if (task.getOutput().getDataFiles() != null) {
        addedDataFiles.addAll(Arrays.asList(task.getOutput().getDataFiles()));
      }
      if (task.getOutput().getDeleteFiles() != null) {
        addedDeleteFiles.addAll(Arrays.asList(task.getOutput().getDeleteFiles()));
      }
      if (task.getInput().rewrittenDataFiles() != null) {
        removedDataFiles.addAll(Arrays.asList(task.getInput().rewrittenDataFiles()));
      }
      if (task.getInput().rewrittenDeleteFiles() != null) {
        removedDeleteFiles.addAll(
            Arrays.stream(task.getInput().rewrittenDeleteFiles())
                .map(ContentFiles::asDeleteFile)
                .collect(Collectors.toSet()));
      }
    }

    try {
      Transaction transaction = table.asUnkeyedTable().newTransaction();
      if (removedDeleteFiles.isEmpty() && !addedDeleteFiles.isEmpty()) {
        /* In order to avoid the validation in
        {@link org.apache.iceberg.BaseRewriteFiles#validateReplacedAndAddedFiles} which will throw
        an error "Delete files to add must be empty because there's no delete file to be rewritten",
        we split the rewrite into 2 steps, first rewrite the data files, then add the delete files.
         */
        rewriteDataFiles(transaction, removedDataFiles, addedDataFiles);
        addDeleteFiles(transaction, addedDeleteFiles);
      } else {
        rewriteFiles(
            transaction, removedDataFiles, addedDataFiles, removedDeleteFiles, addedDeleteFiles);
      }
      transaction.commitTransaction();
    } catch (Exception e) {
      if (needMoveFile2Hive()) {
        correctHiveData(addedDataFiles, addedDeleteFiles);
      }
      LOG.warn("Optimize commit table {} failed, give up commit.", table.id(), e);
      throw new OptimizingCommitException("unexpected commit error ", e);
    }
  }

  private void rewriteDataFiles(
      Transaction transaction, Set<DataFile> removedDataFiles, Set<DataFile> addedDataFiles) {
    rewriteFiles(
        transaction,
        removedDataFiles,
        addedDataFiles,
        Collections.emptySet(),
        Collections.emptySet());
  }

  private void rewriteFiles(
      Transaction transaction,
      Set<DataFile> removedDataFiles,
      Set<DataFile> addedDataFiles,
      Set<DeleteFile> removedDeleteFiles,
      Set<DeleteFile> addedDeleteFiles) {
    if (removedDataFiles.isEmpty()
        && addedDataFiles.isEmpty()
        && removedDeleteFiles.isEmpty()
        && addedDeleteFiles.isEmpty()) {
      return;
    }

    RewriteFiles rewriteFiles = transaction.newRewrite();
    if (targetSnapshotId != AmoroServiceConstants.INVALID_SNAPSHOT_ID) {
      long sequenceNumber = table.asUnkeyedTable().snapshot(targetSnapshotId).sequenceNumber();
      rewriteFiles.validateFromSnapshot(targetSnapshotId).dataSequenceNumber(sequenceNumber);
    }
    removedDataFiles.forEach(rewriteFiles::deleteFile);
    addedDataFiles.forEach(rewriteFiles::addFile);
    removedDeleteFiles.forEach(rewriteFiles::deleteFile);
    addedDeleteFiles.forEach(rewriteFiles::addFile);
    rewriteFiles.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
    if (TableTypeUtil.isHive(table)) {
      if (!needMoveFile2Hive()) {
        rewriteFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");
      }
      rewriteFiles.set(SYNC_DATA_TO_HIVE, "true");
    }
    rewriteFiles.commit();
  }

  private void addDeleteFiles(Transaction transaction, Set<DeleteFile> addDeleteFiles) {
    RowDelta rowDelta = transaction.newRowDelta();
    addDeleteFiles.forEach(rowDelta::addDeletes);
    rowDelta.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
    rowDelta.commit();
  }

  protected boolean needMoveFile2Hive() {
    return OptimizingInputProperties.parse(tasks.stream().findAny().get().getProperties())
        .getMoveFile2HiveLocation();
  }

  protected void correctHiveData(Set<DataFile> addedDataFiles, Set<DeleteFile> addedDeleteFiles)
      throws OptimizingCommitException {
    try {
      UnkeyedTable baseStore = MixedTableUtil.baseStore(table);
      LOG.warn(
          "Optimize commit table {} failed, give up commit and clear files in location.",
          table.id());
      // only delete data files are produced by major optimize, because the major optimize maybe
      // support hive
      // and produce redundant data files in hive location.(don't produce DeleteFile)
      // minor produced files will be clean by orphan file clean
      Set<String> committedFilePath =
          getCommittedDataFilesFromSnapshotId(baseStore, targetSnapshotId);
      for (ContentFile<?> addedDataFile : addedDataFiles) {
        deleteUncommittedFile(committedFilePath, addedDataFile);
      }
      for (ContentFile<?> addedDeleteFile : addedDeleteFiles) {
        deleteUncommittedFile(committedFilePath, addedDeleteFile);
      }
    } catch (Exception ex) {
      throw new OptimizingCommitException(
          "An exception was encountered when the commit failed to clear the file", true);
    }
  }

  private void deleteUncommittedFile(Set<String> committedFilePath, ContentFile<?> majorAddFile) {
    String filePath = TableFileUtil.getUriPath(majorAddFile.path().toString());
    if (!committedFilePath.contains(filePath) && table.io().exists(filePath)) {
      table.io().deleteFile(filePath);
      LOG.warn("Delete orphan file {} when optimize commit failed", filePath);
    }
  }

  private DataFile moveTargetFiles(DataFile targetFile, String hiveLocation) {
    String oldFilePath = targetFile.path().toString();
    String newFilePath = TableFileUtil.getNewFilePath(hiveLocation, oldFilePath);

    if (!table.io().exists(newFilePath)) {
      if (!table.io().exists(hiveLocation)) {
        LOG.debug(
            "{} hive location {} does not exist and need to mkdir before rename",
            table.id(),
            hiveLocation);
        table.io().asFileSystemIO().makeDirectories(hiveLocation);
      }
      table.io().asFileSystemIO().rename(oldFilePath, newFilePath);
      LOG.debug("{} move file from {} to {}", table.id(), oldFilePath, newFilePath);
    }

    // org.apache.iceberg.BaseFile.set
    ((StructLike) targetFile).set(1, newFilePath);
    return targetFile;
  }

  private static Set<String> getCommittedDataFilesFromSnapshotId(
      UnkeyedTable table, Long snapshotId) {
    long currentSnapshotId = IcebergTableUtil.getSnapshotId(table, true);
    if (currentSnapshotId == AmoroServiceConstants.INVALID_SNAPSHOT_ID) {
      return Collections.emptySet();
    }

    if (snapshotId == AmoroServiceConstants.INVALID_SNAPSHOT_ID) {
      snapshotId = null;
    }

    Set<String> committedFilePath = new HashSet<>();
    for (Snapshot snapshot :
        SnapshotUtil.ancestorsBetween(currentSnapshotId, snapshotId, table::snapshot)) {
      for (DataFile dataFile : snapshot.addedDataFiles(table.io())) {
        committedFilePath.add(TableFileUtil.getUriPath(dataFile.path().toString()));
      }
    }

    return committedFilePath;
  }
}
