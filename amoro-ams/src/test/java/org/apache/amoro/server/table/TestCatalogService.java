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

package org.apache.amoro.server.table;

import org.apache.amoro.TableFormat;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.exception.AlreadyExistsException;
import org.apache.amoro.exception.IllegalMetadataException;
import org.apache.amoro.exception.ObjectNotExistsException;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AMSServiceTestBase;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestCatalogService extends AMSServiceTestBase {
  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  private final CatalogTestHelper catalogTestHelper;

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    return new Object[][] {
      {TestedCatalogs.internalCatalog(TableFormat.MIXED_ICEBERG)},
      {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf())}
    };
  }

  public TestCatalogService(CatalogTestHelper catalogTestHelper) {
    this.catalogTestHelper = catalogTestHelper;
  }

  @Test
  public void testCreateAndDropCatalog() {
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    // test create catalog
    CATALOG_MANAGER.createCatalog(catalogMeta);

    // test create duplicate catalog
    Assert.assertThrows(
        AlreadyExistsException.class, () -> CATALOG_MANAGER.createCatalog(catalogMeta));

    // test get catalog
    CatalogMeta readCatalogMeta = CATALOG_MANAGER.getCatalogMeta(catalogMeta.getCatalogName());
    Assert.assertEquals(catalogMeta, readCatalogMeta);

    // test get catalog list
    List<CatalogMeta> catalogMetas = CATALOG_MANAGER.listCatalogMetas();
    Assert.assertEquals(1, catalogMetas.size());
    Assert.assertEquals(
        catalogMeta,
        catalogMetas.stream()
            .filter(meta -> meta.getCatalogName().equals(catalogMeta.getCatalogName()))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("Cannot find expect catalog")));

    // test catalogExist
    Assert.assertTrue(CATALOG_MANAGER.catalogExist(catalogMeta.getCatalogName()));

    // test drop catalog
    CATALOG_MANAGER.dropCatalog(catalogMeta.getCatalogName());

    // test drop not existed catalog
    Assert.assertThrows(
        ObjectNotExistsException.class,
        () -> CATALOG_MANAGER.getCatalogMeta(catalogMeta.getCatalogName()));

    Assert.assertFalse(CATALOG_MANAGER.catalogExist(catalogMeta.getCatalogName()));
  }

  @Test
  public void testUpdateCatalog() {
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    CATALOG_MANAGER.createCatalog(catalogMeta);

    CatalogMeta updateCatalogMeta = new CatalogMeta(catalogMeta);
    updateCatalogMeta.getCatalogProperties().put("k2", "V2");
    updateCatalogMeta.getCatalogProperties().put("k3", "v3");
    CATALOG_MANAGER.updateCatalog(updateCatalogMeta);
    CatalogMeta getCatalogMeta = CATALOG_MANAGER.getCatalogMeta(catalogMeta.getCatalogName());
    Assert.assertEquals("V2", getCatalogMeta.getCatalogProperties().get("k2"));
    Assert.assertEquals("v3", getCatalogMeta.getCatalogProperties().get("k3"));
    Assert.assertEquals(
        updateCatalogMeta, CATALOG_MANAGER.getCatalogMeta(catalogMeta.getCatalogName()));

    // test update catalog type
    final CatalogMeta updateCatalogMeta2 = new CatalogMeta(updateCatalogMeta);
    updateCatalogMeta2.setCatalogType(CatalogMetaProperties.CATALOG_TYPE_CUSTOM);
    Assert.assertThrows(
        IllegalMetadataException.class, () -> CATALOG_MANAGER.updateCatalog(updateCatalogMeta2));

    // test update unknown catalog
    CATALOG_MANAGER.dropCatalog(catalogMeta.getCatalogName());
    Assert.assertThrows(
        ObjectNotExistsException.class, () -> CATALOG_MANAGER.updateCatalog(catalogMeta));
  }

  @Test
  public void testDropCatalogWithDatabase() {
    Assume.assumeTrue(catalogTestHelper.tableFormat().equals(TableFormat.MIXED_ICEBERG));
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta("/tmp");
    CATALOG_MANAGER.createCatalog(catalogMeta);
    InternalCatalog catalog = CATALOG_MANAGER.getInternalCatalog(catalogMeta.getCatalogName());
    catalog.createDatabase("test_db");
    Assert.assertThrows(
        IllegalMetadataException.class,
        () -> CATALOG_MANAGER.dropCatalog(catalogMeta.getCatalogName()));
    catalog.dropDatabase("test_db");
    CATALOG_MANAGER.dropCatalog(catalogMeta.getCatalogName());
  }
}
