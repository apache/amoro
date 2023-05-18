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
import com.netease.arctic.server.optimizing.OptimizingTestHelpers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestKeyedOptimizingPlanner extends TestKeyedOptimizingEvaluator {
  public TestKeyedOptimizingPlanner(CatalogTestHelper catalogTestHelper,
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
  @Override
  public void testFragmentFiles() {
    super.testFragmentFiles();
    OptimizingPlanner optimizingEvaluator = buildOptimizingEvaluator();
    Assert.assertTrue(optimizingEvaluator.isNecessary());
    List<TaskDescriptor> taskDescriptors = optimizingEvaluator.planTasks();
    Assert.assertEquals(1, taskDescriptors.size());
  }

  @Override
  protected OptimizingPlanner buildOptimizingEvaluator() {
    return new OptimizingPlanner(buildTableRuntime(), getArcticTable(),
        OptimizingTestHelpers.getCurrentKeyedTableSnapshot(getArcticTable()), 1);
  }
}
