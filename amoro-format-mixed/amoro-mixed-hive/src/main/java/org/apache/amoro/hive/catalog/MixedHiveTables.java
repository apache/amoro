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

package org.apache.amoro.hive.catalog;

import org.apache.amoro.api.TableMeta;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.table.KeyedHiveTable;
import org.apache.amoro.hive.table.UnkeyedHiveTable;
import org.apache.amoro.hive.utils.HiveSchemaUtil;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.io.AuthenticatedHadoopFileIO;
import org.apache.amoro.io.TableTrashManagers;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.properties.MetaTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.Tables;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.mapping.MappingUtil;
import org.apache.iceberg.mapping.NameMappingParser;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MixedHiveTables {

  private static final Logger LOG = LoggerFactory.getLogger(MixedHiveTables.class);

  protected Tables tables;
  protected TableMetaStore tableMetaStore;
  protected Map<String, String> catalogProperties;
  private volatile CachedHiveClientPool hiveClientPool;

  public MixedHiveTables(Map<String, String> catalogProperties, TableMetaStore metaStore) {
    this.tableMetaStore = metaStore;
    this.catalogProperties = catalogProperties;
    this.tables = new HadoopTables(tableMetaStore.getConfiguration());
    this.hiveClientPool = new CachedHiveClientPool(getTableMetaStore(), catalogProperties);
  }

  protected TableMetaStore getTableMetaStore() {
    return tableMetaStore;
  }

  public CachedHiveClientPool getHiveClientPool() {
    return hiveClientPool;
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

  protected KeyedHiveTable loadKeyedTable(TableMeta tableMeta) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String changeLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_CHANGE);

    AuthenticatedHadoopFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    checkPrivilege(fileIO, baseLocation);
    Table baseIcebergTable = tableMetaStore.doAs(() -> tables.load(baseLocation));
    UnkeyedHiveTable baseTable =
        new KeyedHiveTable.HiveBaseInternalTable(
            tableIdentifier,
            MixedFormatCatalogUtil.useMixedTableOperations(
                baseIcebergTable, baseLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            tableLocation,
            hiveClientPool,
            catalogProperties,
            false);

    Table changeIcebergTable = tableMetaStore.doAs(() -> tables.load(changeLocation));
    ChangeTable changeTable =
        new KeyedHiveTable.HiveChangeInternalTable(
            tableIdentifier,
            MixedFormatCatalogUtil.useMixedTableOperations(
                changeIcebergTable, changeLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);
    return new KeyedHiveTable(
        tableMeta,
        tableLocation,
        buildPrimaryKeySpec(baseTable.schema(), tableMeta),
        hiveClientPool,
        baseTable,
        changeTable);
  }

  /**
   * we check the privilege by calling existing method, the method will throw the
   * UncheckedIOException Exception
   */
  private void checkPrivilege(AuthenticatedFileIO fileIO, String fileLocation) {
    if (!fileIO.exists(fileLocation)) {
      throw new NoSuchTableException("Table's base location %s does not exist ", fileLocation);
    }
  }

  protected UnkeyedHiveTable loadUnKeyedTable(TableMeta tableMeta) {
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    AuthenticatedHadoopFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    checkPrivilege(fileIO, baseLocation);
    Table table = tableMetaStore.doAs(() -> tables.load(baseLocation));
    return new UnkeyedHiveTable(
        tableIdentifier,
        MixedFormatCatalogUtil.useMixedTableOperations(
            table, baseLocation, fileIO, tableMetaStore.getConfiguration()),
        fileIO,
        tableLocation,
        hiveClientPool,
        catalogProperties);
  }

  protected KeyedTable createKeyedTable(
      TableMeta tableMeta,
      Schema schema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    boolean allowExistedHiveTable = allowExistedHiveTable(tableMeta);
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String changeLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_CHANGE);
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    fillTableProperties(tableMeta);
    String hiveLocation =
        tableMeta.getProperties().get(HiveTableProperties.BASE_HIVE_LOCATION_ROOT);
    // Default 1 day
    if (!tableMeta.properties.containsKey(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL)) {
      tableMeta.putToProperties(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "86400000");
    }

    AuthenticatedHadoopFileIO fileIO =
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
                Table createTable =
                    tables.create(schema, partitionSpec, tableMeta.getProperties(), baseLocation);
                createTable
                    .updateProperties()
                    .set(
                        org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING,
                        NameMappingParser.toJson(MappingUtil.create(createTable.schema())))
                    .commit();
                return createTable;
              } catch (Exception e) {
                throw new IllegalStateException("create base table failed", e);
              }
            });
    UnkeyedHiveTable baseTable =
        new KeyedHiveTable.HiveBaseInternalTable(
            tableIdentifier,
            MixedFormatCatalogUtil.useMixedTableOperations(
                baseIcebergTable, baseLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            tableLocation,
            hiveClientPool,
            catalogProperties,
            false);

    Table changeIcebergTable =
        tableMetaStore.doAs(
            () -> {
              try {
                Table createTable =
                    tables.create(schema, partitionSpec, tableMeta.getProperties(), changeLocation);
                createTable
                    .updateProperties()
                    .set(
                        org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING,
                        NameMappingParser.toJson(MappingUtil.create(createTable.schema())))
                    .commit();
                return createTable;
              } catch (Exception e) {
                throw new IllegalStateException("create change table failed", e);
              }
            });
    ChangeTable changeTable =
        new KeyedHiveTable.HiveChangeInternalTable(
            tableIdentifier,
            MixedFormatCatalogUtil.useMixedTableOperations(
                changeIcebergTable, changeLocation, fileIO, tableMetaStore.getConfiguration()),
            fileIO,
            catalogProperties);

    Map<String, String> metaProperties = tableMeta.getProperties();
    try {
      hiveClientPool.run(
          client -> {
            if (allowExistedHiveTable) {
              org.apache.hadoop.hive.metastore.api.Table hiveTable =
                  client.getTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
              Map<String, String> hiveParameters = hiveTable.getParameters();
              hiveParameters.putAll(constructProperties(primaryKeySpec, tableMeta));
              hiveTable.setParameters(hiveParameters);
              client.alterTable(
                  tableIdentifier.getDatabase(), tableIdentifier.getTableName(), hiveTable);
            } else {
              org.apache.hadoop.hive.metastore.api.Table hiveTable =
                  newHiveTable(tableMeta, schema, partitionSpec);
              hiveTable.setSd(
                  HiveTableUtil.storageDescriptor(
                      schema,
                      partitionSpec,
                      hiveLocation,
                      FileFormat.valueOf(
                          PropertyUtil.propertyAsString(
                                  metaProperties,
                                  TableProperties.DEFAULT_FILE_FORMAT,
                                  TableProperties.DEFAULT_FILE_FORMAT_DEFAULT)
                              .toUpperCase(Locale.ENGLISH))));
              setProToHive(hiveTable, primaryKeySpec, tableMeta);
              client.createTable(hiveTable);
            }
            return null;
          });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(
          "Failed to create hive table:" + tableMeta.getTableIdentifier(), e);
    }
    return new KeyedHiveTable(
        tableMeta, tableLocation, primaryKeySpec, hiveClientPool, baseTable, changeTable);
  }

  protected UnkeyedHiveTable createUnKeyedTable(
      TableMeta tableMeta,
      Schema schema,
      PrimaryKeySpec primaryKeySpec,
      PartitionSpec partitionSpec) {
    boolean allowExistedHiveTable = allowExistedHiveTable(tableMeta);
    TableIdentifier tableIdentifier = TableIdentifier.of(tableMeta.getTableIdentifier());
    String baseLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_BASE);
    String tableLocation = checkLocation(tableMeta, MetaTableProperties.LOCATION_KEY_TABLE);
    fillTableProperties(tableMeta);
    String hiveLocation =
        tableMeta.getProperties().get(HiveTableProperties.BASE_HIVE_LOCATION_ROOT);

    Table table =
        tableMetaStore.doAs(
            () -> {
              try {
                Table createTable =
                    tables.create(schema, partitionSpec, tableMeta.getProperties(), baseLocation);
                // Set name mapping using true schema
                createTable
                    .updateProperties()
                    .set(
                        org.apache.iceberg.TableProperties.DEFAULT_NAME_MAPPING,
                        NameMappingParser.toJson(MappingUtil.create(createTable.schema())))
                    .commit();
                return createTable;
              } catch (Exception e) {
                throw new IllegalStateException("create table failed", e);
              }
            });
    try {
      hiveClientPool.run(
          client -> {
            if (allowExistedHiveTable) {
              org.apache.hadoop.hive.metastore.api.Table hiveTable =
                  client.getTable(tableIdentifier.getDatabase(), tableIdentifier.getTableName());
              Map<String, String> hiveParameters = hiveTable.getParameters();
              hiveParameters.putAll(constructProperties(primaryKeySpec, tableMeta));
              hiveTable.setParameters(hiveParameters);
              client.alterTable(
                  tableIdentifier.getDatabase(), tableIdentifier.getTableName(), hiveTable);
            } else {
              org.apache.hadoop.hive.metastore.api.Table hiveTable =
                  newHiveTable(tableMeta, schema, partitionSpec);
              hiveTable.setSd(
                  HiveTableUtil.storageDescriptor(
                      schema,
                      partitionSpec,
                      hiveLocation,
                      FileFormat.valueOf(
                          PropertyUtil.propertyAsString(
                                  tableMeta.getProperties(),
                                  TableProperties.BASE_FILE_FORMAT,
                                  TableProperties.BASE_FILE_FORMAT_DEFAULT)
                              .toUpperCase(Locale.ENGLISH))));
              setProToHive(hiveTable, primaryKeySpec, tableMeta);
              client.createTable(hiveTable);
            }
            return null;
          });
    } catch (TException | InterruptedException e) {
      throw new RuntimeException(
          "Failed to create hive table:" + tableMeta.getTableIdentifier(), e);
    }

    AuthenticatedHadoopFileIO fileIO =
        AuthenticatedFileIOs.buildRecoverableHadoopFileIO(
            tableIdentifier,
            tableLocation,
            tableMeta.getProperties(),
            tableMetaStore,
            catalogProperties);
    return new UnkeyedHiveTable(
        tableIdentifier,
        MixedFormatCatalogUtil.useMixedTableOperations(
            table, baseLocation, fileIO, tableMetaStore.getConfiguration()),
        fileIO,
        tableLocation,
        hiveClientPool,
        catalogProperties);
  }

  public void dropInternalTableByMeta(TableMeta tableMeta, boolean purge) {
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

  public void dropTableByMeta(TableMeta tableMeta, boolean purge) {
    dropInternalTableByMeta(tableMeta, purge);
    if (!HiveTableUtil.checkExist(
        hiveClientPool, TableIdentifier.of(tableMeta.getTableIdentifier()))) {
      // If hive table does not exist, we will not try to drop hive table
      return;
    }
    // Drop hive table operation will only delete hive table metadata
    // Delete data files operation will use MixedHiveCatalog
    if (purge) {
      try {
        hiveClientPool.run(
            client -> {
              client.dropTable(
                  tableMeta.getTableIdentifier().getDatabase(),
                  tableMeta.getTableIdentifier().getTableName(),
                  false /* deleteData */,
                  true /* ignoreUnknownTab */);
              return null;
            });
      } catch (TException | InterruptedException e) {
        throw new RuntimeException("Failed to drop table:" + tableMeta.getTableIdentifier(), e);
      }
    } else {
      // If purge is not true, we will not drop the hive table and need to remove the mixed-hive
      // table
      // flag
      try {
        hiveClientPool.run(
            client -> {
              org.apache.hadoop.hive.metastore.api.Table hiveTable =
                  client.getTable(
                      tableMeta.getTableIdentifier().getDatabase(),
                      tableMeta.getTableIdentifier().getTableName());
              Map<String, String> hiveParameters = hiveTable.getParameters();
              hiveParameters.remove(HiveTableProperties.MIXED_TABLE_FLAG);
              client.alterTable(
                  tableMeta.getTableIdentifier().getDatabase(),
                  tableMeta.tableIdentifier.getTableName(),
                  hiveTable);
              return null;
            });
      } catch (TException | InterruptedException e) {
        throw new RuntimeException(
            "Failed to alter hive table while drop table meta:" + tableMeta.getTableIdentifier(),
            e);
      }
    }
  }

  protected void fillTableProperties(TableMeta meta) {
    meta.putToProperties(
        TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
    meta.putToProperties(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
    meta.putToProperties(
        org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
    meta.putToProperties("flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
    String tableLocation = checkLocation(meta, MetaTableProperties.LOCATION_KEY_TABLE);
    String hiveLocation = HiveTableUtil.hiveRootLocation(tableLocation);
    meta.putToProperties(HiveTableProperties.BASE_HIVE_LOCATION_ROOT, hiveLocation);
  }

  private Map<String, String> constructProperties(PrimaryKeySpec primaryKeySpec, TableMeta meta) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put(HiveTableProperties.MIXED_TABLE_FLAG, "true");
    parameters.put(HiveTableProperties.MIXED_TABLE_PRIMARY_KEYS, primaryKeySpec.description());
    parameters.put(
        HiveTableProperties.MIXED_TABLE_ROOT_LOCATION,
        meta.getLocations().get(MetaTableProperties.LOCATION_KEY_TABLE));
    return parameters;
  }

  private org.apache.hadoop.hive.metastore.api.Table newHiveTable(
      TableMeta meta, Schema schema, PartitionSpec partitionSpec) {
    final long currentTimeMillis = System.currentTimeMillis();

    org.apache.hadoop.hive.metastore.api.Table newTable =
        new org.apache.hadoop.hive.metastore.api.Table(
            meta.getTableIdentifier().getTableName(),
            meta.getTableIdentifier().getDatabase(),
            meta.getProperties()
                .getOrDefault(TableProperties.OWNER, System.getProperty("user.name")),
            (int) currentTimeMillis / 1000,
            (int) currentTimeMillis / 1000,
            Integer.MAX_VALUE,
            null,
            HiveSchemaUtil.hivePartitionFields(schema, partitionSpec),
            new HashMap<>(),
            null,
            null,
            TableType.EXTERNAL_TABLE.toString());

    newTable
        .getParameters()
        .put("EXTERNAL", "TRUE"); // Using the external table type also requires this
    return newTable;
  }

  private void setProToHive(
      org.apache.hadoop.hive.metastore.api.Table hiveTable,
      PrimaryKeySpec primaryKeySpec,
      TableMeta meta) {
    Map<String, String> hiveTableProperties = constructProperties(primaryKeySpec, meta);
    Map<String, String> parameters = Maps.newHashMap();
    parameters.putAll(hiveTable.getParameters());
    parameters.putAll(hiveTableProperties);
    hiveTable.setParameters(parameters);
  }

  private boolean allowExistedHiveTable(TableMeta tableMeta) {
    String allowStringValue =
        tableMeta.getProperties().remove(HiveTableProperties.ALLOW_HIVE_TABLE_EXISTED);
    return Boolean.parseBoolean(allowStringValue);
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
}
