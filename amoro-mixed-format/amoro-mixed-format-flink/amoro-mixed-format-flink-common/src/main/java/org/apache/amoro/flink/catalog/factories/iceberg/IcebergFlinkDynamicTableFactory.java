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

package org.apache.amoro.flink.catalog.factories.iceberg;

import org.apache.amoro.table.TableMetaStore;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.catalog.CatalogTable;
import org.apache.flink.table.catalog.ObjectIdentifier;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.utils.TableSchemaUtils;
import org.apache.iceberg.flink.FlinkCatalog;
import org.apache.iceberg.flink.FlinkDynamicTableFactory;
import org.apache.iceberg.flink.TableLoader;

import java.util.Map;

public class IcebergFlinkDynamicTableFactory extends FlinkDynamicTableFactory {

  private TableMetaStore tableMetaStore = null;

  public IcebergFlinkDynamicTableFactory(FlinkCatalog catalog) {
    super(catalog);
  }

  public IcebergFlinkDynamicTableFactory(FlinkCatalog catalog, TableMetaStore tableMetaStore) {
    super(catalog);
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public DynamicTableSource createDynamicTableSource(Context context) {
    ObjectIdentifier objectIdentifier = context.getObjectIdentifier();
    CatalogTable catalogTable = context.getCatalogTable();
    Map<String, String> tableProps = catalogTable.getOptions();
    TableSchema tableSchema = TableSchemaUtils.getPhysicalSchema(catalogTable.getSchema());

    TableLoader tableLoader;
    if (catalog != null) {
      tableLoader = createTableLoader(catalog, objectIdentifier.toObjectPath());
    } else {
      tableLoader =
          createTableLoader(
              catalogTable,
              tableProps,
              objectIdentifier.getDatabaseName(),
              objectIdentifier.getObjectName());
    }

    return new IcebergFlinkTableSource(
        tableLoader, tableSchema, tableProps, context.getConfiguration(), tableMetaStore);
  }

  @Override
  public DynamicTableSink createDynamicTableSink(Context context) {
    ObjectIdentifier objectIdentifier = context.getObjectIdentifier();
    CatalogTable catalogTable = context.getCatalogTable();
    Map<String, String> writeProps = catalogTable.getOptions();
    TableSchema tableSchema = TableSchemaUtils.getPhysicalSchema(catalogTable.getSchema());

    TableLoader tableLoader;
    if (catalog != null) {
      tableLoader = createTableLoader(catalog, objectIdentifier.toObjectPath());
    } else {
      tableLoader =
          createTableLoader(
              catalogTable,
              writeProps,
              objectIdentifier.getDatabaseName(),
              objectIdentifier.getObjectName());
    }

    return new IcebergFlinkTableSink(
        tableLoader, tableSchema, context.getConfiguration(), writeProps, tableMetaStore);
  }
}
