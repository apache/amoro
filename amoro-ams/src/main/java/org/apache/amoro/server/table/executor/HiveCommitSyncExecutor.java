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

package org.apache.amoro.server.table.executor;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.HiveMetaSynchronizer;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HiveCommitSyncExecutor extends BaseTableExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(HiveCommitSyncExecutor.class);

  // 10 minutes
  private static final long INTERVAL = 10 * 60 * 1000L;

  public HiveCommitSyncExecutor(TableManager tableManager, int poolSize) {
    super(tableManager, poolSize);
  }

  @Override
  protected long getNextExecutingTime(TableRuntime tableRuntime) {
    return INTERVAL;
  }

  @Override
  protected boolean enabled(TableRuntime tableRuntime) {
    return true;
  }

  @Override
  protected void execute(TableRuntime tableRuntime) {
    long startTime = System.currentTimeMillis();
    ServerTableIdentifier tableIdentifier = tableRuntime.getTableIdentifier();
    try {
      MixedTable mixedTable = (MixedTable) loadTable(tableRuntime).originalTable();
      if (!TableTypeUtil.isHive(mixedTable)) {
        LOG.debug("{} is not a support hive table", tableIdentifier);
        return;
      }
      LOG.info("{} start hive sync", tableIdentifier);
      syncIcebergToHive(mixedTable);
    } catch (Exception e) {
      LOG.error("{} hive sync failed", tableIdentifier, e);
    } finally {
      LOG.info(
          "{} hive sync finished, cost {}ms",
          tableIdentifier,
          System.currentTimeMillis() - startTime);
    }
  }

  public static void syncIcebergToHive(MixedTable mixedTable) {
    HiveMetaSynchronizer.syncMixedTableDataToHive((SupportHive) mixedTable);
  }
}
