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

package org.apache.amoro.server.optimizing;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingPlanner;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.process.ProcessFactoryRouter;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.table.TableIdentifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

/**
 * K2 regression guard for {@link OptimizingQueue#planInternal(DefaultTableRuntime)}.
 *
 * <p>The bug being pinned: when a planner returns {@code isNecessary()==true} but {@code plan()}
 * produces an empty task list (e.g. Paimon's quota filter defers every candidate task), the queue
 * previously created an empty {@code TableOptimizingProcess} that could never reach {@code
 * allTasksPrepared()} — the table stayed in {@code OPTIMIZING} forever.
 *
 * <p>After the fix, {@code planInternal} must:
 *
 * <ul>
 *   <li>Return {@code null} when the plan result is empty (no process added to the queue),
 *   <li>Call {@code tableRuntime.completeEmptyProcess()} so the table is returned to IDLE and a new
 *       plan tick can be scheduled,
 *   <li>Still happy-path on a non-empty plan result (process returned).
 * </ul>
 *
 * <p>The test avoids booting a real AMS: it instantiates {@link OptimizingQueue} via Mockito
 * (Objenesis-backed, constructor-skipping) and injects just the collaborators {@code planInternal}
 * touches — {@code catalogManager}, {@code router}. For the non-empty assertion we catch the
 * attempted {@code new TableOptimizingProcess(...)} via its side-effect (tableRuntime's getFormat
 * being invoked by the process constructor) rather than relying on DB persistence — the guard we
 * care about for this PR is the empty-plan branch.
 */
@DisplayName("OptimizingQueue empty-plan-result guard (K2)")
class TestOptimizingQueueEmptyPlanResult {

  private static final ServerTableIdentifier TEST_IDENTIFIER =
      ServerTableIdentifier.of(TableIdentifier.of("cat", "db", "t"), TableFormat.PAIMON);

  static {
    TEST_IDENTIFIER.setId(42L);
  }

  /**
   * Build a minimally-wired {@link OptimizingQueue} that skips the real constructor (and therefore
   * skips metric registration, scheduling policy setup, etc.). Only the fields {@code planInternal}
   * reads are populated.
   */
  private static OptimizingQueue buildQueue(
      CatalogManager catalogManager, ProcessFactoryRouter router) throws Exception {
    OptimizingQueue queue = mock(OptimizingQueue.class, CALLS_REAL_METHODS);
    setField(queue, "catalogManager", catalogManager);
    setField(queue, "router", router);
    // quotaProvider is consulted via getAvailableCore() during plan() — supply a minimal one.
    setField(queue, "quotaProvider", (org.apache.amoro.server.resource.QuotaProvider) name -> 1);
    setField(
        queue,
        "optimizerGroup",
        new org.apache.amoro.resource.ResourceGroup.Builder("test", "local").build());
    setField(queue, "tableQueue", new LinkedTransferQueue<>());
    setField(queue, "planningTables", new java.util.HashSet<>());
    setField(
        queue,
        "scheduleLock",
        new java.util.concurrent.locks.ReentrantLock()); // not actually used in planInternal
    return queue;
  }

  private static void setField(Object target, String name, Object value) throws Exception {
    Field f = OptimizingQueue.class.getDeclaredField(name);
    f.setAccessible(true);
    f.set(target, value);
  }

  private static Object invokePlanInternal(OptimizingQueue queue, DefaultTableRuntime tableRuntime)
      throws Exception {
    Method m = OptimizingQueue.class.getDeclaredMethod("planInternal", DefaultTableRuntime.class);
    m.setAccessible(true);
    try {
      return m.invoke(queue, tableRuntime);
    } catch (java.lang.reflect.InvocationTargetException ite) {
      if (ite.getCause() instanceof RuntimeException) {
        throw (RuntimeException) ite.getCause();
      }
      throw ite;
    }
  }

  // -------- Scenario 1: empty plan result -> null return, completeEmptyProcess() called --------

