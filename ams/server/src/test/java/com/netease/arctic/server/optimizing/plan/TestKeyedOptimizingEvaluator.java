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

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.data.Record;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestKeyedOptimizingEvaluator extends TableTestBase {

  public TestKeyedOptimizingEvaluator(CatalogTestHelper catalogTestHelper,
                                      TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)}};
  }

  @Test
  public void testEmpty() {
    OptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    OptimizingEvaluator.PendingInput pendingInput = optimizingEvaluator.getPendingInput();
    assertEmptyInput(pendingInput);
  }

  @Test
  public void testFragmentFiles() {
    closeFullOptimizing();
    updateChangeHashBucket(1);
    List<Record> newRecords = OptimizingTestHelpers.generateRecord(tableTestHelper(), 1, 4, "2022-01-01T12:00:00");
    long transactionId = getArcticTable().beginTransaction("");
    OptimizingTestHelpers.appendChange(getArcticTable(),
        tableTestHelper().writeChangeStore(getArcticTable(), transactionId, ChangeAction.INSERT,
            newRecords, false));

    OptimizingEvaluator optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertFalse(optimizingEvaluator.isNecessary());
    OptimizingEvaluator.PendingInput pendingInput = optimizingEvaluator.getPendingInput();
    assertEmptyInput(pendingInput);
  }

  @Override
  protected KeyedTable getArcticTable() {
    return super.getArcticTable().asKeyedTable();
  }

  protected OptimizingEvaluator buildOptimizingEvaluator() {
    return new OptimizingEvaluator(buildTableRuntime(), getArcticTable(),
        OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getArcticTable()));
  }

  protected TableRuntime buildTableRuntime() {
    return new TableRuntime(getArcticTable());
  }
  

  protected void assertEmptyInput(OptimizingEvaluator.PendingInput input) {
    Assert.assertEquals(input.getPartitions().size(), 0);
    Assert.assertEquals(input.getDataFileCount(), 0);
    Assert.assertEquals(input.getDataFileSize(), 0);
    Assert.assertEquals(input.getEqualityDeleteBytes(), 0);
    Assert.assertEquals(input.getEqualityDeleteFileCount(), 0);
    Assert.assertEquals(input.getPositionalDeleteBytes(), 0);
    Assert.assertEquals(input.getPositionalDeleteFileCount(), 0);
  }

  protected void updateChangeHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.CHANGE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void updateBaseHashBucket(int bucket) {
    getArcticTable().updateProperties().set(TableProperties.BASE_FILE_INDEX_HASH_BUCKET, bucket + "").commit();
  }

  protected void closeFullOptimizing() {
    getArcticTable().updateProperties().set(TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
  }

}
