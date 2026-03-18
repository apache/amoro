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

import com.sun.net.httpserver.HttpServer;
import org.apache.amoro.Action;
import org.apache.amoro.PaimonActions;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.HttpRemoteSparkStandAloneSubmit;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.server.table.cleanup.CleanupOperation;
import org.apache.amoro.server.table.paimon.PaimonTableRuntime;
import org.apache.amoro.table.StateKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestPaimonExpireSnapshotProcess {
  private static final Pattern OLDER_THAN_MILLIS_PATTERN =
      Pattern.compile("older_than\\s*=>\\s*(\\d+)");

  private HttpServer mockServer;
  private int port;
  private HttpRemoteSparkStandAloneSubmit sparkEngine;

  @BeforeEach
  public void setUp() throws IOException {
    mockServer = HttpServer.create(new InetSocketAddress(0), 0);
    port = mockServer.getAddress().getPort();
    mockServer.start();

    sparkEngine = new HttpRemoteSparkStandAloneSubmit();
    Map<String, String> props = new HashMap<>();
    props.put("base-url", "http://localhost:" + port);
    props.put("cur-user", "sl_hiido_developer");
    props.put("source-tag", "AMORO");
    props.put("default-spark-version", "321");
    sparkEngine.open(props);
  }

  @AfterEach
  public void tearDown() {
    sparkEngine.close();
    mockServer.stop(0);
  }

  // ========== SQL Assembly Tests ==========

  @Test
  public void testBuildExpireSnapshotsSqlWithDefaults() {
    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    String sql = process.buildExpireSnapshotsSql();

    Assertions.assertTrue(
        sql.contains("CALL sys.expire_snapshots"), "SQL should contain CALL sys.expire_snapshots");
    Assertions.assertTrue(
        sql.contains("db.table"), "SQL should contain table name without catalog");
    Assertions.assertFalse(
        sql.contains("catalog.db.table"), "SQL should not contain catalog in table name");
    Assertions.assertTrue(sql.contains("retain_max => 10"), "SQL should contain retain_max => 10");
    Assertions.assertTrue(
        sql.contains("older_than => "), "SQL should contain older_than timestamp expression");
    Assertions.assertFalse(
        sql.contains("from_unixtime"), "SQL should not contain from_unixtime conversion");
  }

  @Test
  public void testBuildExpireSnapshotsSqlWithCustomProperties() {
    Map<String, String> tableConfig = new HashMap<>();
    tableConfig.put("snapshot.time-retained", "2 d");
    tableConfig.put("snapshot.num-retained.max", "5");

    TableRuntime runtime = createMockRuntime(tableConfig);
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    String sql = process.buildExpireSnapshotsSql();

    Assertions.assertTrue(sql.contains("retain_max => 5"), "SQL should contain retain_max => 5");
    Assertions.assertTrue(
        sql.contains("CALL sys.expire_snapshots"), "SQL should contain CALL sys.expire_snapshots");
    Assertions.assertTrue(
        sql.contains("older_than => "), "SQL should contain older_than timestamp expression");
    Assertions.assertFalse(
        sql.contains("from_unixtime"), "SQL should not contain from_unixtime conversion");

    Matcher matcher = OLDER_THAN_MILLIS_PATTERN.matcher(sql);
    Assertions.assertTrue(matcher.find(), "older_than should use epoch millis");
    long actualOlderThanMillis = Long.parseLong(matcher.group(1));
    long expectedOlderThanMillis = System.currentTimeMillis() - Duration.ofDays(2).toMillis();
    Assertions.assertTrue(
        Math.abs(actualOlderThanMillis - expectedOlderThanMillis) <= 5000,
        "older_than epoch millis should align with retained duration");
  }

  @Test
  public void testProcessParametersContainRequiredFields() {
    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    Map<String, String> params = process.getProcessParameters();

    Assertions.assertNotNull(params.get("hql"), "hql should be present");
    Assertions.assertEquals("sl_hiido_developer", params.get("curUser"));
    Assertions.assertEquals("sl_hiido_developer", params.get("logUser"));
    Assertions.assertEquals("sl_hiido_developer", params.get("group"));
    Assertions.assertEquals("470", params.get("userId"));
    Assertions.assertEquals("321", params.get("sparkVersion"));
    Assertions.assertEquals("AMORO", params.get("sourceTag"));
    Assertions.assertEquals("{\"sparkVersion\":\"321\"}", params.get("conf"));
  }

  @Test
  public void testProcessParametersWithCustomSparkVersion() {
    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 350);

    Map<String, String> params = process.getProcessParameters();

    Assertions.assertEquals("350", params.get("sparkVersion"));
    Assertions.assertEquals("{\"sparkVersion\":\"350\"}", params.get("conf"));
  }

  @Test
  public void testActionType() {
    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    Assertions.assertEquals(PaimonActions.EXPIRE_SNAPSHOTS, process.getAction());
  }

  @Test
  public void testExecutionEngineName() {
    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    Assertions.assertEquals(
        HttpRemoteSparkStandAloneSubmit.ENGINE_NAME, process.getExecutionEngine());
  }

  // ========== Duration Parsing Tests ==========

  @Test
  public void testParseDuration() {
    Assertions.assertEquals(Duration.ofHours(1), PaimonExpireSnapshotProcess.parseDuration("1 h"));
    Assertions.assertEquals(Duration.ofDays(2), PaimonExpireSnapshotProcess.parseDuration("2d"));
    Assertions.assertEquals(
        Duration.ofMinutes(30), PaimonExpireSnapshotProcess.parseDuration("30 min"));
    Assertions.assertEquals(
        Duration.ofSeconds(60), PaimonExpireSnapshotProcess.parseDuration("60s"));
    Assertions.assertEquals(
        Duration.ofMillis(500), PaimonExpireSnapshotProcess.parseDuration("500ms"));
  }

  // ========== Factory Trigger Tests ==========

  @Test
  public void testFactorySupportedActionsIncludesExpireSnapshots() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    factory.open(properties);
    factory.availableExecuteEngines(Collections.singleton(sparkEngine));

    Assertions.assertTrue(
        factory
            .supportedActions()
            .getOrDefault(TableFormat.PAIMON, Collections.emptySet())
            .contains(PaimonActions.EXPIRE_SNAPSHOTS));
  }

  @Test
  public void testFactoryTriggerExpireSnapshotsEnabled() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshots.interval", "1 h");
    factory.open(properties);
    factory.availableExecuteEngines(Collections.singleton(sparkEngine));

    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON));
    Mockito.when(runtime.getTableConfig()).thenReturn(Collections.emptyMap());
    Mockito.when(runtime.getLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING)).thenReturn(0L);

    Optional<TableProcess> process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertTrue(
        process.isPresent(), "Process should be present when enabled and interval elapsed");
    Assertions.assertEquals(
        HttpRemoteSparkStandAloneSubmit.ENGINE_NAME, process.get().getExecutionEngine());
  }

  @Test
  public void testFactoryTriggerExpireSnapshotsDisabled() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "false");
    factory.open(properties);
    factory.availableExecuteEngines(Collections.singleton(sparkEngine));

    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);

    Optional<TableProcess> process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertFalse(process.isPresent(), "Process should be empty when disabled");
  }

  @Test
  public void testFactoryTriggerSkipsWhenIntervalNotElapsed() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshots.interval", "1 h");
    factory.open(properties);
    factory.availableExecuteEngines(Collections.singleton(sparkEngine));

    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON));
    // Last cleanup was just now, so interval hasn't elapsed
    Mockito.when(runtime.getLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING))
        .thenReturn(System.currentTimeMillis());

    Optional<TableProcess> process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertFalse(
        process.isPresent(), "Process should be empty when interval not elapsed");
  }

  @Test
  public void testFactoryTriggerReturnsProcessWhenIntervalElapsed() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshots.interval", "1 h");
    factory.open(properties);
    factory.availableExecuteEngines(Collections.singleton(sparkEngine));

    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getFormat()).thenReturn(TableFormat.PAIMON);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON));
    Mockito.when(runtime.getTableConfig()).thenReturn(Collections.emptyMap());
    // Last cleanup was 2 hours ago
    Mockito.when(runtime.getLastCleanTime(CleanupOperation.SNAPSHOTS_EXPIRING))
        .thenReturn(System.currentTimeMillis() - Duration.ofHours(2).toMillis());

    Optional<TableProcess> process = factory.trigger(runtime, PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertTrue(process.isPresent(), "Process should be present when interval elapsed");
  }

  @Test
  public void testFactoryTriggerStrategyForExpireSnapshots() {
    PaimonProcessFactory factory = new PaimonProcessFactory();
    Map<String, String> properties = new HashMap<>();
    properties.put("expire-snapshots.enabled", "true");
    properties.put("expire-snapshots.interval", "2 h");
    factory.open(properties);

    ProcessTriggerStrategy strategy =
        factory.triggerStrategy(TableFormat.PAIMON, PaimonActions.EXPIRE_SNAPSHOTS);
    Assertions.assertEquals(2 * 60 * 60 * 1000L, strategy.getTriggerInterval().toMillis());
  }

  // ========== Submit Success + State Update Tests ==========

  @Test
  public void testSubmitSuccessAndExpireSnapshotsSuccess() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String body =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Assertions.assertTrue(body.contains("\"hql\""), "Request should contain hql");
          Assertions.assertTrue(
              body.contains("CALL sys.expire_snapshots"),
              "Request should contain CALL sys.expire_snapshots");

          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"expire-001\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    String qid = sparkEngine.submitTableProcess(process);
    Assertions.assertEquals("expire-001", qid);
  }

  @Test
  public void testAfterCompleteSuccessUpdatesState() {
    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON));
    Mockito.when(runtime.getTableConfig()).thenReturn(Collections.emptyMap());

    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    process.afterComplete(ProcessStatus.SUCCESS);

    Mockito.verify(runtime)
        .updateLastCleanTime(Mockito.eq(CleanupOperation.SNAPSHOTS_EXPIRING), Mockito.anyLong());
  }

  @Test
  public void testAfterCompleteFailedDoesNotUpdateState() {
    PaimonTableRuntime runtime = Mockito.mock(PaimonTableRuntime.class);
    Mockito.when(runtime.getTableIdentifier())
        .thenReturn(ServerTableIdentifier.of("catalog", "db", "table", TableFormat.PAIMON));
    Mockito.when(runtime.getTableConfig()).thenReturn(Collections.emptyMap());

    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    process.afterComplete(ProcessStatus.FAILED);

    Mockito.verify(runtime, Mockito.never()).updateLastCleanTime(Mockito.any(), Mockito.anyLong());
  }

  // ========== Submit Failure Tests ==========

  @Test
  public void testSubmitFailureThrowsException() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response = "{\"code\":1,\"msg\":\"服务内部错误\",\"data\":null}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    Assertions.assertThrows(RuntimeException.class, () -> sparkEngine.submitTableProcess(process));
  }

  @Test
  public void testFullLifecycleSubmitPollSuccess() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response =
              "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"expire-lifecycle-001\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    String qid = sparkEngine.submitTableProcess(process);
    Assertions.assertEquals("expire-lifecycle-001", qid);

    // Simulate status transitions
    final int[] pollCount = {0};
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String[] statusSequence = {"WAITING", "RUNNING", "SUCCEEDED"};
          int idx = Math.min(pollCount[0], statusSequence.length - 1);
          pollCount[0]++;

          String response =
              String.format(
                  "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"%s\",\"status\":\"%s\"}}",
                  qid, statusSequence[idx]);
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Assertions.assertEquals(ProcessStatus.PENDING, sparkEngine.getStatus(qid));
    Assertions.assertEquals(ProcessStatus.RUNNING, sparkEngine.getStatus(qid));
    Assertions.assertEquals(ProcessStatus.SUCCESS, sparkEngine.getStatus(qid));
  }

  @Test
  public void testFullLifecycleSubmitPollFailure() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"expire-fail-001\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    TableRuntime runtime = createMockRuntime(Collections.emptyMap());
    PaimonExpireSnapshotProcess process =
        new PaimonExpireSnapshotProcess(runtime, sparkEngine, 321);

    String qid = sparkEngine.submitTableProcess(process);

    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String response =
              String.format(
                  "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"%s\",\"status\":\"FAILED\"}}",
                  qid);
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Assertions.assertEquals(ProcessStatus.FAILED, sparkEngine.getStatus(qid));
  }

  // ========== Helper Methods ==========

  private TableRuntime createMockRuntime(Map<String, String> tableConfig) {
    return new SimpleTestRuntime(tableConfig);
  }

  private static class SimpleTestRuntime implements TableRuntime {

    private final Map<String, String> tableConfig;

    SimpleTestRuntime(Map<String, String> tableConfig) {
      this.tableConfig = tableConfig;
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
      return tableConfig;
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
}
