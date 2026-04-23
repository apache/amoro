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

package org.apache.amoro.server.catalog;

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelpers;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AMSManagerTestBase;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link DefaultCatalogManager} graceful handling of catalog initialization failures.
 * Verifies that:
 *
 * <ol>
 *   <li>A single catalog failure does not crash the entire AMS during startup
 *   <li>Failed catalogs can still be updated via the API to allow operational recovery
 * </ol>
 */
public class TestDefaultCatalogManagerRecovery extends AMSManagerTestBase {

  private static final String TEST_CATALOG_NAME = "test_failed_rest_catalog";

  /** Helper to directly access the database, bypassing CatalogManager and buildServerCatalog. */
  static class DirectDbAccess extends PersistentBase {
    void insertCatalog(CatalogMeta meta) {
      doAs(CatalogMetaMapper.class, mapper -> mapper.insertCatalog(meta));
    }

    void deleteCatalog(String name) {
      doAs(CatalogMetaMapper.class, mapper -> mapper.deleteCatalog(name));
    }
  }

  private static final DirectDbAccess DB = new DirectDbAccess();

  private CatalogMeta buildUnreachableRestCatalog() {
    Map<String, String> properties = new HashMap<>();
    properties.put("uri", "http://localhost:1/nonexistent");
    return CatalogTestHelpers.buildCatalogMeta(
        TEST_CATALOG_NAME,
        CatalogMetaProperties.CATALOG_TYPE_REST,
        properties,
        TableFormat.ICEBERG);
  }

  @After
  public void cleanup() {
    CATALOG_MANAGER.removeServerCatalog(TEST_CATALOG_NAME);
    try {
      DB.deleteCatalog(TEST_CATALOG_NAME);
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * Verifies that DefaultCatalogManager initialization does not crash when a catalog in the
   * database fails to load (e.g., an unreachable REST endpoint). The failed catalog should be
   * skipped and other catalogs should still be available.
   */
  @Test
  public void testInitializationSkipsFailedCatalog() {
    // Insert a REST catalog with an unreachable endpoint directly into DB
    DB.insertCatalog(buildUnreachableRestCatalog());

    // Creating a new DefaultCatalogManager should NOT throw — the failed catalog is skipped
    DefaultCatalogManager manager = new DefaultCatalogManager(new Configurations());

    // The manager should be functional — the catalog exists in DB but was skipped during init
    Assert.assertTrue(manager.catalogExist(TEST_CATALOG_NAME));
    // Verify that the catalog metadata is still readable from DB
    Assert.assertNotNull(manager.getCatalogMeta(TEST_CATALOG_NAME));
  }

  /**
   * Simulates a REST catalog that failed to initialize (exists in DB but not in serverCatalogMap),
   * then verifies that updateCatalog() can update its metadata without throwing. This is the core
   * recovery scenario — operators must be able to fix catalog configuration (e.g., correcting a
   * URI) from the dashboard without restarting AMS.
   */
  @Test
  public void testUpdateCatalogThatFailedToInitialize() {
    // Insert directly into DB — simulates the state after init try-catch skips a failed catalog
    CatalogMeta meta = buildUnreachableRestCatalog();
    DB.insertCatalog(meta);

    Assert.assertTrue(CATALOG_MANAGER.catalogExist(TEST_CATALOG_NAME));

    // Update the catalog metadata — this must succeed
    CatalogMeta updatedMeta = new CatalogMeta(meta);
    updatedMeta.getCatalogProperties().put("uri", "http://localhost:2/also-unreachable");
    updatedMeta.getCatalogProperties().put("new-property", "new-value");
    CATALOG_MANAGER.updateCatalog(updatedMeta);

    // Verify the update was persisted
    CatalogMeta readMeta = CATALOG_MANAGER.getCatalogMeta(TEST_CATALOG_NAME);
    Assert.assertEquals(
        "http://localhost:2/also-unreachable", readMeta.getCatalogProperties().get("uri"));
    Assert.assertEquals("new-value", readMeta.getCatalogProperties().get("new-property"));
  }
}
