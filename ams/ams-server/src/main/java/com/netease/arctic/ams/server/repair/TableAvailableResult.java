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

package com.netease.arctic.ams.server.repair;

import com.netease.arctic.io.TableTrashManager;
import com.netease.arctic.op.ArcticHadoopTableOperations;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.iceberg.relocated.com.google.common.base.Preconditions.checkNotNull;

public class TableAvailableResult {

  private TableIdentifier identifier;

  /**
   * It is iceberg format if locationKind is Base, Only for metadata lose
   */
  private LocationKind locationKind;

  /**
   * Only for metadata lose
   */
  private ArcticHadoopTableOperations tableOperations;

  private UnkeyedTable arcticTable;

  private DamageType damageType;

  private Integer metadataVersion;

  private Snapshot snapshot;

  private List<ManifestFile> manifestFiles;

  private List<ContentFile> files;

  private List<Snapshot> rollbackList;

  private TableTrashManager tableTrashManager;

  private Boolean canFindBack;

  private TableAvailableResult(
      TableIdentifier tableIdentifier,
      DamageType damageType,
      Integer metadataVersion,
      Snapshot snapshot,
      List<ManifestFile> manifestFiles,
      List<ContentFile> files,
      List<Snapshot> rollbackList,
      UnkeyedTable arcticTable,
      ArcticHadoopTableOperations tableOperations,
      LocationKind locationKind) {
    this.identifier = tableIdentifier;
    this.damageType = damageType;
    this.metadataVersion = metadataVersion;
    this.snapshot = snapshot;
    this.manifestFiles = manifestFiles;
    this.files = files;
    this.rollbackList = rollbackList;
    this.arcticTable = arcticTable;
    this.tableOperations = tableOperations;
    this.locationKind = locationKind;

    if (damageType == DamageType.METADATA_LOSE) {
      checkNotNull(locationKind);
      checkNotNull(tableOperations);
    } else if (damageType == DamageType.MANIFEST_LIST_LOST ||
        damageType == DamageType.MANIFEST_LOST ||
        damageType == DamageType.FILE_LOSE) {
      checkNotNull(arcticTable);
    }
  }

  public static TableAvailableResult available(TableIdentifier identifier) {
    return new TableAvailableResult(identifier, DamageType.OK, null,null,
        null, null, null, null, null, null);
  }

  public static TableAvailableResult tableNotFound(TableIdentifier identifier) {
    return new TableAvailableResult(identifier, DamageType.TABLE_NOT_FOUND, null,null,
        null, null, null, null, null, null);
  }

  public static TableAvailableResult metadataLose(TableIdentifier identifier, Integer metadataVersion,
      ArcticHadoopTableOperations tableOperations, LocationKind locationKind) {
    return new TableAvailableResult(identifier, DamageType.METADATA_LOSE, metadataVersion,null,
        null, null, null, null, tableOperations, locationKind);
  }

  public static TableAvailableResult manifestListLose(TableIdentifier identifier,
      Snapshot snapshot, UnkeyedTable arcticTable) {
    return new TableAvailableResult(identifier, DamageType.MANIFEST_LIST_LOST, null, snapshot,
        null, null, null, arcticTable, null, null);
  }

  public static TableAvailableResult manifestLost(TableIdentifier identifier,
      List<ManifestFile> manifestFiles, UnkeyedTable arcticTable) {
    return new TableAvailableResult(identifier, DamageType.MANIFEST_LOST, null,null,
        manifestFiles, null, null, arcticTable, null, null);
  }

  public static TableAvailableResult filesLose(TableIdentifier identifier, List<ContentFile> files,
      UnkeyedTable arcticTable) {
    return new TableAvailableResult(identifier, DamageType.FILE_LOSE, null,
        null, null, files, null, arcticTable, null, null);
  }


  public void setRollbackList(List<Snapshot> rollbackList) {
    this.rollbackList = rollbackList;
  }

  public void setTableTrashManager(TableTrashManager tableTrashManager) {
    this.tableTrashManager = tableTrashManager;
  }

  public void setLocationKind(LocationKind locationKind) {
    this.locationKind = locationKind;
  }

  public void setTableOperations(ArcticHadoopTableOperations tableOperations) {
    this.tableOperations = tableOperations;
  }

  public void setArcticTable(UnkeyedTable arcticTable) {
    this.arcticTable = arcticTable;
  }

  public TableIdentifier getIdentifier() {
    return identifier;
  }

  public DamageType getDamageType() {
    return damageType;
  }

  public UnkeyedTable getArcticTable() {
    return arcticTable;
  }

  public Snapshot getSnapshot() {
    return snapshot;
  }

  public List<ManifestFile> getManifestFiles() {
    return manifestFiles;
  }

  public List<ContentFile> getFiles() {
    return files;
  }

  public List<Snapshot> getRollbackList() {
    return rollbackList;
  }

  public LocationKind getLocationKind() {
    return locationKind;
  }

  public Integer getMetadataVersion() {
    return metadataVersion;
  }

  public TableTrashManager getTableTrashManager() {
    return tableTrashManager;
  }

  public boolean isOk() {
    return damageType == DamageType.OK;
  }

  /**
   * Not contain metadata file.
   * @return
   */
  public List<String> lostFiles() {
    if (snapshot != null) {
      return Arrays.asList(snapshot.manifestListLocation());
    }
    if (manifestFiles != null) {
      return manifestFiles.stream().map(ManifestFile::path).collect(Collectors.toList());
    }
    if (files != null) {
      return files.stream().map(ContentFile::path)
          .map(CharSequence::toString).collect(Collectors.toList());
    }
    return Collections.EMPTY_LIST;
  }

  public List<RepairWay> youCan() {
    if (isOk()) {
      return Collections.EMPTY_LIST;
    }
    if (canFindBack()) {
      return Arrays.asList(RepairWay.FIND_BACK);
    }
    List<RepairWay> ways = new ArrayList<>();
    switch (damageType) {
      case METADATA_LOSE: {
        ways.add(RepairWay.ROLLBACK_OR_DROP_TABLE);
        break;
      }
      case MANIFEST_LIST_LOST: {
        addRollback(ways);
        break;
      }
      case MANIFEST_LOST:
        ways.add(RepairWay.SYNC_METADATA);
        addRollback(ways);
        break;
      case FILE_LOSE: {
        ways.add(RepairWay.SYNC_METADATA);
        addRollback(ways);
        break;
      }
    }
    return ways;
  }

  private void addRollback(List<RepairWay> ways) {
    if (rollbackList != null && rollbackList.size() != 0) {
      ways.add(RepairWay.ROLLBACK);
    }
  }

  public boolean canFindBack() {

    if (tableTrashManager == null) {
      return false;
    }
    if (canFindBack != null) {
      return canFindBack;
    }

    //resolve metadata first
    if (damageType == DamageType.METADATA_LOSE) {
      List<Path> metadataCandidateFiles = tableOperations.getMetadataCandidateFiles(metadataVersion);
      for (Path path: metadataCandidateFiles) {
        if (tableTrashManager.fileExistInTrash(path.toString())) {
          return this.canFindBack = true;
        }
      }
      return this.canFindBack = false;
    }

    for (String path: lostFiles()) {
      if (!tableTrashManager.fileExistInTrash(path)) {
        return false;
      }
    }
    return this.canFindBack = true;
  }
}
