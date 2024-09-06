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

package org.apache.amoro.spark.mixed;

import static org.apache.amoro.spark.mixed.SparkSQLProperties.REFRESH_CATALOG_BEFORE_USAGE;
import static org.apache.amoro.spark.mixed.SparkSQLProperties.REFRESH_CATALOG_BEFORE_USAGE_DEFAULT;
import static org.apache.iceberg.CatalogUtil.ICEBERG_CATALOG_TYPE;

import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.shade.guava32.com.google.common.base.Joiner;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.SupportAuthentication;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.NamespaceChange;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Base class of mixed-format spark catalog. */
public abstract class MixedSparkCatalogBase
    implements TableCatalog, SupportsNamespaces, SupportAuthentication {
  private String catalogName = null;
  private TableMetaStore tableMetaStore;

  protected MixedFormatCatalog catalog;
  private CaseInsensitiveStringMap options;

  @Override
  public void setAuthenticationContext(TableMetaStore tableMetaStore) {
    this.tableMetaStore = tableMetaStore;
  }

  @Override
  public final void initialize(String name, CaseInsensitiveStringMap options) {
    this.catalogName = name;
    String catalogUrl = options.get(CatalogMetaProperties.AMS_URI);
    if (StringUtils.isNotBlank(catalogUrl)) {
      // initialize for unified catalog.
      String metastoreType = options.get(ICEBERG_CATALOG_TYPE);
      String registerName = options.get("register-name");
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(metastoreType),
          "Lack required property: type when initialized by unified catalog.");
      Preconditions.checkNotNull(
          tableMetaStore,
          "Authentication context must be set when initialized by unified catalog.");
      Preconditions.checkArgument(
          StringUtils.isNotEmpty(registerName),
          "Lack required property: register-name when initialized by unified catalog");
      catalog = CatalogLoader.createCatalog(registerName, metastoreType, options, tableMetaStore);
    } else {
      catalogUrl = options.get("url");
      if (StringUtils.isBlank(catalogUrl)) {
        catalogUrl = options.get("uri");
      }
      Preconditions.checkArgument(
          StringUtils.isNotBlank(catalogUrl), "lack required properties: url");

      catalog = CatalogLoader.load(catalogUrl, options);
    }
    this.options = options;
  }

  @Override
  public String name() {
    return catalogName;
  }

  @Override
  public String[][] listNamespaces() {
    return catalog.listDatabases().stream().map(d -> new String[] {d}).toArray(String[][]::new);
  }

  // ns
  @Override
  public String[][] listNamespaces(String[] namespace) {
    return new String[0][];
  }

  @Override
  public Map<String, String> loadNamespaceMetadata(String[] namespace)
      throws NoSuchNamespaceException {
    String database = namespace[0];
    return catalog.listDatabases().stream()
        .filter(d -> StringUtils.equals(d, database))
        .map(d -> new HashMap<String, String>())
        .findFirst()
        .orElseThrow(() -> new NoSuchNamespaceException(namespace));
  }

  @Override
  public void createNamespace(String[] namespace, Map<String, String> metadata) {
    if (namespace.length > 1) {
      throw new UnsupportedOperationException(
          "mixed-format does not support multi-level namespace.");
    }
    String database = namespace[0];
    catalog.createDatabase(database);
  }

  @Override
  public void alterNamespace(String[] namespace, NamespaceChange... changes) {
    throw new UnsupportedOperationException(
        "Alter  namespace is not supported by catalog: " + catalogName);
  }

  @Override
  public boolean dropNamespace(String[] namespace) {
    String database = namespace[0];
    catalog.dropDatabase(database);
    return true;
  }

  @Override
  public boolean dropTable(Identifier ident) {
    TableIdentifier identifier = buildIdentifier(ident);
    return catalog.dropTable(identifier, true);
  }

  @Override
  public boolean purgeTable(Identifier ident) throws UnsupportedOperationException {
    TableIdentifier identifier = buildIdentifier(ident);
    return catalog.dropTable(identifier, true);
  }

  @Override
  public void renameTable(Identifier from, Identifier to) {
    throw new UnsupportedOperationException("Unsupported renameTable.");
  }

  @Override
  public Identifier[] listTables(String[] namespace) {
    List<String> database;
    if (namespace == null || namespace.length == 0) {
      database = catalog.listDatabases();
    } else {
      database = new ArrayList<>();
      database.add(namespace[0]);
    }

    List<TableIdentifier> tableIdentifiers =
        database.stream()
            .map(d -> catalog.listTables(d))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

    return tableIdentifiers.stream()
        .map(i -> Identifier.of(new String[] {i.getDatabase()}, i.getTableName()))
        .toArray(Identifier[]::new);
  }

  protected void checkAndRefreshCatalogMeta() {
    SparkSession sparkSession = SparkSession.active();
    if (Boolean.parseBoolean(
        sparkSession
            .conf()
            .get(REFRESH_CATALOG_BEFORE_USAGE, REFRESH_CATALOG_BEFORE_USAGE_DEFAULT))) {
      initialize(catalogName, options);
    }
  }

  /**
   * Build an Amoro {@link TableIdentifier} for the given Spark identifier.
   *
   * @param identifier Spark's identifier
   * @return an Amoro identifier
   */
  protected TableIdentifier buildIdentifier(Identifier identifier) {
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length > 0,
        "database is not specific, table identifier: " + identifier.name());
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length == 1,
        "mixed-format does not support multi-level namespace: "
            + Joiner.on(".").join(identifier.namespace()));
    return TableIdentifier.of(
        catalog.name(), identifier.namespace()[0].split("\\.")[0], identifier.name());
  }

  protected TableIdentifier buildInnerTableIdentifier(Identifier identifier) {
    Preconditions.checkArgument(
        identifier.namespace() != null && identifier.namespace().length > 0,
        "database is not specific, table identifier: " + identifier.name());
    Preconditions.checkArgument(
        identifier.namespace().length == 2,
        "mixed-format does not support multi-level namespace: "
            + Joiner.on(".").join(identifier.namespace()));

    return TableIdentifier.of(catalog.name(), identifier.namespace()[0], identifier.namespace()[1]);
  }

  protected boolean isIdentifierLocation(String location, Identifier identifier) {
    List<String> nameParts = Lists.newArrayList();
    nameParts.add(name());
    nameParts.addAll(Arrays.asList(identifier.namespace()));
    nameParts.add(identifier.name());
    String ident = Joiner.on('.').join(nameParts);
    return ident.equalsIgnoreCase(location);
  }

  protected boolean isInnerTableIdentifier(Identifier identifier) {
    if (identifier.namespace().length != 2) {
      return false;
    }
    return MixedTableStoreType.from(identifier.name()) != null;
  }
}
