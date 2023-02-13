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

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BasicOptimizeTask;
import com.netease.arctic.ams.server.model.FileTree;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MinorOptimizePlan extends AbstractArcticOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(MinorOptimizePlan.class);
  private final List<ArcticFileScanTask> changeFileScanTasks;

  // partition -> maxBaseTableTransactionId
  private long changeTableMaxTransactionId;
  private final Map<String, Long> changeTableMinTransactionId = new HashMap<>();

  public MinorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                           List<FileScanTask> baseFileScanTasks,
                           List<ArcticFileScanTask> changeFileScanTasks,
                           Map<String, Boolean> partitionTaskRunning,
                           int queueId, long currentTime, long changeSnapshotId, long baseSnapshotId) {
    super(arcticTable, tableOptimizeRuntime, baseFileScanTasks,
        partitionTaskRunning, queueId, currentTime, changeSnapshotId, baseSnapshotId);
    this.changeFileScanTasks = changeFileScanTasks;
  }

  @Override
  public boolean partitionNeedPlan(String partitionToPath) {
    long current = System.currentTimeMillis();
    List<DataFile> deleteFiles = getDeleteFilesFromFileTree(partitionToPath);

    // check delete file count
    if (CollectionUtils.isNotEmpty(deleteFiles)) {
      // file count
      if (deleteFiles.size() >= CompatiblePropertyUtil.propertyAsInt(arcticTable.properties(),
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
  protected OptimizeType getOptimizeType() {
    return OptimizeType.Minor;
  }

  @Override
  protected List<BasicOptimizeTask> collectTask(String partition) {
    List<BasicOptimizeTask> result;

    result = collectKeyedTableTasks(partition);

    return result;
  }

  @Override
  protected boolean tableChanged() {
    return changeTableChanged();
  }

  @Override
  protected boolean baseFileShouldOptimize(DataFile baseFile, String partition) {
    return true;
  }

  private boolean changeTableChanged() {
    long lastChangeSnapshotId = tableOptimizeRuntime.getCurrentChangeSnapshotId();
    LOG.debug("{} ==== {} currentChangeSnapshotId={}, lastChangeSnapshotId={}", tableId(), getOptimizeType(),
        currentChangeSnapshotId, lastChangeSnapshotId);
    return currentChangeSnapshotId != lastChangeSnapshotId;
  }

  @Override
  protected void addChangeFilesIntoFileTree() {
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
      if (!anyTaskRunning(partition)) {
        putChangeFileIntoFileTree(partition, file, file.type(), transactionId);
        markTransactionId(partition, transactionId);

        addCnt.getAndIncrement();
      }
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

  private List<BasicOptimizeTask> collectKeyedTableTasks(String partition) {
    FileTree treeRoot = partitionFileTree.get(partition);
    if (treeRoot == null) {
      return Collections.emptyList();
    }
    List<BasicOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, changeTableMaxTransactionId,
        changeTableMinTransactionId.get(partition),
        commitGroup, planGroup, OptimizeType.Minor, createTime, "");
    List<FileTree> subTrees = new ArrayList<>();
    // split tasks
    treeRoot.collectSubTree(subTrees, new ShouldSplitFileTree());
    for (FileTree subTree : subTrees) {
      List<DataFile> insertFiles = new ArrayList<>();
      List<DataFile> deleteFiles = new ArrayList<>();
      List<DataFile> baseFiles = new ArrayList<>();
      List<DeleteFile> posDeleteFiles = new ArrayList<>();
      subTree.collectInsertFiles(insertFiles);
      subTree.collectDeleteFiles(deleteFiles);
      subTree.collectBaseFiles(baseFiles);
      subTree.collectPosDeleteFiles(posDeleteFiles);
      // if no insert files and no eq-delete file, skip
      if (CollectionUtils.isEmpty(insertFiles) && CollectionUtils.isEmpty(deleteFiles)) {
        continue;
      }
      List<DataTreeNode> sourceNodes = new ArrayList<>();
      if (CollectionUtils.isNotEmpty(baseFiles)) {
        sourceNodes = Collections.singletonList(subTree.getNode());
      }
      collector.add(buildOptimizeTask(sourceNodes,
          insertFiles, deleteFiles, baseFiles, posDeleteFiles, taskPartitionConfig));
    }

    return collector;
  }

  private void markTransactionId(String partition, long fileTid) {
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

}
