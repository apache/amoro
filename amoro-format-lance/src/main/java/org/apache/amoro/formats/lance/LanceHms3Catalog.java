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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableMetaStore;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/** Read-only Lance catalog backed by a Hive 3 metastore. */
public class LanceHms3Catalog implements FormatCatalog, AutoCloseable {

  private static final String HIVE3_NAMESPACE_CLASS = "org.lance.namespace.hive3.Hive3Namespace";
  private static final int PAGE_SIZE = 1_000;

  private final String catalogName;
  private final TableMetaStore metaStore;
  private final BufferAllocator allocator;
  private final LanceNamespace delegateNamespace;
  private final LanceNamespace datasetNamespace;

  public LanceHms3Catalog(
      String catalogName, Map<String, String> properties, TableMetaStore metaStore) {
    BufferAllocator newAllocator = new RootAllocator(Long.MAX_VALUE);
    LanceNamespace newNamespace;
    try {
      newNamespace =
          createHive3Namespace(
              properties == null ? Collections.emptyMap() : properties, metaStore, newAllocator);
    } catch (RuntimeException | Error e) {
      newAllocator.close();
      throw e;
    }

    this.catalogName = catalogName;
    this.metaStore = metaStore;
    this.allocator = newAllocator;
    this.delegateNamespace = newNamespace;
    this.datasetNamespace =
        withStorageOptions(
            newNamespace, new LanceStorageOptionsProvider(metaStore.getConfiguration()));
  }

  LanceHms3Catalog(
      String catalogName,
      TableMetaStore metaStore,
      BufferAllocator allocator,
      LanceNamespace delegateNamespace,
      LanceStorageOptionsProvider storageOptionsProvider) {
    this.catalogName = catalogName;
    this.metaStore = metaStore;
    this.allocator = allocator;
    this.delegateNamespace = delegateNamespace;
    this.datasetNamespace = withStorageOptions(delegateNamespace, storageOptionsProvider);
  }

  @Override
  public List<String> listDatabases() {
    TreeSet<String> databases = new TreeSet<>();
    String pageToken = null;
    do {
      ListNamespacesRequest request =
          new ListNamespacesRequest()
              .id(Collections.singletonList(catalogName))
              .limit(PAGE_SIZE)
              .pageToken(pageToken);
      ListNamespacesResponse response =
          metaStore.doAs(() -> delegateNamespace.listNamespaces(request));
      if (response == null) {
        break;
      }
      if (response.getNamespaces() != null) {
        databases.addAll(response.getNamespaces());
      }
      pageToken = nextPageToken(pageToken, response.getPageToken());
    } while (pageToken != null);
    return new ArrayList<>(databases);
  }

  @Override
  public boolean databaseExists(String database) {
    try {
      NamespaceExistsRequest request = new NamespaceExistsRequest().id(identifier(database));
      metaStore.doAs(
          () -> {
            delegateNamespace.namespaceExists(request);
            return null;
          });
      return true;
    } catch (NamespaceNotFoundException e) {
      return false;
    }
  }

  @Override
  public boolean tableExists(String database, String table) {
    try {
      TableExistsRequest request = new TableExistsRequest().id(identifier(database, table));
      metaStore.doAs(
          () -> {
            delegateNamespace.tableExists(request);
            return null;
          });
      return true;
    } catch (NamespaceNotFoundException | TableNotFoundException e) {
      return false;
    }
  }

  @Override
  public void createDatabase(String database) {
    throw readOnly();
  }

  @Override
  public void dropDatabase(String database) {
    throw readOnly();
  }

  @Override
  public AmoroTable<?> loadTable(String database, String table) {
    if (!tableExists(database, table)) {
      throw new NoSuchTableException(
          "Lance table " + catalogName + "." + database + "." + table + " does not exist");
    }

    List<String> tableId = identifier(database, table);
    try {
      Dataset dataset =
          metaStore.doAs(
              () ->
                  Dataset.open()
                      .allocator(allocator)
                      .namespaceClient(datasetNamespace)
                      .tableId(tableId)
                      .build());
      return new LanceTable(
          TableIdentifier.of(catalogName, database, table), dataset, Collections.emptyMap());
    } catch (RuntimeException e) {
      throw new IllegalStateException("Failed to open Lance table " + String.join(".", tableId), e);
    }
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    throw readOnly();
  }

