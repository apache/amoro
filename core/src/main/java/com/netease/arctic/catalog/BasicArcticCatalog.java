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

package com.netease.arctic.catalog;

import static com.netease.arctic.table.TableProperties.LOG_STORE_STORAGE_TYPE_KAFKA;
import static com.netease.arctic.table.TableProperties.LOG_STORE_STORAGE_TYPE_PULSAR;
import static com.netease.arctic.table.TableProperties.LOG_STORE_TYPE;

import com.netease.arctic.AmsClient;
import com.netease.arctic.NoSuchDatabaseException;
import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.AlreadyExistsException;
import com.netease.arctic.ams.api.NoSuchObjectException;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.op.ArcticHadoopTableOperations;
import com.netease.arctic.op.CreateTableTransaction;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.blocker.BasicTableBlockerManager;
import com.netease.arctic.table.blocker.TableBlockerManager;
import com.netease.arctic.utils.CatalogUtil;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.ConvertStructUtil;
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
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Basic {@link ArcticCatalog} implementation. This class is deprecated, using {@link
 * com.netease.arctic.mixed.MixedIcebergAmoroCatalog} instead.
 */
@Deprecated
public class BasicArcticCatalog implements ArcticCatalog {
  private static final Logger LOG = LoggerFactory.getLogger(BasicArcticCatalog.class);

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
  public ArcticTable loadTable(TableIdentifier identifier) {
    validate(identifier);
    TableMeta meta = getArcticTableMeta(identifier);
    if (meta.getLocations() == null) {
      throw new IllegalStateException("load table failed, lack locations info");
    }
    return tables.loadTableByMeta(meta);
  }

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {
    throw new UnsupportedOperationException("unsupported rename arctic table for now.");
  }

  @Override
  public boolean dropTable(TableIdentifier identifier, boolean purge) {
    validate(identifier);
    TableMeta meta;
    try {
      meta = getArcticTableMeta(identifier);
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
    return new ArcticTableBuilder(identifier, schema);
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

  protected TableMeta getArcticTableMeta(TableIdentifier identifier) {
    TableMeta tableMeta;
    try {
      tableMeta = getClient().getTable(CatalogUtil.amsTaleId(identifier));
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

  protected class ArcticTableBuilder implements TableBuilder {
    protected TableIdentifier identifier;
    protected Schema schema;
    protected PartitionSpec partitionSpec;
    protected SortOrder sortOrder;
    protected Map<String, String> properties = new HashMap<>();
    protected PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    protected String location;

    public ArcticTableBuilder(TableIdentifier identifier, Schema schema) {
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
    public ArcticTable create() {
      ConvertStructUtil.TableMetaBuilder builder = createTableMataBuilder();
      doCreateCheck();
      TableMeta meta = builder.build();
      ArcticTable table = createTableByMeta(meta, schema, primaryKeySpec, partitionSpec);
      createTableMeta(meta);
      return table;
    }

    @Override
    public Transaction createTransaction() {
      ArcticFileIO arcticFileIO = ArcticFileIOs.buildHadoopFileIO(tableMetaStore);
      ConvertStructUtil.TableMetaBuilder builder = createTableMataBuilder();
      TableMeta meta = builder.build();
      String location = getTableLocationForCreate();
      TableOperations tableOperations =
          new ArcticHadoopTableOperations(
              new Path(location), arcticFileIO, tableMetaStore.getConfiguration());
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

    protected ArcticTable createTableByMeta(
        TableMeta tableMeta,
        Schema schema,
        PrimaryKeySpec primaryKeySpec,
        PartitionSpec partitionSpec) {
      return tables.createTableByMeta(tableMeta, schema, primaryKeySpec, partitionSpec);
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
        client.getTable(identifier.buildTableIdentifier());
        throw new org.apache.iceberg.exceptions.AlreadyExistsException("table already exist");
      } catch (NoSuchObjectException e) {
        checkProperties();
      } catch (TException e) {
        throw new IllegalStateException("failed when load table", e);
      }
    }

    protected void checkProperties() {
      Map<String, String> mergedProperties =
          CatalogUtil.mergeCatalogPropertiesToTable(properties, catalogProperties);
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
