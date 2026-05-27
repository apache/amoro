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
import org.apache.amoro.AmoroTable;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.hive.utils.HiveMetaSynchronizer;
import org.apache.amoro.hive.utils.TableTypeUtil;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.MixedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** Local table process for syncing Iceberg metadata to Hive. */
public class HiveCommitSyncProcess extends TableProcess implements LocalProcess {

  private static final Logger LOG = LoggerFactory.getLogger(HiveCommitSyncProcess.class);

  public static void syncIcebergToHive(MixedTable mixedTable) {
    HiveMetaSynchronizer.syncMixedTableDataToHive((SupportHive) mixedTable);
  }

  public HiveCommitSyncProcess(TableRuntime tableRuntime, ExecuteEngine engine) {
    super(tableRuntime, engine);
  }

  @Override
  public String tag() {
    return getAction().getName().toLowerCase();
  }

  @Override
  public void run() {
    ServerTableIdentifier tableIdentifier = tableRuntime.getTableIdentifier();
    try {
      AmoroTable<?> amoroTable = tableRuntime.loadTable();
      MixedTable mixedTable = (MixedTable) amoroTable.originalTable();
      if (!TableTypeUtil.isHive(mixedTable)) {
        LOG.debug("{} is not a support hive table", tableIdentifier);
        return;
      }

      LOG.info("{} start hive sync", tableIdentifier);
      syncIcebergToHive(mixedTable);
    } catch (Exception e) {
      LOG.error("{} hive sync failed", tableIdentifier, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public Action getAction() {
    return IcebergActions.SYNC_HIVE_TABLES;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Maps.newHashMap();
  }

  @Override
  public Map<String, String> getSummary() {
    return Maps.newHashMap();
  }
}
