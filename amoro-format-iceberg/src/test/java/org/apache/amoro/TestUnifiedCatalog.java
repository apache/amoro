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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.stream.Stream;

public class TestUnifiedCatalog {

  private static final TestAms testAms = new TestAms();

  @TempDir public Path warehouse;

  @BeforeAll
  public static void startTestAms() throws Exception {
    testAms.before();
  }

  @AfterAll
  public static void stopTestAms() {
    testAms.after();
  }

  public static Stream<Arguments> parameters() {
    return Stream.of(Arguments.of(TestedCatalogs.hadoopCatalog(TableFormat.ICEBERG)));
  }

  private CatalogMeta setupCatalogMeta(CatalogTestHelper testedCatalog) {
    CatalogMeta meta = testedCatalog.buildCatalogMeta(warehouse.toFile().getAbsolutePath());
    testAms.getAmsHandler().dropCatalog(meta.getCatalogName());
    testAms.getAmsHandler().createCatalog(meta);
    return meta;
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testCatalogLoader(CatalogTestHelper testedCatalog) {
    CatalogMeta meta = setupCatalogMeta(testedCatalog);
    UnifiedCatalog unifiedCatalog =
        UnifiedCatalogLoader.loadUnifiedCatalog(
            testAms.getServerUrl(), meta.getCatalogName(), Maps.newHashMap());
    validateUnifiedCatalog(unifiedCatalog);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testCreateUnifiedCatalog(CatalogTestHelper testedCatalog) {
    CatalogMeta meta = setupCatalogMeta(testedCatalog);
    UnifiedCatalog unifiedCatalog =
        new CommonUnifiedCatalog(
            meta.getCatalogName(),
            meta.getCatalogType(),
            meta.getCatalogProperties(),
            TableMetaStore.EMPTY);
    validateUnifiedCatalog(unifiedCatalog);
  }

  private void validateUnifiedCatalog(UnifiedCatalog unifiedCatalog) {
    Assertions.assertNotNull(unifiedCatalog);
    Assertions.assertEquals(
        CommonUnifiedCatalog.class.getName(), unifiedCatalog.getClass().getName());

    unifiedCatalog.createDatabase(TableTestHelper.TEST_DB_NAME);
    Assertions.assertEquals(
        Lists.newArrayList(TableTestHelper.TEST_DB_NAME), unifiedCatalog.listDatabases());
    Assertions.assertEquals(0, unifiedCatalog.listTables(TableTestHelper.TEST_DB_NAME).size());
    unifiedCatalog.dropDatabase(TableTestHelper.TEST_DB_NAME);
    unifiedCatalog.refresh();
  }
}
