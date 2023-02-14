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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.TreeNode;
import com.netease.arctic.ams.api.properties.OptimizeTaskProperties;
import com.netease.arctic.ams.server.model.BasicOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.ams.server.utils.FilesStatisticsBuilder;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.data.file.ContentFileWithSequence;
import com.netease.arctic.data.file.FileNameGenerator;
import com.netease.arctic.data.file.WrapFileWithSequenceNumberHelper;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.SerializationUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractArcticOptimizePlan extends AbstractOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractArcticOptimizePlan.class);

  private final List<ArcticFileScanTask> changeFileScanTasks;
  protected final List<FileScanTask> baseFileScanTasks;
  // Whether to customize the directory
  protected boolean isCustomizeDir;

  // partition -> fileTree
  protected final Map<String, FileTree> partitionFileTree = new LinkedHashMap<>();

  // for base table or unKeyed table
  private final long currentBaseSnapshotId;
  // for change table
  protected final long currentChangeSnapshotId;

  // partition -> maxBaseTableTransactionId
  protected long changeTableMaxTransactionId;
  protected final Map<String, Long> changeTableMinTransactionId = new HashMap<>();

  public AbstractArcticOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                List<ArcticFileScanTask> changeFileScanTasks,
                                List<FileScanTask> baseFileScanTasks,
                                int queueId, long currentTime, long changeSnapshotId, long baseSnapshotId) {
    super(arcticTable, tableOptimizeRuntime, queueId, currentTime);
    this.baseFileScanTasks = baseFileScanTasks;
    this.changeFileScanTasks = changeFileScanTasks;
    this.isCustomizeDir = false;
    this.currentChangeSnapshotId = changeSnapshotId;
    this.currentBaseSnapshotId = baseSnapshotId;
  }

  protected BasicOptimizeTask buildOptimizeTask(@Nullable List<DataTreeNode> sourceNodes,
                                                List<DataFile> insertFiles,
                                                List<DataFile> deleteFiles,
                                                List<DataFile> baseFiles,
                                                List<DeleteFile> posDeleteFiles,
                                                TaskConfig taskConfig) {
    // build task
    BasicOptimizeTask optimizeTask = new BasicOptimizeTask();
    optimizeTask.setTaskCommitGroup(taskConfig.getCommitGroup());
    optimizeTask.setTaskPlanGroup(taskConfig.getPlanGroup());
    optimizeTask.setCreateTime(taskConfig.getCreateTime());

    List<ByteBuffer> baseFileBytesList =
        baseFiles.stream()
            .map(SerializationUtils::toByteBuffer)
            .collect(Collectors.toList());
    List<ByteBuffer> insertFileBytesList =
        insertFiles.stream()
            .map(SerializationUtils::toByteBuffer)
            .collect(Collectors.toList());
    List<ByteBuffer> deleteFileBytesList =
        deleteFiles.stream()
            .map(SerializationUtils::toByteBuffer)
            .collect(Collectors.toList());
    List<ByteBuffer> posDeleteFileBytesList =
        posDeleteFiles.stream()
            .map(SerializationUtils::toByteBuffer)
            .collect(Collectors.toList());
    optimizeTask.setBaseFiles(baseFileBytesList);
    optimizeTask.setInsertFiles(insertFileBytesList);
    optimizeTask.setDeleteFiles(deleteFileBytesList);
    optimizeTask.setPosDeleteFiles(posDeleteFileBytesList);

    FilesStatisticsBuilder baseFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder insertFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder deleteFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder posDeleteFb = new FilesStatisticsBuilder();
    baseFiles.stream().map(DataFile::fileSizeInBytes)
        .forEach(baseFb::addFile);
    insertFiles.stream().map(DataFile::fileSizeInBytes)
        .forEach(insertFb::addFile);
    deleteFiles.stream().map(DataFile::fileSizeInBytes)
        .forEach(deleteFb::addFile);
    posDeleteFiles.stream().map(DeleteFile::fileSizeInBytes)
        .forEach(posDeleteFb::addFile);

    FilesStatistics baseFs = baseFb.build();
    FilesStatistics insertFs = insertFb.build();
    FilesStatistics deleteFs = deleteFb.build();
    FilesStatistics posDeleteFs = posDeleteFb.build();

    // file size
    optimizeTask.setBaseFileSize(baseFs.getTotalSize());
    optimizeTask.setInsertFileSize(insertFs.getTotalSize());
    optimizeTask.setDeleteFileSize(deleteFs.getTotalSize());
    optimizeTask.setPosDeleteFileSize(posDeleteFs.getTotalSize());

    // file count
    optimizeTask.setBaseFileCnt(baseFs.getFileCnt());
    optimizeTask.setInsertFileCnt(insertFs.getFileCnt());
    optimizeTask.setDeleteFileCnt(deleteFs.getFileCnt());
    optimizeTask.setPosDeleteFileCnt(posDeleteFs.getFileCnt());

    optimizeTask.setPartition(taskConfig.getPartition());
    optimizeTask.setQueueId(queueId);
    optimizeTask.setTaskId(new OptimizeTaskId(taskConfig.getOptimizeType(), UUID.randomUUID().toString()));
    optimizeTask.setTableIdentifier(arcticTable.id().buildTableIdentifier());

    // for keyed table
    if (sourceNodes != null) {
      optimizeTask.setSourceNodes(sourceNodes.stream()
          .map(node ->
              new TreeNode(node.getMask(), node.getIndex()))
          .collect(Collectors.toList()));
    }
    if (taskConfig.getMaxTransactionId() != null) {
      optimizeTask.setMaxChangeTransactionId(taskConfig.getMaxTransactionId());
    }

    if (taskConfig.getMinTransactionId() != null) {
      optimizeTask.setMinChangeTransactionId(taskConfig.getMinTransactionId());
    }

    // table ams url
    Map<String, String> properties = new HashMap<>();
    properties.put(OptimizeTaskProperties.ALL_FILE_COUNT, (optimizeTask.getBaseFiles().size() +
        optimizeTask.getInsertFiles().size() + optimizeTask.getDeleteFiles().size()) +
        optimizeTask.getPosDeleteFiles().size() + "");
    properties.put(OptimizeTaskProperties.CUSTOM_HIVE_SUB_DIRECTORY, taskConfig.getCustomHiveSubdirectory());
    optimizeTask.setProperties(properties);
    return optimizeTask;
  }

  @Override
  protected void addOptimizeFiles() {
    addChangeFilesIntoFileTree();
    addBaseFilesIntoFileTree();
    completeTree();
  }

  private void completeTree() {
    partitionFileTree.values().forEach(FileTree::completeTree);
  }

  protected void addChangeFilesIntoFileTree() {
    if (arcticTable.isUnkeyedTable()) {
      return;
    }
    LOG.debug("{} start {} plan change files", tableId(), getOptimizeType());
    ChangeTable changeTable = arcticTable.asKeyedTable().changeTable();

    Set<String> changeFiles = new HashSet<>();
    List<PrimaryKeyedFile> unOptimizedChangeFiles = changeFileScanTasks.stream().map(task -> {
      PrimaryKeyedFile file = task.file();
      String partition = changeTable.spec().partitionToPath(file.partition());
      currentPartitions.add(partition);
      if (changeFiles.contains(file.path().toString())) {
        return null;
      }
      changeFiles.add(file.path().toString());
      return file;
    }).filter(Objects::nonNull).collect(Collectors.toList());

    long maxTransactionIdLimit = getMaxTransactionIdLimit(unOptimizedChangeFiles);

    AtomicInteger addCnt = new AtomicInteger();
    unOptimizedChangeFiles.forEach(file -> {
      long transactionId = file.transactionId();

      if (transactionId >= maxTransactionIdLimit) {
        return;
      }
      String partition = changeTable.spec().partitionToPath(file.partition());
      putChangeFileIntoFileTree(partition, file, file.type(), transactionId);
      markChangeTransactionId(partition, transactionId);

      addCnt.getAndIncrement();
    });
    LOG.debug("{} ==== {} add {} change files into tree, total files: {}." + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, unOptimizedChangeFiles.size(), partitionFileTree.size());
  }

  private long getMaxTransactionIdLimit(List<PrimaryKeyedFile> unOptimizedChangeFiles) {
    final int maxChangeFiles =
        CompatiblePropertyUtil.propertyAsInt(arcticTable.properties(), TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT,
            TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT);
    long maxTransactionIdLimit;
    if (unOptimizedChangeFiles.size() <= maxChangeFiles) {
      maxTransactionIdLimit = Long.MAX_VALUE;
      // For normal cases, files count is less than optimize.max-file-count(default=100000), return all files
      LOG.debug("{} start plan change files with all files, max-cnt limit {}, current file cnt {}", tableId(),
          maxChangeFiles, unOptimizedChangeFiles.size());
    } else {
      List<Long> sortedTransactionIds = unOptimizedChangeFiles.stream().map(PrimaryKeyedFile::transactionId)
          .sorted(Long::compareTo)
          .collect(Collectors.toList());
      maxTransactionIdLimit = sortedTransactionIds.get(maxChangeFiles - 1);
      // If files count is more than optimize.max-file-count, only keep files with small file transaction id
      LOG.debug("{} start plan change files with max-cnt limit {}, current file cnt {}, less than transaction id {}",
          tableId(), maxChangeFiles, unOptimizedChangeFiles.size(), maxTransactionIdLimit);
    }
    return maxTransactionIdLimit;
  }

  protected void addBaseFilesIntoFileTree() {
    LOG.debug("{} start {} plan base files", tableId(), getOptimizeType());
    UnkeyedTable baseTable = getBaseTable();

    Set<String> baseFileSet = new HashSet<>();
    Set<String> deleteFileSet = new HashSet<>();
    baseFileScanTasks.forEach(task -> {
      DataFile baseFile = task.file();
      List<DeleteFile> deletes = task.deletes();
      String partition = baseTable.spec().partitionToPath(baseFile.partition());

      currentPartitions.add(partition);
      if (!baseFileShouldOptimize(baseFile, partition)) {
        return;
      }
      // put base files into file tree
      if (!baseFileSet.contains(baseFile.path().toString())) {
        putBaseFileIntoFileTree(partition, baseFile, DataFileType.BASE_FILE);
        baseFileSet.add(baseFile.path().toString());
      }

      // put delete files into file tree
      deletes.forEach(delete -> {
        Preconditions.checkArgument(delete.content() == FileContent.POSITION_DELETES,
            "only support pos-delete files for base file, unexpected file " + delete);
        if (!deleteFileSet.contains(delete.path().toString())) {
          putBaseFileIntoFileTree(partition, delete, DataFileType.POS_DELETE_FILE);
          deleteFileSet.add(delete.path().toString());
        }
      });
    });

    LOG.debug("{} ==== {} add {} base files, {} pos-delete files into tree. After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), baseFileSet.size(), deleteFileSet.size(), partitionFileTree.size());
  }
  
  protected void putBaseFileIntoFileTree(String partition, ContentFile<?> contentFile, DataFileType fileType) {
    FileTree treeRoot = partitionFileTree.computeIfAbsent(partition, p -> FileTree.newTreeRoot());
    DataTreeNode node = FileNameGenerator.parseFileNodeFromFileName(contentFile.path().toString());
    DefaultKeyedFile.FileMeta fileMeta = FileNameGenerator.parseBase(contentFile.path().toString());
    ContentFileWithSequence<?> wrap = WrapFileWithSequenceNumberHelper.wrap(contentFile, fileMeta.transactionId());
    treeRoot.putNodeIfAbsent(node).addFile(wrap, fileType);
  }

  protected void putChangeFileIntoFileTree(String partition, ContentFile<?> contentFile, DataFileType fileType,
                                           long sequence) {
    FileTree treeRoot = partitionFileTree.computeIfAbsent(partition, p -> FileTree.newTreeRoot());
    DataTreeNode node = FileNameGenerator.parseFileNodeFromFileName(contentFile.path().toString());
    DefaultKeyedFile.FileMeta fileMeta = FileNameGenerator.parseChange(contentFile.path().toString(), sequence);
    ContentFileWithSequence<?> wrap = WrapFileWithSequenceNumberHelper.wrap(contentFile, fileMeta.transactionId());
    treeRoot.putNodeIfAbsent(node).addFile(wrap, fileType);
  }

  protected UnkeyedTable getBaseTable() {
    UnkeyedTable baseTable;
    if (arcticTable.isKeyedTable()) {
      baseTable = arcticTable.asKeyedTable().baseTable();
    } else {
      baseTable = arcticTable.asUnkeyedTable();
    }
    return baseTable;
  }
  
  protected List<DataFile> getBaseFilesFromFileTree(String partition) {
    FileTree fileTree = partitionFileTree.get(partition);
    if (fileTree == null) {
      return Collections.emptyList();
    }
    List<DataFile> baseFiles = new ArrayList<>();
    fileTree.collectBaseFiles(baseFiles);
    return baseFiles;
  }

  protected List<DeleteFile> getPosDeleteFilesFromFileTree(String partition) {
    FileTree fileTree = partitionFileTree.get(partition);
    if (fileTree == null) {
      return Collections.emptyList();
    }
    List<DeleteFile> deleteFiles = new ArrayList<>();
    fileTree.collectPosDeleteFiles(deleteFiles);
    return deleteFiles;
  }

  protected List<DataFile> getDeleteFilesFromFileTree(String partition) {
    FileTree fileTree = partitionFileTree.get(partition);
    if (fileTree == null) {
      return Collections.emptyList();
    }
    List<DataFile> deleteFiles = new ArrayList<>();
    fileTree.collectDeleteFiles(deleteFiles);
    return deleteFiles;
  }

  protected List<DataFile> getInsertFilesFromFileTree(String partition) {
    FileTree fileTree = partitionFileTree.get(partition);
    if (fileTree == null) {
      return Collections.emptyList();
    }
    List<DataFile> insertFiles = new ArrayList<>();
    fileTree.collectInsertFiles(insertFiles);
    return insertFiles;
  }

  /**
   * check whether node task need to build
   * @param posDeleteFiles pos-delete files in node
   * @param baseFiles base files in node
   * @return whether the node task need to build. If true, build task, otherwise skip.
   */
  protected abstract boolean nodeTaskNeedBuild(List<DeleteFile> posDeleteFiles, List<DataFile> baseFiles);

  protected abstract boolean baseFileShouldOptimize(DataFile baseFile, String partition);

  protected boolean hasFileToOptimize() {
    return !partitionFileTree.isEmpty();
  }

  @Override
  protected long getCurrentSnapshotId() {
    return currentBaseSnapshotId;
  }

  @Override
  protected long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  private void markChangeTransactionId(String partition, long fileTid) {
    if (this.changeTableMaxTransactionId < fileTid) {
      this.changeTableMaxTransactionId = fileTid;
    }
    Long tid = changeTableMinTransactionId.get(partition);
    if (tid == null) {
      changeTableMinTransactionId.put(partition, fileTid);
    } else if (fileTid < tid) {
      changeTableMinTransactionId.put(partition, fileTid);
    }
  }

  protected static class SplitIfNoFileExists implements Predicate<FileTree> {

    public SplitIfNoFileExists() {
    }

    /**
     * file tree can split if:
     * - root node isn't leaf node
     * - and no file exists in the root node
     *
     * @param fileTree - file tree to split
     * @return true if this fileTree need split
     */
    @Override
    public boolean test(FileTree fileTree) {
      return !fileTree.isLeaf() && fileTree.isRootEmpty();
    }
  }

}
