/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.flow.checker;

import com.netease.arctic.hive.HMSClientPool;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.optimizing.IcebergCommit;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.optimizing.flow.CompleteOptimizingFlow;
import com.netease.arctic.server.optimizing.plan.OptimizingPlanner;
import com.netease.arctic.server.optimizing.plan.TaskDescriptor;
import com.netease.arctic.table.ArcticTable;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hive.metastore.api.Partition;

public class FullOptimizingChecker implements CompleteOptimizingFlow.Checker {

  private int count;

  @Override
  public boolean condition(
      ArcticTable table,
      @Nullable List<TaskDescriptor> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable IcebergCommit latestCommit
  ) {
    if (CollectionUtils.isNotEmpty(latestTaskDescriptors) &&
        OptimizingInputProperties.parse(latestTaskDescriptors.stream().findAny().get().properties()).getOutputDir() != null) {
      count++;
      return true;
    }
    return false;
  }

  @Override
  public boolean senseHasChecked() {
    return count > 0;
  }

  @Override
  public void check(
      ArcticTable table,
      @Nullable List<TaskDescriptor> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable IcebergCommit latestCommit
  ) throws Exception {
    SupportHive supportHive = (SupportHive) table;
    String hiveLocation = supportHive.hiveLocation();

    HMSClientPool hmsClient = supportHive.getHMSClient();
    List<Partition> list = hmsClient.run(c -> c.listPartitions(table.id().getDatabase(), table.id().getTableName(), (short) 100));
  }
}
