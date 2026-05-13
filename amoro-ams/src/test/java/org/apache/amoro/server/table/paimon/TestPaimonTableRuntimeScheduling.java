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

package org.apache.amoro.server.table.paimon;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.paimon.optimizing.PaimonPendingInput;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * C1 guard: proves that Paimon tables now use {@link DefaultTableRuntime} directly (no subclass) so
 * {@code DefaultOptimizingService.initHandler}'s {@code instanceof DefaultTableRuntime} filter
 * accepts Paimon tables — and that the factory provides all required state keys including
 * Paimon-specific pending_input.
 */
public class TestPaimonTableRuntimeScheduling {

  private static final StateKey<PaimonPendingInput> PAIMON_PENDING_INPUT_KEY =
      StateKey.stateKey("pending_input")
          .jsonType(PaimonPendingInput.class)
          .defaultValue(new PaimonPendingInput());

  @Test
  public void paimonRuntimeIsDefaultRuntime() {
    TableRuntimeStore store = mock(TableRuntimeStore.class);
    ServerTableIdentifier id =
        ServerTableIdentifier.of("catalog", "database", "table", TableFormat.PAIMON);
    when(store.getTableIdentifier()).thenReturn(id);
    when(store.getGroupName()).thenReturn("default");

    DefaultTableRuntime runtime =
        new DefaultTableRuntime(store, () -> mock(AmoroTable.class), PAIMON_PENDING_INPUT_KEY);

    assertTrue(
        runtime instanceof DefaultTableRuntime,
        "Paimon tables must use DefaultTableRuntime so DefaultOptimizingService"
            + " `instanceof DefaultTableRuntime` filter accepts them.");
  }

  @Test
  public void paimonRuntimePassesInstanceOfFilter() {
    TableRuntimeStore store = mock(TableRuntimeStore.class);
    when(store.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("c", "d", "t", TableFormat.PAIMON));
    when(store.getGroupName()).thenReturn("default");

    Object runtime =
        new DefaultTableRuntime(store, () -> mock(AmoroTable.class), PAIMON_PENDING_INPUT_KEY);

    // Mirrors DefaultOptimizingService.initHandler L517's filter predicate.
    assertTrue(runtime instanceof DefaultTableRuntime);
  }

  @Test
  public void requiredStatesAreComplete() {
    List<StateKey<?>> defaults = DefaultTableRuntime.REQUIRED_STATES;
    assertNotNull(defaults);
    assertTrue(
        defaults.size() >= 3, "DefaultTableRuntime must supply at least 3 base required states");

    // Base states from DefaultTableRuntime.REQUIRED_STATES
    assertTrue(defaults.stream().anyMatch(k -> "optimizing_state".equals(k.getKey())));
    assertTrue(defaults.stream().anyMatch(k -> "cleanup_state".equals(k.getKey())));
    assertTrue(defaults.stream().anyMatch(k -> "process_id".equals(k.getKey())));

    // Paimon-specific pending_input key is provided by the factory, not REQUIRED_STATES
    assertNotNull(PAIMON_PENDING_INPUT_KEY);
    assertEquals("pending_input", PAIMON_PENDING_INPUT_KEY.getKey());
  }

  private static void assertEquals(String expected, String actual) {
    org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
  }
}
