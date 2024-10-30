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

import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.server.optimizing.RewriteStageTask;
import org.apache.amoro.server.optimizing.UnKeyedTableCommit;
import org.apache.amoro.server.optimizing.flow.view.TableDataView;
import org.apache.amoro.server.optimizing.plan.OptimizingPlanner;
import org.apache.amoro.table.MixedTable;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nullable;

import java.util.List;

public class FullOptimizingWrite2HiveChecker extends AbstractHiveChecker {

  public FullOptimizingWrite2HiveChecker(TableDataView view) {
    super(view);
  }

  @Override
  protected boolean internalCondition(
      MixedTable table,
      @Nullable List<RewriteStageTask> latestTaskDescriptors,
      OptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit) {
    return CollectionUtils.isNotEmpty(latestTaskDescriptors)
        && latestPlanner.getOptimizingType() == OptimizingType.FULL
        && OptimizingInputProperties.parse(
                    latestTaskDescriptors.stream().findAny().get().getProperties())
                .getOutputDir()
            != null;
  }
}
