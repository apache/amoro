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

package com.netease.arctic.server.catalog;

import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.persistence.PersistentTableMeta;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.utils.IcebergTableUtil;
import org.apache.iceberg.LocationProviders;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IcebergTableOperations extends PersistentBase implements TableOperations {

  private final ServerTableIdentifier identifier;

  private TableMetadata current;
  private final FileIO io;
  private PersistentTableMeta persistentTableMeta;


  public static IcebergTableOperations buildForLoad(
      PersistentTableMeta persistentTableMeta,
      FileIO io) {
    return new IcebergTableOperations(
        persistentTableMeta.getTableIdentifier(),
        persistentTableMeta,
        io);
  }

  public IcebergTableOperations(
      ServerTableIdentifier identifier,
      PersistentTableMeta persistentTableMeta,
      FileIO io) {
    this.io = io;
    this.persistentTableMeta = persistentTableMeta;
    this.identifier = identifier;
  }


  @Override
  public TableMetadata current() {
    if (this.current == null) {
      this.refresh();
    }
    return this.current;
  }

  @Override
  public TableMetadata refresh() {
    if (this.persistentTableMeta == null) {
      this.persistentTableMeta = getAs(TableMetaMapper.class,
          mapper -> mapper.selectTableMetaById(this.identifier.getId()));
    }
    if (this.persistentTableMeta == null) {
      return null;
    }
    this.current = IcebergTableUtil.loadIcebergTableMetadata(io, this.persistentTableMeta);
    return this.current;
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    Preconditions.checkArgument(base != null,
        "Invalid table metadata for create transaction, base is null");

    Preconditions.checkArgument(metadata != null,
        "Invalid table metadata for create transaction, new metadata is null");
    if (base != current()) {
      throw new CommitFailedException("Cannot commit: stale table metadata");
    }

    String newMetadataFileLocation = IcebergTableUtil.genNewMetadataFileLocation(base, metadata);

    try {
      IcebergTableUtil.commitTableInternal(
          persistentTableMeta, base, metadata, newMetadataFileLocation, io);
      PersistentTableMeta updatedMetadata = doCommit();
      IcebergTableUtil.checkCommitSuccess(updatedMetadata, newMetadataFileLocation);
    } catch (Exception e) {
      io.deleteFile(newMetadataFileLocation);
    } finally {
      this.persistentTableMeta = null;
    }
    refresh();
  }

  @Override
  public FileIO io() {
    return this.io;
  }

  @Override
  public String metadataFileLocation(String fileName) {
    return IcebergTableUtil.genMetadataFileLocation(current(), fileName);
  }


  @Override
  public LocationProvider locationProvider() {
    return LocationProviders.locationsFor(current().location(), current().properties());
  }

  @Override
  public TableOperations temp(TableMetadata uncommittedMetadata) {
    return TableOperations.super.temp(uncommittedMetadata);
  }

  private PersistentTableMeta doCommit() {
    ServerTableIdentifier tableIdentifier = persistentTableMeta.getTableIdentifier();
    AtomicInteger effectRows = new AtomicInteger();
    AtomicReference<PersistentTableMeta> metadataRef = new AtomicReference<>();
    doAsTransaction(
        () -> {
          int effects = getAs(TableMetaMapper.class,
              mapper -> mapper.commitTableChange(tableIdentifier.getId(), persistentTableMeta));
          effectRows.set(effects);
        },
        () -> {
          PersistentTableMeta m = getAs(TableMetaMapper.class,
              mapper -> mapper.selectTableMetaById(tableIdentifier.getId()));
          metadataRef.set(m);
        }
    );
    if (effectRows.get() == 0) {
      throw new CommitFailedException("commit failed for version: " +
          persistentTableMeta.getMetaVersion() + " has been committed");
    }
    return metadataRef.get();
  }
}
