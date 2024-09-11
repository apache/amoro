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

import static org.apache.amoro.properties.HiveTableProperties.MIXED_TABLE_PRIMARY_KEYS;
import static org.apache.amoro.properties.HiveTableProperties.MIXED_TABLE_ROOT_LOCATION;
import static org.apache.amoro.table.PrimaryKeySpec.PRIMARY_KEY_COLUMN_JOIN_DELIMITER;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_KAFKA;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_PULSAR;
import static org.apache.amoro.table.TableProperties.LOG_STORE_TYPE;

import org.apache.amoro.AmsClient;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.PooledAmsClient;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.hive.CachedHiveClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.utils.CompatibleHivePropertyUtil;
import org.apache.amoro.hive.utils.HiveSchemaUtil;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.op.CreateTableTransaction;
import org.apache.amoro.op.MixedHadoopTableOperations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.properties.MetaTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.blocker.BasicTableBlockerManager;
import org.apache.amoro.table.blocker.TableBlockerManager;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ConvertStructUtil;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.AlreadyExistsException;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
import org.apache.iceberg.IcebergSchemaUtil;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.Transactions;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Implementation of {@link MixedFormatCatalog} to support Hive table as base store. */
public class MixedHiveCatalog implements MixedFormatCatalog {

  private static final Logger LOG = LoggerFactory.getLogger(MixedHiveCatalog.class);

  protected AmsClient client;
  private CachedHiveClientPool hiveClientPool;
  protected String name;
  protected Map<String, String> catalogProperties;
  protected MixedHiveTables tables;
  protected transient TableMetaStore tableMetaStore;

  @Override
  public String name() {
    return name;
  }

  @Override
  public void initialize(String name, Map<String, String> properties, TableMetaStore metaStore) {
    if (properties.get(CatalogMetaProperties.AMS_URI) != null) {
      this.client = new PooledAmsClient(properties.get(CatalogMetaProperties.AMS_URI));
    }
    this.name = name;
    this.catalogProperties = properties;
    this.tableMetaStore = metaStore;
    this.tables = newMixedHiveTables(properties, metaStore);
    this.hiveClientPool = tables.getHiveClientPool();
  }

  protected MixedHiveTables getTables() {
    return tables;
  }

  protected MixedHiveTables newMixedHiveTables(
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

  @Override
  public TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier) {
    validate(tableIdentifier);
    return BasicTableBlockerManager.build(tableIdentifier, client);
  }

  @Override
  public Map<String, String> properties() {
    return catalogProperties;
  }

  public static void putNotNullProperties(
      Map<String, String> properties, String key, String value) {
    if (value != null) {
      properties.put(key, value);
    }
  }

