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
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.model.TaskConfig;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IcebergMajorOptimizePlan extends BaseIcebergOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergMajorOptimizePlan.class);

  protected final Map<String, List<FileScanTask>> partitionFileList = new LinkedHashMap<>();

  public IcebergMajorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                  Map<String, Boolean> partitionTaskRunning,
                                  int queueId, long currentTime) {
    super(arcticTable, tableOptimizeRuntime, partitionTaskRunning, queueId, currentTime);
  }

  protected void addOptimizeFiles() {
    LOG.debug("{} start plan iceberg table files", tableId());
    AtomicInteger addCnt = new AtomicInteger();
    for (FileScanTask fileScanTask : arcticTable.asUnkeyedTable().newScan().planFiles()) {
      DataFile dataFile = fileScanTask.file();
      String partitionPath = arcticTable.spec().partitionToPath(dataFile.partition());
      currentPartitions.add(partitionPath);
      if (!anyTaskRunning(partitionPath)) {
        List<FileScanTask> fileScanTasks = partitionFileList.computeIfAbsent(partitionPath, p -> new ArrayList<>());
        fileScanTasks.add(fileScanTask);
        addCnt.getAndIncrement();
      }
    }

    LOG.debug("{} ==== {} add {} data files" + " After added, partition cnt of tree: {}",
        tableId(), getOptimizeType(), addCnt, partitionFileList.size());
  }

  @Override
  protected boolean partitionNeedPlan(String partitionToPath) {
    List<FileScanTask> partitionFileScanTasks = partitionFileList.get(partitionToPath);
    if (CollectionUtils.isEmpty(partitionFileScanTasks)) {
      LOG.debug("{} ==== don't need {} optimize plan, skip partition {}, there are no data files",
          tableId(), getOptimizeType(), partitionToPath);
      return false;
    }

    Set<DeleteFile> partitionDeleteFiles = new HashSet<>();
    Set<DataFile> partitionDataFiles = new HashSet<>();
    for (FileScanTask partitionFileScanTask : partitionFileScanTasks) {
      partitionDataFiles.add(partitionFileScanTask.file());
      partitionDeleteFiles.addAll(partitionFileScanTask.deletes());
    }

    double deleteFilesTotalSize = partitionDeleteFiles.stream().mapToLong(DeleteFile::fileSizeInBytes).sum();
    double dataFilesTotalSize = partitionDataFiles.stream().mapToLong(DataFile::fileSizeInBytes).sum();

    long duplicateSize = PropertyUtil.propertyAsLong(arcticTable.properties(),
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD,
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_SIZE_BYTES_THRESHOLD_DEFAULT);
    double duplicateRatio = PropertyUtil.propertyAsDouble(arcticTable.properties(),
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_RATIO_THRESHOLD,
        TableProperties.MAJOR_OPTIMIZE_TRIGGER_DUPLICATE_RATIO_THRESHOLD_DEFAULT);
    // delete files total size reach target or delete files rate reach target
    if (deleteFilesTotalSize > duplicateSize || deleteFilesTotalSize / dataFilesTotalSize >= duplicateRatio) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.Major);
      LOG.debug("{} ==== need native Major optimize plan, partition is {}, " +
              "delete files totalSize is {}, data files totalSize is {}",
          tableId(), partitionToPath, deleteFilesTotalSize, dataFilesTotalSize);
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}, " +
            "delete files totalSize is {}, data files totalSize is {}",
        tableId(), getOptimizeType(), partitionToPath, deleteFilesTotalSize, dataFilesTotalSize);
    return false;
  }

  @Override
  protected OptimizeType getOptimizeType() {
    return OptimizeType.Major;
  }

  public boolean hasFileToOptimize() {
    return !partitionFileList.isEmpty();
  }

  @Override
  protected List<BaseOptimizeTask> collectTask(String partition) {
    List<BaseOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, null,
        commitGroup, planGroup, OptimizeType.Major, createTime, "");
    List<FileScanTask> fileScanTasks = partitionFileList.get(partition);

    List<List<FileScanTask>> binPackFileScanTasks = binPackFileScanTask(fileScanTasks);
    for (List<FileScanTask> fileScanTask : binPackFileScanTasks) {
      List<DataFile> dataFiles = new ArrayList<>();
      List<DeleteFile> eqDeleteFiles = new ArrayList<>();
      List<DeleteFile> posDeleteFiles = new ArrayList<>();
      getOptimizeFile(fileScanTask, dataFiles, eqDeleteFiles, posDeleteFiles);

      collector.add(buildOptimizeTask(dataFiles, Collections.emptyList(),
          eqDeleteFiles, posDeleteFiles, currentSnapshotId, taskPartitionConfig));
    }

    return collector;
  }
}