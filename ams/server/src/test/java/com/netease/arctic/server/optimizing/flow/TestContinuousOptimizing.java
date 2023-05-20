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

package com.netease.arctic.server.optimizing.flow;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.hive.TestHMS;
import com.netease.arctic.hive.catalog.HiveCatalogTestHelper;
import com.netease.arctic.hive.catalog.HiveTableTestHelper;
import com.netease.arctic.server.optimizing.flow.checker.DataConcurrencyChecker;
import com.netease.arctic.server.optimizing.flow.checker.FullOptimizingChecker;
import com.netease.arctic.server.optimizing.flow.checker.OptimizingCountChecker;
import com.netease.arctic.table.ArcticTable;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL;

@RunWith(Parameterized.class)
public class TestContinuousOptimizing extends TableTestBase {

  @ClassRule
  public static TestHMS TEST_HMS = new TestHMS();

  public TestContinuousOptimizing(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{1}.{2}")
  public static Object[] parameters() {
    return new Object[][] {
        // {
        //     new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        //     new BasicTableTestHelper(true, true)
        // }
        // ,
        // {
        //    new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        //    new BasicTableTestHelper(true, false)
        // }
        // ,
        // {
        //     new BasicCatalogTestHelper(TableFormat.ICEBERG),
        //     new BasicTableTestHelper(true, false)
        // }
        // ,
        // {
        //     new BasicCatalogTestHelper(TableFormat.ICEBERG),
        //     new BasicTableTestHelper(true, false)
        // }
        {
            new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
            new HiveTableTestHelper(true, true)
        }
        // ,
        // {
        //     new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        //     new BasicTableTestHelper(true, false)
        // }
    };
  }

  @Test
  public void run() throws Exception {

    int partitionCount = 2;
    int primaryUpperBound = 30000;

    long selfTargetFileSize = 1024 * 128; //128k
    long writeTargetFileSize = 1024 * 12; //12k
    int minorTriggerCount = 4;
    int availableCore = 10;

    int cycle = 5;
    int recordCountOnceWrite = 2500;

    ArcticTable table = getArcticTable();

    TableDataView view = new TableDataView(table, tableTestHelper().primaryKeySpec().getPkSchema(),
        partitionCount, primaryUpperBound, writeTargetFileSize);

    //init checker
    DataConcurrencyChecker dataConcurrencyChecker = new DataConcurrencyChecker(view);
    OptimizingCountChecker optimizingCountChecker = new OptimizingCountChecker(0);
    FullOptimizingChecker fullOptimizingChecker = new FullOptimizingChecker();

    CompleteOptimizingFlow optimizingFlow = CompleteOptimizingFlow
        .builder(table, availableCore)
        .setTargetSize(selfTargetFileSize)
        .setFragmentRatio(null)
        .setDuplicateRatio(null)
        .setMinorTriggerFileCount(minorTriggerCount)
        .addChecker(dataConcurrencyChecker)
        .addChecker(optimizingCountChecker)
        .addChecker(fullOptimizingChecker)
        .build();

    while (cycle-- > 0) {
      view.onlyDelete(recordCountOnceWrite);
      optimizingFlow.optimize();

      view.custom(simpleUpsert());
      optimizingFlow.optimize();

      view.cdc(recordCountOnceWrite);
      optimizingFlow.optimize();

      if (cycle == 3) {
        mustFullCycle(table, () -> {
          view.upsert(recordCountOnceWrite);
          optimizingFlow.optimize();
          return null;
        });
      }
    }

    List<CompleteOptimizingFlow.Checker> checkers = optimizingFlow.unTriggerChecker();
    if (checkers.size() != 0) {
      throw new IllegalStateException("Some checkers are not triggered:" + checkers);
    }
  }

  @NotNull
  private static TableDataView.CustomData simpleUpsert() {
    return new TableDataView.CustomData() {
      @Override
      public List<TableDataView.PKWithAction> data() {
        List<TableDataView.PKWithAction> list = new ArrayList<>();
        for (int i = 2500; i > 0; i--) {
          list.add(new TableDataView.PKWithAction(i, ChangeAction.DELETE));
          list.add(new TableDataView.PKWithAction(i, ChangeAction.INSERT));
        }
        return list;
      }
    };
  }

  private static void mustFullCycle(ArcticTable table, Callable runnable) throws Exception {
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "1").commit();
    runnable.call();
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
  }
}
