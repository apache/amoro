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

package org.apache.amoro.server.dashboard;

import static org.apache.amoro.server.dashboard.OverviewCache.STATUS_COMMITTING;
import static org.apache.amoro.server.dashboard.OverviewCache.STATUS_EXECUTING;
import static org.apache.amoro.server.dashboard.OverviewCache.STATUS_IDLE;
import static org.apache.amoro.server.dashboard.OverviewCache.STATUS_PENDING;
import static org.apache.amoro.server.dashboard.OverviewCache.STATUS_PLANING;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.server.dashboard.model.OverviewTopTableItem;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.executor.TableRuntimeRefreshExecutor;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.Record;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class TestOverviewCache extends AMSTableTestBase {

  private OverviewCache overviewCache;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  public TestOverviewCache(CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  @Before
  public void prepare() {
    createDatabase();
    createTable();
    this.overviewCache = OverviewCache.getInstance();
    this.overviewCache.initialize(10, MetricManager.getInstance().getGlobalRegistry());
    this.overviewCache.refresh();
  }

  @After
  public void clear() {
    try {
      dropTable();
      dropDatabase();
    } catch (Exception e) {
      // ignore
    }
  }

  private void initTableWithFiles() {
    UnkeyedTable table =
        ((MixedTable) tableService().loadTable(serverTableIdentifier()).originalTable())
            .asUnkeyedTable();
    appendData(table, 1);
    appendData(table, 2);
    TableRuntime runtime = tableService().getRuntime(serverTableIdentifier());
    runtime.refresh(tableService().loadTable(serverTableIdentifier()));
  }

  private void appendData(UnkeyedTable table, int id) {
    ArrayList<Record> newRecords =
        Lists.newArrayList(
            MixedDataTestHelpers.createRecord(
                table.schema(), id, "111", 0L, "2022-01-01T12:00:00"));
    List<DataFile> dataFiles = MixedDataTestHelpers.writeBaseStore(table, 0L, newRecords, false);
    AppendFiles appendFiles = table.newAppend();
    dataFiles.forEach(appendFiles::appendFile);
    appendFiles.commit();
  }

  void refreshPending() {
    TableRuntimeRefreshExecutor refresher =
        new TableRuntimeRefreshExecutor(tableService(), 1, Integer.MAX_VALUE);
    refresher.execute(tableService().getRuntime(serverTableIdentifier()));
    refresher.dispose();
  }

  @Test
  public void testOverviewCache() {
    // empty table
    Assertions.assertEquals(1, overviewCache.getTotalCatalog());
    Assertions.assertEquals(1, overviewCache.getTotalTableCount());
    Assertions.assertEquals(0, overviewCache.getTotalDataSize());
    Assertions.assertEquals(0, overviewCache.getTotalCpu());
    Assertions.assertEquals(0, overviewCache.getTotalMemory());

    Assertions.assertEquals(0, overviewCache.getOptimizingStatus().get(STATUS_PENDING));
    Assertions.assertEquals(0, overviewCache.getOptimizingStatus().get(STATUS_COMMITTING));
    Assertions.assertEquals(0, overviewCache.getOptimizingStatus().get(STATUS_EXECUTING));
    Assertions.assertEquals(0, overviewCache.getOptimizingStatus().get(STATUS_PLANING));
    Assertions.assertEquals(1, overviewCache.getOptimizingStatus().get(STATUS_IDLE));

    Assertions.assertEquals(1, overviewCache.getDataSizeHistory(0).size());
    Assertions.assertEquals(1, overviewCache.getResourceUsageHistory(0).size());

    List<OverviewTopTableItem> allTopTableItem = overviewCache.getAllTopTableItem();
    Assertions.assertEquals(1, allTopTableItem.size());
    Assertions.assertEquals(-1, allTopTableItem.get(0).getHealthScore());

    // insert data
    initTableWithFiles();
    refreshPending();
    overviewCache.refresh();

    Assertions.assertTrue(overviewCache.getTotalDataSize() > 0);

    Assertions.assertEquals(1, overviewCache.getOptimizingStatus().get(STATUS_PENDING));
    Assertions.assertEquals(0, overviewCache.getOptimizingStatus().get(STATUS_IDLE));

    Assertions.assertEquals(2, overviewCache.getDataSizeHistory(0).size());
    Assertions.assertEquals(2, overviewCache.getResourceUsageHistory(0).size());
    allTopTableItem = overviewCache.getAllTopTableItem();
    Assertions.assertEquals(100, allTopTableItem.get(0).getHealthScore());
  }
}
