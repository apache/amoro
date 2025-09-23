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

package org.apache.amoro.hive.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.PrimaryKeyedFile;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutorFactory;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.optimizing.plan.CommonPartitionEvaluator;
import org.apache.amoro.optimizing.plan.MixedIcebergPartitionPlan;
import org.apache.amoro.optimizing.plan.PartitionEvaluator;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;

import java.util.List;
import java.util.Map;

public class MixedHivePartitionPlan extends MixedIcebergPartitionPlan {
  private final String hiveLocation;
  private long maxSequence = 0;
  private String customHiveSubdirectory;

  public MixedHivePartitionPlan(
      ServerTableIdentifier identifier,
      MixedTable table,
      OptimizingConfig config,
      Pair<Integer, StructLike> partition,
      String hiveLocation,
      long planTime,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime) {
    super(
        identifier,
        table,
        config,
        partition,
        planTime,
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
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
        identifier,
        config,
        partition,
        partitionProperties,
        hiveLocation,
        planTime,
        isKeyedTable(),
        lastMinorOptimizingTime,
        lastFullOptimizingTime);
  }

  @Override
  protected Map<String, String> buildTaskProperties() {
    Map<String, String> properties = super.buildTaskProperties();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, MixedHiveRewriteExecutorFactory.class.getName());
    if (moveFiles2CurrentHiveLocation()) {
      properties.put(TaskProperties.MOVE_FILE_TO_HIVE_LOCATION, "true");
    } else if (evaluator().isFullNecessary()) {
      properties.put(TaskProperties.OUTPUT_DIR, constructCustomHiveSubdirectory());
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

  public static class MixedHivePartitionEvaluator extends MixedIcebergPartitionEvaluator {
    private final String hiveLocation;
    private final boolean reachHiveRefreshInterval;

    private boolean filesNotInHiveLocation = false;

    public MixedHivePartitionEvaluator(
        ServerTableIdentifier identifier,
        OptimizingConfig config,
        Pair<Integer, StructLike> partition,
        Map<String, String> partitionProperties,
        String hiveLocation,
        long planTime,
        boolean keyedTable,
        long lastMinorOptimizingTime,
        long lastFullOptimizingTime) {
      super(
          identifier,
          config,
          partition,
          partitionProperties,
          planTime,
          keyedTable,
          lastMinorOptimizingTime,
          lastFullOptimizingTime);
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
    protected boolean isUndersizedSegmentFile(DataFile dataFile) {
      return !inHiveLocation(dataFile) && super.isUndersizedSegmentFile(dataFile);
    }

    @Override
    public boolean isFullNecessary() {
      if (!reachFullInterval() && !reachHiveRefreshInterval()) {
        return false;
      }
      return fragmentFileCount > getBaseSplitCount() || hasNewHiveData();
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
    public boolean fileShouldRewrite(DataFile dataFile, List<ContentFile<?>> deletes) {
      if (isFullOptimizing()) {
        return fileShouldFullOptimizing(dataFile, deletes);
      } else {
        // if it is not full optimizing, we only rewrite files not in hive location
        return !inHiveLocation(dataFile) && super.fileShouldRewrite(dataFile, deletes);
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
