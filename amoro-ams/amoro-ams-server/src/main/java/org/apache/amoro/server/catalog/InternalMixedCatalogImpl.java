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

import static org.apache.amoro.server.table.internal.InternalTableConstants.CHANGE_STORE_TABLE_NAME_SUFFIX;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.mixed.InternalMixedIcebergCatalog;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.table.TableMetadata;
import org.apache.amoro.server.table.internal.InternalMixedIcebergCreator;
import org.apache.amoro.server.table.internal.InternalMixedIcebergHandler;
import org.apache.amoro.server.table.internal.InternalTableCreator;
import org.apache.amoro.server.table.internal.InternalTableHandler;
import org.apache.amoro.server.utils.InternalTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;

public class InternalMixedCatalogImpl extends InternalIcebergCatalogImpl {

  protected InternalMixedCatalogImpl(CatalogMeta metadata, Configurations serverConfiguration) {
    super(metadata, serverConfiguration);
  }

  @Override
  protected InternalTableCreator newTableCreator(
      String database, String tableName, CreateTableRequest request) {
    return new InternalMixedIcebergCreator(getMetadata(), database, tableName, request);
  }

  @Override
  public <O> InternalTableHandler<O> newTableHandler(String database, String tableStoreName) {
    String tableName = tableName(tableStoreName);
    boolean isChangeStore = isChangeStoreName(tableStoreName);
    TableMetadata metadata = loadTableMetadata(database, tableName);
    Preconditions.checkState(
        metadata.getFormat() == format(),
        "the catalog only support to handle %s table",
        format().name());
    //noinspection unchecked
    return (InternalTableHandler<O>) newTableStoreHandler(metadata, isChangeStore);
  }

  private InternalTableHandler<TableOperations> newTableStoreHandler(
      TableMetadata metadata, boolean isChangeStore) {
    return new InternalMixedIcebergHandler(getMetadata(), metadata, isChangeStore);
  }

  private String tableName(String tableStoreName) {
    if (isChangeStoreName(tableStoreName)) {
      return tableStoreName.substring(
          0, tableStoreName.length() - CHANGE_STORE_TABLE_NAME_SUFFIX.length());
    }
    return tableStoreName;
  }

  private boolean isChangeStoreName(String tableName) {
    String separator = InternalMixedIcebergCatalog.CHANGE_STORE_SEPARATOR;
    if (!tableName.contains(separator)) {
      return false;
    }
    Preconditions.checkArgument(
        tableName.indexOf(separator) == tableName.lastIndexOf(separator)
            && tableName.endsWith(CHANGE_STORE_TABLE_NAME_SUFFIX),
        "illegal table name: %s, %s is not allowed in table name.",
        tableName,
        separator);

    return true;
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    Preconditions.checkArgument(
        !isChangeStoreName(tableName), "table name is invalid for load table");
    TableMetadata tableMetadata =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableMetaByName(getMetadata().getCatalogName(), database, tableName));
    if (tableMetadata == null) {
      return null;
    }
    Preconditions.checkArgument(
        TableFormat.MIXED_ICEBERG == tableMetadata.getFormat(),
        "Table: %s.%s.%s is not a mixed-iceberg table",
        name(),
        database,
        tableName);

    org.apache.amoro.table.TableIdentifier tableIdentifier =
        org.apache.amoro.table.TableIdentifier.of(name(), database, tableName);
    AuthenticatedFileIO fileIO = InternalTableUtil.newIcebergFileIo(getMetadata());
    MixedTable mixedIcebergTable;

    BaseTable baseTable = loadTableStore(tableMetadata, false);
    if (InternalTableUtil.isKeyedMixedTable(tableMetadata)) {
      BaseTable changeTable = loadTableStore(tableMetadata, true);

      PrimaryKeySpec.Builder keySpecBuilder = PrimaryKeySpec.builderFor(baseTable.schema());
      tableMetadata.buildTableMeta().getKeySpec().getFields().forEach(keySpecBuilder::addColumn);
      PrimaryKeySpec keySpec = keySpecBuilder.build();

      mixedIcebergTable =
          new BasicKeyedTable(
              tableMetadata.getTableLocation(),
              keySpec,
              new BasicKeyedTable.BaseInternalTable(
                  tableIdentifier, baseTable, fileIO, getMetadata().getCatalogProperties()),
              new BasicKeyedTable.ChangeInternalTable(
                  tableIdentifier, changeTable, fileIO, getMetadata().getCatalogProperties()));
    } else {

      mixedIcebergTable =
          new BasicUnkeyedTable(
              tableIdentifier, baseTable, fileIO, getMetadata().getCatalogProperties());
    }

    return new org.apache.amoro.formats.mixed.MixedTable(
        mixedIcebergTable, TableFormat.MIXED_ICEBERG);
  }

  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  protected BaseTable loadTableStore(TableMetadata tableMetadata, boolean isChangeStore) {
    TableOperations ops = newTableStoreHandler(tableMetadata, isChangeStore).newTableOperator();
    return new BaseTable(
        ops,
        TableIdentifier.of(
                tableMetadata.getTableIdentifier().getDatabase(),
                tableMetadata.getTableIdentifier().getTableName())
            .toString());
  }
}
