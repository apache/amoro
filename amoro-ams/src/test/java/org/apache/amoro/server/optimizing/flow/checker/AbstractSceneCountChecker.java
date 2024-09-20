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

package org.apache.amoro.server.optimizing.flow.checker;

import org.apache.amoro.server.optimizing.UnKeyedTableCommit;
import org.apache.amoro.server.optimizing.flow.CompleteOptimizingFlow;
import org.apache.amoro.server.optimizing.plan.OptimizingPlanner;
import org.apache.amoro.server.optimizing.plan.TaskDescriptor;
import org.apache.amoro.table.MixedTable;

import javax.annotation.Nullable;

import java.util.List;

public abstract class AbstractSceneCountChecker implements CompleteOptimizingFlow.Checker {

  private final int except;

  private int count;

  public AbstractSceneCountChecker(int except) {
    this.except = except;
  }

  @Override
  public boolean condition(
      MixedTable table,
      @Nullable List<TaskDescriptor> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit) {
    if (internalCondition(table, latestTaskDescriptors, latestPlanner, latestCommit)) {
      count++;
      return true;
    }
    return false;
  }

  protected abstract boolean internalCondition(
      MixedTable table,
      @Nullable List<TaskDescriptor> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit);

  @Override
  public boolean senseHasChecked() {
    return count >= except;
  }

  @Override
  public void check(
      MixedTable table,
      @Nullable List<TaskDescriptor> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit)
      throws Exception {}
}
