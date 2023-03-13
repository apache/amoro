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
import com.netease.arctic.ams.server.repair.RepairException;
import com.netease.arctic.ams.server.repair.RepairWay;
import com.netease.arctic.ams.server.repair.TableAvailableResult;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogManager;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.TableTypeUtil;
import com.netease.arctic.io.TableTrashManager;
import com.netease.arctic.op.ArcticHadoopTableOperations;
import com.netease.arctic.op.ForcedDeleteFiles;
import com.netease.arctic.op.ForcedDeleteManifests;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManageSnapshots;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Table;

import java.util.List;

import static com.netease.arctic.ams.server.repair.DamageType.FILE_LOSE;
import static com.netease.arctic.ams.server.repair.DamageType.MANIFEST_LOST;
import static com.netease.arctic.ams.server.repair.RepairWay.SYNC_METADATA;

public class RepairCall implements CallCommand {

  private static final String REPAIR_SUCCESS = "Repair is success";

  /**
   * if null then use table name of context
   */
  private String tablePath;

  private RepairWay way;

  /**
   * snapshot id if way is ROLLBACK
   */
  private Long option;

  private CatalogManager catalogManager;

  public RepairCall(
      String tablePath,
      RepairWay way,
      Long option,
      CatalogManager catalogManager) {
    this.tablePath = tablePath;
    this.way = way;
    this.option = option;
    this.catalogManager = catalogManager;
  }

  @Override
  public String call(Context context) throws FullTableNameException {
    TableIdentifier identifier = fullTableName(context, tablePath);

    TableAvailableResult tableAvailableResult = context.getTableAvailableResult(identifier);
    if (tableAvailableResult == null) {
      throw new RepairException("Please do analysis first");
    }
    if (tableAvailableResult.isOk()) {
      throw new RepairException("Table is Ok. No need to repair");
    }

    if (!tableAvailableResult.youCan().contains(way)) {
      throw new RepairException(String.format("No %s this option", way));
    }

    DamageType damageType = tableAvailableResult.getDamageType();

    if (damageType == DamageType.TABLE_NOT_FOUND) {
      throw new RepairException("Table is not found, If you also have data " +
          "you can recreate the table through hive upgrade");
    }

    if (damageType == DamageType.METADATA_LOSE) {
      repairMetadataLose(identifier, tableAvailableResult);
      return REPAIR_SUCCESS;
    }

    ArcticCatalog arcticCatalog = catalogManager.getArcticCatalog(identifier.getCatalog());
    ArcticTable arcticTable = arcticCatalog.loadTable(identifier);

    switch (way) {
      case FIND_BACK: {
        TableTrashManager tableTrashManager = tableAvailableResult.getTableTrashManager();
        List<String> loseFiles = tableAvailableResult.lostFiles();
        for (String path: loseFiles) {
          if (!tableTrashManager.restoreFileFromTrash(path)) {
            throw new RepairException(String.format("Can not find back file %s", path));
          }
        }
        return REPAIR_SUCCESS;
      }
      case SYNC_METADATA: {
        switch (damageType) {
          case MANIFEST_LOST: {
            syncMetadataForManifestLose(tableAvailableResult.getArcticTable(), tableAvailableResult.getManifestFiles());
            return REPAIR_SUCCESS;
          }
          case FILE_LOSE: {
            if (TableTypeUtil.isHive(arcticTable)) {
              SupportHive supportHive = (SupportHive) arcticTable;
              if (supportHive.enableSyncHiveDataToArctic()) {
                arcticCatalog.refresh();
                return REPAIR_SUCCESS;
              }
            }
            syncMetadataForFileLose(tableAvailableResult.getArcticTable(), tableAvailableResult.getFiles());
            return REPAIR_SUCCESS;
          }
          default: {
            throw new RepairException(String.format("%s only for %s and %s", SYNC_METADATA, MANIFEST_LOST, FILE_LOSE));
          }
        }
      }
      case ROLLBACK: {
        rollback(tableAvailableResult.getArcticTable(), option);
        return REPAIR_SUCCESS;
      }
    }
    return REPAIR_SUCCESS;
  }

  private void repairMetadataLose(TableIdentifier identifier, TableAvailableResult tableAvailableResult) {
    ArcticCatalog arcticCatalog = catalogManager.getArcticCatalog(identifier.getCatalog());
    ArcticHadoopTableOperations arcticHadoopTableOperations = null;
    if (tableAvailableResult.getLocationKind() == ChangeLocationKind.INSTANT) {
      arcticHadoopTableOperations = arcticCatalog.getChangeTableOperations(identifier);
    } else {
      arcticHadoopTableOperations = arcticCatalog.getBaseTableOperations(identifier);
    }

    switch (way) {
      case FIND_BACK : {
        List<Path> metadataCandidateFiles =
            arcticHadoopTableOperations.getMetadataCandidateFiles(tableAvailableResult.getMetadataVersion());
        TableTrashManager tableTrashManager = tableAvailableResult.getTableTrashManager();
        for (Path path: metadataCandidateFiles) {
          if (tableTrashManager.moveFileToTrash(path.toString())) {
            return;
          }
        }
        throw new RepairException(String.format("Can not find back, metadata version is %s",
            tableAvailableResult.getMetadataVersion()));
      }
      case ROLLBACK_OR_DROP_TABLE: {
        arcticHadoopTableOperations.removeVersionHit();
        return;
      }
    }
  }

  public void syncMetadataForFileLose(Table table, List<ContentFile> loseFile) {
    ForcedDeleteFiles forcedDeleteFiles = ForcedDeleteFiles.of(table);
    for (ContentFile contentFile: loseFile) {
      if (contentFile instanceof DataFile) {
        forcedDeleteFiles.delete((DataFile) contentFile);
      } else if (contentFile instanceof DeleteFile) {
        forcedDeleteFiles.delete((DeleteFile) contentFile);
      }
    }
    forcedDeleteFiles.commit();
  }

  public void syncMetadataForManifestLose(Table table, List<ManifestFile> loseManifestFile) {
    ForcedDeleteManifests forcedDeleteManifests = ForcedDeleteManifests.of(table);
    for (ManifestFile manifestFile: loseManifestFile) {
      forcedDeleteManifests.deleteManifest(manifestFile);
    }
    forcedDeleteManifests.commit();
  }

  public void rollback(Table table, long snapshot) {
    ManageSnapshots manageSnapshots = table.manageSnapshots();
    manageSnapshots.rollbackTo(snapshot).commit();
  }
}
