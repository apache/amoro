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

package org.apache.amoro.server.optimizing.plan;

import com.netease.arctic.TableFormat;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.properties.HiveTableProperties;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.server.optimizing.OptimizingTestHelpers;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestHiveKeyedPartitionPlan extends TestKeyedPartitionPlan {
  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestHiveKeyedPartitionPlan(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      }
    };
  }

  @Override
  protected void assertTaskProperties(Map<String, String> expect, Map<String, String> actual) {
    actual = Maps.newHashMap(actual);
    String outputDir = actual.remove(OptimizingInputProperties.OUTPUT_DIR);
    if (outputDir != null) {
      Assert.assertTrue(Long.parseLong(outputDir.split("_")[1]) > 0);
    }
    super.assertTaskProperties(expect, actual);
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    SupportHive hiveTable = (SupportHive) getArcticTable();
    String hiveLocation = hiveTable.hiveLocation();
    return new MixedHivePartitionPlan(
        getTableRuntime(),
        getArcticTable(),
        getPartition(),
        hiveLocation,
        System.currentTimeMillis());
  }

  @Test
  public void testFullOptimizingWithHiveDelay() {
    updateChangeHashBucket(4);
    closeFullOptimizingInterval();
    closeMinorOptimizingInterval();
    List<Record> newRecords;
    long transactionId;
    List<DataFile> dataFiles = Lists.newArrayList();
    // write fragment file
    newRecords =
        OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    transactionId = beginTransaction();
    dataFiles.addAll(
        OptimizingTestHelpers.appendChange(
            getArcticTable(),
            tableTestHelper()
                .writeChangeStore(
                    getArcticTable(), transactionId, ChangeAction.INSERT, newRecords, false)));
    StructLike partition = dataFiles.get(0).partition();

    // not trigger optimize
    Assert.assertEquals(0, planWithCurrentFiles().size());

    // update hive delay
    updateTableProperty(HiveTableProperties.REFRESH_HIVE_INTERVAL, 1 + "");
    Assert.assertEquals(4, planWithCurrentFiles().size());
    updatePartitionProperty(
        partition,
        HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME,
        (System.currentTimeMillis() / 1000 - 10) + "");
    Assert.assertEquals(4, planWithCurrentFiles().size());
    updatePartitionProperty(
        partition,
        HiveTableProperties.PARTITION_PROPERTIES_KEY_TRANSIENT_TIME,
        (System.currentTimeMillis() / 1000 + 1000) + "");
    Assert.assertEquals(0, planWithCurrentFiles().size());
  }
}
