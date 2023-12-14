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

import com.netease.arctic.AmsClient;
import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.TableTrashManagers;
import com.netease.arctic.op.CreateTableTransaction;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.blocker.BasicTableBlockerManager;
import com.netease.arctic.table.blocker.TableBlockerManager;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CachingCatalog;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.PropertyUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BasicMixedIcebergCatalog implements ArcticCatalog {

  private Catalog icebergCatalog;
  private TableMetaStore tableMetaStore;
  private Map<String, String> catalogProperties;
  private String name;
  private Pattern databaseFilterPattern;
  private AmsClient client;
  private MixedTables tables;
  private SupportsNamespaces asNamespaceCatalog;

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public void initialize(String name, Map<String, String> properties, TableMetaStore metaStore) {
    boolean cacheEnabled =
        PropertyUtil.propertyAsBoolean(
            properties, CatalogProperties.CACHE_ENABLED, CatalogProperties.CACHE_ENABLED_DEFAULT);

    boolean cacheCaseSensitive =
        PropertyUtil.propertyAsBoolean(
            properties,
            CatalogProperties.CACHE_CASE_SENSITIVE,
            CatalogProperties.CACHE_CASE_SENSITIVE_DEFAULT);

    long cacheExpirationIntervalMs =
        PropertyUtil.propertyAsLong(
            properties,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS,
            CatalogProperties.CACHE_EXPIRATION_INTERVAL_MS_DEFAULT);

    // An expiration interval of 0ms effectively disables caching.
    // Do not wrap with CachingCatalog.
    if (cacheExpirationIntervalMs == 0) {
      cacheEnabled = false;
    }
    Pattern databaseFilterPattern = null;
    if (properties.containsKey(CatalogMetaProperties.KEY_DATABASE_FILTER)) {
      String databaseFilter = properties.get(CatalogMetaProperties.KEY_DATABASE_FILTER);
      databaseFilterPattern = Pattern.compile(databaseFilter);
    }
    Catalog catalog = buildIcebergCatalog(name, properties, metaStore.getConfiguration());
    this.name = name;
    this.tableMetaStore = metaStore;
    this.icebergCatalog =
        cacheEnabled
            ? CachingCatalog.wrap(catalog, cacheCaseSensitive, cacheExpirationIntervalMs)
            : catalog;
    if (catalog instanceof SupportsNamespaces) {
      this.asNamespaceCatalog = (SupportsNamespaces) catalog;
    }
    this.databaseFilterPattern = databaseFilterPattern;
    this.catalogProperties = properties;
    this.tables = newMixedTables(metaStore, properties, icebergCatalog());
    if (properties.containsKey(CatalogMetaProperties.AMS_URI)) {
      this.client = new PooledAmsClient(properties.get(CatalogMetaProperties.AMS_URI));
    }
  }

  @Override
  public List<String> listDatabases() {
    List<String> databases =
        tableMetaStore.doAs(
            () ->
                asNamespaceCatalog().listNamespaces(Namespace.empty()).stream()
                    .map(namespace -> namespace.level(0))
                    .distinct()
                    .collect(Collectors.toList()));
    if (databaseFilterPattern == null) {
      return databases;
    }
    return databases.stream()
        .filter(database -> databaseFilterPattern.matcher(database).matches())
        .collect(Collectors.toList());
  }

  @Override
  public void createDatabase(String database) {
    asNamespaceCatalog().createNamespace(Namespace.of(database));
  }

  @Override
  public void dropDatabase(String databaseName) {
    asNamespaceCatalog().dropNamespace(Namespace.of(databaseName));
  }

  @Override
  public List<TableIdentifier> listTables(String database) {
    List<org.apache.iceberg.catalog.TableIdentifier> icebergTableList =
        tableMetaStore.doAs(() -> icebergCatalog().listTables(Namespace.of(database)));
    List<TableIdentifier> mixedTables = Lists.newArrayList();
    Set<org.apache.iceberg.catalog.TableIdentifier> visited = Sets.newHashSet();
    for (org.apache.iceberg.catalog.TableIdentifier identifier : icebergTableList) {
      if (visited.contains(identifier)) {
        continue;
      }
      Table table = tableMetaStore.doAs(() -> icebergCatalog().loadTable(identifier));
      if (tables.isBaseStore(table)) {
        mixedTables.add(TableIdentifier.of(name(), database, identifier.name()));
        visited.add(identifier);
        PrimaryKeySpec keySpec =
            TablePropertyUtil.parsePrimaryKeySpec(table.schema(), table.properties());
        if (keySpec.primaryKeyExisted()) {
          visited.add(tables.parseChangeIdentifier(table));
        }
      }
    }
    return mixedTables;
  }

  @Override
  public ArcticTable loadTable(TableIdentifier tableIdentifier) {
    Table base =
        tableMetaStore.doAs(
            () -> icebergCatalog().loadTable(toIcebergTableIdentifier(tableIdentifier)));
    if (!tables.isBaseStore(base)) {
      throw new NoSuchTableException("table " + base.name() + " is not a mixed iceberg table");
    }
    return tables.loadTable(base, tableIdentifier);
  }

  @Override
  public void renameTable(TableIdentifier from, String newTableName) {
    throw new UnsupportedOperationException("rename table is not supported");
  }

  @Override
  public boolean dropTable(TableIdentifier tableIdentifier, boolean purge) {
    ArcticTable table;
    try {
      table = loadTable(tableIdentifier);
    } catch (NoSuchTableException e) {
      return false;
    }

    // delete custom trash location
    String customTrashLocation =
        table.properties().get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    ArcticFileIO io = table.io();
    // delete custom trash location
    if (customTrashLocation != null) {
      String trashParentLocation =
          TableTrashManagers.getTrashParentLocation(tableIdentifier, customTrashLocation);
      if (io.supportFileSystemOperations() && io.exists(trashParentLocation)) {
        io.asPrefixFileIO().deletePrefix(trashParentLocation);
      }
    }
    return tables.dropTable(table, purge);
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema) {
    return new MixedIcebergTableBuilder(identifier, schema);
  }

  @Override
  public TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier) {
    if (client == null) {
      throw new UnsupportedOperationException("AMSClient is not initialized");
    }
    return BasicTableBlockerManager.build(tableIdentifier, client);
  }

  @Override
  public Map<String, String> properties() {
    return Maps.newHashMap(catalogProperties);
  }

  protected Catalog icebergCatalog() {
    return this.icebergCatalog;
  }

  protected Catalog buildIcebergCatalog(
      String name, Map<String, String> properties, Configuration hadoopConf) {
    return org.apache.iceberg.CatalogUtil.buildIcebergCatalog(name, properties, hadoopConf);
  }

  protected MixedTables newMixedTables(
      TableMetaStore metaStore, Map<String, String> catalogProperties, Catalog icebergCatalog) {
    return new MixedTables(metaStore, catalogProperties, icebergCatalog);
  }

  private org.apache.iceberg.catalog.TableIdentifier toIcebergTableIdentifier(
      TableIdentifier identifier) {
    return org.apache.iceberg.catalog.TableIdentifier.of(
        identifier.getDatabase(), identifier.getTableName());
  }

  private SupportsNamespaces asNamespaceCatalog() {
    if (asNamespaceCatalog == null) {
      throw new UnsupportedOperationException(
          String.format(
              "Iceberg catalog: %s doesn't implement SupportsNamespaces",
              icebergCatalog().getClass().getName()));
    }
    return asNamespaceCatalog;
  }

  private class MixedIcebergTableBuilder implements TableBuilder {

    private final TableIdentifier identifier;
    private final Schema schema;

    private PartitionSpec partitionSpec;
    private Map<String, String> properties;
    private PrimaryKeySpec primaryKeySpec;

    public MixedIcebergTableBuilder(TableIdentifier identifier, Schema schema) {
      this.identifier = identifier;
      this.schema = schema;
      this.partitionSpec = PartitionSpec.unpartitioned();
      this.properties = Maps.newHashMap();
      this.primaryKeySpec = PrimaryKeySpec.noPrimaryKey();
    }

    @Override
    public TableBuilder withPartitionSpec(PartitionSpec partitionSpec) {
      this.partitionSpec = partitionSpec;
      return this;
    }

    @Override
    public TableBuilder withSortOrder(SortOrder sortOrder) {
      if (sortOrder.isSorted()) {
        throw new UnsupportedOperationException(
            "SortOrder is not supported by mixed-iceberg format");
      }
      return this;
    }

    @Override
    public TableBuilder withProperties(Map<String, String> properties) {
      this.properties = properties;
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
      return tables.createTable(identifier, schema, partitionSpec, primaryKeySpec, properties);
    }

    @Override
    public Transaction createTransaction() {
      Transaction transaction =
          icebergCatalog()
              .newCreateTableTransaction(
                  org.apache.iceberg.catalog.TableIdentifier.of(
                      identifier.getDatabase(), identifier.getTableName()),
                  schema,
                  partitionSpec,
                  properties);
      return new CreateTableTransaction(
          transaction, this::create, () -> dropTable(identifier, true));
    }
  }
}
