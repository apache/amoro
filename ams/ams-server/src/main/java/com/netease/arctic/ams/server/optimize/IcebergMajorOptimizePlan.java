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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class IcebergMajorOptimizePlan extends BaseIcebergOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergMajorOptimizePlan.class);

  public IcebergMajorOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                  List<FileScanTask> fileScanTasks,
                                  Map<String, Boolean> partitionTaskRunning,
                                  int queueId, long currentTime) {
    super(arcticTable, tableOptimizeRuntime, fileScanTasks, partitionTaskRunning, queueId, currentTime);
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

  @Override
  protected List<BaseOptimizeTask> collectTask(String partition) {
    List<BaseOptimizeTask> collector = new ArrayList<>();
    String commitGroup = UUID.randomUUID().toString();
    long createTime = System.currentTimeMillis();

    TaskConfig taskPartitionConfig = new TaskConfig(partition, null,
        commitGroup, planGroup, OptimizeType.Major, createTime, "");
    List<FileScanTask> fileScanTasks = partitionFileList.get(partition);
    collector.add(buildOptimizeTask(fileScanTasks, taskPartitionConfig));

    return collector;
  }
}