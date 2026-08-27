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

import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.util.Preconditions;
import org.apache.iceberg.aliyun.AliyunProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.lance.namespace.LanceNamespace;

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
public class LanceDirectoryV1Catalog extends AbstractLanceCatalog {

  private static final String DEFAULT_DATABASE = "default";
  private static final String STORAGE_ACCESS_KEY_ID = "storage.access_key_id";
  private static final String STORAGE_SECRET_ACCESS_KEY = "storage.secret_access_key";
  private static final String STORAGE_ENDPOINT = "storage.endpoint";

  public LanceDirectoryV1Catalog(String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, buildNamespace(catalogProperties));
  }

  private static LanceNamespace buildNamespace(Map<String, String> catalogProperties) {
    Preconditions.checkArgument(
        catalogProperties != null && !catalogProperties.isEmpty(),
        "Catalog properties must be set.");
    Map<String, String> namespaceProperties = new HashMap<>(catalogProperties);
    String root = namespaceProperties.remove(CatalogMetaProperties.KEY_WAREHOUSE);
    String storageAccessKey =
        removeFirstNonNull(
            namespaceProperties,
            S3FileIOProperties.ACCESS_KEY_ID,
            AliyunProperties.CLIENT_ACCESS_KEY_ID);
    String storageSecretKey =
        removeFirstNonNull(
            namespaceProperties,
            S3FileIOProperties.SECRET_ACCESS_KEY,
            AliyunProperties.CLIENT_ACCESS_KEY_SECRET);

    Preconditions.checkArgument(
        root != null && !root.isEmpty(), "Warehouse must be set in catalogProperties.");
    namespaceProperties.put("manifest_enabled", "false");
    namespaceProperties.put("vend_input_storage_options", "true");
    namespaceProperties.put("root", root);
    if (storageAccessKey != null) {
      namespaceProperties.put(STORAGE_ACCESS_KEY_ID, storageAccessKey);
    }
    if (storageSecretKey != null) {
      namespaceProperties.put(STORAGE_SECRET_ACCESS_KEY, storageSecretKey);
    }
    putStorageEndpointIfPresent(namespaceProperties);
    return LanceNamespace.connect("dir", namespaceProperties, new RootAllocator(Long.MAX_VALUE));
  }

  @Override
  protected List<String> tableId(String database, String table) {
    return Collections.singletonList(table);
  }

  @Override
  protected List<String> tableIdForListTables(String database) {
    return Collections.emptyList();
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
  public void createDatabase(String database) {
    throw new UnsupportedOperationException("Creating Lance databases is not supported.");
  }

  @Override
  public void dropDatabase(String database) {
    throw new UnsupportedOperationException("Dropping Lance databases is not supported.");
  }

  private static String removeFirstNonNull(Map<String, String> properties, String... keys) {
    String value = null;
    for (String key : keys) {
      String removed = properties.remove(key);
      if (value == null && removed != null) {
        value = removed;
      }
    }
    return value;
  }

  private static void putStorageEndpointIfPresent(Map<String, String> properties) {
    if (properties.containsKey(STORAGE_ENDPOINT)) {
      return;
    }

    String endpoint = properties.get(S3FileIOProperties.ENDPOINT);
    if (endpoint == null) {
      endpoint = properties.get(AliyunProperties.OSS_ENDPOINT);
    }
    if (endpoint != null) {
      properties.put(STORAGE_ENDPOINT, endpoint);
    }
  }
}
