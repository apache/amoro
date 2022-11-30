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

import com.google.common.collect.ImmutableList;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.ams.server.utils.ContentFileUtil;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.FileUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MinorOptimizePlan extends BaseArcticOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(MinorOptimizePlan.class);
  private static final long INVALID_TX_ID = -1L;

  // partition -> delete file
  protected final Map<String, List<DataFile>> partitionDeleteFiles = new LinkedHashMap<>();
  // partition -> maxBaseTableTransactionId
  protected StructLikeMap<Long> baseTableMaxTransactionId = null;
  protected StructLikeMap<Long> baseTableLegacyMaxTransactionId = null;
  private long changeTableMaxTransactionId;

  public MinorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                           List<DataFileInfo> baseTableFileList,
                           List<DataFileInfo> changeTableFileList,
                           List<DataFileInfo> posDeleteFileList,
                           Map<String, Boolean> partitionTaskRunning,
                           int queueId, long currentTime, Predicate<Long> snapshotIsCached) {
    super(arcticTable, tableOptimizeRuntime, baseTableFileList, changeTableFileList, posDeleteFileList,
        partitionTaskRunning, queueId, currentTime, snapshotIsCached);
  }

  @Override
  public boolean partitionNeedPlan(String partitionToPath) {
    long current = System.currentTimeMillis();

    // check delete file count
    List<DataFile> deleteFileList = partitionDeleteFiles.get(partitionToPath);
    if (CollectionUtils.isNotEmpty(deleteFileList)) {
      // file count
      if (deleteFileList.size() >= CompatiblePropertyUtil.propertyAsInt(arcticTable.properties(),
          TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT,
          TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT)) {
        partitionOptimizeType.put(partitionToPath, OptimizeType.Minor);
        return true;
      }
    }

    // optimize interval
    if (current - tableOptimizeRuntime.getLatestMinorOptimizeTime(partitionToPath) >=
        CompatiblePropertyUtil.propertyAsLong(arcticTable.properties(),
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL,
            TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT)) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Minor);
      return true;
    }
    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}", tableId(), getOptimizeType(), partitionToPath);
    return false;
  }

  @Override
  protected boolean nodeTaskNeedBuild(List<DeleteFile> posDeleteFiles, List<DataFile> baseFiles) {
    throw new UnsupportedOperationException("Minor optimize not check with this method");
  }

  @Override
  public void addOptimizeFiles() {
    addChangeFilesIntoFileTree();
    addBaseFileIntoFileTree();
  }

  @Override
  protected OptimizeType getOptimizeType() {
    return OptimizeType.Minor;
  }

  @Override
  protected List<BaseOptimizeTask> collectTask(String partition) {
    List<BaseOptimizeTask> result;

    FileTree treeRoot = partitionFileTree.get(partition);
    result = collectKeyedTableTasks(partition, treeRoot);
    // init files
    partitionDeleteFiles.put(partition, Collections.emptyList());
    partitionPosDeleteFiles.put(partition, Collections.emptyList());
    partitionFileTree.get(partition).initFiles();

    return result;
  }

  @Override
  protected boolean tableChanged() {
    return changeTableChanged();
  }

  private boolean changeTableChanged() {
    long lastChangeSnapshotId = tableOptimizeRuntime.getCurrentChangeSnapshotId();
    LOG.debug("{} ==== {} currentChangeSnapshotId={}, lastChangeSnapshotId={}", tableId(), getOptimizeType(),
        currentChangeSnapshotId, lastChangeSnapshotId);
    return currentChangeSnapshotId != lastChangeSnapshotId;
  }

  private void addChangeFilesIntoFileTree() {
    LOG.debug("{} start {} plan change files", tableId(), getOptimizeType());

    List<ChangeFileInfo> unOptimizedChangeFiles = changeTableFileList.stream().map(dataFileInfo -> {
      ChangeFileInfo changeFileInfo = buildChangeFileInfo(dataFileInfo);
      if (changeFileInfo == null) {
        return null;
      }
      String partition = dataFileInfo.getPartition() == null ? "" : dataFileInfo.getPartition();
      currentPartitions.add(partition);
      if (isOptimized(changeFileInfo)) {
        return null;
      }
      return changeFileInfo;
    }).filter(Objects::nonNull).collect(Collectors.toList());

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
      List<Long> sortedTransactionIds = unOptimizedChangeFiles.stream().map(ChangeFileInfo::getTransactionId)
          .sorted(Long::compareTo)
          .collect(Collectors.toList());
      maxTransactionIdLimit = sortedTransactionIds.get(maxChangeFiles - 1);
      // If files count is more than optimize.max-file-count, only keep files with small file transaction id
      LOG.debug("{} start plan change files with max-cnt limit {}, current file cnt {}, less than transaction id {}",
          tableId(), maxChangeFiles, unOptimizedChangeFiles.size(), maxTransactionIdLimit);
    }

    AtomicInteger addCnt = new AtomicInteger();
    unOptimizedChangeFiles.forEach(f -> {
      DataFileInfo dataFileInfo = f.getDataFileInfo();
      DataFile dataFile = f.getDataFile();
      long transactionId = f.getTransactionId();

      String partition = dataFileInfo.getPartition() == null ? "" : dataFileInfo.getPartition();
      if (transactionId >= maxTransactionIdLimit) {
        return;
      }
      if (!anyTaskRunning(partition)) {
        FileTree treeRoot =
            partitionFileTree.computeIfAbsent(partition, p -> FileTree.newTreeRoot());
        treeRoot.putNodeIfAbsent(DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex()))
            .addFile(dataFile, DataFileType.valueOf(dataFileInfo.getType()));
        markFileInfo(f);

        // fill eq delete file map
        if (Objects.equals(dataFileInfo.getType(), DataFileType.EQ_DELETE_FILE.name())) {
          List<DataFile> files = partitionDeleteFiles.computeIfAbsent(partition, e -> new ArrayList<>());
          files.add(dataFile);
          partitionDeleteFiles.put(partition, files);
        }

        addCnt.getAndIncrement();
      }
    });
    LOG.debug("{} ==== {} add {} change files into tree, total files: {}." + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, unOptimizedChangeFiles.size(), partitionFileTree.size());
  }

  private static class ChangeFileInfo {
    private final DataFileInfo dataFileInfo;
    private final DataFile dataFile;
    private final long transactionId;
    private long legacyTransactionId = -1;

    public ChangeFileInfo(DataFileInfo dataFileInfo, DataFile dataFile) {
      this.dataFileInfo = dataFileInfo;
      this.dataFile = dataFile;
      this.transactionId = dataFileInfo.getSequence();
    }

    public DataFileInfo getDataFileInfo() {
      return dataFileInfo;
    }

    public DataFile getDataFile() {
      return dataFile;
    }

    public long getTransactionId() {
      return transactionId;
    }

    public long getLegacyTransactionId() {
      if (legacyTransactionId == -1) {
        legacyTransactionId = FileUtil.parseFileTidFromFileName(dataFileInfo.getPath());
      }
      return legacyTransactionId;
    }
  }
  
  private ChangeFileInfo buildChangeFileInfo(DataFileInfo dataFileInfo) {
    KeyedTable keyedArcticTable = arcticTable.asKeyedTable();
    PartitionSpec partitionSpec = keyedArcticTable.changeTable()
        .specs().get((int) dataFileInfo.getSpecId());

    if (partitionSpec == null) {
      LOG.error("{} {} can not find partitionSpec id: {}", dataFileInfo.getPath(), getOptimizeType(),
          dataFileInfo.specId);
      return null;
    }

    Preconditions.checkArgument(DataFileType.POS_DELETE_FILE != DataFileType.valueOf(dataFileInfo.getType()),
        "not support pos-delete files in change table " + dataFileInfo.getPath());

    DataFile dataFile = (DataFile) ContentFileUtil.buildContentFile(dataFileInfo, partitionSpec, fileFormat);
    return new ChangeFileInfo(dataFileInfo, dataFile);
  }

  private void addBaseFileIntoFileTree() {
    LOG.debug("{} start plan base files", tableId());
    KeyedTable keyedArcticTable = arcticTable.asKeyedTable();

    AtomicInteger addCnt = new AtomicInteger();
    List<DataFileInfo> baseFileList = new ArrayList<>();
    baseFileList.addAll(ImmutableList.copyOf(baseTableFileList));
    baseFileList.addAll(ImmutableList.copyOf(posDeleteFileList));
    List<ContentFile<?>> baseOptimizeFiles = baseFileList.stream().map(dataFileInfo -> {
      PartitionSpec partitionSpec = keyedArcticTable.baseTable().specs().get((int) dataFileInfo.getSpecId());
      String partition = dataFileInfo.getPartition() == null ? "" : dataFileInfo.getPartition();

      if (partitionSpec == null) {
        LOG.error("{} {} can not find partitionSpec id: {}", dataFileInfo.getPath(), getOptimizeType(),
            dataFileInfo.specId);
        return null;
      }

      ContentFile<?> contentFile = ContentFileUtil.buildContentFile(dataFileInfo, partitionSpec, fileFormat);
      currentPartitions.add(partition);
      if (!anyTaskRunning(partition)) {
        FileTree treeRoot =
            partitionFileTree.computeIfAbsent(partition, p -> FileTree.newTreeRoot());
        treeRoot.putNodeIfAbsent(DataTreeNode.of(dataFileInfo.getMask(), dataFileInfo.getIndex()))
            .addFile(contentFile, DataFileType.valueOf(dataFileInfo.getType()).equals(DataFileType.POS_DELETE_FILE) ?
                DataFileType.POS_DELETE_FILE : DataFileType.BASE_FILE);

        // fill node position delete file map
        if (contentFile.content() == FileContent.POSITION_DELETES) {
          List<DeleteFile> files = partitionPosDeleteFiles.computeIfAbsent(partition, e -> new ArrayList<>());
          files.add((DeleteFile) contentFile);
          partitionPosDeleteFiles.put(partition, files);
        }

        addCnt.getAndIncrement();
      }
      return contentFile;
    }).filter(Objects::nonNull).collect(Collectors.toList());

    LOG.debug("{} ==== {} add {} base files into tree, total files: {}." + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, baseOptimizeFiles.size(), partitionFileTree.size());
  }

  private List<BaseOptimizeTask> collectKeyedTableTasks(String partition, FileTree treeRoot) {
    List<BaseOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, changeTableMaxTransactionId,
        commitGroup, planGroup, OptimizeType.Minor, createTime, "");
    treeRoot.completeTree(false);
    List<FileTree> subTrees = new ArrayList<>();
    // split tasks
    treeRoot.splitSubTree(subTrees, new CanSplitFileTree());
    for (FileTree subTree : subTrees) {
      List<DataFile> insertFiles = new ArrayList<>();
      List<DataFile> deleteFiles = new ArrayList<>();
      List<DataFile> baseFiles = new ArrayList<>();
      subTree.collectInsertFiles(insertFiles);
      subTree.collectDeleteFiles(deleteFiles);
      subTree.collectBaseFiles(baseFiles);
      List<DataTreeNode> sourceNodes = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(baseFiles)) {
        sourceNodes = Collections.singletonList(subTree.getNode());
      }
      Set<DataTreeNode> baseFileNodes = baseFiles.stream()
          .map(dataFile -> FileUtil.parseFileNodeFromFileName(dataFile.path().toString()))
          .collect(Collectors.toSet());
      List<DeleteFile> posDeleteFiles = partitionPosDeleteFiles
          .computeIfAbsent(partition, e -> Collections.emptyList()).stream()
          .filter(deleteFile ->
              baseFileNodes.contains(FileUtil.parseFileNodeFromFileName(deleteFile.path().toString())))
          .collect(Collectors.toList());
      // if no insert files and no eq-delete file, skip
      if (CollectionUtils.isEmpty(insertFiles) && CollectionUtils.isEmpty(deleteFiles)) {
        continue;
      }
      collector.add(buildOptimizeTask(sourceNodes,
          insertFiles, deleteFiles, baseFiles, posDeleteFiles, taskPartitionConfig));
    }

    return collector;
  }

  private boolean isOptimized(ChangeFileInfo changeFileInfo) {
    long baseMaxTransactionId = getBaseMaxTransactionId(changeFileInfo.getDataFile().partition());
    if (baseMaxTransactionId != INVALID_TX_ID) {
      return changeFileInfo.getTransactionId() <= baseMaxTransactionId;
    } else {
      long legacyPartitionMaxTransactionId =
          getLegacyPartitionMaxTransactionId(changeFileInfo.getDataFile().partition());
      return changeFileInfo.getLegacyTransactionId() <= legacyPartitionMaxTransactionId;
    }
  }

  private void markFileInfo(ChangeFileInfo changeFileInfo) {
    if (this.changeTableMaxTransactionId < changeFileInfo.getTransactionId()) {
      this.changeTableMaxTransactionId = changeFileInfo.getTransactionId();
    }
  }

  private long getBaseMaxTransactionId(StructLike partition) {
    Long maxTransactionId = getBaseTableMaxTransactionId().get(partition);
    return maxTransactionId == null ? INVALID_TX_ID : maxTransactionId;
  }

  private StructLikeMap<Long> getBaseTableMaxTransactionId() {
    if (baseTableMaxTransactionId == null) {
      baseTableMaxTransactionId = TablePropertyUtil.getPartitionMaxTransactionId(arcticTable.asKeyedTable());
      LOG.debug("{} ==== get base table max transaction id: {}", tableId(), baseTableMaxTransactionId);
    }
    return baseTableMaxTransactionId;
  }

  private long getLegacyPartitionMaxTransactionId(StructLike partition) {
    Long maxTransactionId = getBaseTableLegacyMaxTransactionId().get(partition);
    return maxTransactionId == null ? INVALID_TX_ID : maxTransactionId;
  }

  private StructLikeMap<Long> getBaseTableLegacyMaxTransactionId() {
    if (baseTableLegacyMaxTransactionId == null) {
      baseTableLegacyMaxTransactionId =
          TablePropertyUtil.getLegacyPartitionMaxTransactionId(arcticTable.asKeyedTable());
      LOG.debug("{} ==== get base table legacy max transaction id: {}", tableId(), baseTableLegacyMaxTransactionId);
    }
    return baseTableLegacyMaxTransactionId;
  }

  static class CanSplitFileTree implements Predicate<FileTree> {

    /**
     * file tree can't split:
     * - root node is leaf node
     * - root node contains any base/insert/delete files
     * .
     *
     * @param fileTree - file tree to split
     * @return true if this fileTree need split
     */
    @Override
    public boolean test(FileTree fileTree) {
      if (fileTree.getLeft() == null && fileTree.getRight() == null) {
        return false;
      }

      return fileTree.getBaseFiles().isEmpty() &&
          fileTree.getInsertFiles().isEmpty() &&
          fileTree.getDeleteFiles().isEmpty() &&
          fileTree.findAnyBaseFilesInTree();
    }
  }
}
