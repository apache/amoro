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

package org.apache.amoro.spark;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.spark.sql.catalyst.analysis.NamespaceAlreadyExistsException;
import org.apache.spark.sql.catalyst.analysis.NoSuchNamespaceException;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException;
import org.apache.spark.sql.connector.catalog.CatalogExtension;
import org.apache.spark.sql.connector.catalog.CatalogPlugin;
import org.apache.spark.sql.connector.catalog.Identifier;
import org.apache.spark.sql.connector.catalog.NamespaceChange;
import org.apache.spark.sql.connector.catalog.SupportsNamespaces;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.catalog.TableChange;
import org.apache.spark.sql.connector.expressions.Transform;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.Map;

/** Base class of spark session catalog. */
public abstract class SessionCatalogBase<T extends TableCatalog & SupportsNamespaces>
    implements SupportsNamespaces, CatalogExtension {

  private static final String[] DEFAULT_NAMESPACE = new String[] {"default"};

  private String catalogName = null;

  private T sessionCatalog = null;

  private CaseInsensitiveStringMap options = null;

  private TableCatalog catalog = null;

  @Override
  @SuppressWarnings("unchecked")
  public void setDelegateCatalog(CatalogPlugin sparkSessionCatalog) {
    if (sparkSessionCatalog instanceof TableCatalog) {
      this.sessionCatalog = (T) sparkSessionCatalog;
    } else {
      throw new IllegalArgumentException("Invalid session catalog: " + sparkSessionCatalog);
    }
  }

  protected T getSessionCatalog() {
    Preconditions.checkNotNull(
        sessionCatalog,
        "Delegated SessionCatalog is missing. "
            + "Please make sure your are replacing Spark's default catalog, named 'spark_catalog'.");
    return sessionCatalog;
  }

  @Override
  public String[] defaultNamespace() {
    return DEFAULT_NAMESPACE;
  }

  @Override
  public void initialize(String name, CaseInsensitiveStringMap options) {
    this.catalogName = name;
    this.options = options;
    try {
      this.catalog = buildTargetCatalog(name, options);
    } catch (Exception e) {
      this.catalog = null;
    }
  }

  @Override
  public String name() {
    return catalogName;
  }

  /**
   * Build the target data-source table catalog instance.
   *
   * @param name spark catalog name
   * @param options catalog initialize options
   * @return data-source table catalog instance.
   */
  protected abstract TableCatalog buildTargetCatalog(String name, CaseInsensitiveStringMap options);

  protected TableCatalog getTargetCatalog() {
    if (catalog == null) {
      this.catalog = buildTargetCatalog(this.catalogName, this.options);
    }
    return this.catalog;
  }

  @Override
  public String[][] listNamespaces() throws NoSuchNamespaceException {
    return getSessionCatalog().listNamespaces();
  }

  @Override
  public String[][] listNamespaces(String[] namespace) throws NoSuchNamespaceException {
    return getSessionCatalog().listNamespaces(namespace);
  }

  @Override
  public Map<String, String> loadNamespaceMetadata(String[] namespace)
      throws NoSuchNamespaceException {
    return getSessionCatalog().loadNamespaceMetadata(namespace);
  }

  @Override
  public void createNamespace(String[] namespace, Map<String, String> metadata)
      throws NamespaceAlreadyExistsException {
    getSessionCatalog().createNamespace(namespace, metadata);
  }

  @Override
  public void alterNamespace(String[] namespace, NamespaceChange... changes)
      throws NoSuchNamespaceException {
    getSessionCatalog().alterNamespace(namespace, changes);
  }

  @Override
  public boolean dropNamespace(String[] namespace) throws NoSuchNamespaceException {
    return getSessionCatalog().dropNamespace(namespace);
  }

  @Override
  public Identifier[] listTables(String[] namespace) throws NoSuchNamespaceException {
    // delegate to the session catalog because all tables share the same namespace
    return getSessionCatalog().listTables(namespace);
  }

  /** Check if the table should be loaded via target catalog. */
  protected abstract boolean isManagedTable(Table table);

  @Override
  public Table loadTable(Identifier ident) throws NoSuchTableException {
    if (isManagedSubTable(ident)) {
      // if it's a sub table identifier, must be a managed table
      return getTargetCatalog().loadTable(ident);
    }

    Table table = getSessionCatalog().loadTable(ident);
    if (isManagedTable(table)) {
      return getTargetCatalog().loadTable(ident);
    }
    return table;
  }

  protected boolean isManagedSubTable(Identifier ident) {
    return false;
  }

  /** Check if the provider is managed by target catalog. */
  protected abstract boolean isManagedProvider(String provider);

  @Override
  public Table createTable(
      Identifier ident, StructType schema, Transform[] partitions, Map<String, String> properties)
      throws TableAlreadyExistsException, NoSuchNamespaceException {
    String provider = properties.get("provider");
    if (isManagedProvider(provider)) {
      return getTargetCatalog().createTable(ident, schema, partitions, properties);
    } else {
      // delegate to the session catalog
      return getSessionCatalog().createTable(ident, schema, partitions, properties);
    }
  }

  @Override
  public Table alterTable(Identifier ident, TableChange... changes) throws NoSuchTableException {
    Table table = getSessionCatalog().loadTable(ident);
    if (isManagedTable(table)) {
      return getTargetCatalog().alterTable(ident, changes);
    } else {
      return getSessionCatalog().alterTable(ident, changes);
    }
  }

  @Override
  public boolean dropTable(Identifier ident) {
    // No need to check table existence to determine which catalog to use.
    // if a table doesn't exist then both are required to return false.
    try {
      Table table = getSessionCatalog().loadTable(ident);
      if (isManagedTable(table)) {
        return getTargetCatalog().dropTable(ident) || getSessionCatalog().dropTable(ident);
      } else {
        return getSessionCatalog().dropTable(ident);
      }
    } catch (NoSuchTableException e) {
      return getSessionCatalog().dropTable(ident);
    }
  }

  @Override
  public boolean purgeTable(Identifier ident) throws UnsupportedOperationException {
    // No need to check table existence to determine which catalog to use.
    // if a table doesn't exist then both are required to return false.
    try {
      Table table = getSessionCatalog().loadTable(ident);
      if (isManagedTable(table)) {
        return getTargetCatalog().purgeTable(ident) || getSessionCatalog().purgeTable(ident);
      } else {
        return getSessionCatalog().purgeTable(ident);
      }
    } catch (NoSuchTableException e) {
      return getSessionCatalog().purgeTable(ident);
    }
  }

  @Override
  public void renameTable(Identifier from, Identifier to)
      throws NoSuchTableException, TableAlreadyExistsException {
    Table table = getSessionCatalog().loadTable(from);
    if (isManagedTable(table)) {
      getTargetCatalog().renameTable(from, to);
    } else {
      getSessionCatalog().renameTable(from, to);
    }
  }
}
