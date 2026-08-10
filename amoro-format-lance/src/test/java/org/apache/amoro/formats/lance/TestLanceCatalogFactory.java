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

import org.apache.amoro.FormatCatalog;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.table.TableMetaStore;
import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestLanceCatalogFactory {

  @Test
  public void testHiveMetastoreUsesHms3Catalog() throws Exception {
    Configuration configuration = new Configuration(false);
    configuration.set("hive.metastore.uris", "thrift://127.0.0.1:1");
    TableMetaStore metaStore = TableMetaStore.builder().withConfiguration(configuration).build();

    FormatCatalog catalog =
        new LanceCatalogFactory()
            .create(
                "tenant@catalog",
                CatalogMetaProperties.CATALOG_TYPE_HIVE,
                Collections.emptyMap(),
                metaStore);

    Assertions.assertInstanceOf(LanceHms3Catalog.class, catalog);
    ((AutoCloseable) catalog).close();
  }

  @Test
  public void testFilesystemMetastoreKeepsDirectoryCatalog() {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.KEY_WAREHOUSE, "file:/tmp/lance-catalog");

    FormatCatalog catalog =
        new LanceCatalogFactory()
            .create(
                "filesystem-catalog",
                CatalogMetaProperties.CATALOG_TYPE_FILESYSTEM,
                properties,
                TableMetaStore.EMPTY);

    Assertions.assertInstanceOf(LanceDirectoryV1Catalog.class, catalog);
  }
}
