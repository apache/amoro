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

import org.apache.amoro.AmoroTable;
import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.TestedCatalogs;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.server.table.AMSTableTestBase;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.TableConfigurations;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.Map;

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
        new HiveCatalogTestHelper(TableFormat.ICEBERG, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      },
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      }
    };
  }

  private static final Map<String, String> eventTriggerTableProperties =
      ImmutableMap.<String, String>builder()
          .put("self-optimizing.refresh.event-triggered", "true")
          .build();

  public TestTableRuntimeRefreshExecutor(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Test
  public void testTableRuntimeRefreshByDefault() throws InterruptedException {
    TableRuntimeRefresher executor = new TableRuntimeRefresher();
    tableTestHelper().tableProperties().clear();
    createDatabase();
    createTable();

    TableRuntime tableRuntime = tableService().getRuntime(serverTableIdentifier().getId());

    // Test add table
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());
    executor.handleTableAdded(tableService().loadTable(serverTableIdentifier()), tableRuntime);
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());

    // After the table added, execute() will be scheduled after 10s
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());
    Thread.sleep(15000L);
    // Verify that execute() is called and table is loaded
    Assertions.assertTrue(executor.hasExecute);
    Assertions.assertTrue(executor.hasLoadTable);

    dropTable();
    dropDatabase();
  }

  @Test
  public void testTableRuntimeRefreshWithEventTrigger() throws InterruptedException {
    TableRuntimeRefresher executor = new TableRuntimeRefresher(3600000L);
    tableTestHelper().tableProperties().putAll(eventTriggerTableProperties);
    createDatabase();
    createTable();

    TableRuntime tableRuntime = tableService().getRuntime(serverTableIdentifier().getId());
    ((DefaultTableRuntime) tableRuntime)
        .setLastRefreshTime(
            System
                .currentTimeMillis()); // Set the last plan time to prevent table refresh triggered
    // by fallback interval

    // Test add table
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());
    executor.handleTableAdded(tableService().loadTable(serverTableIdentifier()), tableRuntime);
    Assertions.assertEquals(1, executor.managedEventTriggerTables.size());

    // After the table added, execute() will be scheduled after 10s
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());
    Thread.sleep(15000L);
    // Verify that execute() is called but table is not loaded due to not triggered
    Assertions.assertTrue(executor.hasExecute);
    Assertions.assertFalse(executor.hasLoadTable);

    // Test when the table is triggered to refresh
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());
    executor.addTableToRefresh(serverTableIdentifier().getIdentifier());
    Assertions.assertEquals(1, executor.pendingRefreshTables.size());
    executor.hasExecute = false;
    executor.hasLoadTable = false;

    executor.execute(tableRuntime);
    Assertions.assertTrue(executor.hasExecute);
    Assertions.assertTrue(executor.hasLoadTable);
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());

    // Test when the table is triggered to refresh due to fallback interval has been reached
    executor.hasExecute = false;
    executor.hasLoadTable = false;
    executor.execute(tableRuntime);
    Assertions.assertTrue(executor.hasExecute);
    Assertions.assertFalse(executor.hasLoadTable);

    ((DefaultTableRuntime) tableRuntime)
        .setLastRefreshTime(
            System.currentTimeMillis()
                - 5000000); // Set the last plan time to make table refresh triggered by fallback
    // interval
    executor.execute(tableRuntime);
    Assertions.assertTrue(executor.hasLoadTable);

    // Test when a table changes from event-triggered to non-event-triggered configuration
    // This should remove the table from managedEventTriggerTables and pendingRefreshTables
    executor.addTableToRefresh(serverTableIdentifier().getIdentifier());
    Assertions.assertEquals(1, executor.managedEventTriggerTables.size());
    Assertions.assertEquals(1, executor.pendingRefreshTables.size());

    ((DefaultTableRuntime) tableRuntime).store().getTableConfig().clear();
    executor.handleConfigChanged(
        tableRuntime, TableConfigurations.parseTableConfig(eventTriggerTableProperties));
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());

    // Test when a table changes from non-event-triggered to event-triggered configuration
    // This should remove the table from managedEventTriggerTables
    ((DefaultTableRuntime) tableRuntime)
        .store()
        .getTableConfig()
        .putAll(eventTriggerTableProperties);
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());
    executor.handleConfigChanged(
        tableRuntime, TableConfigurations.parseTableConfig(Collections.emptyMap()));
    Assertions.assertEquals(1, executor.managedEventTriggerTables.size());
    Assertions.assertEquals(0, executor.pendingRefreshTables.size());

    // test remove table triggered by event
    Assertions.assertEquals(1, executor.managedEventTriggerTables.size());
    executor.handleTableRemoved(tableRuntime);
    Assertions.assertEquals(0, executor.managedEventTriggerTables.size());

    dropTable();
    dropDatabase();
  }

  private class TableRuntimeRefresher extends TableRuntimeRefreshExecutor {

    public TableRuntimeRefresher() {
      super(tableService(), Integer.MAX_VALUE, Long.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
    }

    public TableRuntimeRefresher(long maxInterval) {
      super(tableService(), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, maxInterval);
    }

    private boolean hasLoadTable = false;
    private boolean hasExecute = false;

    @Override
    public AmoroTable<?> loadTable(TableRuntime tableRuntime) {
      hasLoadTable = true;
      return super.loadTable(tableRuntime);
    }

    @Override
    public void execute(TableRuntime tableRuntime) {
      hasExecute = true;
      super.execute(tableRuntime);
    }
  }
}
