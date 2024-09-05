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

package org.apache.amoro.server.optimizing.flow;

import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES;
import static org.apache.amoro.table.TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.hive.TestHMS;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.optimizing.flow.checker.DataConcurrencyChecker;
import org.apache.amoro.server.optimizing.flow.checker.FullOptimizingMove2HiveChecker;
import org.apache.amoro.server.optimizing.flow.checker.FullOptimizingWrite2HiveChecker;
import org.apache.amoro.server.optimizing.flow.checker.MinorOptimizingCheck;
import org.apache.amoro.server.optimizing.flow.checker.OptimizingCountChecker;
import org.apache.amoro.server.optimizing.flow.view.UnKeyedTableDataView;
import org.apache.amoro.table.MixedTable;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.List;

@RunWith(Parameterized.class)
public class TestUnKeyedContinuousOptimizing extends TableTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestUnKeyedContinuousOptimizing(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      },
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)},
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new BasicTableTestHelper(false, false)
      }
    };
  }

  @Test
  public void run() throws Exception {
    MixedTable table = getMixedTable();

    int partitionCount = 2;

    long writeTargetFileSize = 1024 * 12;
    long selfTargetFileSize = 1024 * 384;
    int minorTriggerCount = 3;
    int availableCore = 10;

    int cycle = 5;
    int recordCountOnceWrite = 1000;

    // close full optimize
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
    // Need move file to hive scene

    UnKeyedTableDataView view =
        new UnKeyedTableDataView(table, partitionCount, writeTargetFileSize, null);

    // init checker
    DataConcurrencyChecker dataConcurrencyChecker = new DataConcurrencyChecker(view);
    OptimizingCountChecker optimizingCountChecker = new OptimizingCountChecker(0);
    FullOptimizingWrite2HiveChecker fullOptimizingWrite2HiveChecker =
        new FullOptimizingWrite2HiveChecker(view);
    FullOptimizingMove2HiveChecker fullOptimizingMove2HiveChecker =
        new FullOptimizingMove2HiveChecker(view);
    MinorOptimizingCheck minorOptimizingCheck = new MinorOptimizingCheck();

    CompleteOptimizingFlow.Builder builder =
        CompleteOptimizingFlow.builder(table, availableCore)
            .setTargetSize(selfTargetFileSize)
            .setFragmentRatio(null)
            .setDuplicateRatio(null)
            .setMinorTriggerFileCount(minorTriggerCount)
            .addChecker(dataConcurrencyChecker)
            .addChecker(optimizingCountChecker)
            .addChecker(minorOptimizingCheck);

    if (table.format() == TableFormat.MIXED_HIVE) {
      builder
          .addChecker(fullOptimizingWrite2HiveChecker)
          .addChecker(fullOptimizingMove2HiveChecker);
    }

    CompleteOptimizingFlow optimizingFlow = builder.build();

    table.updateProperties().set(SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES, "false").commit();
    // full optimizing need move file to hive from change
    append(recordCountOnceWrite, view);
    mustFullCycle(table, optimizingFlow::optimize);

    append(recordCountOnceWrite, view);
    optimizingFlow.optimize();

    table.updateProperties().set(SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES, "true").commit();
    append(recordCountOnceWrite, view);
    mustFullCycle(table, optimizingFlow::optimize);

    while (cycle-- > 0) {
      append(recordCountOnceWrite, view);
      if (cycle % 2 == 0) {
        mustFullCycle(table, optimizingFlow::optimize);
      } else {
        optimizingFlow.optimize();
      }
    }

    List<CompleteOptimizingFlow.Checker> checkers = optimizingFlow.unTriggerChecker();
    if (checkers.size() != 0) {
      throw new IllegalStateException("Some checkers are not triggered:" + checkers);
    }
  }

  private void append(int count, UnKeyedTableDataView view) throws IOException {
    for (int i = 0; i < 3; i++) {
      view.append(count);
    }
  }

  private static void mustFullCycle(MixedTable table, RunnableWithException runnable)
      throws Exception {
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "1").commit();
    runnable.run();
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
  }

  public interface RunnableWithException {
    void run() throws Exception;
  }
}
