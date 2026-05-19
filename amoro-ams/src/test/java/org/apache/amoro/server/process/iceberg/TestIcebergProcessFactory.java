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

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.process.LocalExecutionEngine;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
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
    assertSupportedAction("expire-snapshots", IcebergActions.EXPIRE_SNAPSHOTS, Duration.ofHours(1));
    assertSupportedAction(
        "clean-orphan-files", IcebergActions.DELETE_ORPHANS, Duration.ofHours(24));
    assertSupportedAction(
        "clean-dangling-delete-files", IcebergActions.CLEAN_DANGLING_DELETE, Duration.ofHours(24));
    assertSupportedAction("expire-data", IcebergActions.EXPIRE_DATA, Duration.ofHours(24));
  }

  @Test
  public void testTriggerActionWhenDue() {
    assertTriggerWhenDue(
        "expire-snapshots", IcebergActions.EXPIRE_SNAPSHOTS, SnapshotsExpiringProcess.class, 0);
    assertTriggerWhenDue(
        "clean-orphan-files", IcebergActions.DELETE_ORPHANS, OrphanFilesCleaningProcess.class, 0);
    assertTriggerWhenDue(
        "clean-dangling-delete-files",
        IcebergActions.CLEAN_DANGLING_DELETE,
        DanglingDeleteFilesCleaningProcess.class,
        0);
    assertTriggerWhenDue("expire-data", IcebergActions.EXPIRE_DATA, DataExpiringProcess.class, 0);
  }

  @Test
  public void testTriggerActionNotDue() {
    assertTriggerNotDue(
        "expire-snapshots", IcebergActions.EXPIRE_SNAPSHOTS, System.currentTimeMillis());
    assertTriggerNotDue(
        "clean-orphan-files", IcebergActions.DELETE_ORPHANS, System.currentTimeMillis());
    assertTriggerNotDue(
        "clean-dangling-delete-files",
        IcebergActions.CLEAN_DANGLING_DELETE,
        System.currentTimeMillis());
    assertTriggerNotDue("expire-data", IcebergActions.EXPIRE_DATA, System.currentTimeMillis());
  }

  @Test
  public void testTriggerActionDisabled() {
    assertTriggerDisabled("expire-snapshots", IcebergActions.EXPIRE_SNAPSHOTS, false, 0);
    assertTriggerDisabled("clean-orphan-files", IcebergActions.DELETE_ORPHANS, false, 0);
    assertTriggerDisabled(
        "clean-dangling-delete-files", IcebergActions.CLEAN_DANGLING_DELETE, false, 0);
    assertTriggerDisabled("expire-data", IcebergActions.EXPIRE_DATA, false, 0);
  }

  @Test
  public void testRecoverExpireSnapshotsProcess() {
    assertRecover(
        "expire-snapshots", IcebergActions.EXPIRE_SNAPSHOTS, SnapshotsExpiringProcess.class);
  }

  @Test
  public void testRecoverOrphanFilesCleaningProcess() {
    assertRecover(
        "clean-orphan-files", IcebergActions.DELETE_ORPHANS, OrphanFilesCleaningProcess.class);
  }

  @Test
  public void testRecoverCleanDanglingDeleteProcess() {
    assertRecover(
        "clean-dangling-delete-files",
        IcebergActions.CLEAN_DANGLING_DELETE,
        DanglingDeleteFilesCleaningProcess.class);
  }

  @Test
  public void testRecoverDataExpiringProcess() {
    assertRecover("expire-data", IcebergActions.EXPIRE_DATA, DataExpiringProcess.class);
  }

  @Test
  public void testRecoverUnsupportedActionThrows() {
    IcebergProcessFactory factory = openedFactory("expire-snapshots");

    LocalExecutionEngine localEngine = mock(LocalExecutionEngine.class);
    doReturn(LocalExecutionEngine.ENGINE_NAME).when(localEngine).name();
    factory.availableExecuteEngines(Arrays.asList(localEngine));

    TableProcessStore store = mock(TableProcessStore.class);
    doReturn(IcebergActions.REWRITE).when(store).getAction();

    Assert.assertThrows(
        RecoverProcessFailedException.class,
        () -> factory.recover(mock(TableRuntime.class), store));
  }

  @Test
  public void testRecoverWithoutLocalEngineThrows() {
    IcebergProcessFactory factory = openedFactory("expire-snapshots");

    TableProcessStore store = mock(TableProcessStore.class);
    doReturn(IcebergActions.EXPIRE_SNAPSHOTS).when(store).getAction();

    Assert.assertThrows(
        RecoverProcessFailedException.class,
        () -> factory.recover(mock(TableRuntime.class), store));
  }

  private void assertRecover(String configKey, Action action, Class<?> processClass) {
    IcebergProcessFactory factory = openedFactory(configKey);

    LocalExecutionEngine localEngine = mock(LocalExecutionEngine.class);
    doReturn(LocalExecutionEngine.ENGINE_NAME).when(localEngine).name();
    factory.availableExecuteEngines(Arrays.asList(localEngine));

    TableProcessStore store = mock(TableProcessStore.class);
    doReturn(action).when(store).getAction();

    TableProcess process = factory.recover(mock(TableRuntime.class), store);

    Assert.assertNotNull(process);
    Assert.assertTrue(processClass.isInstance(process));
    Assert.assertEquals(action, process.getAction());
    Assert.assertEquals(LocalExecutionEngine.ENGINE_NAME, process.getExecutionEngine());
  }

  private IcebergProcessFactory openedFactory(String configKey) {
    IcebergProcessFactory factory = new IcebergProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put(configKey + ".enabled", "true");
    properties.put(configKey + ".interval", "1h");
    factory.open(properties);
    return factory;
  }

  private void assertSupportedAction(
      String configKey, org.apache.amoro.Action action, Duration interval) {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put(configKey + ".enabled", "true");
    properties.put(configKey + ".interval", interval.toHours() + "h");

    factory.open(properties);

    Map<TableFormat, Set<org.apache.amoro.Action>> supported = factory.supportedActions();
    Assert.assertTrue(supported.get(TableFormat.ICEBERG).contains(action));
    Assert.assertTrue(supported.get(TableFormat.MIXED_ICEBERG).contains(action));
    Assert.assertTrue(supported.get(TableFormat.MIXED_HIVE).contains(action));

    ProcessTriggerStrategy strategy = factory.triggerStrategy(TableFormat.ICEBERG, action);
    Assert.assertEquals(interval, strategy.getTriggerInterval());
  }

  private void assertTriggerWhenDue(
      String configKey, org.apache.amoro.Action action, Class<?> processClass, long lastTime) {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put(configKey + ".enabled", "true");
    properties.put(configKey + ".interval", "1h");
    factory.open(properties);

    LocalExecutionEngine localEngine = mock(LocalExecutionEngine.class);
    doReturn(LocalExecutionEngine.ENGINE_NAME).when(localEngine).name();
    factory.availableExecuteEngines(Arrays.asList(localEngine));

    TableRuntime runtime = createRuntime(configKey, true, lastTime);

    Optional<org.apache.amoro.process.TableProcess> process = factory.trigger(runtime, action);

    Assert.assertTrue(process.isPresent());
    Assert.assertTrue(processClass.isInstance(process.get()));
    Assert.assertEquals(LocalExecutionEngine.ENGINE_NAME, process.get().getExecutionEngine());
  }

  private void assertTriggerNotDue(
      String configKey, org.apache.amoro.Action action, long lastTime) {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put(configKey + ".enabled", "true");
    properties.put(configKey + ".interval", "1h");
    factory.open(properties);

    factory.availableExecuteEngines(Arrays.asList(mock(LocalExecutionEngine.class)));

    TableRuntime runtime = createRuntime(configKey, true, lastTime);

    Optional<org.apache.amoro.process.TableProcess> process = factory.trigger(runtime, action);

    Assert.assertFalse(process.isPresent());
  }

  private void assertTriggerDisabled(
      String configKey, org.apache.amoro.Action action, boolean enabled, long lastTime) {
    IcebergProcessFactory factory = new IcebergProcessFactory();

    Map<String, String> properties = new HashMap<>();
    properties.put(configKey + ".enabled", "true");
    properties.put(configKey + ".interval", "1h");
    factory.open(properties);

    factory.availableExecuteEngines(Arrays.asList(mock(LocalExecutionEngine.class)));

    TableRuntime runtime = createRuntime(configKey, enabled, lastTime);

    Optional<org.apache.amoro.process.TableProcess> process = factory.trigger(runtime, action);

    Assert.assertFalse(process.isPresent());
  }

  private TableRuntime createRuntime(String configKey, boolean enabled, long lastTime) {
    TableConfiguration tableConfiguration = new TableConfiguration();
    if ("expire-snapshots".equals(configKey)) {
      tableConfiguration.setExpireSnapshotEnabled(enabled);
    } else if ("clean-orphan-files".equals(configKey)) {
      tableConfiguration.setCleanOrphanEnabled(enabled);
    } else if ("clean-dangling-delete-files".equals(configKey)) {
      tableConfiguration.setDeleteDanglingDeleteFilesEnabled(enabled);
    } else if ("expire-data".equals(configKey)) {
      tableConfiguration.setExpiringDataConfig(new DataExpirationConfig().setEnabled(enabled));
    }

    TableRuntimeCleanupState cleanupState = new TableRuntimeCleanupState();
    if ("expire-snapshots".equals(configKey)) {
      cleanupState.setLastSnapshotsExpiringTime(lastTime);
    } else if ("clean-orphan-files".equals(configKey)) {
      cleanupState.setLastOrphanFilesCleanTime(lastTime);
    } else if ("clean-dangling-delete-files".equals(configKey)) {
      cleanupState.setLastDanglingDeleteFilesCleanTime(lastTime);
    } else if ("expire-data".equals(configKey)) {
      cleanupState.setLastDataExpiringTime(lastTime);
    }

    TableRuntime runtime = mock(TableRuntime.class);
    doReturn(tableConfiguration).when(runtime).getTableConfiguration();
    doReturn(cleanupState).when(runtime).getState(DefaultTableRuntime.CLEANUP_STATE_KEY);

    return runtime;
  }
}
