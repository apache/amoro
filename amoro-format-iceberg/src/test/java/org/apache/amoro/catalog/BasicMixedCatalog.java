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

import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_KAFKA;
import static org.apache.amoro.table.TableProperties.LOG_STORE_STORAGE_TYPE_PULSAR;
import static org.apache.amoro.table.TableProperties.LOG_STORE_TYPE;

import org.apache.amoro.AmsClient;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.PooledAmsClient;
import org.apache.amoro.TableFormat;
import org.apache.amoro.api.AlreadyExistsException;
import org.apache.amoro.api.NoSuchObjectException;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.mixed.InternalMixedIcebergCatalog;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.op.CreateTableTransaction;
import org.apache.amoro.op.MixedHadoopTableOperations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.thrift.org.apache.thrift.TException;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableBuilder;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.blocker.BasicTableBlockerManager;
import org.apache.amoro.table.blocker.TableBlockerManager;
import org.apache.amoro.utils.CatalogUtil;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.ConvertStructUtil;
import org.apache.amoro.utils.MixedFormatCatalogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.Transactions;
import org.apache.iceberg.exceptions.NoSuchTableException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic {@link MixedFormatCatalog} implementation. This class is deprecated, using {@link
 * InternalMixedIcebergCatalog} instead.
 *
 * @deprecated since 0.7.0, will be removed in 0.9.0;
 */
@Deprecated
public class BasicMixedCatalog implements MixedFormatCatalog {

  protected AmsClient client;
  protected String name;
  protected Map<String, String> catalogProperties;
  protected MixedTables tables;
  protected transient TableMetaStore tableMetaStore;

  @Override
  public String name() {
    return name;
  }

  @Override
  public void initialize(String name, Map<String, String> properties, TableMetaStore metaStore) {
    Preconditions.checkArgument(
        properties.containsKey(CatalogMetaProperties.AMS_URI),
        "property: %s must be set",
        CatalogMetaProperties.AMS_URI);
    this.client = new PooledAmsClient(properties.get(CatalogMetaProperties.AMS_URI));
    this.name = name;
    this.catalogProperties = properties;
    this.tableMetaStore = metaStore;
    this.tables = newMixedTables(properties, metaStore);
  }

  protected MixedTables newMixedTables(
      Map<String, String> catalogProperties, TableMetaStore metaStore) {
    return new MixedTables(catalogProperties, metaStore);
  }

  protected AmsClient getClient() {
    if (client == null) {
      throw new IllegalStateException("AMSClient is not initialized");
    }
    return client;
  }

  @Override
  public List<String> listDatabases() {
    try {
      return getClient().getDatabases(name());
    } catch (TException e) {
      throw new IllegalStateException("failed load database", e);
    }
  }

  @Override
  public void createDatabase(String databaseName) {
    try {
      getClient().createDatabase(name(), databaseName);
    } catch (AlreadyExistsException e) {
      throw new org.apache.iceberg.exceptions.AlreadyExistsException(
          "Database already exists, %s", databaseName);
    } catch (TException e) {
      throw new IllegalStateException("failed create database", e);
    }
  }

