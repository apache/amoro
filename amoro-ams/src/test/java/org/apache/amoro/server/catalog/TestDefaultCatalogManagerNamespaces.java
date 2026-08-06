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
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.CatalogMetaMapper;
import org.apache.amoro.server.persistence.mapper.NamespaceAllowlistMapper;
import org.apache.amoro.server.table.DerbyPersistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TestDefaultCatalogManagerNamespaces {

  private static final TestPersistence PERSISTENCE = new TestPersistence();
  private static final DirectDbAccess DB = new DirectDbAccess();

  @BeforeAll
  public static void initializePersistence() {
    PERSISTENCE.initialize();
  }

  @AfterEach
  public void cleanUp() {
    DB.deleteCatalog("tenant-a@las");
    DB.deleteCatalog("tenant-b@las");
    DB.deleteNamespace("tenant-a");
    DB.deleteNamespace("tenant-b");
  }

  @Test
  public void testNamespaceSupportIsEnabledByDefault() {
    DefaultCatalogManager manager = new DefaultCatalogManager(new Configurations());

    Assertions.assertTrue(manager.supportNamespace());
    Assertions.assertTrue(manager.listNamespaces().isEmpty());
  }

  @Test
  public void testDisabledNamespaceRemainsCompatible() {
    Configurations configurations = new Configurations();
    configurations.setBoolean(AmoroManagementConf.CATALOG_NAMESPACE_ENABLED, false);
    DefaultCatalogManager manager = new DefaultCatalogManager(configurations);

    Assertions.assertFalse(manager.supportNamespace());
    Assertions.assertEquals(Collections.singletonList("default"), manager.listNamespaces());
    Assertions.assertEquals(manager.listCatalogMetas(), manager.listCatalogMetas("default"));
    Assertions.assertTrue(manager.listCatalogMetas("unknown").isEmpty());
  }

  @Test
  public void testEnabledNamespaceAllowlistAndCatalogFiltering() {
    Configurations configurations = new Configurations();
    configurations.setBoolean(AmoroManagementConf.CATALOG_NAMESPACE_ENABLED, true);
    DefaultCatalogManager manager = new DefaultCatalogManager(configurations);

    Assertions.assertTrue(manager.listNamespaces().isEmpty());
    manager.addNamespace("tenant-b");
    manager.addNamespace("tenant-a");
    manager.addNamespace("tenant-a");
    Assertions.assertEquals(Arrays.asList("tenant-a", "tenant-b"), manager.listNamespaces());

    DB.insertCatalog(catalog("tenant-a@las", "tenant-a"));
    DB.insertCatalog(catalog("tenant-b@las", "tenant-b"));

    Assertions.assertEquals(
        Collections.singletonList("tenant-a@las"),
        catalogNames(manager.listCatalogMetas("tenant-a")));
    Assertions.assertEquals(
        Collections.singletonList("tenant-b@las"),
        catalogNames(manager.listCatalogMetas("tenant-b")));
    Assertions.assertTrue(manager.listCatalogMetas("unknown").isEmpty());
    Assertions.assertEquals(2, manager.listCatalogMetas().size());

    manager.removeNamespace("tenant-a");
    manager.removeNamespace("tenant-a");
    Assertions.assertEquals(Collections.singletonList("tenant-b"), manager.listNamespaces());
    Assertions.assertTrue(manager.listCatalogMetas("tenant-a").isEmpty());
  }

  private static CatalogMeta catalog(String catalogName, String namespace) {
    HashMap<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.NAMESPACE, namespace);
    return CatalogTestHelpers.buildCatalogMeta(
        catalogName, CatalogMetaProperties.CATALOG_TYPE_HIVE, properties, TableFormat.ICEBERG);
  }

  private static List<String> catalogNames(List<CatalogMeta> catalogs) {
    return catalogs.stream().map(CatalogMeta::getCatalogName).collect(Collectors.toList());
  }

  private static class TestPersistence extends DerbyPersistence {
    void initialize() {
      // Class initialization creates the shared Derby schema used by persistence tests.
    }
  }

  private static class DirectDbAccess extends PersistentBase {
    void insertCatalog(CatalogMeta catalogMeta) {
      doAs(CatalogMetaMapper.class, mapper -> mapper.insertCatalog(catalogMeta));
    }

    void deleteCatalog(String catalogName) {
      doAs(CatalogMetaMapper.class, mapper -> mapper.deleteCatalog(catalogName));
    }

    void deleteNamespace(String namespace) {
      doAs(NamespaceAllowlistMapper.class, mapper -> mapper.deleteNamespace(namespace));
    }
  }
}
