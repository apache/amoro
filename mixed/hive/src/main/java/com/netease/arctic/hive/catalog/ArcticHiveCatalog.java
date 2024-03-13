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

package com.netease.arctic.hive.catalog;

import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_PRIMARY_KEYS;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_ROOT_LOCATION;
import static com.netease.arctic.table.PrimaryKeySpec.PRIMARY_KEY_COLUMN_JOIN_DELIMITER;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.catalog.BasicArcticCatalog;
import com.netease.arctic.catalog.MixedTables;
import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.HMSClientPool;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.utils.CompatibleHivePropertyUtil;
import com.netease.arctic.hive.utils.HiveSchemaUtil;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.ConvertStructUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.iceberg.IcebergSchemaUtil;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link com.netease.arctic.catalog.ArcticCatalog} to support Hive table as base
 * store.
 */
public class ArcticHiveCatalog extends BasicArcticCatalog {

  private static final Logger LOG = LoggerFactory.getLogger(ArcticHiveCatalog.class);

  private CachedHiveClientPool hiveClientPool;

  @Override
  public void initialize(String name, Map<String, String> properties, TableMetaStore metaStore) {
    super.initialize(name, properties, metaStore);
    this.hiveClientPool = ((MixedHiveTables) tables).getHiveClientPool();
  }

  protected MixedTables getTables() {
    return tables;
  }

  @Override
  protected MixedTables newMixedTables(
      Map<String, String> catalogProperties, TableMetaStore metaStore) {
    return new MixedHiveTables(catalogProperties, metaStore);
  }

  @Override
  public List<String> listDatabases() {
    try {
      return hiveClientPool.run(HMSClient::getAllDatabases);
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to list databases", e);
    }
  }

  @Override
  public void createDatabase(String databaseName) {
    try {
      hiveClientPool.run(
          client -> {
            Database database = new Database();
            database.setName(databaseName);
            client.createDatabase(database);
            return null;
          });
    } catch (AlreadyExistsException e) {
      throw new org.apache.iceberg.exceptions.AlreadyExistsException(
          e, "Database '%s' already exists!", databaseName);

    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to create database:" + databaseName, e);
    }
  }

  public static void putNotNullProperties(
      Map<String, String> properties, String key, String value) {
    if (value != null) {
      properties.put(key, value);
    }
  }

