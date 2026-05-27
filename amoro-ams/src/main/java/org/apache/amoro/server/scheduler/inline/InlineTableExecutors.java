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

package org.apache.amoro.server.scheduler.inline;

import org.apache.amoro.config.Configurations;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.table.TableService;

import java.time.Duration;

public class InlineTableExecutors {

  private static final InlineTableExecutors instance = new InlineTableExecutors();
  private TableRuntimeRefreshExecutor tableRefreshingExecutor;
  private BlockerExpiringExecutor blockerExpiringExecutor;
  private OptimizingCommitExecutor optimizingCommitExecutor;
  private ProcessDataExpiringExecutor processDataExpiringExecutor;

  public static InlineTableExecutors getInstance() {
    return instance;
  }

  public void setup(TableService tableService, Configurations conf) {
    this.optimizingCommitExecutor =
        new OptimizingCommitExecutor(
            tableService, conf.getInteger(AmoroManagementConf.OPTIMIZING_COMMIT_THREAD_COUNT));
    Duration optimizingKeepTime =
        conf.contains(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_TIME)
            ? conf.get(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_TIME)
            : Duration.ofDays(
                conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_KEEP_DAYS));
    Duration expireInterval =
        conf.contains(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL)
            ? conf.get(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL)
            : Duration.ofHours(
                conf.getInteger(AmoroManagementConf.OPTIMIZING_RUNTIME_DATA_EXPIRE_INTERVAL_HOURS));
    Duration processKeepTime =
        conf.contains(AmoroManagementConf.PROCESS_HISTORY_DATA_KEEP_TIME)
            ? conf.get(AmoroManagementConf.PROCESS_HISTORY_DATA_KEEP_TIME)
            : Duration.ofDays(conf.getInteger(AmoroManagementConf.PROCESS_HISTORY_DATA_KEEP_DAYS));
    this.processDataExpiringExecutor =
        new ProcessDataExpiringExecutor(
            tableService, optimizingKeepTime, expireInterval, processKeepTime);
    this.blockerExpiringExecutor = new BlockerExpiringExecutor(tableService);
    this.tableRefreshingExecutor =
        new TableRuntimeRefreshExecutor(
            tableService,
            conf.getInteger(AmoroManagementConf.REFRESH_TABLES_THREAD_COUNT),
            conf.get(AmoroManagementConf.REFRESH_TABLES_INTERVAL).toMillis(),
            conf.getInteger(AmoroManagementConf.REFRESH_MAX_PENDING_PARTITIONS));
  }

  public TableRuntimeRefreshExecutor getTableRefreshingExecutor() {
    return tableRefreshingExecutor;
  }

  public BlockerExpiringExecutor getBlockerExpiringExecutor() {
    return blockerExpiringExecutor;
  }

  public OptimizingCommitExecutor getOptimizingCommitExecutor() {
    return optimizingCommitExecutor;
  }

  public ProcessDataExpiringExecutor getProcessDataExpiringExecutor() {
    return processDataExpiringExecutor;
  }
}
