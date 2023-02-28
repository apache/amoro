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

package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.server.repair.Context;
import com.netease.arctic.ams.server.repair.DamageType;
import com.netease.arctic.ams.server.repair.RepairWay;
import com.netease.arctic.ams.server.repair.TableAvailableAnalyzer;
import com.netease.arctic.ams.server.repair.TableAvailableResult;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogManager;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;
import org.apache.iceberg.Snapshot;

public class AnalyzeCall implements CallCommand {

  private static final String TABLE_NAME = "TABLE_NAME";
  private static final String YOU_CAN = "YOU CAN";
  private static final String TABLE_IS_OK = "TABLE IS AVAILABLE";


  private String tablePath;

  private CatalogManager catalogManager;

  private Integer maxFindSnapshotNum;

  private Integer maxRollbackSnapNum;

  public AnalyzeCall(
      String tablePath,
      CatalogManager catalogManager,
      Integer maxFindSnapshotNum,
      Integer maxRollbackSnapNum) {
    this.tablePath = tablePath;
    this.catalogManager = catalogManager;
    this.maxFindSnapshotNum = maxFindSnapshotNum;
    this.maxRollbackSnapNum = maxRollbackSnapNum;
  }

  @Override
  public String call(Context context) throws FullTableNameException {
    TableIdentifier identifier = fullTableName(context, tablePath);

    ArcticCatalog arcticCatalog = catalogManager.getArcticCatalog(identifier.getCatalog());
    TableAvailableAnalyzer availableAnalyzer = new TableAvailableAnalyzer(arcticCatalog, identifier,
        maxFindSnapshotNum, maxRollbackSnapNum);
    TableAvailableResult availableResult = availableAnalyzer.check();
    context.setTableAvailableResult(availableResult);
    return format(availableResult);
  }

  /**
   * Format is like:
   *
   * TABLE_NAME:
   *   catalog.db.table
   * FILE_LOSE:
   *   hdfs://xxxx/xxxx/xxx
   * YOU CAN:
   *     FIND_BACK
   *     SYNC_METADATA
   *     ROLLBACK:
   *       597568753507019307
   *       512339827482937422
   */
  private String format(TableAvailableResult availableResult) {
    LikeYmlFormat root = LikeYmlFormat.blank();
    root.child(TABLE_NAME).child(availableResult.getIdentifier().getTableName());
    if (availableResult.isOk()) {
      root.child(TABLE_IS_OK);
      return root.toString();
    }

    DamageType damageType = availableResult.getDamageType();
    LikeYmlFormat damageTypeFormat = root.child(damageType.name());
    for (String path: availableResult.lostFiles()) {
      damageTypeFormat.child(path);
    }

    LikeYmlFormat youCanFormat = root.child(YOU_CAN);
    List<RepairWay> repairWays = availableResult.youCan();
    if (repairWays != null) {
      for (RepairWay repairWay: repairWays) {
        LikeYmlFormat wayFormat = youCanFormat.child(repairWay.name());
        if (repairWay == RepairWay.ROLLBACK) {
          for (Snapshot snapshot: availableResult.getRollbackList()) {
            wayFormat.child(String.valueOf(snapshot.snapshotId()));
          }
        }
      }
    }
    return root.print();
  }
}
