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

import com.netease.arctic.table.TableIdentifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Snapshot;

public class TableAvailableResult {

  private TableIdentifier identifier;

  private DamageType damageType;

  private Snapshot snapshot;

  private List<ManifestFile> manifestFiles;

  private List<ContentFile> files;

  private List<Snapshot> rollbackList;

  private TableAvailableResult(
      TableIdentifier tableIdentifier,
      DamageType damageType,
      Snapshot snapshot,
      List<ManifestFile> manifestFiles,
      List<ContentFile> files,
      List<Snapshot> rollbackList) {
    this.identifier = tableIdentifier;
    this.damageType = damageType;
    this.snapshot = snapshot;
    this.manifestFiles = manifestFiles;
    this.files = files;
    this.rollbackList = rollbackList;
  }

  public static TableAvailableResult available(TableIdentifier identifier) {
    return new TableAvailableResult(identifier, DamageType.OK, null, null, null, null);
  }

  public static TableAvailableResult metadataLose(TableIdentifier identifier) {
    return new TableAvailableResult(identifier, DamageType.METADATA_LOSE, null, null, null, null);
  }

  public static TableAvailableResult tableNotFound(TableIdentifier identifier) {
    return new TableAvailableResult(identifier, DamageType.TABLE_NOT_FOUND, null, null, null, null);
  }

  public static TableAvailableResult manifestListLose(TableIdentifier identifier, Snapshot snapshot) {
    return new TableAvailableResult(identifier, DamageType.MANIFEST_LIST_LOST, snapshot, null, null, null);
  }

  public static TableAvailableResult manifestLost(TableIdentifier identifier, List<ManifestFile> manifestFiles) {
    return new TableAvailableResult(identifier, DamageType.MANIFEST_LOST, null, manifestFiles, null, null);
  }

  public static TableAvailableResult filesLose(TableIdentifier identifier, List<ContentFile> files) {
    return new TableAvailableResult(identifier, DamageType.FILE_LOSE, null, null, files, null);
  }

  public TableIdentifier getIdentifier() {
    return identifier;
  }

  public void setIdentifier(TableIdentifier identifier) {
    this.identifier = identifier;
  }

  public DamageType getDamageType() {
    return damageType;
  }

  public void setDamageType(DamageType damageType) {
    this.damageType = damageType;
  }

  public Snapshot getSnapshot() {
    return snapshot;
  }

  public void setSnapshot(Snapshot snapshot) {
    this.snapshot = snapshot;
  }

  public List<ManifestFile> getManifestFiles() {
    return manifestFiles;
  }

  public void setManifestFiles(List<ManifestFile> manifestFiles) {
    this.manifestFiles = manifestFiles;
  }

  public List<ContentFile> getFiles() {
    return files;
  }

  public void setFiles(List<ContentFile> files) {
    this.files = files;
  }

  public List<Snapshot> getRollbackList() {
    return rollbackList;
  }

  public void setRollbackList(List<Snapshot> rollbackList) {
    this.rollbackList = rollbackList;
  }

  public boolean isOk() {
    return damageType == DamageType.OK;
  }

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
    //todo confirm rollback at the end
    return null;
  }
}
