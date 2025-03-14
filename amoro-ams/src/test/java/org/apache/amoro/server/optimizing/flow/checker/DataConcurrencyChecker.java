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

import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.plan.AbstractOptimizingPlanner;
import org.apache.amoro.server.optimizing.UnKeyedTableCommit;
import org.apache.amoro.server.optimizing.flow.CompleteOptimizingFlow;
import org.apache.amoro.server.optimizing.flow.DataReader;
import org.apache.amoro.server.optimizing.flow.view.MatchResult;
import org.apache.amoro.server.optimizing.flow.view.TableDataView;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.data.Record;

import javax.annotation.Nullable;

import java.util.List;

public class DataConcurrencyChecker implements CompleteOptimizingFlow.Checker {

  private final TableDataView view;

  private int count;

  public DataConcurrencyChecker(TableDataView view) {
    this.view = view;
  }

  @Override
  public boolean condition(
      MixedTable table,
      @Nullable List<RewriteStageTask> latestTaskDescriptors,
      AbstractOptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit) {
    count++;
    return true;
  }

  @Override
  public boolean senseHasChecked() {
    return count > 0;
  }

  @Override
  public void check(
      MixedTable table,
      @Nullable List<RewriteStageTask> latestTaskDescriptors,
      AbstractOptimizingPlanner latestPlanner,
      @Nullable UnKeyedTableCommit latestCommit)
      throws Exception {
    table.refresh();
    List<Record> records = new DataReader(table).allData();

    MatchResult match = view.match(records);
    if (!match.isOk()) {
      throw new RuntimeException("Data is error: " + match);
    }
  }
}
