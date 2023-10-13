/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.catalog;

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.formats.AmoroCatalogTestHelper;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.server.table.TableServiceTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

public class TableCatalogTestBase extends TableServiceTestBase {

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  private final AmoroCatalogTestHelper<?> amoroCatalogTestHelper;

  private AmoroCatalog amoroCatalog;

  private Object originalCatalog;

  public TableCatalogTestBase(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    this.amoroCatalogTestHelper = amoroCatalogTestHelper;
  }

  @Before
  public void init() throws IOException {
    String path = temp.newFolder().getPath();
    amoroCatalogTestHelper.initWarehouse(path);
    amoroCatalogTestHelper.initHiveConf(TEST_HMS.getHiveConf());
    this.amoroCatalog = amoroCatalogTestHelper.amoroCatalog();
    tableService().createCatalog(amoroCatalogTestHelper.getCatalogMeta());
    this.originalCatalog = amoroCatalogTestHelper.originalCatalog();
  }

  @After
  public void clean() {
    tableService().dropCatalog(amoroCatalogTestHelper.catalogName());
    amoroCatalogTestHelper.clean();
  }

  public AmoroCatalog getAmoroCatalog() {
    return amoroCatalog;
  }

  public Object getOriginalCatalog() {
    return originalCatalog;
  }

  public AmoroCatalogTestHelper<?> getAmoroCatalogTestHelper() {
    return amoroCatalogTestHelper;
  }
}
