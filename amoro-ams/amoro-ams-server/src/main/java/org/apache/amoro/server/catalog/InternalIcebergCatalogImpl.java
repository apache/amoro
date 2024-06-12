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

package org.apache.amoro.server.catalog;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.api.config.Configurations;
import org.apache.amoro.formats.iceberg.IcebergTable;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.RestCatalogService;
import org.apache.amoro.server.exception.ObjectNotExistsException;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.internal.InternalIcebergCreator;
import org.apache.amoro.server.table.internal.InternalIcebergHandler;
import org.apache.amoro.server.table.internal.InternalTableCreator;
import org.apache.amoro.server.table.internal.InternalTableHandler;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.rest.RESTCatalog;
import org.apache.iceberg.rest.requests.CreateTableRequest;

public class InternalIcebergCatalogImpl extends InternalCatalog {

  private static final String URI = "uri";

  final int httpPort;
  final String exposedHost;

  protected InternalIcebergCatalogImpl(CatalogMeta metadata, Configurations serverConfiguration) {
    super(metadata);
    this.httpPort = serverConfiguration.getInteger(AmoroManagementConf.HTTP_SERVER_PORT);
    this.exposedHost = serverConfiguration.getString(AmoroManagementConf.SERVER_EXPOSE_HOST);
  }

  @Override
  public CatalogMeta getMetadata() {
    CatalogMeta meta = super.getMetadata();
    if (!meta.getCatalogProperties().containsKey(URI)) {
      meta.putToCatalogProperties(URI, defaultRestURI());
    }
    meta.putToCatalogProperties(CatalogProperties.CATALOG_IMPL, RESTCatalog.class.getName());
    return meta.deepCopy();
  }

  @Override
  public void updateMetadata(CatalogMeta metadata) {
    String defaultUrl = defaultRestURI();
    String uri = metadata.getCatalogProperties().getOrDefault(URI, defaultUrl);
    if (defaultUrl.equals(uri)) {
      metadata.getCatalogProperties().remove(URI);
    }
    super.updateMetadata(metadata);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    InternalTableHandler<TableOperations> handler;
    try {
      handler = newTableHandler(database, tableName);
    } catch (ObjectNotExistsException e) {
      return null;
    }
    TableMetadata tableMetadata = handler.tableMetadata();
    TableOperations ops = handler.newTableOperator();

    BaseTable table =
        new BaseTable(
            ops,
            TableIdentifier.of(
                    tableMetadata.getTableIdentifier().getDatabase(),
                    tableMetadata.getTableIdentifier().getTableName())
                .toString());
    org.apache.amoro.table.TableIdentifier tableIdentifier =
        org.apache.amoro.table.TableIdentifier.of(name(), database, tableName);

    return IcebergTable.newIcebergTable(
        tableIdentifier,
        table,
        MixedCatalogUtil.buildMetaStore(getMetadata()),
        getMetadata().getCatalogProperties());
  }

  protected AuthenticatedFileIO fileIO(CatalogMeta catalogMeta) {
    return InternalTableUtil.newIcebergFileIo(catalogMeta);
  }

  private String defaultRestURI() {
    return "http://" + exposedHost + ":" + httpPort + RestCatalogService.ICEBERG_REST_API_PREFIX;
  }

  @Override
  public InternalTableCreator newTableCreator(
      String database, String tableName, TableFormat format, CreateTableRequest creatorArguments) {

    Preconditions.checkArgument(
        format == format(), "the catalog only support to create %s table", format().name());
    if (tableExists(database, tableName)) {
      throw new AlreadyExistsException(
          "Table " + name() + "." + database + "." + tableName + " already " + "exists.");
    }
    return newTableCreator(database, tableName, creatorArguments);
  }

  protected TableFormat format() {
    return TableFormat.ICEBERG;
  }

  protected InternalTableCreator newTableCreator(
      String database, String tableName, CreateTableRequest request) {
    return new InternalIcebergCreator(getMetadata(), database, tableName, request);
  }

  @Override
  public <O> InternalTableHandler<O> newTableHandler(String database, String tableName) {
    TableMetadata metadata = loadTableMetadata(database, tableName);
    Preconditions.checkState(
        metadata.getFormat() == format(),
        "the catalog only support to handle %s table",
        format().name());
    //noinspection unchecked
    return (InternalTableHandler<O>) new InternalIcebergHandler(getMetadata(), metadata);
  }
}
