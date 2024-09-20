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

package org.apache.amoro;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.amoro.utils.CatalogUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUnifiedCatalog implements UnifiedCatalog {

  private final String catalogName;
  private final String metaStoreType;
  private final Supplier<CatalogMeta> metaSupplier;
  // Client side catalog properties
  private final Map<String, String> clientProperties;
  // Catalog properties after merging the server and client
  private Map<String, String> catalogProperties;
  private CatalogMeta meta;
  private TableMetaStore tableMetaStore;
  private Map<TableFormat, FormatCatalog> formatCatalogs = Maps.newHashMap();

  public CommonUnifiedCatalog(
      Supplier<CatalogMeta> catalogMetaSupplier, Map<String, String> properties) {
    CatalogMeta catalogMeta = catalogMetaSupplier.get();
    CatalogUtil.mergeCatalogProperties(catalogMeta, properties);
    this.meta = catalogMeta;
    this.catalogName = catalogMeta.getCatalogName();
    this.metaStoreType = catalogMeta.getCatalogType();
    this.tableMetaStore = CatalogUtil.buildMetaStore(catalogMeta);
    this.clientProperties = properties;
    this.catalogProperties = catalogMeta.getCatalogProperties();
    this.metaSupplier = catalogMetaSupplier;
    initializeFormatCatalogs();
  }

  public CommonUnifiedCatalog(
      String catalogName,
      String metaStoreType,
      Map<String, String> properties,
      TableMetaStore tableMetaStore) {
    this.catalogName = catalogName;
    this.metaStoreType = metaStoreType;
    this.tableMetaStore = tableMetaStore;
    this.clientProperties = properties;
    this.catalogProperties = properties;
    this.metaSupplier = null;
    initializeFormatCatalogs();
  }

  @Override
  public String metastoreType() {
    return metaStoreType;
  }

  @Override
  public TableMetaStore authenticationContext() {
    return this.tableMetaStore;
  }

  @Override
  public List<String> listDatabases() {
    return findFirstFormatCatalog(TableFormat.values()).listDatabases();
  }

  @Override
  public boolean databaseExists(String database) {
    return listDatabases().contains(database);
  }

  @Override
  public boolean tableExists(String database, String table) {
    return formatCatalogAsOrder(TableFormat.values())
        .anyMatch(formatCatalog -> formatCatalog.tableExists(database, table));
  }

  @Override
  public void createDatabase(String database) {
    if (databaseExists(database)) {
      throw new AlreadyExistsException("Database: " + database + " already exists.");
    }

    findFirstFormatCatalog(TableFormat.values()).createDatabase(database);
  }

  @Override
  public void dropDatabase(String database) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }
    if (!listTables(database).isEmpty()) {
      throw new IllegalStateException("Database: " + database + " is not empty.");
    }
    findFirstFormatCatalog(TableFormat.values()).dropDatabase(database);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }

    return formatCatalogAsOrder(
            TableFormat.MIXED_HIVE,
            TableFormat.MIXED_ICEBERG,
            TableFormat.ICEBERG,
            TableFormat.PAIMON,
            TableFormat.HUDI)
        .map(
            formatCatalog -> {
              try {
                return formatCatalog.loadTable(database, table);
              } catch (NoSuchTableException e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow(() -> new NoSuchTableException("Table: " + table + " does not exist."));
  }

  @Override
  public String name() {
    return catalogName;
  }

  @Override
  public List<TableIDWithFormat> listTables(String database) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }
    TableFormat[] formats =
        new TableFormat[] {
          TableFormat.MIXED_HIVE,
          TableFormat.MIXED_ICEBERG,
          TableFormat.ICEBERG,
          TableFormat.PAIMON,
          TableFormat.HUDI
        };

    Map<String, TableFormat> tableNameToFormat = Maps.newHashMap();
    for (TableFormat format : formats) {
      if (formatCatalogs.containsKey(format)) {
        formatCatalogs
            .get(format)
            .listTables(database)
            .forEach(table -> tableNameToFormat.putIfAbsent(table, format));
      }
    }

    return tableNameToFormat.keySet().stream()
        .map(
            tableName -> {
              TableFormat format = tableNameToFormat.get(tableName);
              return TableIDWithFormat.of(
                  TableIdentifier.of(catalogName, database, tableName), format);
            })
        .collect(Collectors.toList());
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    try {
      AmoroTable<?> t = loadTable(database, table);
      return findFirstFormatCatalog(t.format()).dropTable(database, table, purge);
    } catch (NoSuchTableException e) {
      return false;
    }
  }

  @Override
  public synchronized void refresh() {
    if (metaSupplier != null) {
      CatalogMeta newMeta = metaSupplier.get();
      CatalogUtil.mergeCatalogProperties(meta, clientProperties);
      if (newMeta.equals(this.meta)) {
        return;
      }
      this.catalogProperties = newMeta.getCatalogProperties();
      this.tableMetaStore = CatalogUtil.buildMetaStore(newMeta);
      this.meta = newMeta;
      this.initializeFormatCatalogs();
    }
  }

  @Override
  public Map<String, String> properties() {
    return catalogProperties;
  }

  protected void initializeFormatCatalogs() {
    ServiceLoader<FormatCatalogFactory> loader = ServiceLoader.load(FormatCatalogFactory.class);
    Set<TableFormat> formats = CatalogUtil.tableFormats(metaStoreType, catalogProperties);
    Map<TableFormat, FormatCatalog> formatCatalogs = Maps.newConcurrentMap();
    for (FormatCatalogFactory factory : loader) {
      if (formats.contains(factory.format())) {
        Map<String, String> formatCatalogProperties =
            factory.convertCatalogProperties(name(), metaStoreType, this.catalogProperties);
        FormatCatalog catalog =
            factory.create(name(), metaStoreType, formatCatalogProperties, tableMetaStore);
        formatCatalogs.put(factory.format(), catalog);
      }
    }
    this.formatCatalogs = formatCatalogs;
  }

  /** get format catalogs as given format order */
  private Stream<FormatCatalog> formatCatalogAsOrder(TableFormat... formats) {
    return Stream.of(formats).filter(formatCatalogs::containsKey).map(formatCatalogs::get);
  }

  private FormatCatalog findFirstFormatCatalog(TableFormat... formats) {
    return Stream.of(formats)
        .filter(formatCatalogs::containsKey)
        .map(formatCatalogs::get)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No format catalog found."));
  }
}
