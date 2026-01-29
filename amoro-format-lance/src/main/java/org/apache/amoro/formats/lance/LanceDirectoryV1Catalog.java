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
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableIdentifier;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.Preconditions;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.TableExistsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local catalog implementation for Lance.
 *
 * <p>This catalog treats a single root directory as the backing "metastore". Under the configured
 * root directory, each immediate subdirectory whose name ends with ".lance" is treated as a Lance
 * dataset. All tables live in a single logical database named "default".
 */
public class LanceDirectoryV1Catalog implements FormatCatalog {

  private static final String DEFAULT_DATABASE = "default";
  private final String catalogName;
  private final Map<String, String> namespaceProperties;
  private final LanceNamespace namespace;

  public LanceDirectoryV1Catalog(String catalogName, Map<String, String> catalogProperties) {
    Preconditions.checkArgument(
        catalogProperties != null && !catalogProperties.isEmpty(),
        "Catalog properties must be set.");
    this.catalogName = catalogName;
    this.namespaceProperties = new HashMap<>(catalogProperties);
    String root = namespaceProperties.remove(CatalogMetaProperties.KEY_WAREHOUSE);
    String s3AccessKey = namespaceProperties.remove(S3FileIOProperties.ACCESS_KEY_ID);
    String s3SecretKey = namespaceProperties.remove(S3FileIOProperties.SECRET_ACCESS_KEY);

    Preconditions.checkArgument(
        root != null && !root.isEmpty(), "Warehouse must be set in catalogProperties.");
    this.namespaceProperties.put("manifest_enabled", "false");
    this.namespaceProperties.put("root", root);
    if (s3AccessKey != null) {
      this.namespaceProperties.put("storage.access_key_id", s3AccessKey);
    }
    if (s3SecretKey != null) {
      this.namespaceProperties.put("storage.secret_access_key", s3SecretKey);
    }
    this.namespace = initializeNamespace(new RootAllocator(Long.MAX_VALUE));
  }

  @Override
  public List<String> listDatabases() {
    return Collections.singletonList(DEFAULT_DATABASE);
  }

  @Override
  public boolean databaseExists(String database) {
    return DEFAULT_DATABASE.equals(database);
  }

  @Override
  public boolean tableExists(String database, String table) {
    validateDatabase(database);

    try {
      TableExistsRequest request = new TableExistsRequest().id(Collections.singletonList(table));
      namespace.tableExists(request);
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }

  @Override
  public void createDatabase(String database) {
    throw new UnsupportedOperationException("Creating Lance databases is not supported.");
  }

  @Override
  public void dropDatabase(String database) {
    throw new UnsupportedOperationException("Dropping Lance databases is not supported.");
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    validateDatabase(database);
    if (!tableExists(database, tableName)) {
      throw new NoSuchTableException("Table: " + database + "." + tableName + " does not exist");
    }

    TableIdentifier identifier = TableIdentifier.of(catalogName, database, tableName);
    Dataset dataset =
        Dataset.open().namespace(namespace).tableId(Collections.singletonList(tableName)).build();
    return new LanceTable(identifier, dataset, namespaceProperties);
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    namespace.dropTable(new DropTableRequest().id(Collections.singletonList(table)));
    return true;
  }

  @Override
  public List<String> listTables(String database) {
    validateDatabase(database);
    return listTablesFromNamespace();
  }

  private List<String> listTablesFromNamespace() {
    if (namespace == null) {
      return Collections.emptyList();
    }

    ListTablesRequest request = new ListTablesRequest().id(Collections.emptyList());
    ListTablesResponse response = namespace.listTables(request);
    if (response == null) {
      return Collections.emptyList();
    } else {
      response.getTables();
    }

    return new ArrayList<>(response.getTables());
  }

  private LanceNamespace initializeNamespace(BufferAllocator allocator) {
    return LanceNamespace.connect("dir", namespaceProperties, allocator);
  }

  private void validateDatabase(String database) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist");
    }
  }
}
