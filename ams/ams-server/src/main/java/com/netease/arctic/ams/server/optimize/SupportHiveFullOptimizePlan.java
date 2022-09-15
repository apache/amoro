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

import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.DataFileInfo;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class SupportHiveFullOptimizePlan extends FullOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(SupportHiveFullOptimizePlan.class);

  // hive location.
  protected final String hiveLocation;
  // files in locations don't need to major optimize
  protected final Set<String> excludeLocations = new HashSet<>();

  public SupportHiveFullOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                                      List<DataFileInfo> baseTableFileList, List<DataFileInfo> posDeleteFileList,
                                      Map<String, Boolean> partitionTaskRunning, int queueId, long currentTime,
                                      Predicate<Long> snapshotIsCached) {
    super(arcticTable, tableOptimizeRuntime, baseTableFileList, posDeleteFileList,
        partitionTaskRunning, queueId, currentTime, snapshotIsCached);

    Preconditions.checkArgument(TableTypeUtil.isHive(arcticTable), "The table not support hive");
    hiveLocation = ((SupportHive) arcticTable).hiveLocation();
    excludeLocations.add(hiveLocation);
  }

  @Override
  protected boolean partitionNeedPlan(String partitionToPath) {
    long current = System.currentTimeMillis();

    List<DeleteFile> posDeleteFiles = partitionPosDeleteFiles.getOrDefault(partitionToPath, new ArrayList<>());
    List<DataFile> baseFiles = partitionFileTree.get(partitionToPath).getBaseFiles();
    long inHiveSmallFileCount = 0;
    long notInHiveFileCount = 0;
    for (DataFile baseFile : baseFiles) {
      boolean inHive = baseFile.path().toString().contains(((SupportHive) arcticTable).hiveLocation());
      if (!inHive) {
        notInHiveFileCount++;
      } else if (baseFile.fileSizeInBytes() <=
          PropertyUtil.propertyAsLong(arcticTable.properties(),
              TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD,
              TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD_DEFAULT)) {
        inHiveSmallFileCount++;
      }
    }
    // check whether partition need plan by files info
    // for no pos-delete, there are files in not hive location or small file count bigger than 2 in hive location
    // for has pos-delete, need full optimize
    boolean partitionNeedPlan =
        CollectionUtils.isNotEmpty(posDeleteFiles) || inHiveSmallFileCount >= 2 || notInHiveFileCount > 0;

    // check position delete file total size
    if (checkPosDeleteTotalSize(partitionToPath) && partitionNeedPlan) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.FullMajor);
      return true;
    }

    // check full optimize interval
    if (checkFullOptimizeInterval(current, partitionToPath) && partitionNeedPlan) {
      partitionOptimizeType.put(partitionToPath, OptimizeType.FullMajor);
      return true;
    }

    LOG.debug("{} ==== don't need {} optimize plan, skip partition {}, partitionNeedPlan is {}",
        tableId(), getOptimizeType(), partitionToPath, partitionNeedPlan);
    return false;
  }

  @Override
  protected boolean needOptimize(List<DeleteFile> posDeleteFiles, List<DataFile> baseFiles) {
    return true;
  }
}
