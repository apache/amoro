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
import org.apache.amoro.io.reader.CombinedDeleteFilter;
import org.apache.amoro.server.optimizing.flow.checker.DataConcurrencyChecker;
import org.apache.amoro.server.optimizing.flow.checker.FullOptimizingMove2HiveChecker;
import org.apache.amoro.server.optimizing.flow.checker.FullOptimizingWrite2HiveChecker;
import org.apache.amoro.server.optimizing.flow.checker.MinorOptimizingCheck;
import org.apache.amoro.server.optimizing.flow.checker.OptimizingCountChecker;
import org.apache.amoro.server.optimizing.flow.view.KeyedTableDataView;
import org.apache.amoro.table.MixedTable;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@Ignore
@RunWith(Parameterized.class)
public class TestKeyedContinuousOptimizing extends TableTestBase {

  @ClassRule public static TestHMS TEST_HMS = new TestHMS();

  public TestKeyedContinuousOptimizing(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)
      },
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, false)},
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new BasicTableTestHelper(true, false)
      }
    };
  }

  @Test
  public void run() throws Exception {
    MixedTable table = getMixedTable();

    int partitionCount = 2;
    int primaryUpperBound = 30000;

    long writeTargetFileSize = 1024 * 12;
    long selfTargetFileSize = table.format() == TableFormat.ICEBERG ? 1024 * 384 : 1024 * 128;
    int minorTriggerCount = table.format() == TableFormat.ICEBERG ? 3 : 4;
    int availableCore = 10;

    int cycle = 5;
    int recordCountOnceWrite = 2500;

    // close full optimize
    table.updateProperties().set(SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL, "-1").commit();
    // Need move file to hive scene
    table.updateProperties().set(SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES, "false").commit();

    KeyedTableDataView view =
        new KeyedTableDataView(
            table,
            tableTestHelper().primaryKeySpec().getPkSchema(),
            partitionCount,
            primaryUpperBound,
            writeTargetFileSize,
            null);

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

    // full optimizing need move file to hive from change
    view.append(recordCountOnceWrite);
    mustFullCycle(table, optimizingFlow::optimize);

    view.append(recordCountOnceWrite);
    optimizingFlow.optimize();

    // full optimizing need move file to hive from change and base
    view.append(recordCountOnceWrite);
    mustFullCycle(table, optimizingFlow::optimize);

    while (cycle-- > 0) {
      view.onlyDelete(recordCountOnceWrite);
      optimizingFlow.optimize();

      view.cdc(recordCountOnceWrite);
      optimizingFlow.optimize();

      view.upsert(recordCountOnceWrite);
      if (cycle % 2 == 0) {
        // Trigger BloomFilter
        CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 2499L;

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
