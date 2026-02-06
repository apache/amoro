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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.ContentFiles;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IcebergPartitionPlan extends AbstractPartitionPlan {

  public IcebergPartitionPlan(
      ServerTableIdentifier identifier,
      OptimizingConfig config,
      MixedTable table,
      Pair<Integer, StructLike> partition,
      long planTime,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime,
      long lastMajorOptimizingTime) {
    super(
        identifier,
        table,
        config,
        partition,
        planTime,
        lastMinorOptimizingTime,
        lastFullOptimizingTime,
        lastMajorOptimizingTime);
  }

  @Override
  protected TaskSplitter buildTaskSplitter() {
    return new BinPackingTaskSplitter();
  }

  @Override
  protected Map<String, String> buildTaskProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, IcebergRewriteExecutorFactory.class.getName());
    return properties;
  }

  @Override
  protected List<SplitTask> filterSplitTasks(List<SplitTask> splitTasks) {
    return splitTasks.stream().filter(this::enoughInputFiles).collect(Collectors.toList());
  }

  protected boolean enoughInputFiles(SplitTask splitTask) {
    boolean only1DataFile =
        splitTask.getRewriteDataFiles().size() == 1
            && splitTask.getRewritePosDataFiles().size() == 0
            && splitTask.getDeleteFiles().size() == 0
            && !(config.isRewriteAllAvro()
                && ContentFiles.isAvroFile(splitTask.getRewriteDataFiles().iterator().next()));
    return !only1DataFile;
  }
}
