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

package org.apache.amoro.server.process.paimon;

import org.apache.amoro.PaimonActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TestPaimonProcessFactory {

  @Test
  public void testSupportedActionAndTriggerStrategy() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("sync-table-meta.enabled", "true");
    properties.put("sync-table-meta.interval", "2 h");
    properties.put("sync-table-meta.trigger-parallelism", "3");
    factory.open(properties);

    Assert.assertTrue(
        factory.supportedActions().getOrDefault(TableFormat.PAIMON, Collections.emptySet()).stream()
            .anyMatch(action -> action.equals(PaimonActions.SYNC_TABLE_META)));

    ProcessTriggerStrategy strategy =
        factory.triggerStrategy(TableFormat.PAIMON, PaimonActions.SYNC_TABLE_META);
    Assert.assertEquals(3, strategy.getTriggerParallelism());
    Assert.assertEquals(2 * 60 * 60 * 1000L, strategy.getTriggerInterval().toMillis());
  }

  @Test
  public void testTriggerAndRecoverUseLocalEngine() throws Exception {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Collections.singletonMap("sync-table-meta.enabled", "true"));

    DefaultTableRuntime runtime = Mockito.mock(DefaultTableRuntime.class);
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);

    Optional<TableProcess> process = factory.trigger(runtime, PaimonActions.SYNC_TABLE_META);
    Assert.assertTrue(process.isPresent());
    Assert.assertEquals(LocalExecutionEngine.ENGINE_NAME, process.get().getExecutionEngine());
  }

  @Test
  public void testOpenWithEmptyPropertiesUseDefaults() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Collections.emptyMap());

    Set<org.apache.amoro.Action> actions =
        factory.supportedActions().getOrDefault(TableFormat.PAIMON, Collections.emptySet());
    Assert.assertTrue(actions.contains(PaimonActions.SYNC_TABLE_META));
    Assert.assertTrue(actions.contains(PaimonActions.EXPIRE_SNAPSHOTS));

    ProcessTriggerStrategy syncStrategy =
        factory.triggerStrategy(TableFormat.PAIMON, PaimonActions.SYNC_TABLE_META);
    Assert.assertEquals(1, syncStrategy.getTriggerParallelism());
    Assert.assertEquals(60 * 60 * 1000L, syncStrategy.getTriggerInterval().toMillis());
  }

  @Test
  public void testOpenShouldResetPreviousActions() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Collections.emptyMap());

    Map<String, String> disabled = new HashMap<>();
    disabled.put("sync-table-meta.enabled", "false");
    disabled.put("expire-snapshots.enabled", "false");
    factory.open(disabled);

    Set<org.apache.amoro.Action> actions =
        factory.supportedActions().getOrDefault(TableFormat.PAIMON, Collections.emptySet());
    Assert.assertFalse(actions.contains(PaimonActions.SYNC_TABLE_META));
    Assert.assertFalse(actions.contains(PaimonActions.EXPIRE_SNAPSHOTS));
  }

  @Test
  public void testCloseShouldClearActions() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    factory.open(Collections.emptyMap());
    factory.close();

    Set<org.apache.amoro.Action> actions =
        factory.supportedActions().getOrDefault(TableFormat.PAIMON, Collections.emptySet());
    Assert.assertTrue(actions.isEmpty());
  }
}
