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
import org.apache.amoro.process.ProcessEvent;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessStatusInfo;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.table.StateKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class TestTableProcessExecutor {

  @Test
  public void testBuildFailureMessageContainsStackTrace() {
    Throwable throwable = new IllegalStateException("submit failed");

    String failureMessage = TableProcessExecutor.buildFailureMessage(throwable);

    Assert.assertTrue(failureMessage.contains("IllegalStateException"));
    Assert.assertTrue(failureMessage.contains("submit failed"));
    Assert.assertTrue(failureMessage.contains("at "));
  }

  @Test
  public void testBuildFailureMessageDoesNotTruncateThrowable() {
    String longMessage = repeat("x", 6000);
    Throwable throwable = new RuntimeException(longMessage);

    String failureMessage = TableProcessExecutor.buildFailureMessage(throwable);

    Assert.assertTrue(failureMessage.length() > 4096);
    Assert.assertTrue(failureMessage.contains(longMessage));
  }

  @Test
  public void testPersistRemoteFailureMessageWhenFirstPollFails() {
    String remoteError = "org.apache.spark.sql.catalyst.parser.ParseException: CALL failed";
    InMemoryTableProcessStore store = new InMemoryTableProcessStore();
    SequencedExecuteEngine engine =
        new SequencedExecuteEngine(
            ProcessStatusInfo.of(ProcessStatus.FAILED, remoteError), Collections.emptyList());

    TableProcessExecutor executor =
        new TableProcessExecutor(new TestingTableProcess(), store, engine);

    executor.run();

    Assert.assertEquals(ProcessStatus.FAILED, store.getStatus());
    Assert.assertEquals(remoteError, store.getFailMessage());
    Assert.assertEquals("qid-1", store.getExternalProcessIdentifier());
    Assert.assertTrue(store.getFinishTime() > 0);
    Assert.assertEquals(
        Arrays.asList(ProcessEvent.SUBMIT_REQUESTED.name(), ProcessEvent.COMPLETE_FAILED.name()),
        store.getEvents());
  }

  @Test
  public void testPersistQidWhenFirstPollThrowsException() {
    InMemoryTableProcessStore store = new InMemoryTableProcessStore();
    ThrowingFirstPollExecuteEngine engine =
        new ThrowingFirstPollExecuteEngine(new RuntimeException("first poll failed"));

    TableProcessExecutor executor =
        new TableProcessExecutor(new TestingTableProcess(), store, engine);

    executor.run();

    Assert.assertEquals(ProcessStatus.FAILED, store.getStatus());
    Assert.assertTrue(store.getFailMessage().contains("first poll failed"));
    Assert.assertEquals("qid-1", store.getExternalProcessIdentifier());
    Assert.assertEquals(
        Arrays.asList(ProcessEvent.SUBMIT_REQUESTED.name(), ProcessEvent.COMPLETE_FAILED.name()),
        store.getEvents());
  }

  @Test
  public void testPersistQidBeforeFirstStatusPoll() {
    AtomicInteger sequence = new AtomicInteger(0);
    OrderedInMemoryTableProcessStore store = new OrderedInMemoryTableProcessStore(sequence);
    SequencedExecuteEngine engine =
        new SequencedExecuteEngine(
            ProcessStatusInfo.of(ProcessStatus.RUNNING, ""),
            Collections.singletonList(ProcessStatusInfo.of(ProcessStatus.SUCCESS, "")),
            sequence);

    TableProcessExecutor executor =
        new TableProcessExecutor(new TestingTableProcess(), store, engine);

    executor.run();

    Assert.assertTrue(store.getSubmitEventOrder() > 0);
    Assert.assertTrue(engine.getFirstPollOrder() > 0);
    Assert.assertTrue(store.getSubmitEventOrder() < engine.getFirstPollOrder());
  }

  @Test
  public void testPersistQidWhenFirstPollReturnsCanceled() {
    InMemoryTableProcessStore store = new InMemoryTableProcessStore();
    SequencedExecuteEngine engine =
        new SequencedExecuteEngine(
            ProcessStatusInfo.of(ProcessStatus.CANCELED, ""), Collections.emptyList());

    TableProcessExecutor executor =
        new TableProcessExecutor(new TestingTableProcess(), store, engine);

    executor.run();

    Assert.assertEquals(ProcessStatus.CANCELED, store.getStatus());
    Assert.assertEquals("qid-1", store.getExternalProcessIdentifier());
    Assert.assertEquals(
        Arrays.asList(ProcessEvent.SUBMIT_REQUESTED.name(), ProcessEvent.CANCEL_REQUESTED.name()),
        store.getEvents());
  }

  @Test
  public void testPendingStatusContinuesPollingUntilSuccess() {
    InMemoryTableProcessStore store = new InMemoryTableProcessStore();
    SequencedExecuteEngine engine =
        new SequencedExecuteEngine(
            ProcessStatusInfo.of(ProcessStatus.PENDING, ""),
            Arrays.asList(
                ProcessStatusInfo.of(ProcessStatus.RUNNING, ""),
                ProcessStatusInfo.of(ProcessStatus.SUCCESS, "")));

    TableProcessExecutor executor =
        new TableProcessExecutor(new TestingTableProcess(), store, engine);

    executor.run();

    Assert.assertEquals(ProcessStatus.SUCCESS, store.getStatus());
    Assert.assertEquals("", store.getFailMessage());
    Assert.assertEquals("qid-1", store.getExternalProcessIdentifier());
    Assert.assertTrue(store.getFinishTime() > 0);
    Assert.assertEquals(
        Arrays.asList(ProcessEvent.SUBMIT_REQUESTED.name(), ProcessEvent.COMPLETE_SUCCESS.name()),
        store.getEvents());
    Assert.assertEquals(3, engine.getPollCount());
  }

  private static class SequencedExecuteEngine implements ExecuteEngine {

    private final ProcessStatusInfo firstStatus;
    private final List<ProcessStatusInfo> remainingStatuses;
    private final AtomicInteger pollCount = new AtomicInteger();
    private final AtomicInteger callSequence;
    private int firstPollOrder = -1;

    private SequencedExecuteEngine(
        ProcessStatusInfo firstStatus, List<ProcessStatusInfo> remainingStatuses) {
      this(firstStatus, remainingStatuses, null);
    }

    private SequencedExecuteEngine(
        ProcessStatusInfo firstStatus,
        List<ProcessStatusInfo> remainingStatuses,
        AtomicInteger callSequence) {
      this.firstStatus = firstStatus;
      this.remainingStatuses = new ArrayList<>(remainingStatuses);
      this.callSequence = callSequence;
    }

    @Override
    public EngineType engineType() {
      return EngineType.of("test-engine");
    }

    @Override
    public ProcessStatus getStatus(String processIdentifier) {
      return getStatusInfo(processIdentifier).getStatus();
    }

    @Override
    public ProcessStatusInfo getStatusInfo(String processIdentifier) {
      int current = pollCount.getAndIncrement();
      if (current == 0 && callSequence != null) {
        firstPollOrder = callSequence.incrementAndGet();
      }
      if (current == 0) {
        return firstStatus;
      }
      if (remainingStatuses.isEmpty()) {
        return firstStatus;
      }
      int index = Math.min(current - 1, remainingStatuses.size() - 1);
      return remainingStatuses.get(index);
    }

    @Override
    public String submitTableProcess(TableProcess tableProcess) {
      return "qid-1";
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
      return "test-engine";
    }

    public int getPollCount() {
      return pollCount.get();
    }

    public int getFirstPollOrder() {
      return firstPollOrder;
    }
  }

  private static String repeat(String value, int count) {
    StringBuilder builder = new StringBuilder(value.length() * count);
    for (int i = 0; i < count; i++) {
      builder.append(value);
    }
    return builder.toString();
  }

  private static class ThrowingFirstPollExecuteEngine implements ExecuteEngine {

    private final RuntimeException firstPollException;
    private final AtomicInteger pollCount = new AtomicInteger();

    private ThrowingFirstPollExecuteEngine(RuntimeException firstPollException) {
      this.firstPollException = firstPollException;
    }

    @Override
    public EngineType engineType() {
      return EngineType.of("test-engine");
    }

    @Override
    public ProcessStatus getStatus(String processIdentifier) {
      return getStatusInfo(processIdentifier).getStatus();
    }

    @Override
    public ProcessStatusInfo getStatusInfo(String processIdentifier) {
      if (pollCount.getAndIncrement() == 0) {
        throw firstPollException;
      }
      return ProcessStatusInfo.of(ProcessStatus.SUCCESS, "");
    }

    @Override
    public String submitTableProcess(TableProcess tableProcess) {
      return "qid-1";
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
      return "test-engine";
    }
  }

  private static class InMemoryTableProcessStore implements TableProcessStore {

    private ProcessStatus status = ProcessStatus.PENDING;
    private String externalProcessIdentifier = "";
    private String failMessage = "";
    private long finishTime = 0L;
    private final List<String> events = new ArrayList<>();

    @Override
    public long getProcessId() {
      return 1L;
    }

    @Override
    public long getTableId() {
      return 1L;
    }

    @Override
    public String getExternalProcessIdentifier() {
      return externalProcessIdentifier;
    }

    @Override
    public ProcessStatus getStatus() {
      return status;
    }

    @Override
    public String getProcessType() {
      return "test";
    }

    @Override
    public String getProcessStage() {
      return "default";
    }

    @Override
    public String getExecutionEngine() {
      return "test-engine";
    }

    @Override
    public int getRetryNumber() {
      return 0;
    }

    @Override
    public long getCreateTime() {
      return 1L;
    }

    @Override
    public long getFinishTime() {
      return finishTime;
    }

    @Override
    public String getFailMessage() {
      return failMessage;
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getSummary() {
      return Collections.emptyMap();
    }

    @Override
    public Action getAction() {
      return Action.register("test");
    }

    @Override
    public void dispose() {}

    @Override
    public boolean tryTransitState(
        ProcessStatus newStatus,
        ProcessEvent processEvent,
        String externalProcessIdentifier,
        String reason,
        Map<String, String> processParameters,
        Map<String, String> summary) {
      if (isTerminal(status)) {
        return false;
      }
      if (!validTransition(processEvent, newStatus)) {
        return false;
      }
      events.add(processEvent.name());
      this.status = newStatus;
      this.externalProcessIdentifier = externalProcessIdentifier;
      if (processEvent == ProcessEvent.COMPLETE_FAILED) {
        this.failMessage = reason;
        this.finishTime = System.currentTimeMillis();
      } else if (processEvent == ProcessEvent.COMPLETE_SUCCESS
          || processEvent == ProcessEvent.CANCEL_REQUESTED
          || processEvent == ProcessEvent.KILL_REQUESTED) {
        this.finishTime = System.currentTimeMillis();
      }
      return true;
    }

    private boolean isTerminal(ProcessStatus current) {
      return current == ProcessStatus.SUCCESS
          || current == ProcessStatus.FAILED
          || current == ProcessStatus.KILLED
          || current == ProcessStatus.CLOSED
          || current == ProcessStatus.CANCELED;
    }

    private boolean validTransition(ProcessEvent processEvent, ProcessStatus newStatus) {
      switch (processEvent) {
        case SUBMIT_REQUESTED:
          return status == ProcessStatus.UNKNOWN
              || status == ProcessStatus.PENDING
              || status == ProcessStatus.SUBMITTED
              || status == ProcessStatus.RUNNING;
        case COMPLETE_SUCCESS:
          return (status == ProcessStatus.SUBMITTED
                  || status == ProcessStatus.RUNNING
                  || status == ProcessStatus.PENDING)
              && newStatus == ProcessStatus.SUCCESS;
        case COMPLETE_FAILED:
          return (status == ProcessStatus.SUBMITTED
                  || status == ProcessStatus.RUNNING
                  || status == ProcessStatus.PENDING)
              && newStatus == ProcessStatus.FAILED;
        case CANCEL_REQUESTED:
          return (status == ProcessStatus.UNKNOWN
                  || status == ProcessStatus.SUBMITTED
                  || status == ProcessStatus.RUNNING
                  || status == ProcessStatus.PENDING)
              && newStatus == ProcessStatus.CANCELED;
        case KILL_REQUESTED:
          return (status == ProcessStatus.UNKNOWN
                  || status == ProcessStatus.SUBMITTED
                  || status == ProcessStatus.RUNNING
                  || status == ProcessStatus.PENDING)
              && newStatus == ProcessStatus.KILLED;
        case RETRY_REQUESTED:
          return status == ProcessStatus.FAILED && newStatus == ProcessStatus.PENDING;
        default:
          return false;
      }
    }

    @Override
    public TableProcessOperation begin() {
      throw new UnsupportedOperationException();
    }

    public List<String> getEvents() {
      return events;
    }
  }

  private static class OrderedInMemoryTableProcessStore extends InMemoryTableProcessStore {

    private final AtomicInteger callSequence;
    private int submitEventOrder = -1;

    private OrderedInMemoryTableProcessStore(AtomicInteger callSequence) {
      this.callSequence = callSequence;
    }

    @Override
    public boolean tryTransitState(
        ProcessStatus newStatus,
        ProcessEvent processEvent,
        String externalProcessIdentifier,
        String reason,
        Map<String, String> processParameters,
        Map<String, String> summary) {
      if (processEvent == ProcessEvent.SUBMIT_REQUESTED && submitEventOrder < 0) {
        submitEventOrder = callSequence.incrementAndGet();
      }
      return super.tryTransitState(
          newStatus, processEvent, externalProcessIdentifier, reason, processParameters, summary);
    }

    public int getSubmitEventOrder() {
      return submitEventOrder;
    }
  }

  private static class TestingTableProcess extends TableProcess {

    private TestingTableProcess() {
      super(new TestingTableRuntime(), new NoopExecuteEngine());
    }

    @Override
    public Action getAction() {
      return Action.register("test");
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
      return key.getDefaultValue();
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
