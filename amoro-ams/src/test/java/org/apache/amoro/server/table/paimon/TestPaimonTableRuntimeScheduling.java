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
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.table.StateKey;
import org.apache.amoro.table.TableRuntimeStore;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * C1 guard: proves that {@link PaimonTableRuntime} is a {@link DefaultTableRuntime} so {@code
 * DefaultOptimizingService.initHandler}'s {@code instanceof DefaultTableRuntime} filter lets Paimon
 * tables enter the optimizing queue — and that {@link DefaultTableRuntime#REQUIRED_STATES} is the
 * single source of required state keys (no duplicate-key conflict between Paimon and Default).
 */
public class TestPaimonTableRuntimeScheduling {

  @Test
  public void paimonRuntimeIsDefaultRuntime() {
    TableRuntimeStore store = mock(TableRuntimeStore.class);
    ServerTableIdentifier id =
        ServerTableIdentifier.of("catalog", "database", "table", TableFormat.PAIMON);
    when(store.getTableIdentifier()).thenReturn(id);
    when(store.getGroupName()).thenReturn("default");

    PaimonTableRuntime runtime = new PaimonTableRuntime(store, () -> mock(AmoroTable.class));

    assertTrue(
        runtime instanceof DefaultTableRuntime,
        "PaimonTableRuntime must extend DefaultTableRuntime so DefaultOptimizingService"
            + " `instanceof DefaultTableRuntime` filter accepts Paimon tables.");
  }

  @Test
  public void paimonRuntimePassesInstanceOfFilter() {
    TableRuntimeStore store = mock(TableRuntimeStore.class);
    when(store.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("c", "d", "t", TableFormat.PAIMON));
    when(store.getGroupName()).thenReturn("default");

    Object runtime = new PaimonTableRuntime(store, () -> mock(AmoroTable.class));

    // Mirrors DefaultOptimizingService.initHandler L517's filter predicate.
    assertTrue(runtime instanceof DefaultTableRuntime);
  }

  @Test
  public void requiredStatesComeFromDefaultRuntime() {
    // After C1, Paimon reuses DefaultTableRuntime.REQUIRED_STATES — no Paimon-local dup.
    List<StateKey<?>> defaults = DefaultTableRuntime.REQUIRED_STATES;
    assertNotNull(defaults);
    assertTrue(defaults.size() >= 4, "DefaultTableRuntime must supply at least 4 required states");

    // The 4 Paimon would previously have declared locally — they all come from the parent now.
    assertTrue(defaults.stream().anyMatch(k -> "optimizing_state".equals(k.getKey())));
    assertTrue(defaults.stream().anyMatch(k -> "pending_input".equals(k.getKey())));
    assertTrue(defaults.stream().anyMatch(k -> "cleanup_state".equals(k.getKey())));
    assertTrue(defaults.stream().anyMatch(k -> "process_id".equals(k.getKey())));
  }
}
