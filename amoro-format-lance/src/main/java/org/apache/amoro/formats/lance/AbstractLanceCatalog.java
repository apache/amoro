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

package org.apache.amoro.formats.lance;

import org.apache.amoro.AlreadyExistsException;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.table.TableIdentifier;
import org.apache.arrow.util.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.InvalidInputException;
import org.lance.namespace.errors.NamespaceAlreadyExistsException;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Base implementation of {@link FormatCatalog} for Lance. */
public abstract class AbstractLanceCatalog implements FormatCatalog {

  protected static final String URI_PROPERTY = "uri";

  private static final String CATALOG_PROPERTY = "catalog-name";

  protected final String catalogName;
  protected final LanceNamespace namespace;
  private final List<String> catalogPrefix;

  protected AbstractLanceCatalog(String catalogName, LanceNamespace namespace) {
    this(catalogName, namespace, Collections.emptyList());
  }

  protected AbstractLanceCatalog(
      String catalogName, LanceNamespace namespace, List<String> catalogPrefix) {
    this.catalogName = catalogName;
    this.namespace = namespace;
    this.catalogPrefix = catalogPrefix;
  }

  protected static List<String> catalogPrefix(
      Map<String, String> catalogProperties, String defaultCatalog) {
    String catalog = catalogProperties == null ? null : catalogProperties.get(CATALOG_PROPERTY);
    if (StringUtils.isEmpty(catalog)) {
      catalog = defaultCatalog;
    }
    return StringUtils.isEmpty(catalog)
        ? Collections.emptyList()
        : Collections.singletonList(catalog);
  }

  /** Composes the namespace table id used to reference a single table. */
  protected List<String> tableId(String database, String table) {
    return concat(catalogPrefix, database, table);
  }

  /** Composes the namespace id used to list tables of a database. */
  protected List<String> namespaceId(String database) {
    return concat(catalogPrefix, database);
  }

  protected static List<String> concat(List<String> prefix, String... parts) {
    List<String> id = new ArrayList<>(prefix);
    Collections.addAll(id, parts);
    return id;
  }

  protected static void validateProperties(Map<String, String> properties) {
    Preconditions.checkArgument(
        properties != null && !properties.isEmpty(), "Catalog properties must be set.");
  }

  protected AmoroTable<?> openTable(String database, String table) {
    namespace.tableExists(new TableExistsRequest().id(tableId(database, table)));
    Dataset dataset =
        Dataset.open().namespaceClient(namespace).tableId(tableId(database, table)).build();
    return new LanceTable(
        TableIdentifier.of(catalogName, database, table), dataset, Collections.emptyMap());
  }

  protected void dropInNamespace(String database, String table, boolean purge) {
    namespace.dropTable(new DropTableRequest().id(tableId(database, table)));
  }

  @Override
  public boolean tableExists(String database, String table) {
    if (!databaseExists(database)) {
      return false;
    }

    try {
      namespace.tableExists(new TableExistsRequest().id(tableId(database, table)));
      return true;
    } catch (TableNotFoundException | InvalidInputException e) {
      return false;
    }
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    validateDatabase(database);

    try {
      return openTable(database, table);
    } catch (TableNotFoundException | InvalidInputException e) {
      throw new NoSuchTableException("Table: " + database + "." + table + " does not exist", e);
    }
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    validateDatabase(database);

    try {
      dropInNamespace(database, table, purge);
      return true;
    } catch (TableNotFoundException | InvalidInputException e) {
      return false;
    }
  }

  @Override
  public List<String> listTables(String database) {
    validateDatabase(database);

    Set<String> tables = new LinkedHashSet<>();
    String pageToken = null;
    do {
      ListTablesRequest request = new ListTablesRequest().id(namespaceId(database));
      if (pageToken != null) {
        request.pageToken(pageToken);
      }

      ListTablesResponse response = namespace.listTables(request);
      if (response == null) {
        break;
      }
      if (response.getTables() != null) {
        tables.addAll(response.getTables());
      }
      pageToken = response.getPageToken();
    } while (StringUtils.isNotEmpty(pageToken));

    return new ArrayList<>(tables);
  }

  @Override
  public List<String> listDatabases() {
    Set<String> namespaces = new LinkedHashSet<>();
    String pageToken = null;
    do {
      ListNamespacesRequest request = new ListNamespacesRequest().id(catalogPrefix);
      if (pageToken != null) {
        request.pageToken(pageToken);
      }

      ListNamespacesResponse response = namespace.listNamespaces(request);
      if (response == null) {
        break;
      }
      if (response.getNamespaces() != null) {
        namespaces.addAll(response.getNamespaces());
      }

      pageToken = response.getPageToken();
    } while (StringUtils.isNotEmpty(pageToken));

    return new ArrayList<>(namespaces);
  }

  protected void validateDatabase(String database) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist");
    }
  }

  @Override
  public boolean databaseExists(String database) {
    try {
      namespace.namespaceExists(new NamespaceExistsRequest().id(namespaceId(database)));
      return true;
    } catch (NamespaceNotFoundException e) {
      return false;
    }
  }

  @Override
  public void createDatabase(String database) {
    try {
      namespace.createNamespace(new CreateNamespaceRequest().id(namespaceId(database)));
    } catch (NamespaceAlreadyExistsException e) {
      throw new AlreadyExistsException("Database: " + database + " already exists", e);
    }
  }

  @Override
  public void dropDatabase(String database) {
    validateDatabase(database);
    try {
      namespace.dropNamespace(new DropNamespaceRequest().id(namespaceId(database)));
    } catch (InvalidInputException e) {
      throw new IllegalStateException("Failed to drop database: " + database, e);
    }
  }
}