  /** HMS is case-insensitive for table name and database */
  @Override
  protected TableMeta getArcticTableMeta(TableIdentifier identifier) {
    org.apache.hadoop.hive.metastore.api.Table hiveTable = null;
    try {
      hiveTable = HiveTableUtil.loadHmsTable(this.hiveClientPool, identifier);
    } catch (RuntimeException e) {
      throw new IllegalStateException(String.format("failed load hive table %s.", identifier), e);
    }
    if (hiveTable == null) {
      throw new NoSuchTableException("load table failed %s.", identifier);
    }

    Map<String, String> hiveParameters = hiveTable.getParameters();

    String arcticRootLocation = hiveParameters.get(ARCTIC_TABLE_ROOT_LOCATION);
    if (arcticRootLocation == null) {
      // if hive location ends with /hive, then it's a mixed-hive table. we need to remove /hive to
      // get root location.
      // if hive location doesn't end with /hive, then it's a pure-hive table. we can use the
      // location as root location.
      String hiveRootLocation = hiveTable.getSd().getLocation();
      if (hiveRootLocation.endsWith("/hive")) {
        arcticRootLocation = hiveRootLocation.substring(0, hiveRootLocation.length() - 5);
      } else {
        arcticRootLocation = hiveRootLocation;
      }
    }

    // full path of base, change and root location
    String baseLocation = arcticRootLocation + "/base";
    String changeLocation = arcticRootLocation + "/change";
    // load base table for get arctic table properties
    Table baseIcebergTable = getTables().loadHadoopTableByLocation(baseLocation);
    if (baseIcebergTable == null) {
      throw new NoSuchTableException("load table failed %s, base table not found.", identifier);
    }
    Map<String, String> properties = baseIcebergTable.properties();
    // start to construct TableMeta
    TableMeta tableMeta = new TableMeta();
    tableMeta.setTableIdentifier(identifier.buildTableIdentifier());

    Map<String, String> locations = new HashMap<>();
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_TABLE, arcticRootLocation);
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_CHANGE, changeLocation);
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_BASE, baseLocation);
    // set table location
    tableMeta.setLocations(locations);

    // set table properties
    Map<String, String> newProperties = new HashMap<>(properties);
    tableMeta.setProperties(newProperties);

    // set table's primary key when needed
    if (hiveParameters != null) {
      String primaryKey = hiveParameters.get(ARCTIC_TABLE_PRIMARY_KEYS);
      // primary key info come from hive properties
      if (StringUtils.isNotBlank(primaryKey)) {
        com.netease.arctic.ams.api.PrimaryKeySpec keySpec =
            new com.netease.arctic.ams.api.PrimaryKeySpec();
        List<String> fields =
            Arrays.stream(primaryKey.split(PRIMARY_KEY_COLUMN_JOIN_DELIMITER))
                .collect(Collectors.toList());
        keySpec.setFields(fields);
        tableMeta.setKeySpec(keySpec);
      }
    }
    // set table format to mixed-hive format
    tableMeta.setFormat(TableFormat.MIXED_HIVE.toString());
    return tableMeta;
  }

  @Override
  public void dropDatabase(String databaseName) {
    try {
      hiveClientPool.run(
          client -> {
            client.dropDatabase(
                databaseName,
                false /* deleteData */,
                false /* ignoreUnknownDb */,
                false /* cascade */);
            return null;
          });
    } catch (NoSuchObjectException e) {
      // pass
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to drop database:" + databaseName, e);
    }
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema) {
    return new MixedHiveTableBuilder(identifier, schema);
  }

  public HMSClientPool getHMSClient() {
    return hiveClientPool;
  }

  /**
   *
   *
   * <ul>
   *   <li>1、call getTableObjectsByName to get all Table objects of database
   *   <li>2、filter hive tables whose properties don't have arctic table flag
   * </ul>
   *
   * we don't do cache here because we create/drop table through engine (like spark) connector, they
   * have another ArcticHiveCatalog instance。 we can't find a easy way to update cache.
   *
   * @param database
   * @return
   */
  @Override
  public List<TableIdentifier> listTables(String database) {
    final List<TableIdentifier> result = new ArrayList<>();
    try {
      hiveClientPool.run(
          client -> {
            List<String> tableNames = client.getAllTables(database);
            long start = System.currentTimeMillis();
            List<org.apache.hadoop.hive.metastore.api.Table> hiveTables =
                client.getTableObjectsByName(database, tableNames);
            LOG.info("call getTableObjectsByName cost {} ms", System.currentTimeMillis() - start);
            // filter hive tables whose properties don't have arctic table flag
            if (hiveTables != null && !hiveTables.isEmpty()) {
              List<TableIdentifier> loadResult =
                  hiveTables.stream()
                      .filter(
                          table ->
                              table.getParameters() != null
                                  && CompatibleHivePropertyUtil.propertyAsBoolean(
                                      table.getParameters(),
                                      HiveTableProperties.ARCTIC_TABLE_FLAG,
                                      false))
                      .map(table -> TableIdentifier.of(name(), database, table.getTableName()))
                      .collect(Collectors.toList());
              if (loadResult != null && !loadResult.isEmpty()) {
                result.addAll(loadResult);
              }
              LOG.debug(
                  "load {} tables from database {} of catalog {}",
                  loadResult == null ? 0 : loadResult.size(),
                  database,
                  name());
            } else {
              LOG.debug("load no tables from database {} of catalog {}", database, name());
            }
            return result;
          });
    } catch (NoSuchObjectException e) {
      // pass
    } catch (TException | InterruptedException e) {
      throw new RuntimeException("Failed to listTables of database :" + database, e);
    }
    return result;
  }

  @Override
  protected void doDropTable(TableMeta meta, boolean purge) {
    tables.dropTableByMeta(meta, purge);
  }

  class MixedHiveTableBuilder extends ArcticTableBuilder {

    public MixedHiveTableBuilder(TableIdentifier identifier, Schema schema) {
      super(identifier.toLowCaseIdentifier(), HiveSchemaUtil.changeFieldNameToLowercase(schema));
    }

    boolean allowExistedHiveTable = false;

    @Override
    public TableBuilder withPartitionSpec(PartitionSpec partitionSpec) {
      return super.withPartitionSpec(IcebergSchemaUtil.copyPartitionSpec(partitionSpec, schema));
    }

    @Override
    public TableBuilder withSortOrder(SortOrder sortOrder) {
      return super.withSortOrder(IcebergSchemaUtil.copySortOrderSpec(sortOrder, schema));
    }

    @Override
    public TableBuilder withPrimaryKeySpec(PrimaryKeySpec primaryKeySpec) {
      PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(schema);
      primaryKeySpec
          .fields()
          .forEach(
              primaryKeyField ->
                  builder.addColumn(primaryKeyField.fieldName().toLowerCase(Locale.ROOT)));
      return super.withPrimaryKeySpec(builder.build());
    }

    @Override
    public TableBuilder withProperty(String key, String value) {
      if (key.equals(HiveTableProperties.ALLOW_HIVE_TABLE_EXISTED) && value.equals("true")) {
        allowExistedHiveTable = true;
        super.withProperty(key, value);
      } else if (key.equals(TableProperties.TABLE_EVENT_TIME_FIELD)) {
        super.withProperty(key, value.toLowerCase(Locale.ROOT));
      } else {
        super.withProperty(key, value);
      }
      return this;
    }

    @Override
    public TableBuilder withProperties(Map<String, String> properties) {
      properties.forEach(this::withProperty);
      return this;
    }
    /** for we saved metadata in base table properties, we do nothing here */
    @Override
    protected void checkAmsTableMetadata() {
      // do nothing here for mixed-hive table
    }

    @Override
    protected void doCreateCheck() {

      super.doCreateCheck();
      try {
        org.apache.hadoop.hive.metastore.api.Table hiveTable =
            hiveClientPool.run(
                client -> client.getTable(identifier.getDatabase(), identifier.getTableName()));
        if (hiveTable != null) {
          // do some check for whether the table has been upgraded!!!
          if (CompatibleHivePropertyUtil.propertyAsBoolean(
              hiveTable.getParameters(), HiveTableProperties.ARCTIC_TABLE_FLAG, false)) {
            throw new IllegalArgumentException(
                String.format("Table %s has already been upgraded !", identifier));
          }
        }
        if (allowExistedHiveTable) {
          LOG.info("No need to check hive table exist");
        } else {
          if (hiveTable != null) {
            throw new IllegalArgumentException(
                "Table is already existed in hive meta store:" + identifier);
          }
        }
      } catch (org.apache.hadoop.hive.metastore.api.NoSuchObjectException noSuchObjectException) {
        // ignore this exception
      } catch (TException | InterruptedException e) {
        throw new RuntimeException("Failed to check table exist:" + identifier, e);
      }
      if (!partitionSpec.isUnpartitioned()) {
        for (PartitionField partitionField : partitionSpec.fields()) {
          if (!partitionField.transform().isIdentity()) {
            throw new IllegalArgumentException(
                "Unsupported partition transform:" + partitionField.transform().toString());
          }
          Preconditions.checkArgument(
              schema.columns().indexOf(schema.findField(partitionField.sourceId()))
                  >= (schema.columns().size() - partitionSpec.fields().size()),
              "Partition field should be at last of " + "schema");
        }
      }
    }

    @Override
    protected String getDatabaseLocation() {
      try {
        return hiveClientPool.run(
            client -> client.getDatabase(identifier.getDatabase()).getLocationUri());
      } catch (TException | InterruptedException e) {
        throw new RuntimeException(
            "Failed to get database location:" + identifier.getDatabase(), e);
      }
    }

    @Override
    protected void doRollbackCreateTable(TableMeta meta) {
      if (allowExistedHiveTable) {
        LOG.info(
            "No need to drop hive table {}.{}",
            meta.getTableIdentifier().getDatabase(),
            meta.getTableIdentifier().getTableName());
        tables.dropTableByMeta(meta, false);
      } else {
        super.doRollbackCreateTable(meta);
      }
    }

    @Override
    protected ConvertStructUtil.TableMetaBuilder createTableMataBuilder() {
      ConvertStructUtil.TableMetaBuilder builder = super.createTableMataBuilder();
      return builder.withFormat(TableFormat.MIXED_HIVE);
    }

    @Override
    protected void createTableMeta(TableMeta meta) {
      // for we save metadata in iceberg table properties, we do nothing here
    }
  }
}
