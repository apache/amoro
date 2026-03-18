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

package org.apache.amoro.server.process;

import org.apache.amoro.Action;
import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.EngineType;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.AMSManagerTestBase;
import org.apache.amoro.table.StateKey;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class TestProcessServiceRetryScheduling extends AMSManagerTestBase {
  private static final long RETRY_ASSERT_TIMEOUT_SECONDS = 8L;

  @Test(timeout = 20_000)
  public void testRetryDelayShouldNotBlockProcessExecutionThreads() throws Exception {
    Field retrySchedulingPool = ProcessService.class.getDeclaredField("retrySchedulingPool");
    Assert.assertTrue(
        ScheduledExecutorService.class.isAssignableFrom(retrySchedulingPool.getType()));

    ProcessService processService = new ProcessService(null);
    BlockingAwareFailingExecuteEngine executeEngine = new BlockingAwareFailingExecuteEngine(11);
    processService.installExecuteEngine(executeEngine);
    processService.installActionCoordinator(new MockActionCoordinator(executeEngine));

    try {
      for (int i = 0; i < 11; i++) {
        TableRuntime runtime = new TestingTableRuntime(10_000L + i);
        processService.register(
            runtime,
            new TestingTableProcess(runtime, executeEngine, MockActionCoordinator.DEFAULT_ACTION));
      }

      Assert.assertTrue(
          "失败重试应异步调度，不能因为等待重试而阻塞第 11 个 process 的提交",
          executeEngine.awaitSubmissions(RETRY_ASSERT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    } finally {
      shutdownExecutor(processService, "processExecutionPool");
      shutdownExecutor(processService, "retrySchedulingPool");
      processService.dispose();
    }
  }

  private void shutdownExecutor(ProcessService processService, String fieldName) {
    try {
      Field field = ProcessService.class.getDeclaredField(fieldName);
      field.setAccessible(true);
      Object executor = field.get(processService);
      if (executor instanceof ExecutorService) {
        ExecutorService executorService = (ExecutorService) executor;
        executorService.shutdown();
        if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
          executorService.shutdownNow();
        }
      }
    } catch (NoSuchFieldException ignored) {
      // Some fields only exist after the production change is applied.
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static class BlockingAwareFailingExecuteEngine implements ExecuteEngine {
    private final CountDownLatch submissionLatch;
    private final Map<String, AtomicInteger> statusChecks = new ConcurrentHashMap<>();
    private final AtomicLong identifierGenerator = new AtomicLong(0L);

    private BlockingAwareFailingExecuteEngine(int expectedSubmissions) {
      this.submissionLatch = new CountDownLatch(expectedSubmissions);
    }

    @Override
    public EngineType engineType() {
      return EngineType.of(name());
    }

    @Override
    public ProcessStatus getStatus(String processIdentifier) {
      AtomicInteger checks =
          statusChecks.computeIfAbsent(processIdentifier, ignored -> new AtomicInteger(0));
      return checks.incrementAndGet() == 1 ? ProcessStatus.RUNNING : ProcessStatus.FAILED;
    }

    @Override
    public String submitTableProcess(TableProcess tableProcess) {
      submissionLatch.countDown();
      return "failed-process-" + identifierGenerator.incrementAndGet();
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
      return "retry-fail";
    }

    private boolean awaitSubmissions(long timeout, TimeUnit unit) throws InterruptedException {
      return submissionLatch.await(timeout, unit);
    }
  }

  private static class TestingTableProcess extends TableProcess {
    private final Action action;

    private TestingTableProcess(TableRuntime tableRuntime, ExecuteEngine engine, Action action) {
      super(tableRuntime, engine);
      this.action = action;
    }

    @Override
    public Action getAction() {
      return action;
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
    private final ServerTableIdentifier tableIdentifier;

    private TestingTableRuntime(long tableId) {
      this.tableIdentifier =
          ServerTableIdentifier.of(
              tableId, "catalog", "db", "table_" + tableId, TableFormat.PAIMON);
    }

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
      return tableIdentifier;
    }

    @Override
    public AmoroTable<?> loadTable() {
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
}
