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

package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

@RunWith(Parameterized.class)
public class TestHiveKeyedPartitionPlan extends TestKeyedPartitionPlan {
  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestHiveKeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
                                    TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true)},
        {new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, false)}};
  }

  @Override
  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    actual = Maps.newHashMap(actual);
    String outputDir = actual.remove(OptimizingInputProperties.OUTPUT_DIR);
    Assert.assertTrue(Long.parseLong(outputDir.split("_")[1]) > 0);
    super.assertTaskProperties(expect, actual);
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    SupportHive hiveTable = (SupportHive) getArcticTable();
    String hiveLocation = hiveTable.hiveLocation();
    return new HiveKeyedTablePartitionPlan(getTableRuntime(), getArcticTable(), getPartition(), hiveLocation,
        System.currentTimeMillis());
  }
}
