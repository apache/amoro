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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.formats.paimon.PaimonHadoopCatalogTestHelper;
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.optimizing.PendingInputResult;
import org.apache.amoro.server.AMSServiceTestBase;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.scheduler.inline.TableRuntimeRefreshExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Optional;

/**
 * Verifies that {@link TableRuntimeRefreshExecutor} transitions Paimon tables from IDLE to PENDING
 * when a new snapshot exists, and stays IDLE when no snapshot change or optimizing is disabled.
 *
 * <p>Uses a real H2-backed {@link DefaultTableRuntime} with Paimon-typed pending input key (so
 * state transitions actually persist) but overrides {@code loadTable()} to return a Mockito mock
 * instead of hitting a real filesystem catalog.
 */
public class TestTableRuntimeRefreshExecutorForPaimon extends AMSServiceTestBase {

  private static final int MAX_PENDING_PARTITIONS = 1;
  private static final long INTERVAL = 60_000L;
  private static final String CATALOG_NAME = "test_paimon_catalog";
  private static final String DB_NAME = "test_db";
  private static final String TABLE_NAME = "test_table";

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private PaimonHadoopCatalogTestHelper catalogHelper;
  private DefaultTableRuntime paimonRuntime;

  @Before
  public void setUp() throws Exception {
    String warehouse = temp.newFolder("warehouse").getAbsolutePath();
    catalogHelper = new PaimonHadoopCatalogTestHelper(CATALOG_NAME, new java.util.HashMap<>());
    catalogHelper.initWarehouse(warehouse);

    CATALOG_MANAGER.createCatalog(catalogHelper.getCatalogMeta());
    catalogHelper.createDatabase(DB_NAME);
    catalogHelper.createTable(DB_NAME, TABLE_NAME);

    tableService().exploreTableRuntimes();

    ServerTableIdentifier id =
        tableManager().listManagedTables().stream()
            .filter(s -> TABLE_NAME.equals(s.getTableName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Paimon table not registered"));

    DefaultTableRuntime runtime = (DefaultTableRuntime) tableService().getRuntime(id.getId());
    if (!(runtime instanceof DefaultTableRuntime)) {
      throw new IllegalStateException(
          "Expected DefaultTableRuntime, got " + runtime.getClass().getName());
    }
    paimonRuntime = runtime;

    // The optimizer handler chain may have already transitioned the runtime to PENDING
    // during exploreTableRuntimes(). Reset to IDLE so each test starts clean.
    paimonRuntime.completeEmptyProcess();
  }

  @After
  public void tearDown() throws Exception {
    try {
      catalogHelper.clean();
    } catch (Exception ignored) {
    }
    CATALOG_MANAGER.dropCatalog(CATALOG_NAME);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private TableSnapshot snapshotWithId(long id) {
    TableSnapshot s = mock(TableSnapshot.class);
    when(s.id()).thenReturn(String.valueOf(id));
    return s;
  }

  private AmoroTable<?> paimonAmoroTable(TableSnapshot snapshot) {
    return paimonAmoroTable(snapshot, true);
  }

  private AmoroTable<?> paimonAmoroTable(TableSnapshot snapshot, boolean optimizingNecessary) {
    AmoroTable<?> t = mock(AmoroTable.class);
    when(t.format()).thenReturn(TableFormat.PAIMON);
    when(t.originalTable())
        .thenReturn(mock(org.apache.paimon.table.AppendOnlyFileStoreTable.class));
    when(t.currentSnapshot()).thenReturn(snapshot);
    when(t.refreshOptimizingState(any())).thenCallRealMethod();
    when(t.properties()).thenReturn(new java.util.HashMap<>());
    // Mock evaluatePendingInput to return PaimonPendingInput when called
    PaimonPendingInput pendingInput = new PaimonPendingInput();
    when(t.evaluatePendingInput(any(), anyInt()))
        .thenReturn(Optional.of(new PendingInputResult(pendingInput, optimizingNecessary)));
    return t;
  }

  private TableRuntimeRefreshExecutor executorWith(AmoroTable<?> table) {
    return new TableRuntimeRefreshExecutor(tableService(), 1, INTERVAL, MAX_PENDING_PARTITIONS) {
      @Override
      protected AmoroTable<?> loadTable(TableRuntime tableRuntime) {
        return table;
      }
    };
  }

  // ---------------------------------------------------------------------------
  // Tests
  // ---------------------------------------------------------------------------

  @Test
  public void newSnapshotTransitionsIdleToPending() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(snapshotWithId(100L));
    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);

    assertEquals(OptimizingStatus.IDLE, paimonRuntime.getOptimizingStatus());

    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.PENDING, paimonRuntime.getOptimizingStatus());
  }

  @Test
  public void noNewSnapshotKeepsIdle() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(null);
    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);

    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.IDLE, paimonRuntime.getOptimizingStatus());
  }

  @Test
  public void newSnapshotWithNoOptimizingDemandKeepsIdle() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(snapshotWithId(150L), false);
    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);

    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.IDLE, paimonRuntime.getOptimizingStatus());
  }

  @Test
  public void optimizingDisabledKeepsIdle() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(snapshotWithId(200L));
    java.util.Map<String, String> disabledProps = new java.util.HashMap<>();
    disabledProps.put("self-optimizing.enabled", "false");
    when(mockAmoroTable.properties()).thenReturn(disabledProps);

    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);

    // Refresh so the runtime picks up disabled config
    paimonRuntime.refresh(mockAmoroTable);
    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.IDLE, paimonRuntime.getOptimizingStatus());
  }

  @Test
  public void alreadyPendingDoesNotDoubleTrigger() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(snapshotWithId(300L));
    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);
    executor.execute(paimonRuntime);
    assertEquals(OptimizingStatus.PENDING, paimonRuntime.getOptimizingStatus());

    // Second execute with same snapshot
    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.PENDING, paimonRuntime.getOptimizingStatus());
  }

  @Test
  public void planningStatusDoesNotReTrigger() throws Exception {
    AmoroTable<?> mockAmoroTable = paimonAmoroTable(snapshotWithId(400L));
    TableRuntimeRefreshExecutor executor = executorWith(mockAmoroTable);
    executor.execute(paimonRuntime);
    assertEquals(OptimizingStatus.PENDING, paimonRuntime.getOptimizingStatus());
    paimonRuntime.beginPlanning();
    assertEquals(OptimizingStatus.PLANNING, paimonRuntime.getOptimizingStatus());

    // Execute again while PLANNING
    executor.execute(paimonRuntime);

    assertEquals(OptimizingStatus.PLANNING, paimonRuntime.getOptimizingStatus());
  }
}
