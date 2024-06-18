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

package org.apache.amoro.catalog;

import org.apache.amoro.api.TableMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.io.TableTrashManagers;
import org.apache.amoro.properties.MetaTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedCatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.hadoop.HadoopTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * TODO: this class will be removed when we support using restCatalog as base store for
 * InternalCatalog
 */
@Deprecated
public class MixedTables {

  private static final Logger LOG = LoggerFactory.getLogger(MixedTables.class);

  protected Tables tables;
  protected TableMetaStore tableMetaStore;
  protected Map<String, String> catalogProperties;

  public MixedTables(Map<String, String> catalogProperties, TableMetaStore metaStore) {
    this.tableMetaStore = metaStore;
    this.catalogProperties = catalogProperties;
    this.tables = new HadoopTables(tableMetaStore.getConfiguration());
  }

  public Table loadHadoopTableByLocation(String location) {
    return tableMetaStore.doAs(() -> tables.load(location));
  }

  public MixedTable loadTableByMeta(TableMeta tableMeta) {
    if (tableMeta.getKeySpec() != null
        && tableMeta.getKeySpec().getFields() != null
        && tableMeta.getKeySpec().getFields().size() > 0) {
      return loadKeyedTable(tableMeta);
    } else {
      return loadUnKeyedTable(tableMeta);
    }
  }

