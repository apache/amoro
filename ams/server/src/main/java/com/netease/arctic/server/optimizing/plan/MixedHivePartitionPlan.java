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

package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;

public class MixedHivePartitionPlan extends MixedIcebergPartitionPlan {
  private final String hiveLocation;
  private long maxSequence = 0;
  private String customHiveSubdirectory;

  public MixedHivePartitionPlan(
      TableRuntime tableRuntime,
      ArcticTable table,
      String partition,
      String hiveLocation,
      long planTime) {
    super(tableRuntime, table, partition, planTime);
    this.hiveLocation = hiveLocation;
  }

  @Override
  public boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    if (!super.addFile(dataFile, deletes)) {
      return false;
    }
    long sequenceNumber = dataFile.dataSequenceNumber();
    if (sequenceNumber > maxSequence) {
      maxSequence = sequenceNumber;
    }
    return true;
  }

  @Override
  protected void beforeSplit() {
    super.beforeSplit();
    if (evaluator().isFullOptimizing() && moveFiles2CurrentHiveLocation()) {
      // This is an improvement for full optimizing of hive table, if there are no delete files, we
      // only have to move
      // files not in hive location to hive location, so the files in the hive location should not
      // be optimizing.
      Preconditions.checkArgument(reservedDeleteFiles.isEmpty(), "delete files should be empty");
      rewriteDataFiles.entrySet().removeIf(entry -> evaluator().inHiveLocation(entry.getKey()));
      rewritePosDataFiles.entrySet().removeIf(entry -> evaluator().inHiveLocation(entry.getKey()));
    }
  }

  private boolean moveFiles2CurrentHiveLocation() {
    return evaluator().isFullNecessary()
        && !config.isFullRewriteAllFiles()
        && !evaluator().anyDeleteExist();
  }

  @Override
  protected MixedHivePartitionEvaluator evaluator() {
    return ((MixedHivePartitionEvaluator) super.evaluator());
  }

  @Override
  protected CommonPartitionEvaluator buildEvaluator() {
    return new MixedHivePartitionEvaluator(
        tableRuntime, partition, partitionProperties, hiveLocation, planTime, isKeyedTable());
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = super.buildTaskProperties();
    if (moveFiles2CurrentHiveLocation()) {
      properties.needMoveFile2HiveLocation();
    } else if (evaluator().isFullNecessary()) {
      properties.setOutputDir(constructCustomHiveSubdirectory());
    }
    return properties;
  }

  private String constructCustomHiveSubdirectory() {
    if (customHiveSubdirectory == null) {
      if (isKeyedTable()) {
        customHiveSubdirectory = HiveTableUtil.newHiveSubdirectory(maxSequence);
      } else {
        customHiveSubdirectory = HiveTableUtil.newHiveSubdirectory();
      }
    }
    return customHiveSubdirectory;
  }

  protected static class MixedHivePartitionEvaluator extends MixedIcebergPartitionEvaluator {
    private final String hiveLocation;
    private final boolean reachHiveRefreshInterval;

    private boolean filesNotInHiveLocation = false;

    public MixedHivePartitionEvaluator(
        TableRuntime tableRuntime,
        String partition,
        Map<String, String> partitionProperties,
        String hiveLocation,
        long planTime,
        boolean keyedTable) {
      super(tableRuntime, partition, partitionProperties, planTime, keyedTable);
      this.hiveLocation = hiveLocation;
      String optimizedTime =
          partitionProperties.get(HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME);
      // the unit of transient-time is seconds
      long lastHiveOptimizedTime =
          optimizedTime == null ? 0 : Integer.parseInt(optimizedTime) * 1000L;
      this.reachHiveRefreshInterval =
          config.getHiveRefreshInterval() >= 0
              && planTime - lastHiveOptimizedTime > config.getHiveRefreshInterval();
    }

    @Override
    public boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes) {
      if (!super.addFile(dataFile, deletes)) {
        return false;
      }
      if (!filesNotInHiveLocation && !inHiveLocation(dataFile)) {
        filesNotInHiveLocation = true;
      }
      return true;
    }

    @Override
    protected boolean isFragmentFile(DataFile dataFile) {
      PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
      if (file.type() == DataFileType.BASE_FILE) {
        // we treat all files in hive location as segment files
        return dataFile.fileSizeInBytes() <= fragmentSize && !inHiveLocation(dataFile);
      } else if (file.type() == DataFileType.INSERT_FILE) {
        // we treat all insert files as fragment files
        return true;
      } else {
        throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
      }
    }

    @Override
    public boolean isFullNecessary() {
      if (!reachFullInterval() && !reachHiveRefreshInterval()) {
        return false;
      }
      return fragmentFileCount > getBaseSplitCount()
          || undersizedSegmentFileCount + rewriteSegmentFileCount > getBaseSplitCount()
          || hasNewHiveData();
    }

    @Override
    protected boolean isFullOptimizing() {
      return reachFullInterval() || reachHiveRefreshInterval();
    }

    protected boolean hasNewHiveData() {
      return anyDeleteExist() || hasChangeFiles || filesNotInHiveLocation;
    }

    protected boolean reachHiveRefreshInterval() {
      return reachHiveRefreshInterval;
    }

    @Override
    public boolean segmentShouldRewrite(DataFile dataFile, List<ContentFile<?>> deletes) {
      if (isFullOptimizing()) {
        return fileShouldFullOptimizing(dataFile, deletes);
      } else {
        // if it is not full optimizing, we only rewrite files not in hive location
        return !inHiveLocation(dataFile) && super.segmentShouldRewrite(dataFile, deletes);
      }
    }

    @Override
    protected boolean fileShouldFullOptimizing(
        DataFile dataFile, List<ContentFile<?>> deleteFiles) {
      return true;
    }

    @Override
    public PartitionEvaluator.Weight getWeight() {
      return new Weight(
          getCost(),
          hasChangeFiles && reachBaseRefreshInterval()
              || hasNewHiveData() && reachHiveRefreshInterval());
    }

    private boolean inHiveLocation(ContentFile<?> file) {
      return file.path().toString().contains(hiveLocation);
    }
  }
}
