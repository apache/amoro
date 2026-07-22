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

package org.apache.amoro.server.process.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for {@link TableProcessExecutor}.
 */
public class TestTableProcessExecutor {

  /**
   * When a recovered process has a non-empty externalProcessIdentifier and the engine returns
   * UNKNOWN (simulating AMS restart where LocalExecutionEngine's in-memory process map is cleared),
   * the executor should mark the process as FAILED instead of leaving it stuck in UNKNOWN.
   */
  @Test
  public void testUnknownStatusFromEngineMarksAsFailed() {
    // Mock store: simulate a recovered process with RUNNING status and a stale identifier
    TableProcessStore store = Mockito.mock(TableProcessStore.class);
    when(store.getStatus()).thenReturn(ProcessStatus.RUNNING);
    when(store.getExternalProcessIdentifier()).thenReturn("stale-identifier-from-before-restart");
    when(store.getProcessId()).thenReturn(1L);
    when(store.tryTransitState(
            any(ProcessStatus.class),
            any(ProcessEvent.class),
            anyString(),
            anyString(),
            any(),
            any()))
        .thenReturn(true);

    // Mock engine: returns UNKNOWN for the stale identifier (process lost after restart)
    ExecuteEngine engine = Mockito.mock(ExecuteEngine.class);
    when(engine.getStatus("stale-identifier-from-before-restart"))
        .thenReturn(ProcessStatus.UNKNOWN);

    // Mock table process
    TableProcess process = Mockito.mock(TableProcess.class);
    when(process.getProcessParameters()).thenReturn(java.util.Collections.emptyMap());
    when(process.getSummary()).thenReturn(java.util.Collections.emptyMap());

    // Track the final status transit
    AtomicReference<ProcessStatus> finalStatus = new AtomicReference<>();
    when(store.tryTransitState(
            any(ProcessStatus.class),
            any(ProcessEvent.class),
            anyString(),
            anyString(),
            any(),
            any()))
        .thenAnswer(
            invocation -> {
              ProcessStatus status = invocation.getArgument(0);
              // Capture the FAILED status transit (not the SUBMIT_REQUESTED which is skipped)
              if (status == ProcessStatus.FAILED) {
                finalStatus.set(status);
                when(store.getStatus()).thenReturn(ProcessStatus.FAILED);
              }
              return true;
            });

    TableProcessExecutor executor = new TableProcessExecutor(process, store, engine);
    executor.run();

    // Verify the process was marked as FAILED, not left as UNKNOWN
    assertEquals(
        ProcessStatus.FAILED,
        finalStatus.get(),
        "Process should be marked FAILED when engine returns UNKNOWN");
  }

  /**
   * When the engine returns a normal terminal status (SUCCESS), the executor should transit to
   * SUCCESS normally. This is a regression test to ensure the UNKNOWN fix doesn't break the normal
   * path.
   */
  @Test
  public void testNormalSuccessPathNotAffected() {
    TableProcessStore store = Mockito.mock(TableProcessStore.class);
    when(store.getStatus()).thenReturn(ProcessStatus.RUNNING);
    when(store.getExternalProcessIdentifier()).thenReturn("valid-identifier");
    when(store.getProcessId()).thenReturn(2L);
    when(store.tryTransitState(
            any(ProcessStatus.class),
            any(ProcessEvent.class),
            anyString(),
            anyString(),
            any(),
            any()))
        .thenReturn(true);

    ExecuteEngine engine = Mockito.mock(ExecuteEngine.class);
    when(engine.getStatus("valid-identifier")).thenReturn(ProcessStatus.SUCCESS);

    TableProcess process = Mockito.mock(TableProcess.class);
    when(process.getProcessParameters()).thenReturn(java.util.Collections.emptyMap());
    when(process.getSummary()).thenReturn(java.util.Collections.emptyMap());

    AtomicReference<ProcessStatus> finalStatus = new AtomicReference<>();
    when(store.tryTransitState(
            any(ProcessStatus.class),
            any(ProcessEvent.class),
            anyString(),
            anyString(),
            any(),
            any()))
        .thenAnswer(
            invocation -> {
              ProcessStatus status = invocation.getArgument(0);
              if (status == ProcessStatus.SUCCESS || status == ProcessStatus.RUNNING) {
                finalStatus.set(status);
              }
              return true;
            });

    TableProcessExecutor executor = new TableProcessExecutor(process, store, engine);
    executor.run();

    assertEquals(
        ProcessStatus.SUCCESS,
        finalStatus.get(),
        "Process should complete with SUCCESS on normal path");
  }
}
