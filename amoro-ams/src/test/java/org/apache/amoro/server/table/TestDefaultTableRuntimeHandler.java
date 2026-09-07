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

package org.apache.amoro.server.table;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.hive.catalog.HiveCatalogTestHelper;
import org.apache.amoro.hive.catalog.HiveTableTestHelper;
import org.apache.amoro.metrics.Gauge;
import org.apache.amoro.metrics.Metric;
import org.apache.amoro.metrics.MetricDefine;
import org.apache.amoro.metrics.MetricKey;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.server.manager.EventsManager;
import org.apache.amoro.server.manager.MetricManager;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.iceberg.util.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestDefaultTableRuntimeHandler extends AMSTableTestBase {

  private DefaultTableService tableService;
  private final DefaultTableRuntimeFactory runtimeFactory;

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {
        new HiveCatalogTestHelper(TableFormat.MIXED_HIVE, TEST_HMS.getHiveConf()),
        new HiveTableTestHelper(true, true)
      }
    };
  }

  public TestDefaultTableRuntimeHandler(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
    this.runtimeFactory = new DefaultTableRuntimeFactory();
  }

  @Test
  public void testInitialize() throws Exception {
    tableService = new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
    TestHandler handler = new TestHandler();
    tableService.addHandlerChain(handler);
    tableService.initialize();
    if (!(catalogTestHelper().tableFormat().equals(TableFormat.MIXED_HIVE)
        && TEST_HMS.getHiveClient().getDatabase(TableTestHelper.TEST_DB_NAME) != null)) {
      createDatabase();
    }
    createTable();
    ServerTableIdentifier createTableId = tableManager().listManagedTables().get(0);
    Assert.assertEquals(1, handler.getAddedTables().size());
    validateMixedTable(handler.getAddedTables().get(0).first());
    validateTableRuntime(handler.getAddedTables().get(0).second());
    tableService.dispose();
    MetricManager.dispose();
    EventsManager.dispose();
    Assert.assertTrue(handler.isDisposed());

    // initialize with a history table
    tableService = new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
    handler = new TestHandler();
    tableService.addHandlerChain(handler);
    tableService.initialize();
    Assert.assertEquals(1, handler.getInitTables().size());
    Assert.assertEquals(
        (Long) createTableId.getId().longValue(),
        handler.getInitTables().get(0).getTableIdentifier().getId());

    // test change properties
    MixedTable mixedTable = (MixedTable) tableService().loadTable(createTableId).originalTable();

    mixedTable.updateProperties().set(TableProperties.ENABLE_ORPHAN_CLEAN, "true").commit();
    getDefaultTableRuntime(createTableId.getId())
        .refresh(tableService.loadTable(serverTableIdentifier()));
    Assert.assertEquals(1, handler.getConfigChangedTables().size());
    validateTableRuntime(handler.getConfigChangedTables().get(0).first());
    Assert.assertTrue(
        handler
            .getConfigChangedTables()
            .get(0)
            .first()
            .getTableConfiguration()
            .isCleanOrphanEnabled());
    Assert.assertFalse(handler.getConfigChangedTables().get(0).second().isCleanOrphanEnabled());

    // drop table
    dropTable();
    Assert.assertEquals(1, handler.getRemovedTables().size());

    dropDatabase();
    tableService.dispose();
    tableService = null;
  }

  @Test
  public void testRefreshUpdatesOptimizerGroup() throws Exception {
    tableService = new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
    TestHandler handler = new TestHandler();
    tableService.addHandlerChain(handler);
    tableService.initialize();
    if (!(catalogTestHelper().tableFormat().equals(TableFormat.MIXED_HIVE)
        && TEST_HMS.getHiveClient().getDatabase(TableTestHelper.TEST_DB_NAME) != null)) {
      createDatabase();
    }
    createTable();

    ServerTableIdentifier tableId = tableManager().listManagedTables().get(0);
    DefaultTableRuntime runtime = getDefaultTableRuntime(tableId.getId());

    // Verify initial group name is "default"
    String initialGroup = runtime.getGroupName();
    Assert.assertEquals(TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT, initialGroup);

    // Change optimizer group property
    String newGroupName = "new-optimizer-group";
    MixedTable mixedTable = (MixedTable) tableService().loadTable(tableId).originalTable();
    mixedTable.updateProperties().set(TableProperties.SELF_OPTIMIZING_GROUP, newGroupName).commit();

    // Refresh the runtime
    runtime.refresh(tableService.loadTable(tableId));

    // Verify that getGroupName() returns the new group name
    Assert.assertEquals(newGroupName, runtime.getGroupName());

    // Verify config changed handler was called
    Assert.assertEquals(1, handler.getConfigChangedTables().size());

    // Cleanup
    dropTable();
    dropDatabase();
    tableService.dispose();
    MetricManager.dispose();
    EventsManager.dispose();
    tableService = null;
  }

  @Test
  public void testStatusChangeMetricsWiring() throws Exception {
    tableService = new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
    tableService.addHandlerChain(new TestHandler());
    tableService.initialize();
    if (!(catalogTestHelper().tableFormat().equals(TableFormat.MIXED_HIVE)
        && TEST_HMS.getHiveClient().getDatabase(TableTestHelper.TEST_DB_NAME) != null)) {
      createDatabase();
    }
    createTable();
    ServerTableIdentifier tableId = tableManager().listManagedTables().get(0);
    DefaultTableRuntime runtime = getDefaultTableRuntime(tableId.getId());

    // a brand-new table reports idle in the status gauges
    Assert.assertEquals(OptimizingStatus.IDLE, runtime.getOptimizingStatus());
    Assert.assertEquals(1L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_IDLE));
    Assert.assertEquals(0L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_PLANNING));

    // IDLE -> PLANNING notifies the metrics via the status-change handler callback
    runtime.beginPlanning();
    Assert.assertEquals(OptimizingStatus.PLANNING, runtime.getOptimizingStatus());
    Assert.assertEquals(1L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_PLANNING));
    Assert.assertEquals(0L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_IDLE));
    Thread.sleep(50);
    Assert.assertTrue(
        gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_PLANNING_DURATION) > 0);

    // PLANNING -> PENDING resets the planning duration and moves the pending gauges
    runtime.planFailed();
    Assert.assertEquals(OptimizingStatus.PENDING, runtime.getOptimizingStatus());
    Assert.assertEquals(1L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_PENDING));
    Assert.assertEquals(0L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_PLANNING));
    Assert.assertEquals(
        0L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_PLANNING_DURATION));
    Thread.sleep(50);
    long pendingDurationBeforeRestart =
        gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_PENDING_DURATION);
    Assert.assertTrue(pendingDurationBeforeRestart > 0);

    // A same-status write must not restart the persisted duration clock used after a restart.
    DefaultTableRuntimeStore runtimeStore = (DefaultTableRuntimeStore) runtime.store();
    long pendingStatusUpdateTime = runtimeStore.getStatusCodeUpdateTime();
    runtime.store().begin().updateStatusCode(status -> status).commit();
    Assert.assertEquals(pendingStatusUpdateTime, runtimeStore.getStatusCodeUpdateTime());

    // metrics survive a restart: the restored runtime is seeded from the persisted status
    tableService.dispose();
    MetricManager.dispose();
    EventsManager.dispose();
    tableService = new DefaultTableService(new Configurations(), CATALOG_MANAGER, runtimeFactory);
    tableService.addHandlerChain(new TestHandler());
    tableService.initialize();
    DefaultTableRuntime restoredRuntime = getDefaultTableRuntime(tableId.getId());
    Assert.assertEquals(OptimizingStatus.PENDING, restoredRuntime.getOptimizingStatus());
    Assert.assertEquals(1L, gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_IN_PENDING));
    Assert.assertTrue(
        gaugeValue(TableOptimizingMetrics.TABLE_OPTIMIZING_STATUS_PENDING_DURATION)
            >= pendingDurationBeforeRestart);

    dropTable();
    dropDatabase();
    tableService.dispose();
    MetricManager.dispose();
    EventsManager.dispose();
    tableService = null;
  }

  private long gaugeValue(MetricDefine define) {
    MetricRegistry registry = MetricManager.getInstance().getGlobalRegistry();
    for (Map.Entry<MetricKey, Metric> entry : registry.getMetrics().entrySet()) {
      MetricKey key = entry.getKey();
      if (define.equals(key.getDefine())
          && TableTestHelper.TEST_TABLE_NAME.equals(key.valueOfTag("table"))) {
        return ((Gauge<? extends Number>) entry.getValue()).getValue().longValue();
      }
    }
    throw new AssertionError("Gauge not registered for " + define.getName());
  }

  protected DefaultTableService tableService() {
    if (tableService != null) {
      return tableService;
    } else {
      return super.tableService();
    }
  }

  static class TestHandler extends RuntimeHandlerChain {

    private final List<TableRuntime> initTables = Lists.newArrayList();
    private final List<Pair<TableRuntime, OptimizingStatus>> statusChangedTables =
        Lists.newArrayList();
    private final List<Pair<TableRuntime, TableConfiguration>> configChangedTables =
        Lists.newArrayList();
    private final List<Pair<MixedTable, TableRuntime>> addedTables = Lists.newArrayList();
    private final List<TableRuntime> removedTables = Lists.newArrayList();
    private boolean disposed = false;

    @Override
    protected void handleStatusChanged(TableRuntime tableRuntime, OptimizingStatus originalStatus) {
      statusChangedTables.add(Pair.of(tableRuntime, originalStatus));
    }

    @Override
    protected void handleConfigChanged(
        TableRuntime tableRuntime, TableConfiguration originalConfig) {
      configChangedTables.add(Pair.of(tableRuntime, originalConfig));
    }

    @Override
    protected void handleTableAdded(AmoroTable<?> table, TableRuntime tableRuntime) {
      addedTables.add(Pair.of((MixedTable) table.originalTable(), tableRuntime));
    }

    @Override
    protected void handleTableRemoved(TableRuntime tableRuntime) {
      removedTables.add(tableRuntime);
    }

    @Override
    protected void initHandler(List<TableRuntime> tableRuntimeList) {
      initTables.addAll(tableRuntimeList);
    }

    @Override
    protected void doDispose() {
      disposed = true;
    }

    public List<TableRuntime> getInitTables() {
      return initTables;
    }

    public List<Pair<TableRuntime, TableConfiguration>> getConfigChangedTables() {
      return configChangedTables;
    }

    public List<Pair<MixedTable, TableRuntime>> getAddedTables() {
      return addedTables;
    }

    public List<TableRuntime> getRemovedTables() {
      return removedTables;
    }

    public boolean isDisposed() {
      return disposed;
    }
  }
}
