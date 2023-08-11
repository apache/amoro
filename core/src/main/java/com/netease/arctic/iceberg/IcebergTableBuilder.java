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
import com.netease.arctic.table.BasicTableBuilder;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Map;

public class IcebergTableBuilder extends BasicTableBuilder<IcebergTableBuilder> {

  TableMetaStore tableMetaStore;
  Catalog icebergCatalog;
  Map<String, String> catalogProperties;

  public IcebergTableBuilder(
      TableMetaStore tableMetaStore,
      Catalog icebergCatalog,
      Map<String, String> catalogProperties,
      Schema schema,
      TableIdentifier identifier) {
    super(schema, TableFormat.ICEBERG, identifier);
    this.tableMetaStore = tableMetaStore;
    this.icebergCatalog = icebergCatalog;
    this.catalogProperties = catalogProperties;
  }

  @Override
  public ArcticTable create() {
    Table table = icebergCatalog.buildTable(
            org.apache.iceberg.catalog.TableIdentifier.of(
                identifier.getDatabase(), identifier.getTableName()
            ), schema
        ).withPartitionSpec(spec)
        .withProperties(properties)
        .withSortOrder(sortOrder)
        .create();

    FileIO io = table.io();
    ArcticFileIO arcticFileIO = ArcticFileIOs.buildAdaptIcebergFileIO(tableMetaStore, io);
    return new BasicIcebergTable(identifier, table, arcticFileIO, catalogProperties);
  }

  @Override
  public TableBuilder withPrimaryKeySpec(PrimaryKeySpec primaryKeySpec) {
    Preconditions.checkArgument(
        primaryKeySpec == null || !primaryKeySpec.primaryKeyExisted(),
        "can't create an iceberg table with primary key");
    return this;
  }

  @Override
  protected IcebergTableBuilder self() {
    return this;
  }
}