  @Override
  public void dropDatabase(String databaseName) {
    try {
      getClient().dropDatabase(name(), databaseName);
    } catch (NoSuchObjectException e0) {
      throw new NoSuchDatabaseException(e0, databaseName);
    } catch (TException e) {
      throw new IllegalStateException("failed drop database", e);
    }
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    try {
      return getClient().listTables(name(), database).stream()
          .map(t -> TableIdentifier.of(name(), database, t.getTableIdentifier().getTableName()))
          .collect(Collectors.toList());
    } catch (TException e) {
      throw new IllegalStateException("failed load tables", e);
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
  public void renameTable(TableIdentifier from, String newTableName) {
    throw new UnsupportedOperationException("unsupported rename mixed table for now.");
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
    try {
      getClient().removeTable(meta.getTableIdentifier(), purge);
    } catch (TException e) {
      throw new IllegalStateException("error when delete table metadata from metastore");
    }

    tables.dropTableByMeta(meta, purge);
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema) {
    validate(identifier);
    return new MixedTableBuilder(identifier, schema);
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

  protected TableMeta getMixedTableMeta(TableIdentifier identifier) {
    TableMeta tableMeta;
    try {
      tableMeta = getClient().getTable(CatalogUtil.amsTableId(identifier));
      return tableMeta;
    } catch (NoSuchObjectException e) {
      throw new NoSuchTableException(e, "load table failed %s.", identifier);
    } catch (TException e) {
      throw new IllegalStateException(String.format("failed load table %s.", identifier), e);
    }
  }

  private void validate(TableIdentifier identifier) {
    if (StringUtils.isEmpty(identifier.getCatalog())) {
      identifier.setCatalog(this.name());
    } else if (!this.name().equals(identifier.getCatalog())) {
      throw new IllegalArgumentException("catalog name miss match");
    }
  }

  protected class MixedTableBuilder implements TableBuilder {
    protected TableIdentifier identifier;
    protected Schema schema;
    protected PartitionSpec partitionSpec;
    protected SortOrder sortOrder;
    protected Map<String, String> properties = new HashMap<>();
    protected PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    protected String location;

    public MixedTableBuilder(TableIdentifier identifier, Schema schema) {
      Preconditions.checkArgument(
          identifier.getCatalog().equals(name()),
          "Illegal table id:%s for catalog:%s",
          identifier.toString(),
          name());
      this.identifier = identifier;
      this.schema = schema;
      this.partitionSpec = PartitionSpec.unpartitioned();
      this.sortOrder = SortOrder.unsorted();
    }

    @Override
    public TableBuilder withPartitionSpec(PartitionSpec partitionSpec) {
      this.partitionSpec = partitionSpec;
      return this;
    }

    @Override
    public TableBuilder withSortOrder(SortOrder sortOrder) {
      this.sortOrder = sortOrder;
      return this;
    }

    @Override
    public TableBuilder withProperties(Map<String, String> properties) {
      this.properties.putAll(properties);
      return this;
    }

    @Override
    public TableBuilder withProperty(String key, String value) {
      this.properties.put(key, value);
      return this;
    }

    @Override
    public TableBuilder withPrimaryKeySpec(PrimaryKeySpec primaryKeySpec) {
      this.primaryKeySpec = primaryKeySpec;
      return this;
    }

    @Override
    public MixedTable create() {
      ConvertStructUtil.TableMetaBuilder builder = createTableMataBuilder();
      doCreateCheck();
      TableMeta meta = builder.build();
      MixedTable table = createTableByMeta(meta, schema, primaryKeySpec, partitionSpec);
      createTableMeta(meta);
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
            } catch (TException e) {
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

    protected void checkAmsTableMetadata() {
      try {
        client.getTable(identifier.buildTableIdentifier());
        throw new org.apache.iceberg.exceptions.AlreadyExistsException("table already exist");
      } catch (NoSuchObjectException e) {
        checkProperties();
      } catch (TException e) {
        throw new IllegalStateException("failed when load table", e);
      }
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

      checkAmsTableMetadata();
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

    protected void createTableMeta(TableMeta meta) {
      boolean tableCreated = false;
      try {
        client.createTableMeta(meta);
        tableCreated = true;
      } catch (AlreadyExistsException e) {
        throw new org.apache.iceberg.exceptions.AlreadyExistsException("table already exist", e);
      } catch (TException e) {
        throw new IllegalStateException("update table meta failed", e);
      } finally {
        if (!tableCreated) {
          doRollbackCreateTable(meta);
        }
      }
    }

    protected void doRollbackCreateTable(TableMeta meta) {
      tables.dropTableByMeta(meta, true);
    }

    protected ConvertStructUtil.TableMetaBuilder createTableMataBuilder() {
      ConvertStructUtil.TableMetaBuilder builder =
          ConvertStructUtil.newTableMetaBuilder(this.identifier, this.schema);
      String tableLocation = getTableLocationForCreate();

      builder
          .withTableLocation(tableLocation)
          .withProperties(this.properties)
          .withFormat(TableFormat.MIXED_ICEBERG)
          .withPrimaryKeySpec(this.primaryKeySpec);

      if (this.primaryKeySpec.primaryKeyExisted()) {
        builder =
            builder
                .withBaseLocation(tableLocation + "/base")
                .withChangeLocation(tableLocation + "/change");
      } else {
        builder = builder.withBaseLocation(tableLocation + "/base");
      }
      return builder;
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

    protected void fillTableProperties(TableMeta meta) {
      meta.putToProperties(
          TableProperties.TABLE_CREATE_TIME, String.valueOf(System.currentTimeMillis()));
      meta.putToProperties(org.apache.iceberg.TableProperties.FORMAT_VERSION, "2");
      meta.putToProperties(
          org.apache.iceberg.TableProperties.METADATA_DELETE_AFTER_COMMIT_ENABLED, "true");
      meta.putToProperties("flink.max-continuous-empty-commits", String.valueOf(Integer.MAX_VALUE));
    }

    protected String getDatabaseLocation() {
      if (catalogProperties != null) {
        String catalogWarehouse =
            catalogProperties.getOrDefault(CatalogMetaProperties.KEY_WAREHOUSE, null);
        if (catalogWarehouse == null) {
          catalogWarehouse =
              catalogProperties.getOrDefault(CatalogMetaProperties.KEY_WAREHOUSE_DIR, null);
        }
        if (catalogWarehouse == null) {
          throw new NullPointerException("Catalog warehouse is null.");
        }
        if (!Objects.equals("/", catalogWarehouse) && catalogWarehouse.endsWith("/")) {
          catalogWarehouse = catalogWarehouse.substring(0, catalogWarehouse.length() - 1);
        }
        if (StringUtils.isNotBlank(catalogWarehouse)) {
          return catalogWarehouse + '/' + identifier.getDatabase();
        }
      }
      return null;
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
  }
}
