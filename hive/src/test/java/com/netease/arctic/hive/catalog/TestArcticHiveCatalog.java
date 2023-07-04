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
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TestBasicArcticCatalog;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

import static com.netease.arctic.BasicTableTestHelper.PRIMARY_KEY_SPEC;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_FLAG;
import static com.netease.arctic.hive.HiveTableProperties.ARCTIC_TABLE_ROOT_LOCATION;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.HIVE_SPEC;
import static com.netease.arctic.hive.catalog.HiveTableTestHelper.HIVE_TABLE_SCHEMA;

@RunWith(Parameterized.class)
public class TestArcticHiveCatalog extends TestBasicArcticCatalog {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestArcticHiveCatalog(CatalogTestHelper catalogTestHelper) {
    super(catalogTestHelper);
  }

  @Parameterized.Parameters(name = "testFormat = {0}")
  public static Object[] parameters() {
    return new Object[] {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
                         new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf())};
  }

  private void checkTableArcticPropertiesAndDropTable(ArcticCatalog catalog, TableIdentifier tableIdentifier)
          throws TException{
    String dbName = tableIdentifier.getDatabase();
    String tbl = tableIdentifier.getTableName();
    Map<String,String> tableParameter =  TEST_HMS.getHiveClient()
            .getTable(dbName, tbl).getParameters();

    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_ROOT_LOCATION));
    Assert.assertTrue(tableParameter.get(ARCTIC_TABLE_ROOT_LOCATION).endsWith(tbl));
    Assert.assertTrue(tableParameter.containsKey(ARCTIC_TABLE_FLAG));

    catalog.dropTable(tableIdentifier, true);
    TEST_AMS.getAmsHandler().getTableCommitMetas().remove(tableIdentifier.buildTableIdentifier());
  }

  @Test
  public void testArcticTableRootLocation() throws TException {
    ArcticCatalog hiveCatalog = getCatalog();
    // iceberg table doesn't support to set hive's property
    Assume.assumeTrue(TableFormat.MIXED_HIVE.equals(getTestFormat()));

    String dbName = "arctic_location_test";
    String unkeyedTable = "unkeyed_test_hive_table_tmp";
    String keyedTable = "keyed_test_hive_table_tmp";
    boolean createDatabase = false;
    createDatabase = !(hiveCatalog.listDatabases().contains(dbName));
    if (createDatabase) {
      hiveCatalog.createDatabase(dbName);
    }
    final TableIdentifier unKeyedHiveTableIdentifier =
            TableIdentifier.of(TableTestHelper.TEST_CATALOG_NAME, dbName, unkeyedTable);

    final TableIdentifier keyedHiveTableIdentifier =
            TableIdentifier.of(TableTestHelper.TEST_CATALOG_NAME, dbName, keyedTable);
    // check unkeyed table
    hiveCatalog.newTableBuilder(unKeyedHiveTableIdentifier, HIVE_TABLE_SCHEMA)
            .withPartitionSpec(HIVE_SPEC)
            .create().asUnkeyedTable();
    checkTableArcticPropertiesAndDropTable(hiveCatalog, unKeyedHiveTableIdentifier);
    //check keyed table
    hiveCatalog.newTableBuilder(keyedHiveTableIdentifier, HIVE_TABLE_SCHEMA)
            .withPartitionSpec(HIVE_SPEC)
            .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
            .create().asKeyedTable();
    checkTableArcticPropertiesAndDropTable(hiveCatalog, keyedHiveTableIdentifier);
    if (createDatabase) {
      getCatalog().dropDatabase(dbName);
    }
  }
}