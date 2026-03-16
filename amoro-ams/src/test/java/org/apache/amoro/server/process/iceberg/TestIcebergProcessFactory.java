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

package org.apache.amoro.server.process.iceberg;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.table.cleanup.TableRuntimeCleanupState;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TestIcebergProcessFactory {

  @Test
  public void testOpenAndSupportedActions() {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshot.interval", "1h");

    factory.open(properties);

    Map<TableFormat, Set<org.apache.amoro.Action>> supported = factory.supportedActions();
    Assert.assertTrue(supported.get(TableFormat.ICEBERG).contains(IcebergActions.EXPIRE_SNAPSHOTS));
    Assert.assertTrue(
        supported.get(TableFormat.MIXED_ICEBERG).contains(IcebergActions.EXPIRE_SNAPSHOTS));
    Assert.assertTrue(
        supported.get(TableFormat.MIXED_HIVE).contains(IcebergActions.EXPIRE_SNAPSHOTS));

    ProcessTriggerStrategy strategy =
        factory.triggerStrategy(TableFormat.ICEBERG, IcebergActions.EXPIRE_SNAPSHOTS);
    Assert.assertEquals(Duration.ofHours(1), strategy.getTriggerInterval());
  }

  @Test
  public void testTriggerExpireSnapshotWhenDue() {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshot.interval", "1h");
    factory.open(properties);

    LocalExecutionEngine localEngine = mock(LocalExecutionEngine.class);
    doReturn(LocalExecutionEngine.ENGINE_NAME).when(localEngine).name();
    factory.availableExecuteEngines(Arrays.asList(localEngine));

    TableConfiguration tableConfiguration = new TableConfiguration().setExpireSnapshotEnabled(true);
    TableRuntimeCleanupState cleanupState =
        new TableRuntimeCleanupState().setLastSnapshotsExpiringTime(0);

    TableRuntime runtime = mock(TableRuntime.class);
    doReturn(tableConfiguration).when(runtime).getTableConfiguration();
    doReturn(cleanupState).when(runtime).getState(DefaultTableRuntime.CLEANUP_STATE_KEY);

    Optional<org.apache.amoro.process.TableProcess> process =
        factory.trigger(runtime, IcebergActions.EXPIRE_SNAPSHOTS);

    Assert.assertTrue(process.isPresent());
    Assert.assertTrue(process.get() instanceof SnapshotsExpiringProcess);
    Assert.assertEquals(LocalExecutionEngine.ENGINE_NAME, process.get().getExecutionEngine());
  }

  @Test
  public void testTriggerExpireSnapshotNotDue() {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshot.interval", "1h");
    factory.open(properties);

    factory.availableExecuteEngines(Arrays.asList(mock(LocalExecutionEngine.class)));

    TableConfiguration tableConfiguration = new TableConfiguration().setExpireSnapshotEnabled(true);
    long now = System.currentTimeMillis();
    TableRuntimeCleanupState cleanupState =
        new TableRuntimeCleanupState().setLastSnapshotsExpiringTime(now);

    TableRuntime runtime = mock(TableRuntime.class);
    doReturn(tableConfiguration).when(runtime).getTableConfiguration();
    doReturn(cleanupState).when(runtime).getState(DefaultTableRuntime.CLEANUP_STATE_KEY);

    Optional<org.apache.amoro.process.TableProcess> process =
        factory.trigger(runtime, IcebergActions.EXPIRE_SNAPSHOTS);

    Assert.assertFalse(process.isPresent());
  }

  @Test
  public void testTriggerExpireSnapshotDisabled() {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshot.interval", "1h");
    factory.open(properties);

    factory.availableExecuteEngines(Arrays.asList(mock(LocalExecutionEngine.class)));

    TableConfiguration tableConfiguration =
        new TableConfiguration().setExpireSnapshotEnabled(false);
    TableRuntimeCleanupState cleanupState =
        new TableRuntimeCleanupState().setLastSnapshotsExpiringTime(0);

    TableRuntime runtime = mock(TableRuntime.class);
    doReturn(tableConfiguration).when(runtime).getTableConfiguration();
    doReturn(cleanupState).when(runtime).getState(DefaultTableRuntime.CLEANUP_STATE_KEY);

    Optional<org.apache.amoro.process.TableProcess> process =
        factory.trigger(runtime, IcebergActions.EXPIRE_SNAPSHOTS);

    Assert.assertFalse(process.isPresent());
  }
}
