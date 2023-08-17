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

package com.netease.arctic;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUnifiedCatalog implements UnifiedCatalog {

  private Supplier<CatalogMeta> metaSupplier;
  private CatalogMeta meta;
  private Map<TableFormat, FormatCatalog> formatCatalogs = Maps.newHashMap();
  private Map<String, String> properties = Maps.newHashMap();

  public CommonUnifiedCatalog(
      Supplier<CatalogMeta> catalogMetaSupplier, CatalogMeta meta, Map<String, String> properties
  ) {
    this.meta = meta;
    this.properties.putAll(properties);
    this.metaSupplier = catalogMetaSupplier;
    initializeFormatCatalogs();
  }

  @Override
  public List<String> listDatabases() {
    return findFirstFormatCatalog(TableFormat.values()).listDatabases();
  }

  @Override
  public boolean exist(String database) {
    return listDatabases().contains(database);
  }

  @Override
  public boolean exist(String database, String table) {
    return formatCatalogAsOrder(TableFormat.values())
        .anyMatch(formatCatalog -> formatCatalog.exist(database, table));
  }

  @Override
  public void createDatabase(String database) {
    if (exist(database)) {
      throw new AlreadyExistsException("Database: " + database + " already exists.");
    }

    findFirstFormatCatalog(TableFormat.values()).createDatabase(database);
  }

  @Override
  public void dropDatabase(String database) {
    if (!exist(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }
    if (listTableMetas(database).size() > 0) {
      throw new IllegalStateException("Database: " + database + " is not empty.");
    }
    findFirstFormatCatalog(TableFormat.values()).dropDatabase(database);
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    if (!exist(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }

    return formatCatalogAsOrder(TableFormat.MIXED_ICEBERG, TableFormat.ICEBERG, TableFormat.PAIMON)
        .map(formatCatalog -> {
          try {
            return formatCatalog.loadTable(database, table);
          } catch (NoSuchTableException e) {
            return null;
          }
        })
        .filter(Objects::nonNull).findFirst()
        .orElseThrow(() -> new NoSuchTableException("Table: " + table + " does not exist."));
  }

  @Override
  public String name() {
    return this.meta.getCatalogName();
  }

  @Override
  public List<TableMeta> listTableMetas(String database) {
    if (!exist(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist.");
    }
    TableFormat[] formats = new TableFormat[] {TableFormat.MIXED_ICEBERG, TableFormat.ICEBERG, TableFormat.PAIMON};

    Map<String, TableFormat> tableNameToFormat = Maps.newHashMap();
    for (TableFormat format : formats) {
      if (formatCatalogs.containsKey(format)) {
        formatCatalogs.get(format)
            .listTables(database)
            .forEach(table -> tableNameToFormat.putIfAbsent(table, format));
      }
    }

    return tableNameToFormat.keySet().stream()
        .map(tableName -> {
          TableFormat format = tableNameToFormat.get(tableName);
          return TableMeta.of(TableIdentifier.of(this.meta.getCatalogName(), database, tableName), format);
        }).collect(Collectors.toList());
  }

  @Override
  public synchronized void refresh() {
    this.meta = metaSupplier.get();
    CatalogUtil.mergeCatalogProperties(meta, properties);
  }

  protected void initializeFormatCatalogs() {
    ServiceLoader<FormatCatalogFactory> loader = ServiceLoader.load(FormatCatalogFactory.class);
    Set<TableFormat> formats = CatalogUtil.tableFormats(this.meta);
    TableMetaStore store = CatalogUtil.buildMetaStore(this.meta);
    Map<TableFormat, FormatCatalog> formatCatalogs = Maps.newConcurrentMap();
    for (FormatCatalogFactory factory : loader) {
      if (formats.contains(factory.format())) {
        FormatCatalog catalog = factory.create(name(), meta.getCatalogProperties(), store.getConfiguration());
        formatCatalogs.put(factory.format(), catalog);
      }
    }
    this.formatCatalogs = formatCatalogs;
  }

  /**
   * get format catalogs as given format order
   */
  private Stream<FormatCatalog> formatCatalogAsOrder(TableFormat... formats) {
    return Stream.of(formats)
        .filter(formatCatalogs::containsKey)
        .map(formatCatalogs::get);
  }

  private FormatCatalog findFirstFormatCatalog(TableFormat... formats) {
    return Stream.of(formats)
        .filter(formatCatalogs::containsKey)
        .map(formatCatalogs::get)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No format catalog found."));
  }
}