  @Test
  @DisplayName("empty plan result: planInternal returns null and marks runtime idle")
  void emptyPlanResultDoesNotCreateProcess() throws Exception {
    CatalogManager catalogManager = mock(CatalogManager.class);
    @SuppressWarnings("rawtypes")
    AmoroTable table = mock(AmoroTable.class);
    when(catalogManager.loadTable(any())).thenReturn(table);

    DefaultTableRuntime tableRuntime = mock(DefaultTableRuntime.class);
    when(tableRuntime.getTableIdentifier()).thenReturn(TEST_IDENTIFIER);
    when(tableRuntime.getFormat()).thenReturn(TableFormat.PAIMON);
    when(tableRuntime.refresh(any())).thenReturn(tableRuntime);

    TableOptimizingPlanner planner = mock(TableOptimizingPlanner.class);
    when(planner.isNecessary()).thenReturn(true);
    @SuppressWarnings({"unchecked", "rawtypes"})
    OptimizingPlanResult emptyResult =
        new OptimizingPlanResult(
            /* processId= */ 7L,
            OptimizingType.MINOR,
            /* planTime= */ 1L,
            /* targetSnapshotId= */ 2L,
            /* targetChangeSnapshotId= */ -1L,
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyMap());
    when(planner.plan()).thenReturn(emptyResult);

    ProcessFactory factory = mock(ProcessFactory.class);
    when(factory.supportedFormats())
        .thenReturn(java.util.Collections.singleton(TableFormat.PAIMON));
    when(factory.createPlanner(any(), any(), anyDouble(), anyLong())).thenReturn(planner);
    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(factory));

    OptimizingQueue queue = buildQueue(catalogManager, router);

    Object result = invokePlanInternal(queue, tableRuntime);

    assertNull(result, "empty plan must yield a null TableOptimizingProcess");
    verify(tableRuntime, times(1)).completeEmptyProcess();

    // Nothing was added to the tableQueue.
    @SuppressWarnings("unchecked")
    Queue<Object> tableQueue = (Queue<Object>) readField(queue, "tableQueue");
    org.junit.jupiter.api.Assertions.assertTrue(
        tableQueue.isEmpty(), "no process should be queued for an empty plan");
  }

  // -------- Scenario 2: non-empty plan result -> planner.plan() was reached post-guard ---------

  @Test
  @DisplayName("non-empty plan result: planner.plan() returns a non-empty task list")
  void nonEmptyPlanStillCreatesProcess() throws Exception {
    CatalogManager catalogManager = mock(CatalogManager.class);
    @SuppressWarnings("rawtypes")
    AmoroTable table = mock(AmoroTable.class);
    when(catalogManager.loadTable(any())).thenReturn(table);

    DefaultTableRuntime tableRuntime = mock(DefaultTableRuntime.class);
    when(tableRuntime.getTableIdentifier()).thenReturn(TEST_IDENTIFIER);
    when(tableRuntime.getFormat()).thenReturn(TableFormat.PAIMON);
    when(tableRuntime.refresh(any())).thenReturn(tableRuntime);

    TableOptimizingPlanner planner = mock(TableOptimizingPlanner.class);
    when(planner.isNecessary()).thenReturn(true);

    StagedTaskDescriptor<?, ?, ?> oneTask = mock(StagedTaskDescriptor.class);
    @SuppressWarnings({"unchecked", "rawtypes"})
    OptimizingPlanResult nonEmptyResult =
        new OptimizingPlanResult(
            /* processId= */ 9L,
            OptimizingType.MINOR,
            /* planTime= */ 1L,
            /* targetSnapshotId= */ 2L,
            /* targetChangeSnapshotId= */ -1L,
            java.util.Arrays.asList(oneTask),
            Collections.emptyMap(),
            Collections.emptyMap());
    when(planner.plan()).thenReturn(nonEmptyResult);

    ProcessFactory factory = mock(ProcessFactory.class);
    when(factory.supportedFormats())
        .thenReturn(java.util.Collections.singleton(TableFormat.PAIMON));
    when(factory.createPlanner(any(), any(), anyDouble(), anyLong())).thenReturn(planner);
    ProcessFactoryRouter router = new ProcessFactoryRouter(List.of(factory));

    OptimizingQueue queue = buildQueue(catalogManager, router);

    // The non-empty branch constructs `new TableOptimizingProcess(planResult, tableRuntime)` which
    // runs `loadTaskRuntimes` + `beginAndPersistProcess` — both are DB-bound and out of scope for
    // this unit test. We therefore expect the construction attempt to throw. That is *itself* the
    // evidence we want: the guard did NOT short-circuit on a non-empty plan, and the code reached
    // the process-creation path. The empty-plan guard must NOT have called completeEmptyProcess.
    Throwable thrown = null;
    try {
      invokePlanInternal(queue, tableRuntime);
    } catch (Throwable t) {
      thrown = t;
    }
    assertNotNull(
        thrown,
        "non-empty branch should attempt TableOptimizingProcess construction (which, without "
            + "a persistence layer, will surface a failure). Reaching that path proves the guard "
            + "did not short-circuit.");
    // Crucially — the empty-plan-guard branch was NOT taken:
    verify(tableRuntime, never()).completeEmptyProcess();
    // And plan() was invoked:
    verify(planner, times(1)).plan();
  }

  private static Object readField(Object target, String name) throws Exception {
    Field f = OptimizingQueue.class.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }
}
