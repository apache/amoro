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

package org.apache.amoro.hive.formats;

import org.apache.amoro.formats.AmoroCatalogTestHelper;
import org.apache.amoro.formats.TestMixedIcebergFormatCatalog;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.io.AuthenticatedFileIOs;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestMixedIcebergHiveAmoroCatalog extends TestMixedIcebergFormatCatalog {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestMixedIcebergHiveAmoroCatalog(AmoroCatalogTestHelper<?> amoroCatalogTestHelper) {
    super(amoroCatalogTestHelper);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Object[] parameters() {
    Map<String, String> properties = new HashMap<>();
    properties.put(CatalogMetaProperties.HDFS_IMPERSONATION_ENABLED, "true");
    return new Object[] {
      new MixedIcebergHiveCatalogTestHelper("test_mixed_iceberg_hive_catalog", properties)
    };
  }

  @Override
  public void setupCatalog() throws IOException {
    catalogTestHelper.initHiveConf(TEST_HMS.getHiveConf());
    super.setupCatalog();
  }

  @Test
  public void testHdfsImpersonationUsesHiveOwner() throws Exception {
    String database = "owner_db";
    String table = "owner_table";
    String owner = "hms-owner";
    createDatabase(database);
    createTable(database, table, new HashMap<>());

    org.apache.hadoop.hive.metastore.api.Table hiveTable =
        TEST_HMS.getHiveClient().getTable(database, table);
    hiveTable.setOwner(owner);
    TEST_HMS.getHiveClient().alter_table(database, table, hiveTable);

    org.apache.amoro.formats.mixed.MixedTable loaded =
        (org.apache.amoro.formats.mixed.MixedTable)
            AuthenticatedFileIOs.withOptimizingCommitImpersonation(
                () -> amoroCatalog.loadTable(database, table));
    Assert.assertEquals(
        owner,
        loaded
            .originalTable()
            .io()
            .doAs(() -> UserGroupInformation.getCurrentUser().getShortUserName()));
  }
}
