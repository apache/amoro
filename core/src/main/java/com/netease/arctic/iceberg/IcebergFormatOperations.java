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

package com.netease.arctic.iceberg;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableFormatOperations;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;

import java.util.Map;

public class IcebergFormatOperations implements TableFormatOperations {

  protected final Catalog catalog;
  protected final Map<String, String> catalogProperties;
  protected final TableMetaStore tableMetaStore;


  public IcebergFormatOperations(Catalog catalog, Map<String, String> catalogProperties, TableMetaStore tableMetaStore) {
    this.catalog = catalog;
    this.catalogProperties = catalogProperties;
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public TableBuilder newTableBuilder(Schema schema, TableIdentifier identifier) {
    return new IcebergTableBuilder(
        tableMetaStore, catalog, catalogProperties, schema, identifier);
  }

  @Override
  public ArcticTable loadTable(TableIdentifier identifier) {
    Table table = catalog.loadTable(
        org.apache.iceberg.catalog.TableIdentifier.of(
            identifier.getDatabase(), identifier.getTableName()
        )
    );
    ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(tableMetaStore, table.io());
    return new BasicIcebergTable(identifier, table, io, catalogProperties);
  }

  @Override
  public boolean dropTable(TableIdentifier identifier, boolean purge) {
    return catalog.dropTable(org.apache.iceberg.catalog.TableIdentifier.of(
        identifier.getDatabase(), identifier.getTableName()
    ), purge);
  }
}
