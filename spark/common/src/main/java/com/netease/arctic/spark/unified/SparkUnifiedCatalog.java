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

package com.netease.arctic.spark.unified;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableIDWithFormat;
import com.netease.arctic.UnifiedCatalog;
import com.netease.arctic.UnifiedCatalogLoader;
import com.netease.arctic.ams.api.Constants;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.client.ArcticThriftUrl;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.sql.catalyst.analysis.NamespaceAlreadyExistsException;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.catalyst.analysis.NoSuchProcedureException;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException;
import org.apache.spark.sql.connector.catalog.CatalogManager;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.NamespaceChange;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.TableChange;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.connector.iceberg.catalog.Procedure;
import org.apache.spark.sql.connector.iceberg.catalog.ProcedureCatalog;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import org.apache.spark.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class SparkUnifiedCatalog implements TableCatalog, SupportsNamespaces, ProcedureCatalog {

  private static final Logger LOG = LoggerFactory.getLogger(SparkUnifiedCatalog.class);
  private static final Map<TableFormat, String> defaultTableCatalogImplMap = ImmutableMap.of(
      TableFormat.ICEBERG, "org.apache.iceberg.spark.SparkCatalog",
      TableFormat.MIXED_HIVE, "com.netease.arctic.spark.ArcticSparkCatalog",
      TableFormat.MIXED_ICEBERG, "com.netease.arctic.spark.ArcticSparkCatalog",
      TableFormat.PAIMON, "org.apache.paimon.spark.SparkCatalog"
  );

  private UnifiedCatalog unifiedCatalog;
  private String name;
  private final Map<TableFormat, TableCatalog> tableCatalogs = Maps.newConcurrentMap();

  @Override
  public void initialize(String name, CaseInsensitiveStringMap options) {
    Map<String, String> properties = Maps.newHashMap(options);
    String uri = options.get(SparkUnifiedCatalogProperties.URI);
    properties.remove(SparkUnifiedCatalogProperties.URI);
    Preconditions.checkNotNull(uri, "lack required option: %s", SparkUnifiedCatalogProperties.URI);

    ArcticThriftUrl catalogUri = ArcticThriftUrl.parse(uri, Constants.THRIFT_TABLE_SERVICE_NAME);
    String registerCatalogName = catalogUri.catalogName();

    if (StringUtils.isBlank(registerCatalogName)) {
      registerCatalogName = name;
      if (CatalogManager.SESSION_CATALOG_NAME().equalsIgnoreCase(registerCatalogName)) {
        LOG.warn("catalog name is not exists in catalog uri, using spark catalog as register catalog name, but " +
            "current name is spark session catalog name.");
      }
    }
    this.name = name;
    this.unifiedCatalog = UnifiedCatalogLoader.loadUnifiedCatalog(
        catalogUri.serverUrl(), registerCatalogName, properties);
  }

  @Override
  public String name() {
    return name;
  }

  private String namespaceToDatabase(String[] namespace) {
    Preconditions.checkArgument(
        namespace.length == 1,
        "only support namespace with 1 level.");
    return namespace[0];
  }

  @Override
  public String[][] listNamespaces() throws NoSuchNamespaceException {
    return unifiedCatalog.listDatabases().stream().map(d -> new String[] {d}).toArray(String[][]::new);
  }

  @Override
  public String[][] listNamespaces(String[] namespace) throws NoSuchNamespaceException {
    return new String[0][];
  }

  @Override
  public boolean namespaceExists(String[] namespace) {
    return unifiedCatalog.exist(namespaceToDatabase(namespace));
  }

  @Override
  public Map<String, String> loadNamespaceMetadata(String[] namespace) throws NoSuchNamespaceException {
    if (namespaceExists(namespace)) {
      return ImmutableMap.of();
    }
    throw new NoSuchNamespaceException(namespace);
  }

  @Override
  public void createNamespace(String[] namespace, Map<String, String> metadata) throws NamespaceAlreadyExistsException {
    String database = namespaceToDatabase(namespace);
    if (metadata != null && !metadata.isEmpty()) {
      LOG.warn("doesn't support properties for database, all properties will be discard.");
    }
    unifiedCatalog.createDatabase(database);
  }

  @Override
  public void alterNamespace(String[] namespace, NamespaceChange... changes) throws NoSuchNamespaceException {
    throw new UnsupportedOperationException("Cannot apply namespace change");
  }

  @Override
  public boolean dropNamespace(String[] namespace) throws NoSuchNamespaceException {
    String database = namespaceToDatabase(namespace);
    List<TableIDWithFormat> tables = unifiedCatalog.listTables(database);
    for (TableIDWithFormat id : tables) {
      unifiedCatalog.dropTable(database, id.getIdentifier().getTableName(), true);
    }
    unifiedCatalog.dropDatabase(database);
    return !unifiedCatalog.exist(database);
  }

  @Override
  public Identifier[] listTables(String[] namespace) throws NoSuchNamespaceException {
    String database = namespaceToDatabase(namespace);
    List<TableIDWithFormat> tables = unifiedCatalog.listTables(database);

    return tables.stream().map(id -> Identifier.of(new String[] {id.database()}, id.table()))
        .toArray(Identifier[]::new);
  }

  @Override
  public Table loadTable(Identifier ident) throws NoSuchTableException {
    try {
      AmoroTable<?> table = unifiedCatalog.loadTable(namespaceToDatabase(ident.namespace()), ident.name());
      return tableCatalog(table.format()).loadTable(ident);
    } catch (com.netease.arctic.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
  }

  @Override
  public void invalidateTable(Identifier ident) {
    try {
      AmoroTable<?> table = unifiedCatalog.loadTable(namespaceToDatabase(ident.namespace()), ident.name());
      tableCatalog(table.format()).invalidateTable(ident);
    } catch (com.netease.arctic.NoSuchTableException e) {
      //pass
    }
  }

  @Override
  public boolean tableExists(Identifier ident) {
    return unifiedCatalog.exist(namespaceToDatabase(ident.namespace()), ident.name());
  }

  @Override
  public Table createTable(Identifier ident, StructType schema, Transform[] partitions, Map<String, String> properties)
      throws TableAlreadyExistsException, NoSuchNamespaceException {
    String provider = properties.get(PROP_PROVIDER);
    if (StringUtils.isBlank(provider)) {
      throw new IllegalArgumentException("table provider is required.");
    }
    TableFormat format = TableFormat.valueOf(provider.toUpperCase());
    TableCatalog catalog = tableCatalog(format);
    return catalog.createTable(ident, schema, partitions, properties);
  }

  @Override
  public Table alterTable(Identifier ident, TableChange... changes) throws NoSuchTableException {
    try {
      AmoroTable<?> table = unifiedCatalog.loadTable(namespaceToDatabase(ident.namespace()), ident.name());
      return tableCatalog(table.format()).alterTable(ident, changes);
    } catch (com.netease.arctic.NoSuchTableException e) {
      throw new NoSuchTableException(ident);
    }
  }

  @Override
  public boolean dropTable(Identifier ident) {
    String database = namespaceToDatabase(ident.namespace());
    return unifiedCatalog.dropTable(database, ident.name(), false);
  }

  @Override
  public boolean purgeTable(Identifier ident) throws UnsupportedOperationException {
    String database = namespaceToDatabase(ident.namespace());
    return unifiedCatalog.dropTable(database, ident.name(), true);
  }

  @Override
  public void renameTable(Identifier oldIdent, Identifier newIdent)
      throws NoSuchTableException, TableAlreadyExistsException {
    String database = namespaceToDatabase(oldIdent.namespace());
    String tableName = oldIdent.name();
    AmoroTable<?> table = unifiedCatalog.loadTable(database, tableName);
    TableFormat format = table.format();
    TableCatalog catalog = tableCatalog(format);
    catalog.renameTable(oldIdent, newIdent);
  }

  @Override
  public Procedure loadProcedure(Identifier ident) throws NoSuchProcedureException {
    TableCatalog tableCatalog = tableCatalog(TableFormat.MIXED_ICEBERG);
    ProcedureCatalog procedureCatalog = (ProcedureCatalog) tableCatalog;
    return procedureCatalog.loadProcedure(ident);
  }

  private TableCatalog tableCatalog(TableFormat format) {
    return tableCatalogs.computeIfAbsent(format, this::initializeTableCatalog);
  }

  private TableCatalog initializeTableCatalog(TableFormat format) {
    String catalogOptions = format.name().toLowerCase().replace("_", "-") + ".spark-catalog-impl";
    String impl = unifiedCatalog.properties().get(catalogOptions);
    if (StringUtils.isBlank(impl)) {
      impl = defaultTableCatalogImplMap.get(format);
    }
    if (StringUtils.isBlank(impl)) {
      throw new IllegalStateException("failed to initialize spark TableCatalog for format:" + format.name());
    }
    try {
      Class<?> catalogClass = Utils.getContextOrSparkClassLoader().loadClass(impl);
      if (!TableCatalog.class.isAssignableFrom(catalogClass)) {
        throw new IllegalStateException("Plugin class[" + catalogClass.getName() + "] for format: " +
            format.name() + " does not  implement TableCatalog");
      }
      TableCatalog tableCatalog = (TableCatalog) catalogClass.getDeclaredConstructor().newInstance();
      Map<String, String> tableCatalogInitializeProperties = Maps.newHashMap();
      tableCatalogInitializeProperties.putAll(unifiedCatalog.properties());
      tableCatalogInitializeProperties.put("type", unifiedCatalog.metastoreType());
      if (tableCatalog instanceof SupportAuthentication) {
        ((SupportAuthentication) tableCatalog).setAuthenticationContext(unifiedCatalog.authenticationContext());
      }
      tableCatalog.initialize(
          unifiedCatalog.name(), new CaseInsensitiveStringMap(tableCatalogInitializeProperties));
      return tableCatalog;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find catalog plugin class for format: " + format, e);
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException(
          "Failed to invoke public no-arg constructor for format: " + format.name() + " : " + impl, e);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException(
          "Failed to find public no-arg constructor for format: " + format.name() + " : " + impl, e);
    }
  }
}
