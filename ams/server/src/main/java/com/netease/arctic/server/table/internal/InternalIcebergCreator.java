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
import static com.netease.arctic.server.utils.InternalTableUtil.genNewMetadataFileLocation;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.ams.api.ServerTableIdentifier;
import com.netease.arctic.server.table.TableMetadata;
import com.netease.arctic.server.utils.InternalTableUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.TableMetadataParser;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.util.LocationUtil;

/** Table creator for iceberg format */
public class InternalIcebergCreator implements InternalTableCreator {

  protected final ArcticFileIO io;
  protected final CreateTableRequest request;
  private final CatalogMeta catalogMeta;
  protected final String database;
  protected final String tableName;
  private boolean closed = false;

  private String metadataFileLocation;
  protected final org.apache.iceberg.TableMetadata icebergMetadata;

  public InternalIcebergCreator(
      CatalogMeta catalog, String database, String tableName, CreateTableRequest request) {
    this.io = InternalTableUtil.newIcebergFileIo(catalog);
    this.catalogMeta = catalog;
    this.database = database;
    this.tableName = tableName;
    this.request = request;

    String tableLocation = tableLocation();
    PartitionSpec spec = request.spec();
    SortOrder sortOrder = request.writeOrder();
    this.icebergMetadata =
        org.apache.iceberg.TableMetadata.newTableMetadata(
            request.schema(),
            spec != null ? spec : PartitionSpec.unpartitioned(),
            sortOrder != null ? sortOrder : SortOrder.unsorted(),
            tableLocation,
            request.properties());
  }

  @Override
  public TableMetadata create() {
    checkClosed();

    String icebergMetadataFileLocation = genNewMetadataFileLocation(null, icebergMetadata);
    TableMeta meta = new TableMeta();
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_TABLE, icebergMetadata.location());
    meta.putToLocations(MetaTableProperties.LOCATION_KEY_BASE, icebergMetadata.location());
    meta.setFormat(format().name());
    meta.putToProperties(PROPERTIES_METADATA_LOCATION, icebergMetadataFileLocation);

    ServerTableIdentifier serverTableIdentifier =
        ServerTableIdentifier.of(catalogMeta.getCatalogName(), database, tableName, format());
    meta.setTableIdentifier(serverTableIdentifier.getIdentifier());
    // write metadata file.
    OutputFile outputFile = io.newOutputFile(icebergMetadataFileLocation);
    this.metadataFileLocation = icebergMetadataFileLocation;
    TableMetadataParser.overwrite(icebergMetadata, outputFile);
    return new TableMetadata(serverTableIdentifier, meta, catalogMeta);
  }

  protected TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public void rollback() {
    checkClosed();
    if (StringUtils.isNotEmpty(this.metadataFileLocation)) {
      io.deleteFile(this.metadataFileLocation);
    }
  }

  @Override
  public void close() {
    io.close();
    closed = true;
  }

  private void checkClosed() {
    Preconditions.checkState(
        !closed,
        this.getClass().getSimpleName()
            + " for table ["
            + catalogMeta.getCatalogName()
            + "."
            + database
            + "."
            + tableName
            + "] is "
            + "closed");
  }

  private String tableLocation() {
    String location = this.request.location();
    if (StringUtils.isBlank(location)) {
      String warehouse =
          catalogMeta.getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE);
      Preconditions.checkState(
          StringUtils.isNotBlank(warehouse), "catalog warehouse is not configured");
      warehouse = LocationUtil.stripTrailingSlash(warehouse);
      location = warehouse + "/" + database + "/" + tableName;
    } else {
      location = LocationUtil.stripTrailingSlash(location);
    }
    return location;
  }
}
