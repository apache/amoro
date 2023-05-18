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
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.table.KeyedTable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestContinuousOptimizing extends TableTestBase {
  public TestContinuousOptimizing() {
    super(new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true));
  }

  @Test
  public void run() throws Exception {

    int partitionCount = 2;
    int primaryUpperBound = 30000;
    long targetFileSize = 1024 * 128; //128k

    KeyedTable table = getArcticTable().asKeyedTable();

    TableDataView view = new TableDataView(table, table.primaryKeySpec().getPkSchema(),
        partitionCount, primaryUpperBound);

    DataConcurrencyChecker dataConcurrencyChecker = new DataConcurrencyChecker(view);

    CompleteOptimizingFlow optimizingFlow = CompleteOptimizingFlow
        .builder(table, 10)
        .setTargetSize(targetFileSize)
        .setFragmentRatio(null)
        .setDuplicateRatio(null)
        .setMinorTriggerFileCount(4)
        .addChecker(dataConcurrencyChecker)
        .build();
    for (int i = 0; i < 20; i++) {
      view.custom(new TableDataView.CustomData() {
        @Override
        public List<TableDataView.PKWithAction> data() {
          List<TableDataView.PKWithAction> list = new ArrayList<>();
          for (int i = 0; i < 100; i++) {
            list.add(new TableDataView.PKWithAction(i, ChangeAction.DELETE));
            list.add(new TableDataView.PKWithAction(i, ChangeAction.INSERT));
          }
          return list;
        }
      });
      optimizingFlow.optimize();

      view.upsert(2500);
      optimizingFlow.optimize();
    }
  }
}
