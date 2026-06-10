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

package org.apache.amoro.server.process.iceberg;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.utils.SnowflakeIdGenerator;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** Local table process for expiring optimizing runtime data and process history records. */
public class ProcessDataExpiringProcess extends TableProcess implements LocalProcess {

  private static final Logger LOG = LoggerFactory.getLogger(ProcessDataExpiringProcess.class);

  private final Persistency persistency = new Persistency();
  private final long runtimeKeepTimeMs;
  private final long historyKeepTimeMs;

  public ProcessDataExpiringProcess(
      TableRuntime tableRuntime,
      ExecuteEngine engine,
      long runtimeKeepTimeMs,
      long historyKeepTimeMs) {
    super(tableRuntime, engine);
    this.runtimeKeepTimeMs = runtimeKeepTimeMs;
    this.historyKeepTimeMs = historyKeepTimeMs;
  }

  @Override
  public String tag() {
    return getAction().getName().toLowerCase();
  }

  @Override
  public void run() {
    try {
      persistency.doExpiring(tableRuntime);
    } catch (Throwable t) {
      LOG.error("Expiring table runtimes of {} failed.", tableRuntime.getTableIdentifier(), t);
      throw new RuntimeException(t);
    }
  }

  @Override
  public Action getAction() {
    return IcebergActions.EXPIRE_PROCESS_DATA;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Maps.newHashMap();
  }

  @Override
  public Map<String, String> getSummary() {
    return Maps.newHashMap();
  }

  private class Persistency extends PersistentBase {
    public void doExpiring(TableRuntime tableRuntime) {
      long tableId = tableRuntime.getTableIdentifier().getId();
      long now = System.currentTimeMillis();

      // 1. Expire optimizing runtime data
      long optimizingMinId = SnowflakeIdGenerator.getMinSnowflakeId(now - runtimeKeepTimeMs);
      doAsTransaction(
          () ->
              doAs(
                  TableProcessMapper.class,
                  mapper -> mapper.deleteBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteProcessStateBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteTaskRuntimesBefore(tableId, optimizingMinId)),
          () ->
              doAs(
                  OptimizingProcessMapper.class,
                  mapper -> mapper.deleteOptimizingQuotaBefore(tableId, optimizingMinId)));

      // 2. Expire process history terminal records
      //    Only deletes terminal records in the window between historyKeepTime and runtimeKeepTime,
      //    since records older than runtimeKeepTime are already removed by step 1.
      if (historyKeepTimeMs < runtimeKeepTimeMs) {
        long processMinId = SnowflakeIdGenerator.getMinSnowflakeId(now - historyKeepTimeMs);
        doAs(
            TableProcessMapper.class,
            mapper -> mapper.deleteExpiredProcesses(tableId, processMinId));
      }
    }
  }
}
