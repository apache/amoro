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

import org.apache.amoro.table.TableMetaStore;
import org.apache.arrow.memory.RootAllocator;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TestLanceHms3Catalog {

  @Test
  public void testPhysicalCatalogScopesDatabaseAndTableRequests() {
    TestingNamespace namespace = new TestingNamespace();
    TableMetaStore metaStore = metaStore();
    try (LanceHms3Catalog catalog =
        new LanceHms3Catalog(
            "tenant@catalog",
            metaStore,
            new RootAllocator(Long.MAX_VALUE),
            namespace,
            new LanceStorageOptionsProvider(metaStore.getConfiguration()))) {
      Assertions.assertEquals(Arrays.asList("db_a", "db_b"), catalog.listDatabases());
      Assertions.assertEquals(Arrays.asList("table_a", "table_b"), catalog.listTables("db_a"));
      Assertions.assertTrue(catalog.databaseExists("db_a"));
      Assertions.assertFalse(catalog.databaseExists("missing"));
      Assertions.assertTrue(catalog.tableExists("db_a", "table_a"));
      Assertions.assertFalse(catalog.tableExists("db_a", "missing"));

      Assertions.assertEquals(Collections.singletonList("tenant@catalog"), namespace.databaseId);
      Assertions.assertEquals(Arrays.asList("tenant@catalog", "db_a"), namespace.tableListId);
      Assertions.assertTrue(namespace.includeDeclared);
      Assertions.assertEquals(
          Arrays.asList("tenant@catalog", "db_a", "missing"), namespace.tableExistsId);
    }
    Assertions.assertTrue(namespace.closed);
  }

  @Test
  public void testMutationsAreRejected() {
    TestingNamespace namespace = new TestingNamespace();
    TableMetaStore metaStore = metaStore();
    try (LanceHms3Catalog catalog =
        new LanceHms3Catalog(
            "tenant@catalog",
            metaStore,
            new RootAllocator(Long.MAX_VALUE),
            namespace,
            new LanceStorageOptionsProvider(metaStore.getConfiguration()))) {
      Assertions.assertThrows(
          UnsupportedOperationException.class, () -> catalog.createDatabase("db"));
      Assertions.assertThrows(
          UnsupportedOperationException.class, () -> catalog.dropDatabase("db"));
      Assertions.assertThrows(
          UnsupportedOperationException.class, () -> catalog.dropTable("db", "table", false));
    }
  }

  private static TableMetaStore metaStore() {
    return TableMetaStore.builder().withConfiguration(new Configuration(false)).build();
  }

  private static class TestingNamespace implements LanceNamespace, AutoCloseable {
    private List<String> databaseId;
    private List<String> tableListId;
    private List<String> tableExistsId;
    private boolean includeDeclared;
    private boolean closed;

    @Override
    public void initialize(
        Map<String, String> properties, org.apache.arrow.memory.BufferAllocator allocator) {}

    @Override
    public String namespaceId() {
      return "test";
    }

    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
      databaseId = request.getId();
      return new ListNamespacesResponse().namespaces(new HashSet<>(Arrays.asList("db_b", "db_a")));
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
      if (request.getId().contains("missing")) {
        throw new NamespaceNotFoundException("missing");
      }
    }

    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
      tableListId = request.getId();
      includeDeclared = Boolean.TRUE.equals(request.getIncludeDeclared());
      return new ListTablesResponse().tables(new HashSet<>(Arrays.asList("table_b", "table_a")));
    }

    @Override
    public void tableExists(TableExistsRequest request) {
      tableExistsId = request.getId();
      if (request.getId().contains("missing")) {
        throw new TableNotFoundException("missing");
      }
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
