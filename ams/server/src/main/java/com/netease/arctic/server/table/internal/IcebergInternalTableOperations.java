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

package com.netease.arctic.server.table.internal;

import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_METADATA_LOCATION;
import static com.netease.arctic.server.table.internal.InternalTableConstants.PROPERTIES_PREV_METADATA_LOCATION;

import com.netease.arctic.server.persistence.PersistentBase;
import com.netease.arctic.server.persistence.mapper.TableMetaMapper;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.utils.InternalTableUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.LocationProviders;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IcebergInternalTableOperations extends PersistentBase implements TableOperations {

  private final ServerTableIdentifier identifier;

  private TableMetadata current;
  private final FileIO io;
  private com.netease.arctic.server.table.TableMetadata tableMetadata;

  public IcebergInternalTableOperations(
      ServerTableIdentifier identifier,
      com.netease.arctic.server.table.TableMetadata tableMetadata,
      FileIO io) {
    this.io = io;
    this.tableMetadata = tableMetadata;
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
    if (this.tableMetadata == null) {
      this.tableMetadata =
          getAs(
              TableMetaMapper.class, mapper -> mapper.selectTableMetaById(this.identifier.getId()));
    }
    if (this.tableMetadata == null) {
      return null;
    }
    String metadataFileLocation = tableMetadataLocation(this.tableMetadata);
    if (StringUtils.isBlank(metadataFileLocation)) {
      return null;
    }
    this.current = TableMetadataParser.read(io, metadataFileLocation);
    return this.current;
  }

  protected String tableMetadataLocation(com.netease.arctic.server.table.TableMetadata tableMeta) {
    return tableMeta.getProperties().get(PROPERTIES_METADATA_LOCATION);
  }

  @Override
  public void commit(TableMetadata base, TableMetadata metadata) {
    Preconditions.checkArgument(
        base != null, "Invalid table metadata for create transaction, base is null");

    Preconditions.checkArgument(
        metadata != null, "Invalid table metadata for create transaction, new metadata is null");
    if (base != current()) {
      throw new CommitFailedException("Cannot commit: stale table metadata");
    }

    String newMetadataFileLocation = InternalTableUtil.genNewMetadataFileLocation(base, metadata);

    try {
      commitTableInternal(tableMetadata, base, metadata, newMetadataFileLocation);
      com.netease.arctic.server.table.TableMetadata updatedMetadata = doCommit();
      checkCommitSuccess(updatedMetadata, newMetadataFileLocation);
    } catch (Exception e) {
      io.deleteFile(newMetadataFileLocation);
    } finally {
      this.tableMetadata = null;
    }
    refresh();
  }

  @Override
  public FileIO io() {
    return this.io;
  }

  @Override
  public String metadataFileLocation(String fileName) {
    return InternalTableUtil.genMetadataFileLocation(current(), fileName);
  }

  @Override
  public LocationProvider locationProvider() {
    return LocationProviders.locationsFor(current().location(), current().properties());
  }

  @Override
  public TableOperations temp(TableMetadata uncommittedMetadata) {
    return TableOperations.super.temp(uncommittedMetadata);
  }

  private com.netease.arctic.server.table.TableMetadata doCommit() {
    ServerTableIdentifier tableIdentifier = tableMetadata.getTableIdentifier();
    AtomicInteger effectRows = new AtomicInteger();
    AtomicReference<com.netease.arctic.server.table.TableMetadata> metadataRef =
        new AtomicReference<>();
    doAsTransaction(
        () -> {
          int effects =
              getAs(
                  TableMetaMapper.class,
                  mapper -> mapper.commitTableChange(tableIdentifier.getId(), tableMetadata));
          effectRows.set(effects);
        },
        () -> {
          com.netease.arctic.server.table.TableMetadata m =
              getAs(
                  TableMetaMapper.class,
                  mapper -> mapper.selectTableMetaById(tableIdentifier.getId()));
          metadataRef.set(m);
        });
    if (effectRows.get() == 0) {
      throw new CommitFailedException(
          "commit failed for version: " + tableMetadata.getMetaVersion() + " has been committed");
    }
    return metadataRef.get();
  }

  /** write iceberg table metadata and apply changes to AMS tableMetadata to commit. */
  private void commitTableInternal(
      com.netease.arctic.server.table.TableMetadata amsTableMetadata,
      TableMetadata base,
      TableMetadata newMetadata,
      String newMetadataFileLocation) {
    if (!Objects.equals(newMetadata.location(), base.location())) {
      throw new UnsupportedOperationException("SetLocation is not supported.");
    }
    OutputFile outputFile = io.newOutputFile(newMetadataFileLocation);
    TableMetadataParser.overwrite(newMetadata, outputFile);

    updateMetadataLocationProperties(
        amsTableMetadata, base.metadataFileLocation(), newMetadataFileLocation);
  }

  protected void updateMetadataLocationProperties(
      com.netease.arctic.server.table.TableMetadata amsTableMetadata,
      String oldMetadataFileLocation,
      String newMetadataFileLocation) {
    Map<String, String> properties = amsTableMetadata.getProperties();
    properties.put(PROPERTIES_PREV_METADATA_LOCATION, oldMetadataFileLocation);
    properties.put(PROPERTIES_METADATA_LOCATION, newMetadataFileLocation);
    amsTableMetadata.setProperties(properties);
  }

  private void checkCommitSuccess(
      com.netease.arctic.server.table.TableMetadata updatedTableMetadata,
      String metadataFileLocation) {
    String metaLocationInDatabase = tableMetadataLocation(updatedTableMetadata);
    if (!Objects.equals(metaLocationInDatabase, metadataFileLocation)) {
      throw new CommitFailedException(
          "commit conflict, some other commit happened during this commit. ");
    }
  }
}
