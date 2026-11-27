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

package org.apache.amoro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCommonUnifiedCatalog {

  private CatalogMeta serverMeta;
  private AtomicInteger initializations;

  @BeforeEach
  public void setup() {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.TABLE_FORMATS, TableFormat.ICEBERG.name());
    properties.put("probe-key", "server");
    serverMeta =
        new CatalogMeta(
            "test",
            CatalogMetaProperties.CATALOG_TYPE_HADOOP,
            Collections.emptyMap(),
            Collections.emptyMap(),
            properties);
    initializations = new AtomicInteger();
  }

  @Test
  public void testRefreshUnchangedMetadataPreservesClientOverride() {
    CommonUnifiedCatalog catalog = createCatalog(Collections.singletonMap("probe-key", "client"));
    assertEquals("client", catalog.properties().get("probe-key"));
    assertEquals(1, initializations.get());

    catalog.refresh();
    catalog.refresh();

    assertEquals("client", catalog.properties().get("probe-key"));
    assertEquals(1, initializations.get());
    assertEquals("server", serverMeta.getCatalogProperties().get("probe-key"));
  }

  @Test
  public void testRefreshOverriddenServerPropertyDoesNotReinitializeCatalogs() {
    CommonUnifiedCatalog catalog = createCatalog(Collections.singletonMap("probe-key", "client"));
    serverMeta.putToCatalogProperties("probe-key", "updated-server");

    catalog.refresh();

    assertEquals("client", catalog.properties().get("probe-key"));
    assertEquals(1, initializations.get());
  }

  @Test
  public void testRefreshChangedServerPropertyPreservesClientOverride() {
    serverMeta.putToCatalogProperties("server-only", "before");
    CommonUnifiedCatalog catalog = createCatalog(Collections.singletonMap("probe-key", "client"));
    assertEquals("before", catalog.properties().get("server-only"));
    serverMeta.putToCatalogProperties("server-only", "after");

    catalog.refresh();

    assertEquals("after", catalog.properties().get("server-only"));
    assertEquals("client", catalog.properties().get("probe-key"));
    assertEquals(2, initializations.get());

    catalog.refresh();

    assertEquals("client", catalog.properties().get("probe-key"));
    assertEquals(2, initializations.get());
  }

  @Test
  public void testRefreshPreservesClientOnlyProperty() {
    CommonUnifiedCatalog catalog = createCatalog(Collections.singletonMap("client-only", "client"));
    assertEquals("client", catalog.properties().get("client-only"));

    catalog.refresh();

    assertEquals("client", catalog.properties().get("client-only"));
    assertEquals(1, initializations.get());

    serverMeta.putToCatalogProperties("probe-key", "updated-server");
    catalog.refresh();

    assertEquals("client", catalog.properties().get("client-only"));
    assertEquals("updated-server", catalog.properties().get("probe-key"));
    assertEquals(2, initializations.get());
  }

  @Test
  public void testRefreshWithoutClientPropertiesAppliesServerChanges() {
    CommonUnifiedCatalog catalog = createCatalog(Collections.emptyMap());

    catalog.refresh();

    assertEquals("server", catalog.properties().get("probe-key"));
    assertEquals(1, initializations.get());

    serverMeta.putToCatalogProperties("probe-key", "updated-server");
    catalog.refresh();

    assertEquals("updated-server", catalog.properties().get("probe-key"));
    assertEquals(2, initializations.get());
  }

  private CommonUnifiedCatalog createCatalog(Map<String, String> clientProperties) {
    return new CommonUnifiedCatalog(serverMeta::deepCopy, clientProperties) {
      @Override
      protected void initializeFormatCatalogs() {
        initializations.incrementAndGet();
      }
    };
  }
}
