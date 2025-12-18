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

package org.apache.amoro.server.utils;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableMetaStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestInternalTableUtil {

  @BeforeEach
  public void before() {
    // Clear the cache before each test to ensure tests are isolated
    InternalTableUtil.clearFileIOCache();
  }

  @Test
  public void testFileIOCaching() {
    // Create a catalog meta with properties
    CatalogMeta catalogMeta = new CatalogMeta();
    catalogMeta.setCatalogName("test_catalog");
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, "file:/tmp/warehouse");
    properties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE, TableMetaStore.AUTH_METHOD_AK_SK);
    properties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_ACCESS_KEY, "dummy_access_key");
    properties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_SECRET_KEY, "dummy_secret_key");
    catalogMeta.setCatalogProperties(properties);

    // Get the first FileIO instance
    AuthenticatedFileIO fileIO1 = InternalTableUtil.newIcebergFileIo(catalogMeta);

    // Get the second FileIO instance with the same catalog meta
    AuthenticatedFileIO fileIO2 = InternalTableUtil.newIcebergFileIo(catalogMeta);

    // Verify they are the same instance (using the same TableMetaStore)
    Assertions.assertSame(
        fileIO1, fileIO2, "FileIO instances should be the same for the same TableMetaStore");

    // Create a different catalog meta with different name and warehouse path
    // This will result in a different TableMetaStore instance
    CatalogMeta differentCatalogMeta = new CatalogMeta();
    differentCatalogMeta.setCatalogName("different_catalog");
    Map<String, String> differentProperties = new HashMap<>(properties);
    differentProperties.put(CatalogMetaProperties.KEY_WAREHOUSE, "file:/tmp/different_warehouse");
    differentProperties.put(
        CatalogMetaProperties.AUTH_CONFIGS_KEY_TYPE, TableMetaStore.AUTH_METHOD_AK_SK);
    differentProperties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_ACCESS_KEY, "dummy_access_key2");
    differentProperties.put(CatalogMetaProperties.AUTH_CONFIGS_KEY_SECRET_KEY, "dummy_secret_key2");
    differentCatalogMeta.setCatalogProperties(differentProperties);

    // Get a FileIO instance with the different catalog meta
    AuthenticatedFileIO differentFileIO = InternalTableUtil.newIcebergFileIo(differentCatalogMeta);

    // Verify it's a different instance (different TableMetaStore)
    Assertions.assertNotSame(
        fileIO1,
        differentFileIO,
        "FileIO instances should be different for different TableMetaStore instances");
  }
}
