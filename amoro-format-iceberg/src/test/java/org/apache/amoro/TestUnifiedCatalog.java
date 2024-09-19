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

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableMetaStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestUnifiedCatalog {

  @ClassRule public static TestAms testAms = new TestAms();

  @Rule public TemporaryFolder warehouse = new TemporaryFolder();

  @Parameterized.Parameters
  public static Object[] parameters() {
    return new Object[] {
      TestedCatalogs.hadoopCatalog(TableFormat.ICEBERG),
    };
  }

  private final CatalogTestHelper testedCatalog;
  private CatalogMeta meta;

  public TestUnifiedCatalog(CatalogTestHelper testedCatalog) {
    this.testedCatalog = testedCatalog;
  }

  @Before
  public void setupCatalogMeta() {
    meta = testedCatalog.buildCatalogMeta(warehouse.getRoot().getAbsolutePath());
    testAms.getAmsHandler().dropCatalog(meta.getCatalogName());
    testAms.getAmsHandler().createCatalog(meta);
  }

  @Test
  public void testCatalogLoader() {
    UnifiedCatalog unifiedCatalog =
        UnifiedCatalogLoader.loadUnifiedCatalog(
            testAms.getServerUrl(), meta.getCatalogName(), Maps.newHashMap());
    validateUnifiedCatalog(unifiedCatalog);
  }

  @Test
  public void testCreateUnifiedCatalog() {
    UnifiedCatalog unifiedCatalog =
        new CommonUnifiedCatalog(
            meta.getCatalogName(),
            meta.getCatalogType(),
            meta.getCatalogProperties(),
            TableMetaStore.EMPTY);
    validateUnifiedCatalog(unifiedCatalog);
  }

  private void validateUnifiedCatalog(UnifiedCatalog unifiedCatalog) {
    Assert.assertNotNull(unifiedCatalog);
    Assert.assertEquals(CommonUnifiedCatalog.class.getName(), unifiedCatalog.getClass().getName());

    unifiedCatalog.createDatabase(TableTestHelper.TEST_DB_NAME);
    Assert.assertEquals(
        Lists.newArrayList(TableTestHelper.TEST_DB_NAME), unifiedCatalog.listDatabases());
    Assert.assertEquals(0, unifiedCatalog.listTables(TableTestHelper.TEST_DB_NAME).size());
    unifiedCatalog.dropDatabase(TableTestHelper.TEST_DB_NAME);
    unifiedCatalog.refresh();
  }
}