  /** HMS is case-insensitive for table name and database */
  protected TableMeta getMixedTableMeta(TableIdentifier identifier) {
    org.apache.hadoop.hive.metastore.api.Table hiveTable = null;
    identifier = identifier.toLowCaseIdentifier();
    try {
      hiveTable = HiveTableUtil.loadHmsTable(this.hiveClientPool, identifier);
    } catch (RuntimeException e) {
      throw new IllegalStateException(String.format("failed load hive table %s.", identifier), e);
    }
    if (hiveTable == null) {
      throw new NoSuchTableException("load table failed %s.", identifier);
    }

    Map<String, String> hiveParameters = hiveTable.getParameters();

    String mixedTableRootLocation = hiveParameters.get(MIXED_TABLE_ROOT_LOCATION);
    if (mixedTableRootLocation == null) {
      // if hive location ends with /hive, then it's a mixed-hive table. we need to remove /hive to
      // get root location.
      // if hive location doesn't end with /hive, then it's a pure-hive table. we can use the
      // location as root location.
      String hiveRootLocation = hiveTable.getSd().getLocation();
      if (hiveRootLocation.endsWith("/hive")) {
        mixedTableRootLocation = hiveRootLocation.substring(0, hiveRootLocation.length() - 5);
      } else {
        mixedTableRootLocation = hiveRootLocation;
      }
    }

    // full path of base, change and root location
    String baseLocation = mixedTableRootLocation + "/base";
    String changeLocation = mixedTableRootLocation + "/change";
    // load base table for get table properties
    Table baseIcebergTable = getTables().loadHadoopTableByLocation(baseLocation);
    if (baseIcebergTable == null) {
      throw new NoSuchTableException("load table failed %s, base table not found.", identifier);
    }
    Map<String, String> properties = baseIcebergTable.properties();
    // start to construct TableMeta
    TableMeta tableMeta = new TableMeta();
    tableMeta.setTableIdentifier(identifier.buildTableIdentifier());

    Map<String, String> locations = new HashMap<>();
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_TABLE, mixedTableRootLocation);
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_CHANGE, changeLocation);
    putNotNullProperties(locations, MetaTableProperties.LOCATION_KEY_BASE, baseLocation);
    // set table location
    tableMeta.setLocations(locations);

    // set table properties
    Map<String, String> newProperties = new HashMap<>(properties);
    tableMeta.setProperties(newProperties);

    // set table's primary key when needed
    if (hiveParameters != null) {
      String primaryKey = hiveParameters.get(MIXED_TABLE_PRIMARY_KEYS);
      // primary key info come from hive properties
      if (StringUtils.isNotBlank(primaryKey)) {
        org.apache.amoro.api.PrimaryKeySpec keySpec = new org.apache.amoro.api.PrimaryKeySpec();
        List<String> fields =
            Arrays.stream(primaryKey.split(PRIMARY_KEY_COLUMN_JOIN_DELIMITER))
                .collect(Collectors.toList());
        keySpec.setFields(fields);
        tableMeta.setKeySpec(keySpec);
      }
    }
    // set table format to mixed-hive format
    tableMeta.setFormat(TableFormat.MIXED_HIVE.name());
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

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {
    throw new UnsupportedOperationException("unsupported rename mixed-hive table for now.");
  }

  public HMSClientPool getHMSClient() {
    return hiveClientPool;
  }

  /**
   *
   *
   * <ul>
   *   <li>1、call getTableObjectsByName to get all Table objects of database
   *   <li>2、filter hive tables whose properties don't have mixed-hive table flag
   * </ul>
   *
   * we don't do cache here because we create/drop table through engine (like spark) connector, they
   * have another MixedHiveCatalog instance。 we can't find a easy way to update cache.
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
            // filter hive tables whose properties don't have mixed-hive table flag
            if (hiveTables != null && !hiveTables.isEmpty()) {
              List<TableIdentifier> loadResult =
                  hiveTables.stream()
                      .filter(
                          table ->
                              table.getParameters() != null
                                  && CompatibleHivePropertyUtil.propertyAsBoolean(
                                      table.getParameters(),
                                      HiveTableProperties.MIXED_TABLE_FLAG,
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

  private void validate(TableIdentifier identifier) {
    if (StringUtils.isNotBlank(identifier.getCatalog())) {
      identifier.setCatalog(this.name());
    } else if (!this.name().equals(identifier.getCatalog())) {
      throw new IllegalArgumentException("catalog name miss match");
    }
  }

  @Override
  public MixedTable loadTable(TableIdentifier identifier) {
    validate(identifier);
    TableMeta meta = getMixedTableMeta(identifier);
    if (meta.getLocations() == null) {
      throw new IllegalStateException("load table failed, lack locations info");
    }
    return tables.loadTableByMeta(meta);
  }

  @Override
  public boolean dropTable(TableIdentifier identifier, boolean purge) {
    validate(identifier);
    TableMeta meta;
    try {
      meta = getMixedTableMeta(identifier);
    } catch (NoSuchTableException e) {
      return false;
    }

    doDropTable(meta, purge);
    return true;
  }

  protected void doDropTable(TableMeta meta, boolean purge) {
    tables.dropTableByMeta(meta, purge);
  }

  class MixedHiveTableBuilder implements TableBuilder {
    protected TableIdentifier identifier;
    protected Schema schema;
    protected PartitionSpec partitionSpec;
    protected SortOrder sortOrder;
    protected Map<String, String> properties = new HashMap<>();
    protected PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    protected String location;

    @Override
    public MixedTable create() {
      ConvertStructUtil.TableMetaBuilder builder = createTableMataBuilder();
      doCreateCheck();
      TableMeta meta = builder.build();
      MixedTable table = createTableByMeta(meta, schema, primaryKeySpec, partitionSpec);
      return table;
    }

    @Override
    public Transaction createTransaction() {
      AuthenticatedFileIO authenticatedFileIO =
          AuthenticatedFileIOs.buildHadoopFileIO(tableMetaStore);
      ConvertStructUtil.TableMetaBuilder builder = createTableMataBuilder();
      TableMeta meta = builder.build();
      String location = getTableLocationForCreate();
      TableOperations tableOperations =
          new MixedHadoopTableOperations(
              new Path(location), authenticatedFileIO, tableMetaStore.getConfiguration());
      TableMetadata tableMetadata =
          tableMetadata(schema, partitionSpec, sortOrder, properties, location);
      Transaction transaction =
          Transactions.createTableTransaction(
              identifier.getTableName(), tableOperations, tableMetadata);
      return new CreateTableTransaction(
          transaction,
          this::create,
          () -> {
            doRollbackCreateTable(meta);
            try {
              client.removeTable(identifier.buildTableIdentifier(), true);
            } catch (org.apache.amoro.shade.thrift.org.apache.thrift.TException e) {
              throw new RuntimeException(e);
            }
          });
    }

    protected MixedTable createTableByMeta(
        TableMeta tableMeta,
        Schema schema,
        PrimaryKeySpec primaryKeySpec,
        PartitionSpec partitionSpec) {
      return tables.createTableByMeta(tableMeta, schema, primaryKeySpec, partitionSpec);
    }

    protected void checkProperties() {
      Map<String, String> mergedProperties =
          MixedFormatCatalogUtil.mergeCatalogPropertiesToTable(properties, catalogProperties);
      boolean enableStream =
          CompatiblePropertyUtil.propertyAsBoolean(
              mergedProperties,
              TableProperties.ENABLE_LOG_STORE,
              TableProperties.ENABLE_LOG_STORE_DEFAULT);
      if (enableStream) {
        Preconditions.checkArgument(
            mergedProperties.containsKey(TableProperties.LOG_STORE_MESSAGE_TOPIC),
            "log-store.topic must not be null when log-store.enabled is true.");
        Preconditions.checkArgument(
            mergedProperties.containsKey(TableProperties.LOG_STORE_ADDRESS),
            "log-store.address must not be null when log-store.enabled is true.");
        String logStoreType = mergedProperties.get(LOG_STORE_TYPE);
        Preconditions.checkArgument(
            logStoreType == null
                || logStoreType.equals(LOG_STORE_STORAGE_TYPE_KAFKA)
                || logStoreType.equals(LOG_STORE_STORAGE_TYPE_PULSAR),
            String.format(
                "%s can not be set %s, valid values are: [%s, %s].",
                LOG_STORE_TYPE,
                logStoreType,
                LOG_STORE_STORAGE_TYPE_KAFKA,
                LOG_STORE_STORAGE_TYPE_PULSAR));
        properties.putIfAbsent(
            TableProperties.LOG_STORE_DATA_FORMAT, TableProperties.LOG_STORE_DATA_FORMAT_DEFAULT);
      }
    }

    private String getTableLocationForCreate() {
      if (StringUtils.isNotBlank(location)) {
        return location;
      }

      if (properties.containsKey(TableProperties.LOCATION)) {
        String tableLocation = properties.get(TableProperties.LOCATION);
        if (!Objects.equals("/", tableLocation) && tableLocation.endsWith("/")) {
          tableLocation = tableLocation.substring(0, tableLocation.length() - 1);
        }
        if (StringUtils.isNotBlank(tableLocation)) {
          return tableLocation;
        }
      }

      String databaseLocation = getDatabaseLocation();

      if (StringUtils.isNotBlank(databaseLocation)) {
        return databaseLocation + '/' + identifier.getTableName();
      } else {
        throw new IllegalStateException(
            "either `location` in table properties or "
                + "`warehouse` in catalog properties is specified");
      }
    }

    protected TableMetadata tableMetadata(
        Schema schema,
        PartitionSpec spec,
        SortOrder order,
        Map<String, String> properties,
        String location) {
      Preconditions.checkNotNull(schema, "A table schema is required");

      Map<String, String> tableProps = properties == null ? ImmutableMap.of() : properties;
      PartitionSpec partitionSpec = spec == null ? PartitionSpec.unpartitioned() : spec;
      SortOrder sortOrder = order == null ? SortOrder.unsorted() : order;
      return TableMetadata.newTableMetadata(schema, partitionSpec, sortOrder, location, tableProps);
    }

    public MixedHiveTableBuilder(TableIdentifier identifier, Schema schema) {
      Preconditions.checkArgument(
          identifier.getCatalog().equals(name()),
          "Illegal table id:%s for catalog:%s",
          identifier.toString(),
          name());
      this.identifier = identifier.toLowCaseIdentifier();
      this.schema = HiveSchemaUtil.changeFieldNameToLowercase(schema);
      this.partitionSpec = PartitionSpec.unpartitioned();
      this.sortOrder = SortOrder.unsorted();
    }

    boolean allowExistedHiveTable = false;

    @Override
    public TableBuilder withPartitionSpec(PartitionSpec partitionSpec) {
      this.partitionSpec = IcebergSchemaUtil.copyPartitionSpec(partitionSpec, schema);
      return this;
    }

    @Override
    public TableBuilder withSortOrder(SortOrder sortOrder) {
      this.sortOrder = IcebergSchemaUtil.copySortOrderSpec(sortOrder, schema);
      return this;
    }

    @Override
    public TableBuilder withPrimaryKeySpec(PrimaryKeySpec primaryKeySpec) {
      PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(schema);
      primaryKeySpec
          .fields()
          .forEach(
              primaryKeyField ->
                  builder.addColumn(primaryKeyField.fieldName().toLowerCase(Locale.ROOT)));
      this.primaryKeySpec = builder.build();
      return this;
    }

    @Override
    public TableBuilder withProperty(String key, String value) {
      if (key.equals(HiveTableProperties.ALLOW_HIVE_TABLE_EXISTED) && value.equals("true")) {
        allowExistedHiveTable = true;
        this.properties.put(key, value);
      } else if (key.equals(TableProperties.TABLE_EVENT_TIME_FIELD)) {
        this.properties.put(key, value.toLowerCase(Locale.ROOT));
      } else {
        this.properties.put(key, value);
      }
      return this;
    }

    @Override
    public TableBuilder withProperties(Map<String, String> properties) {
      properties.forEach(this::withProperty);
      return this;
    }

    protected void doCreateCheck() {
      if (primaryKeySpec.primaryKeyExisted()) {
        primaryKeySpec
            .fieldNames()
            .forEach(
                primaryKey -> {
                  if (schema.findField(primaryKey).isOptional()) {
                    throw new IllegalArgumentException(
                        "please check your schema, the primary key nested field must"
                            + " be required and field name is "
                            + primaryKey);
                  }
                });
      }
      listDatabases().stream()
          .filter(d -> d.equals(identifier.getDatabase()))
          .findFirst()
          .orElseThrow(() -> new NoSuchDatabaseException(identifier.getDatabase()));

      try {
        org.apache.hadoop.hive.metastore.api.Table hiveTable =
            hiveClientPool.run(
                client -> client.getTable(identifier.getDatabase(), identifier.getTableName()));
        if (hiveTable != null) {
          // do some check for whether the table has been upgraded!!!
          if (CompatibleHivePropertyUtil.propertyAsBoolean(
              hiveTable.getParameters(), HiveTableProperties.MIXED_TABLE_FLAG, false)) {
            // flink will ignore the AlreadyExistsdException to continue
            throw new org.apache.iceberg.exceptions.AlreadyExistsException(
                String.format("Table %s has already been upgraded !", identifier));
          }
        }
        if (allowExistedHiveTable) {
          LOG.info("No need to check hive table exist");
        } else {
          if (hiveTable != null) {
            // we should throw the exception for stop create.
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

      checkProperties();
    }

    protected String getDatabaseLocation() {
      try {
        return hiveClientPool.run(
            client -> client.getDatabase(identifier.getDatabase()).getLocationUri());
      } catch (TException | InterruptedException e) {
        throw new RuntimeException(
            "Failed to get database location:" + identifier.getDatabase(), e);
      }
    }

    protected void doRollbackCreateTable(TableMeta meta) {
      if (allowExistedHiveTable) {
        LOG.info(
            "No need to drop hive table {}.{}",
            meta.getTableIdentifier().getDatabase(),
            meta.getTableIdentifier().getTableName());
        tables.dropTableByMeta(meta, false);
      } else {
        tables.dropTableByMeta(meta, true);
      }
    }

    protected ConvertStructUtil.TableMetaBuilder createTableMataBuilder() {
      ConvertStructUtil.TableMetaBuilder builder =
          ConvertStructUtil.newTableMetaBuilder(this.identifier, this.schema);
      String tableLocation = getTableLocationForCreate();

      builder
          .withTableLocation(tableLocation)
          .withProperties(this.properties)
          .withPrimaryKeySpec(this.primaryKeySpec);

      if (this.primaryKeySpec.primaryKeyExisted()) {
        builder =
            builder
                .withBaseLocation(tableLocation + "/base")
                .withChangeLocation(tableLocation + "/change");
      } else {
        builder = builder.withBaseLocation(tableLocation + "/base");
      }
      return builder.withFormat(TableFormat.MIXED_HIVE);
    }
  }
}
