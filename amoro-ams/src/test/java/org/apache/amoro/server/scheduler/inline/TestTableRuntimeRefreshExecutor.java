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

package org.apache.amoro.server.scheduler.inline;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class TestTableRuntimeRefreshExecutor extends AMSTableTestBase {
  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, true)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      },
      {
        TestedCatalogs.hadoopCatalog(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(false, false)
      },
      {
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, false)
      },
    };
  }

  private final boolean isIcebergTable;

  public TestTableRuntimeRefreshExecutor(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
    isIcebergTable = catalogTestHelper.tableFormat() == TableFormat.ICEBERG;
  }

  private static final long INTERVAL = 60000L; // 1 minute
  private static final int MAX_PENDING_PARTITIONS = 1;

  private TableRuntime getMockTableRuntimeUsingDefaultRefreshEvent(
      DefaultTableRuntime tableRuntime) {
    DefaultTableRuntime mockTableRuntime = Mockito.mock(DefaultTableRuntime.class);

    Mockito.when(mockTableRuntime.getTableIdentifier())
        .thenReturn(tableRuntime.getTableIdentifier());

    Mockito.when(mockTableRuntime.getLastOptimizedSnapshotId())
        .thenReturn(tableRuntime.getLastOptimizedSnapshotId());
    Mockito.when(mockTableRuntime.getLastOptimizedChangeSnapshotId())
        .thenReturn(tableRuntime.getLastOptimizedChangeSnapshotId());
    Mockito.when(mockTableRuntime.getCurrentSnapshotId()).thenReturn(0L);
    Mockito.when(mockTableRuntime.getCurrentChangeSnapshotId()).thenReturn(0L);

    OptimizingConfig mockoptimizingConfig = Mockito.mock(OptimizingConfig.class);
    Mockito.when(mockoptimizingConfig.getRefreshEventIdentifier()).thenReturn("default");
    Mockito.when(mockTableRuntime.getOptimizingConfig()).thenReturn(mockoptimizingConfig);
    return mockTableRuntime;
  }

  @Test
  public void testTableRuntimeDefaultRefreshEvent() {
    createDatabase();
    createTable();

    TableRuntime tableRuntime = tableService().getRuntime(serverTableIdentifier().getId());
    TableRuntime mockTableRuntime =
        getMockTableRuntimeUsingDefaultRefreshEvent((DefaultTableRuntime) tableRuntime);

    TableRuntimeRefreshExecutor executor =
        new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS);
    executor.execute(mockTableRuntime);

    verify(((DefaultTableRuntime) mockTableRuntime).getOptimizingConfig(), times(1)).isEnabled();

    dropTable();
    dropDatabase();
  }
}
