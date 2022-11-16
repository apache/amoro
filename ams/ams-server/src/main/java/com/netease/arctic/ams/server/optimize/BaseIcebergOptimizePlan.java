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
import com.netease.arctic.ams.api.properties.OptimizeTaskProperties;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.FilesStatistics;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.ams.server.utils.FilesStatisticsBuilder;
import com.netease.arctic.ams.server.utils.UnKeyedTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * only used for native iceberg
 */
public abstract class BaseIcebergOptimizePlan extends BaseOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(BaseIcebergOptimizePlan.class);

  protected final List<FileScanTask> fileScanTasks;
  protected final Map<String, List<FileScanTask>> partitionFileList = new LinkedHashMap<>();
  protected long currentSnapshotId = TableOptimizeRuntime.INVALID_SNAPSHOT_ID;

  public BaseIcebergOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                 List<FileScanTask> fileScanTasks,
                                 Map<String, Boolean> partitionTaskRunning,
                                 int queueId, long currentTime) {
    super(arcticTable, tableOptimizeRuntime, partitionTaskRunning, queueId, currentTime);
    this.fileScanTasks = fileScanTasks;
  }

  protected boolean tableChanged() {
    long lastSnapshotId = tableOptimizeRuntime.getCurrentSnapshotId();
    LOG.debug("{} ==== {} currentSnapshotId={}, lastSnapshotId={}", tableId(), getOptimizeType(),
        currentSnapshotId, lastSnapshotId);
    return currentSnapshotId != lastSnapshotId;
  }

  protected void addOptimizeFiles() {
    LOG.debug("{} start plan native table files", tableId());
    AtomicInteger addCnt = new AtomicInteger();

    fileScanTasks.forEach(fileScanTask -> {
      DataFile dataFile = fileScanTask.file();
      String partitionPath = arcticTable.spec().partitionToPath(dataFile.partition());
      currentPartitions.add(partitionPath);
      if (!anyTaskRunning(partitionPath)) {
        List<FileScanTask> fileScanTasks = partitionFileList.computeIfAbsent(partitionPath, p -> new ArrayList<>());
        fileScanTasks.add(fileScanTask);
        addCnt.getAndIncrement();
      }
    });

    LOG.debug("{} ==== {} add {} data files into tree, total files: {}." + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, fileScanTasks.size(), partitionFileList.size());
  }

  protected BaseOptimizeTask buildOptimizeTask(List<FileScanTask> fileScanTasks,
                                               TaskConfig taskConfig) {
    // build task
    BaseOptimizeTask optimizeTask = new BaseOptimizeTask();
    optimizeTask.setTaskCommitGroup(taskConfig.getCommitGroup());
    optimizeTask.setTaskPlanGroup(taskConfig.getPlanGroup());
    optimizeTask.setCreateTime(taskConfig.getCreateTime());

    List<ByteBuffer> fileScanTaskBytesList =
        fileScanTasks.stream()
            .map(SerializationUtil::toByteBuffer)
            .collect(Collectors.toList());
    optimizeTask.setBaseFiles(Collections.emptyList());
    optimizeTask.setPosDeleteFiles(Collections.emptyList());
    optimizeTask.setInsertFiles(Collections.emptyList());
    optimizeTask.setDeleteFiles(Collections.emptyList());
    optimizeTask.setIcebergFileScanTasks(fileScanTaskBytesList);

    FilesStatisticsBuilder baseFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder eqDeleteFb = new FilesStatisticsBuilder();
    FilesStatisticsBuilder posDeleteFb = new FilesStatisticsBuilder();
    Set<DeleteFile> deleteFileSet = new HashSet<>();
    for (FileScanTask fileScanTask : fileScanTasks) {
      baseFb.addFile(fileScanTask.file().fileSizeInBytes());
      for (DeleteFile delete : fileScanTask.deletes()) {
        if (!deleteFileSet.contains(delete)) {
          if (delete.content() == FileContent.POSITION_DELETES) {
            posDeleteFb.addFile(delete.fileSizeInBytes());
          } else {
            eqDeleteFb.addFile(delete.fileSizeInBytes());
          }
          deleteFileSet.add(delete);
        }
      }
    }

    FilesStatistics baseFs = baseFb.build();
    FilesStatistics eqDeleteFs = eqDeleteFb.build();
    FilesStatistics posDeleteFs = posDeleteFb.build();

    // file size
    optimizeTask.setBaseFileSize(baseFs.getTotalSize());
    optimizeTask.setEqDeleteFileSize(eqDeleteFs.getTotalSize());
    optimizeTask.setPosDeleteFileSize(posDeleteFs.getTotalSize());

    // file count
    optimizeTask.setBaseFileCnt(baseFs.getFileCnt());
    optimizeTask.setEqDeleteFileCnt(eqDeleteFs.getFileCnt());
    optimizeTask.setPosDeleteFileCnt(posDeleteFs.getFileCnt());

    optimizeTask.setPartition(taskConfig.getPartition());
    optimizeTask.setQueueId(queueId);
    optimizeTask.setTaskId(new OptimizeTaskId(taskConfig.getOptimizeType(), UUID.randomUUID().toString()));
    optimizeTask.setTableIdentifier(arcticTable.id().buildTableIdentifier());

    Map<String, String> properties = new HashMap<>();

    // fill task summary to check
    AtomicInteger fileCntInFileScanTask = new AtomicInteger();
    fileScanTasks
        .forEach(fileScanTask ->
            fileCntInFileScanTask.set(fileCntInFileScanTask.get() + 1 + fileScanTask.deletes().size()));
    properties.put(OptimizeTaskProperties.ALL_FILE_COUNT, fileCntInFileScanTask.get() + "");
    optimizeTask.setProperties(properties);
    return optimizeTask;
  }

  public boolean tableNeedPlan() {
    this.currentSnapshotId = UnKeyedTableUtil.getSnapshotId(arcticTable.asUnkeyedTable());
    return tableChanged();
  }

  public boolean hasFileToOptimize() {
    return !partitionFileList.isEmpty();
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    throw new IllegalArgumentException("Native iceberg don't have change snapshot");
  }
}