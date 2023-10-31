/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.mixed;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.BasicKeyedTable;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CatalogUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.AlreadyExistsException;
import org.apache.iceberg.relocated.com.google.common.base.Joiner;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MixedTables {
  private static final Logger LOG = LoggerFactory.getLogger(MixedTables.class);

  protected TableMetaStore tableMetaStore;
  protected CatalogMeta catalogMeta;
  protected Catalog icebergCatalog;

  public MixedTables(TableMetaStore tableMetaStore, CatalogMeta catalogMeta, Catalog catalog) {
    this.tableMetaStore = tableMetaStore;
    this.catalogMeta = catalogMeta;
    this.icebergCatalog = catalog;
  }

  public boolean isBaseStore(Table table) {
    return TablePropertyUtil.isBaseStore(table.properties(), TableFormat.MIXED_ICEBERG);
  }

  public TableIdentifier parseChangeIdentifier(Table base) {
    return TablePropertyUtil.parseChangeIdentifier(base.properties());
  }

  protected TableIdentifier generateChangeStoreIdentifier(TableIdentifier baseIdentifier) {
    String separator = catalogMeta.getCatalogProperties().getOrDefault(
        CatalogMetaProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR,
        CatalogMetaProperties.MIXED_FORMAT_TABLE_STORE_SEPARATOR_DEFAULT
    );
    return TableIdentifier.of(
        baseIdentifier.namespace(), baseIdentifier.name() + separator + "change" + separator);
  }

  public ArcticTable loadTable(
      Table base, com.netease.arctic.table.TableIdentifier tableIdentifier) {
    ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());
    PrimaryKeySpec keySpec = PrimaryKeySpec.parse(base.schema(), base.properties());
    if (!keySpec.primaryKeyExisted()) {
      return new BasicUnkeyedTable(
          tableIdentifier,
          useArcticTableOperation(base, io),
          io,
          catalogMeta.getCatalogProperties());
    }
    Table changeIcebergTable = loadChangeStore(base);
    BaseTable baseStore =
        new BasicKeyedTable.BaseInternalTable(
            tableIdentifier,
            useArcticTableOperation(base, io),
            io,
            catalogMeta.getCatalogProperties());
    ChangeTable changeStore =
        new BasicKeyedTable.ChangeInternalTable(
            tableIdentifier,
            useArcticTableOperation(changeIcebergTable, io),
            io,
            catalogMeta.getCatalogProperties());
    return new BasicKeyedTable(keySpec, baseStore, changeStore);
  }

  public ArcticTable createTable(
      com.netease.arctic.table.TableIdentifier identifier,
      Schema schema,
      PartitionSpec partitionSpec,
      PrimaryKeySpec keySpec,
      Map<String, String> properties) {
    Map<String, String> tableStoreProperties = tableStoreProperties(keySpec, properties);
    TableIdentifier baseIdentifier = TableIdentifier.of(identifier.getDatabase(), identifier.getTableName());
    TableIdentifier changeIdentifier = generateChangeStoreIdentifier(baseIdentifier);
    if (keySpec.primaryKeyExisted() && tableStoreExists(changeIdentifier)) {
      throw new AlreadyExistsException("change store already exists");
    }

    Catalog.TableBuilder baseBuilder =
        icebergCatalog
            .buildTable(baseIdentifier, schema)
            .withPartitionSpec(partitionSpec)
            .withProperty(
                TableProperties.MIXED_FORMAT_TABLE_STORE,
                TableProperties.MIXED_FORMAT_TABLE_STORE_BASE)
            .withProperties(tableStoreProperties);
    if (keySpec.primaryKeyExisted()) {
      baseBuilder.withProperty(
          TableProperties.MIXED_FORMAT_CHANGE_STORE_IDENTIFIER, changeIdentifier.toString());
    }


    if (!keySpec.primaryKeyExisted()) {
      Table base = tableMetaStore.doAs(baseBuilder::create);
      ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());
      return new BasicUnkeyedTable(
          identifier, useArcticTableOperation(base, io), io, catalogMeta.getCatalogProperties());
    }

    Table base = tableMetaStore.doAs(baseBuilder::create);
    ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(this.tableMetaStore, base.io());

    Catalog.TableBuilder changeBuilder =
        icebergCatalog
            .buildTable(changeIdentifier, schema)
            .withProperties(tableStoreProperties)
            .withPartitionSpec(partitionSpec)
            .withProperty(
                TableProperties.MIXED_FORMAT_TABLE_STORE,
                TableProperties.MIXED_FORMAT_TABLE_STORE_CHANGE);
    Table change;
    try {
      change = tableMetaStore.doAs(changeBuilder::create);
    } catch (RuntimeException e) {
      LOG.warn("Create base store failed for reason: " + e.getMessage());
      tableMetaStore.doAs(() -> icebergCatalog.dropTable(baseIdentifier, true));
      throw e;
    }
    BaseTable baseStore =
        new BasicKeyedTable.BaseInternalTable(
            identifier, useArcticTableOperation(base, io), io, catalogMeta.getCatalogProperties());
    ChangeTable changeStore =
        new BasicKeyedTable.ChangeInternalTable(
            identifier,
            useArcticTableOperation(change, io),
            io,
            catalogMeta.getCatalogProperties());
    return new BasicKeyedTable(keySpec, baseStore, changeStore);
  }

  private Map<String, String> tableStoreProperties(
      PrimaryKeySpec keySpec, Map<String, String> tableProperties) {
    Map<String, String> properties = Maps.newHashMap(tableProperties);
    properties.put(TableProperties.TABLE_FORMAT, TableFormat.MIXED_ICEBERG.name());
    if (keySpec.primaryKeyExisted()) {
      String fields = Joiner.on(",").join(keySpec.fieldNames());
      properties.put(TableProperties.MIXED_FORMAT_PRIMARY_KEY_FIELDS, fields);
    }

    properties.put(TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
    properties.put(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    properties.put(org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
    properties.put("flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
    return properties;
  }

  private Table loadChangeStore(Table base) {
    TableIdentifier changeIdentifier = parseChangeIdentifier(base);
    return tableMetaStore.doAs(
        () -> icebergCatalog.loadTable(changeIdentifier));
  }

  private boolean tableStoreExists(TableIdentifier identifier) {
    return tableMetaStore.doAs(() -> icebergCatalog.tableExists(identifier));
  }

  private Table useArcticTableOperation(Table table, ArcticFileIO io) {
    return CatalogUtil.useArcticTableOperations(
        table, table.location(), io, tableMetaStore.getConfiguration());
  }
}
