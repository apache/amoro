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

package com.netease.arctic.mixed.catalog;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.TableTrashManagers;
import com.netease.arctic.mixed.table.MixedTables;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.blocker.BasicTableBlockerManager;
import com.netease.arctic.table.blocker.TableBlockerManager;
import com.netease.arctic.utils.CatalogUtil;
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
import org.apache.thrift.TException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BasicMixedIcebergCatalog implements ArcticCatalog {

  private Map<String, String> clientSideProperties;
  private Catalog icebergCatalog;
  private TableMetaStore tableMetaStore;
  private CatalogMeta meta;
  private Pattern databaseFilterPattern;
  private AmsClient client;
  private MixedTables tables;

  public BasicMixedIcebergCatalog() {
  }

  public BasicMixedIcebergCatalog(CatalogMeta meta) {
    this.initialize(meta);
  }

  @Override
  public String name() {
    return meta.getCatalogName();
  }

  @Override
  public void initialize(AmsClient client, CatalogMeta meta, Map<String, String> properties) {
    this.client = client;
    this.clientSideProperties = properties == null ? Maps.newHashMap() : properties;
    this.initialize(meta);
  }

  private void initialize(CatalogMeta catalogMeta) {
    CatalogUtil.mergeCatalogProperties(catalogMeta, clientSideProperties);

    catalogMeta.putToCatalogProperties(
        org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE, catalogMeta.getCatalogType());
    if (catalogMeta.getCatalogProperties().containsKey(CatalogProperties.CATALOG_IMPL)) {
      catalogMeta.getCatalogProperties().remove("type");
    }

    TableMetaStore metaStore = CatalogUtil.buildMetaStore(catalogMeta);
    Catalog icebergCatalog = metaStore.doAs(() -> org.apache.iceberg.CatalogUtil.buildIcebergCatalog(
        catalogMeta.getCatalogName(), catalogMeta.getCatalogProperties(), metaStore.getConfiguration()));
    Pattern databaseFilterPattern = null;
    if (catalogMeta.getCatalogProperties().containsKey(CatalogMetaProperties.KEY_DATABASE_FILTER_REGULAR_EXPRESSION)) {
      String databaseFilter =
          catalogMeta.getCatalogProperties().get(CatalogMetaProperties.KEY_DATABASE_FILTER_REGULAR_EXPRESSION);
      databaseFilterPattern = Pattern.compile(databaseFilter);
    }
    MixedTables tables = new MixedTables(metaStore, catalogMeta, icebergCatalog);
    synchronized (this) {
      this.tableMetaStore = metaStore;
      this.icebergCatalog = icebergCatalog;
      this.databaseFilterPattern = databaseFilterPattern;
      this.tables = tables;
      this.meta = catalogMeta;
    }
  }

  @Override
  public List<String> listDatabases() {
    List<String> databases =
        tableMetaStore.doAs(() ->
            asNamespaceCatalog().listNamespaces(Namespace.empty())
                .stream()
                .map(namespace -> namespace.level(0))
                .distinct()
                .collect(Collectors.toList())
        );
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
        tableMetaStore.doAs(() -> icebergCatalog.listTables(Namespace.of(database)));
    List<TableIdentifier> mixedTables = Lists.newArrayList();
    Set<org.apache.iceberg.catalog.TableIdentifier> visited = Sets.newHashSet();
    for (org.apache.iceberg.catalog.TableIdentifier identifier : icebergTableList) {
      if (visited.contains(identifier)) {
        continue;
      }
      Table table = tableMetaStore.doAs(() -> icebergCatalog.loadTable(identifier));
      if (tables.isBaseStore(table)) {
        mixedTables.add(TableIdentifier.of(meta.getCatalogName(), database, identifier.name()));
        visited.add(identifier);
        PrimaryKeySpec keySpec = tables.getPrimaryKeySpec(table);
        if (keySpec.primaryKeyExisted()) {
          visited.add(tables.changeStoreIdentifier(table));
        }
      }
    }
    return mixedTables;
  }

  @Override
  public ArcticTable loadTable(TableIdentifier tableIdentifier) {
    Table base = tableMetaStore.doAs(
        () -> icebergCatalog.loadTable(toIcebergTableIdentifier(tableIdentifier)));
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
    ArcticTable base = table.isKeyedTable() ? table.asKeyedTable().baseTable() : table;
    // delete custom trash location
    String customTrashLocation = table.properties().get(TableProperties.TABLE_TRASH_CUSTOM_ROOT_LOCATION);
    ArcticFileIO io = table.io();
    boolean deleted = dropTableInternal(toIcebergTableIdentifier(tableIdentifier), purge);
    boolean changeDeleted = false;
    if (table.isKeyedTable()) {
      try {
        changeDeleted = dropTableInternal(tables.changeStoreIdentifier(base.asUnkeyedTable()), purge);
      } catch (Exception e) {
        // pass
      }
    }
    // delete custom trash location
    if (customTrashLocation != null) {
      String trashParentLocation = TableTrashManagers.getTrashParentLocation(tableIdentifier, customTrashLocation);
      if (io.supportFileSystemOperations() && io.exists(trashParentLocation)) {
        io.asPrefixFileIO().deletePrefix(trashParentLocation);
      }
    }
    return deleted || changeDeleted;
  }

  @Override
  public TableBuilder newTableBuilder(TableIdentifier identifier, Schema schema) {
    return new MixedIcebergTableBuilder(identifier, schema);
  }

  @Override
  public void refresh() {
    try {
      CatalogMeta catalogMeta = client.getCatalog(this.name());
      this.initialize(catalogMeta);
    } catch (TException e) {
      throw new IllegalStateException(String.format("failed load catalog %s.", name()), e);
    }
  }

  @Override
  public TableBlockerManager getTableBlockerManager(TableIdentifier tableIdentifier) {
    return BasicTableBlockerManager.build(tableIdentifier, client);
  }

  @Override
  public Map<String, String> properties() {
    return Maps.newHashMap(this.meta.getCatalogProperties());
  }

  private org.apache.iceberg.catalog.TableIdentifier toIcebergTableIdentifier(TableIdentifier identifier) {
    return org.apache.iceberg.catalog.TableIdentifier.of(identifier.getDatabase(), identifier.getTableName());
  }

  private boolean dropTableInternal(org.apache.iceberg.catalog.TableIdentifier tableIdentifier, boolean purge) {
    return tableMetaStore.doAs(() -> icebergCatalog.dropTable(tableIdentifier, purge));
  }

  private SupportsNamespaces asNamespaceCatalog() {
    if (!(icebergCatalog instanceof SupportsNamespaces)) {
      throw new UnsupportedOperationException(String.format(
          "Iceberg catalog: %s doesn't implement SupportsNamespaces",
          icebergCatalog.getClass().getName()));
    }
    return (SupportsNamespaces) icebergCatalog;
  }

  private class MixedIcebergTableBuilder implements TableBuilder {

    private final TableIdentifier identifier;
    private final Schema schema;

    private PartitionSpec partitionSpec;
    private SortOrder sortOrder;
    private Map<String, String> properties;
    private PrimaryKeySpec primaryKeySpec;

    public MixedIcebergTableBuilder(TableIdentifier identifier, Schema schema) {
      this.identifier = identifier;
      this.schema = schema;
      this.partitionSpec = PartitionSpec.unpartitioned();
      this.sortOrder = SortOrder.unsorted();
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
      this.sortOrder = sortOrder;
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
    public Transaction newCreateTableTransaction() {
      return null;
    }
  }
}