  @Override
  public List<String> listTables(String database) {
    if (!databaseExists(database)) {
      return Collections.emptyList();
    }

    TreeSet<String> tables = new TreeSet<>();
    String pageToken = null;
    do {
      ListTablesRequest request =
          new ListTablesRequest()
              .id(identifier(database))
              // HMS is the metadata source of truth. Do not make table discovery depend on an
              // object-store probe, which runs before temporary TOS credentials are vended.
              .includeDeclared(true)
              .limit(PAGE_SIZE)
              .pageToken(pageToken);
      ListTablesResponse response = metaStore.doAs(() -> delegateNamespace.listTables(request));
      if (response == null) {
        break;
      }
      if (response.getTables() != null) {
        tables.addAll(response.getTables());
      }
      pageToken = nextPageToken(pageToken, response.getPageToken());
    } while (pageToken != null);
    return new ArrayList<>(tables);
  }

  @Override
  public void close() {
    try {
      if (delegateNamespace instanceof AutoCloseable) {
        ((AutoCloseable) delegateNamespace).close();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Failed to close Lance HMS3 namespace", e);
    } finally {
      allocator.close();
    }
  }

  private static LanceNamespace createHive3Namespace(
      Map<String, String> properties, TableMetaStore metaStore, BufferAllocator allocator) {
    try {
      Class<?> namespaceClass = Class.forName(HIVE3_NAMESPACE_CLASS);
      Object namespaceObject = namespaceClass.getDeclaredConstructor().newInstance();
      if (!(namespaceObject instanceof LanceNamespace)) {
        throw new IllegalStateException(
            HIVE3_NAMESPACE_CLASS + " does not implement the configured LanceNamespace API");
      }
      namespaceClass
          .getMethod("setHadoopConf", Configuration.class)
          .invoke(namespaceObject, metaStore.getConfiguration());
      LanceNamespace namespace = (LanceNamespace) namespaceObject;
      namespace.initialize(new HashMap<>(properties), allocator);
      return namespace;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Lance HMS3 support requires the lance-namespace-hive3 runtime", e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Failed to initialize Lance HMS3 namespace", e.getCause());
    } catch (ReflectiveOperationException | LinkageError e) {
      throw new IllegalStateException(
          "Lance HMS3 namespace is unavailable with the current Hive runtime", e);
    }
  }

  private static LanceNamespace withStorageOptions(
      LanceNamespace delegate, LanceStorageOptionsProvider storageOptionsProvider) {
    return (LanceNamespace)
        Proxy.newProxyInstance(
            LanceNamespace.class.getClassLoader(),
            new Class<?>[] {LanceNamespace.class},
            (proxy, method, args) -> {
              try {
                Object result = method.invoke(delegate, args);
                if (result instanceof DescribeTableResponse) {
                  DescribeTableResponse response = (DescribeTableResponse) result;
                  String location = response.getLocation();
                  Map<String, String> storageOptions = new HashMap<>();
                  if (response.getStorageOptions() != null) {
                    storageOptions.putAll(response.getStorageOptions());
                  }
                  storageOptions.putAll(storageOptionsProvider.storageOptions(location));
                  response.setLocation(storageOptionsProvider.datasetLocation(location));
                  response.setStorageOptions(storageOptions);
                }
                return result;
              } catch (InvocationTargetException e) {
                throw e.getCause();
              }
            });
  }

  private List<String> identifier(String... parts) {
    List<String> identifier = new ArrayList<>(parts.length + 1);
    identifier.add(catalogName);
    Collections.addAll(identifier, parts);
    return identifier;
  }

  private static String nextPageToken(String previous, String next) {
    if (StringUtils.isBlank(next) || next.equals(previous)) {
      return null;
    }
    return next;
  }

  private static UnsupportedOperationException readOnly() {
    return new UnsupportedOperationException("Lance HMS3 catalog is read-only");
  }
}
