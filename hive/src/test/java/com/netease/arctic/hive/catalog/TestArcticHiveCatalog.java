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

package com.netease.arctic.hive.catalog;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TestArcticCatalog;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestArcticHiveCatalog extends TestArcticCatalog {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestArcticHiveCatalog(CatalogTestHelper catalogTestHelper, TableFormat format) {
    super(catalogTestHelper, format);
  }

  @Parameterized.Parameters(name = "catalogType={0}, testFormat = {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new HiveCatalogTestHelper(TEST_HMS.getHiveConf(), TableFormat.MIXED_HIVE), TableFormat.MIXED_HIVE},
        {new HiveCatalogTestHelper(TEST_HMS.getHiveConf(), TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
            TableFormat.ICEBERG},
        {new HiveCatalogTestHelper(TEST_HMS.getHiveConf(), TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG),
            TableFormat.MIXED_ICEBERG}};
  }

  @Test
  public void testDropTableButNotDropHiveTable() throws MetaException {
    if (getCatalog() instanceof ArcticHiveCatalog) {
      getCatalog().createDatabase(TableTestHelper.TEST_DB_NAME);
      createTestTable();
      getCatalog().dropTable(TableIdentifier.of(getCatalog().name(),
          TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME), false);
      Assert.assertTrue(TEST_HMS.getHiveClient().getAllTables(TableTestHelper.TEST_DB_NAME)
          .contains(TableTestHelper.TEST_TABLE_NAME));
    }
  }

  @After
  public void cleanUp() throws TException {
    if (getCatalog() instanceof ArcticHiveCatalog) {
      TEST_HMS.getHiveClient().dropTable(TableTestHelper.TEST_DB_NAME, TableTestHelper.TEST_TABLE_NAME,
          true, true);
    }
  }
}