  protected KeyedTable loadKeyedTable(TableMeta tableMeta) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String changeLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_CHANGE);

    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    Table baseIcebergTable = tableMetaStore.doAs(() -> tables.load(baseLocation));
    BaseTable baseTable =
        new BasicKeyedTable.BaseInternalTable(
            tableIdentifier,
            MixedCatalogUtil.useMixedTableOperations(
                baseIcebergTable, baseLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);

    Table changeIcebergTable = tableMetaStore.doAs(() -> tables.load(changeLocation));
    ChangeTable changeTable =
        new BasicKeyedTable.ChangeInternalTable(
            tableIdentifier,
            MixedCatalogUtil.useMixedTableOperations(
                changeIcebergTable, changeLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);
    PrimaryKeySpec keySpec = buildPrimaryKeySpec(baseTable.schema(), tableMeta);
    return new BasicKeyedTable(tableLocation, keySpec, baseTable, changeTable);
  }

  protected String checkLocation(TableMeta meta, String locationKey) {
    String location = meta.getLocations().get(locationKey);
    Preconditions.checkArgument(StringUtils.isNotBlank(location), "table location can't found");
    return location;
  }

  protected PrimaryKeySpec buildPrimaryKeySpec(Schema schema, TableMeta tableMeta) {
    PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(schema);
    if (tableMeta.getKeySpec() != null
        && tableMeta.getKeySpec().getFields() != null
        && !tableMeta.getKeySpec().getFields().isEmpty()) {
      for (String field : tableMeta.getKeySpec().getFields()) {
        builder.addColumn(field);
      }
    }
    return builder.build();
  }

  protected UnkeyedTable loadUnKeyedTable(TableMeta tableMeta) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    Table table = tableMetaStore.doAs(() -> tables.load(baseLocation));

    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    return new BasicUnkeyedTable(
        tableIdentifier,
        MixedCatalogUtil.useMixedTableOperations(
            table, baseLocation, fileIO, tableMetaStore.getConfiguration()),
        fileIO,
        catalogProperties);
  }

  public MixedTable createTableByMeta(
      TableMeta tableMeta,
      Schema schema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    if (primaryKeySpec.primaryKeyExisted()) {
      return createKeyedTable(tableMeta, schema, primaryKeySpec, partitionSpec);
    } else {
      return createUnKeyedTable(tableMeta, schema, primaryKeySpec, partitionSpec);
    }
  }

  protected KeyedTable createKeyedTable(
      TableMeta tableMeta,
      Schema schema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String changeLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_CHANGE);

    fillTableProperties(tableMeta);
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    Table baseIcebergTable =
        tableMetaStore.doAs(
            () -> {
              try {
                return tables.create(
                    schema, partitionSpec, tableMeta.getProperties(), baseLocation);
              } catch (Exception e) {
                throw new IllegalStateException("create base table failed", e);
              }
            });
    BaseTable baseTable =
        new BasicKeyedTable.BaseInternalTable(
            tableIdentifier,
            MixedCatalogUtil.useMixedTableOperations(
                baseIcebergTable, baseLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);

    Table changeIcebergTable =
        tableMetaStore.doAs(
            () -> {
              try {
                return tables.create(
                    schema, partitionSpec, tableMeta.getProperties(), changeLocation);
              } catch (Exception e) {
                throw new IllegalStateException("create change table failed", e);
              }
            });
    ChangeTable changeTable =
        new BasicKeyedTable.ChangeInternalTable(
            tableIdentifier,
            MixedCatalogUtil.useMixedTableOperations(
                changeIcebergTable, changeLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);
    return new BasicKeyedTable(tableLocation, primaryKeySpec, baseTable, changeTable);
  }

  protected void fillTableProperties(TableMeta tableMeta) {
    tableMeta.putToProperties(
        TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
    tableMeta.putToProperties(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    tableMeta.putToProperties(
        org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
    tableMeta.putToProperties(
        "flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
  }

  protected UnkeyedTable createUnKeyedTable(
      TableMeta tableMeta,
      Schema schema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);

    fillTableProperties(tableMeta);
    Table table =
        tableMetaStore.doAs(
            () -> {
              try {
                return tables.create(
                    schema, partitionSpec, tableMeta.getProperties(), baseLocation);
              } catch (Exception e) {
                throw new IllegalStateException("create table failed", e);
              }
            });
    AuthenticatedFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    return new BasicUnkeyedTable(
        tableIdentifier,
        MixedCatalogUtil.useMixedTableOperations(
            table, baseLocation, fileIO, tableMetaStore.getConfiguration()),
        fileIO,
        catalogProperties);
  }

  public void dropTableByMeta(TableMeta tableMeta, boolean purge) {
    try {
      AuthenticatedFileIO fileIO = AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);
      Map<String, String> tableProperties = Maps.newHashMap();
      try {
        MixedTable mixedTable = loadTableByMeta(tableMeta);
        tableProperties.putAll(mixedTable.properties());
      } catch (Exception loadException) {
        LOG.warn("load table failed when dropping table", loadException);
      }

      // If purge is true, all manifest/data files must be located under the table directory.
      if (!purge) {
        String baseLocation = tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_BASE);
        String changeLocation =
            tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_CHANGE);
        try {
          if (StringUtils.isNotBlank(baseLocation)) {
            dropInternalTable(tableMetaStore, baseLocation, false);
          }
          if (StringUtils.isNotBlank(changeLocation)) {
            dropInternalTable(tableMetaStore, changeLocation, false);
          }
        } catch (Exception e) {
          LOG.warn("drop base/change iceberg table fail ", e);
        }
      } else {
        String tableLocation = tableMeta.getLocations().get(MetaTableProperties.LOCATION_KEY_TABLE);
        if (fileIO.exists(tableLocation)) {
          LOG.info("try to delete table directory location is {}", tableLocation);
          fileIO.asPrefixFileIO().deletePrefix(tableLocation);
        }
      }

      // delete custom trash location
      String customTrashLocation =
          tableProperties.get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
      if (customTrashLocation != null) {
        TableIdentifier tableId = TableIdentifier.of(tableMeta.getTableIdentifier());
        String trashParentLocation =
            TableTrashManagers.getTrashParentLocation(tableId, customTrashLocation);
        if (fileIO.exists(trashParentLocation)) {
          fileIO.asPrefixFileIO().deletePrefix(trashParentLocation);
        }
      }
    } catch (Exception e) {
      LOG.warn("drop table directory fail ", e);
    }
  }

  private void dropInternalTable(
      TableMetaStore tableMetaStore, String internalTableLocation, boolean purge) {
    final HadoopTables internalTables = new HadoopTables(tableMetaStore.getConfiguration());
    tableMetaStore.doAs(
        () -> {
          internalTables.dropTable(internalTableLocation, purge);
          return null;
        });
  }

  protected TableMetaStore getTableMetaStore() {
    return tableMetaStore;
  }
}
