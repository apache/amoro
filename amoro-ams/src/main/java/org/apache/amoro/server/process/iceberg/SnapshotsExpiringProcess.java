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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.maintainer.TableMaintainer;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.optimizing.maintainer.TableMaintainers;
import org.apache.amoro.server.process.AmsProcessContext;
import org.apache.amoro.server.process.executor.LocalExecutionEngine;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Local table process for expiring Iceberg snapshots. */
public class SnapshotsExpiringProcess extends TableProcess implements LocalProcess {

  private static final Logger LOG = LoggerFactory.getLogger(SnapshotsExpiringProcess.class);

  public SnapshotsExpiringProcess(TableRuntime tableRuntime, TableProcessStore store) {
    super(tableRuntime, store);
  }

  @Override
  public String tag() {
    return LocalExecutionEngine.SNAPSHOTS_EXPIRING_POOL;
  }

  @Override
  public void run() {
    try {
      TableService tableService = AmsProcessContext.tableService();
      if (tableService == null) {
        throw new IllegalStateException("TableService is not initialized");
      }
      AmoroTable<?> amoroTable = tableService.loadTable(tableRuntime.getTableIdentifier());
      TableMaintainer tableMaintainer = TableMaintainers.create(amoroTable, tableRuntime);
      tableMaintainer.expireSnapshots();
    } catch (Throwable t) {
      LOG.error("unexpected expire error of table {} ", tableRuntime.getTableIdentifier(), t);
    } finally {
      if (tableRuntime instanceof DefaultTableRuntime) {
        ((DefaultTableRuntime) tableRuntime)
            .updateLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING, System.currentTimeMillis());
      }
    }
  }

  @Override
  protected void closeInternal() {}
}
