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

package org.apache.amoro.server.refresh.events;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.server.refresh.RefreshEvent;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Default refresh event that evaluates PendingInput each time the executor executes */
public class DefaultRefreshEvent implements RefreshEvent {
  private static final Logger logger = LoggerFactory.getLogger(DefaultRefreshEvent.class);
  private static final String IDENTIFIER = "default";

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public void execute(
      TableRuntime tableRuntime, TableService tableService, int maxPendingPartitions) {
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;

    long lastOptimizedSnapshotId = defaultTableRuntime.getLastOptimizedSnapshotId();
    long lastOptimizedChangeSnapshotId = defaultTableRuntime.getLastOptimizedChangeSnapshotId();
    AmoroTable<?> table = tableService.loadTable(tableRuntime.getTableIdentifier());
    defaultTableRuntime.refresh(table);
    MixedTable mixedTable = (MixedTable) table.originalTable();
    if ((mixedTable.isKeyedTable()
            && (lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId()
                || lastOptimizedChangeSnapshotId
                    != defaultTableRuntime.getCurrentChangeSnapshotId()))
        || (mixedTable.isUnkeyedTable()
            && lastOptimizedSnapshotId != defaultTableRuntime.getCurrentSnapshotId())) {
      tryEvaluatingPendingInput(defaultTableRuntime, mixedTable, maxPendingPartitions, logger);
    }
  }

  /**
   * Tries to evaluate pending input for tableRuntime.
   *
   * @param table The table to be evaluated.
   * @param tableRuntime The runtime information of the table.
   * @param maxPendingPartitions The maximum number of pending partitions.
   */
  public void tryEvaluatingPendingInput(
      DefaultTableRuntime tableRuntime, MixedTable table, int maxPendingPartitions, Logger logger) {
    logger.info("Try evaluating pendingInput for table {}", tableRuntime.getTableIdentifier());
    if (tableRuntime.getOptimizingConfig().isEnabled()
        && !tableRuntime.getOptimizingStatus().isProcessing()) {
      AbstractOptimizingEvaluator evaluator =
          IcebergTableUtil.createOptimizingEvaluator(tableRuntime, table, maxPendingPartitions);
      if (evaluator.isNecessary()) {
        AbstractOptimizingEvaluator.PendingInput pendingInput =
            evaluator.getOptimizingPendingInput();
        logger.debug(
            "{} optimizing is necessary and get pending input {}",
            tableRuntime.getTableIdentifier(),
            pendingInput);
        tableRuntime.setPendingInput(pendingInput);
      } else {
        tableRuntime.optimizingNotNecessary();
      }
      tableRuntime.setTableSummary(evaluator.getPendingInput());
    }
  }

  @Override
  public long getNextExecutingTime(TableRuntime tableRuntime, long defaultInterval) {
    DefaultTableRuntime defaultTableRuntime = (DefaultTableRuntime) tableRuntime;
    return Math.min(
        defaultTableRuntime.getOptimizingConfig().getMinorLeastInterval() * 4L / 5,
        defaultInterval);
  }
}
