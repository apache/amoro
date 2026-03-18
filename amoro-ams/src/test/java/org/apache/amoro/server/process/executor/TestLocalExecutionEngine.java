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

import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.EngineType;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.table.StateKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class TestLocalExecutionEngine {

  @Test
  public void testSubmitAndCompleteLocalProcess() throws Exception {
    LocalExecutionEngine engine = new LocalExecutionEngine();
    engine.open(Collections.singletonMap("default.thread-count", "1"));
    try {
      CountDownLatch executed = new CountDownLatch(1);
      AtomicBoolean invoked = new AtomicBoolean(false);
      String processId =
          engine.submitTableProcess(
              new TestingLocalProcess(
                  () -> {
                    invoked.set(true);
                    executed.countDown();
                  },
                  LocalExecutionEngine.DEFAULT_POOL));

      Assert.assertTrue(executed.await(10, TimeUnit.SECONDS));
      waitForTerminalStatus(engine, processId);
      Assert.assertTrue(invoked.get());
      Assert.assertEquals(ProcessStatus.SUCCESS, engine.getStatus(processId));
    } finally {
      engine.close();
    }
  }

  @Test
  public void testCancelLocalProcess() throws Exception {
    LocalExecutionEngine engine = new LocalExecutionEngine();
    engine.open(Collections.singletonMap("table-meta-sync.thread-count", "1"));
    try {
      CountDownLatch started = new CountDownLatch(1);
      CountDownLatch release = new CountDownLatch(1);
      String processId =
          engine.submitTableProcess(
              new TestingLocalProcess(
                  () -> {
                    started.countDown();
                    try {
                      release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                      Thread.currentThread().interrupt();
                    }
                  },
                  "table-meta-sync"));

      Assert.assertTrue(started.await(10, TimeUnit.SECONDS));
      Assert.assertEquals(ProcessStatus.RUNNING, engine.getStatus(processId));

      ProcessStatus status =
          engine.tryCancelTableProcess(
              new TestingLocalProcess(() -> {}, "table-meta-sync"), processId);
      Assert.assertEquals(ProcessStatus.CANCELING, status);

      release.countDown();
      waitForTerminalStatus(engine, processId);
      Assert.assertEquals(ProcessStatus.CANCELED, engine.getStatus(processId));
    } finally {
      engine.close();
    }
  }

  private void waitForTerminalStatus(LocalExecutionEngine engine, String processId)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
    while (System.currentTimeMillis() < deadline) {
      ProcessStatus status = engine.getStatus(processId);
      if (status != ProcessStatus.RUNNING && status != ProcessStatus.CANCELING) {
        return;
      }
      Thread.sleep(50L);
    }
    Assert.fail("Process did not reach terminal status within timeout");
  }

  private static class TestingLocalProcess extends TableProcess implements LocalProcess {

    private final Runnable runnable;
    private final String tag;

    private TestingLocalProcess(Runnable runnable, String tag) {
      super(new TestingTableRuntime(), new NoopExecuteEngine());
      this.runnable = runnable;
      this.tag = tag;
    }

    @Override
    public void run() {
      runnable.run();
    }

    @Override
    public String tag() {
      return tag;
    }

    @Override
    public Action getAction() {
      return Action.register("local-test");
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getSummary() {
      return Collections.emptyMap();
    }
  }

  private static class TestingTableRuntime implements TableRuntime {

    @Override
    public List<? extends TableProcessStore> getProcessStates() {
      return Collections.emptyList();
    }

    @Override
    public List<? extends TableProcessStore> getProcessStates(Action action) {
      return Collections.emptyList();
    }

    @Override
    public String getGroupName() {
      return "default";
    }

    @Override
    public ServerTableIdentifier getTableIdentifier() {
      return ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON);
    }

    @Override
    public org.apache.amoro.AmoroTable<?> loadTable() {
      throw new UnsupportedOperationException();
    }

    @Override
    public TableConfiguration getTableConfiguration() {
      return new TableConfiguration();
    }

    @Override
    public Map<String, String> getTableConfig() {
      return Collections.emptyMap();
    }

    @Override
    public <T> T getState(StateKey<T> key) {
      return null;
    }

    @Override
    public <T> void updateState(StateKey<T> key, Function<T, T> updater) {}

    @Override
    public void registerMetric(MetricRegistry metricRegistry) {}

    @Override
    public void unregisterMetric() {}
  }

  private static class NoopExecuteEngine implements ExecuteEngine {

    @Override
    public EngineType engineType() {
      return EngineType.of("noop");
    }

    @Override
    public ProcessStatus getStatus(String processIdentifier) {
      return ProcessStatus.SUCCESS;
    }

    @Override
    public String submitTableProcess(TableProcess tableProcess) {
      return "noop";
    }

    @Override
    public ProcessStatus tryCancelTableProcess(
        TableProcess tableProcess, String processIdentifier) {
      return ProcessStatus.CANCELED;
    }

    @Override
    public void open(Map<String, String> properties) {}

    @Override
    public void close() {}

    @Override
    public String name() {
      return "noop";
    }
  }
}
