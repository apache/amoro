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

import com.sun.net.httpserver.HttpServer;
import org.apache.amoro.Action;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.metrics.MetricRegistry;
import org.apache.amoro.process.EngineType;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.HttpRemoteSparkStandAloneSubmit;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessStatusInfo;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;
import org.apache.amoro.table.StateKey;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TestHttpRemoteSparkStandAloneSubmit {

  private HttpServer mockServer;
  private int port;
  private HttpRemoteSparkStandAloneSubmit engine;

  @Before
  public void setUp() throws IOException {
    mockServer = HttpServer.create(new InetSocketAddress(0), 0);
    port = mockServer.getAddress().getPort();
    mockServer.start();

    engine = new HttpRemoteSparkStandAloneSubmit();
    Map<String, String> props = new HashMap<>();
    props.put("base-url", "http://localhost:" + port);
    props.put("cur-user", "test-user");
    props.put("source-tag", "schedule");
    props.put("default-spark-version", "321");
    engine.open(props);
  }

  @After
  public void tearDown() {
    engine.close();
    mockServer.stop(0);
  }

  @Test
  public void testEngineNameAndType() {
    Assert.assertEquals("sl-spark-http", engine.name());
    Assert.assertEquals(EngineType.of("sl-spark-http"), engine.engineType());
  }

  @Test
  public void testOpenWithoutBaseUrlUsesDefault() {
    HttpRemoteSparkStandAloneSubmit eng = new HttpRemoteSparkStandAloneSubmit();
    eng.open(Collections.emptyMap());
    eng.close();
  }

  @Test
  public void testMapRemoteStateAll() {
    Assert.assertEquals(ProcessStatus.PENDING, engine.mapRemoteState("WAITING"));
    Assert.assertEquals(ProcessStatus.SUBMITTED, engine.mapRemoteState("SUBMITTING"));
    Assert.assertEquals(ProcessStatus.SUBMITTED, engine.mapRemoteState("PENDING"));
    Assert.assertEquals(ProcessStatus.RUNNING, engine.mapRemoteState("RUNNING"));
    Assert.assertEquals(ProcessStatus.SUCCESS, engine.mapRemoteState("SUCCEEDED"));
    Assert.assertEquals(ProcessStatus.FAILED, engine.mapRemoteState("FAILED"));
    Assert.assertEquals(ProcessStatus.FAILED, engine.mapRemoteState("SUBMIT_TIMEOUT"));
    Assert.assertEquals(ProcessStatus.KILLED, engine.mapRemoteState("KILLED"));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.mapRemoteState("SOMETHING_ELSE"));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.mapRemoteState(null));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.mapRemoteState(""));
  }

  @Test
  public void testSubmitTableProcess() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"123456789\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");

    String qid = engine.submitTableProcess(createTestProcess(params));
    Assert.assertEquals("123456789", qid);
  }

  @Test(expected = RuntimeException.class)
  public void testSubmitTableProcessFailure() {
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

    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");
    engine.submitTableProcess(createTestProcess(params));
  }

  @Test
  public void testGetStatus() {
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String response =
              "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"123\",\"status\":\"RUNNING\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    ProcessStatus status = engine.getStatus("123");
    Assert.assertEquals(ProcessStatus.RUNNING, status);
  }

  @Test
  public void testGetStatusWithSucceeded() {
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String response =
              "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"123\",\"status\":\"SUCCEEDED\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Assert.assertEquals(ProcessStatus.SUCCESS, engine.getStatus("123"));
  }

  @Test
  public void testGetStatusInfoWithFailedErrMsg() {
    String errMsg = "org.apache.spark.sql.catalyst.parser.ParseException: mismatched input 'CALL'";
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String response =
              String.format(
                  "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"123\",\"status\":\"FAILED\",\"errMsg\":\"%s\"}}",
                  errMsg.replace("\\", "\\\\").replace("\"", "\\\""));
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    ProcessStatusInfo statusInfo = engine.getStatusInfo("123");

    Assert.assertEquals(ProcessStatus.FAILED, statusInfo.getStatus());
    Assert.assertEquals(errMsg, statusInfo.getMessage());
  }

  @Test
  public void testGetStatusNullIdentifier() {
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus(null));
    Assert.assertEquals(ProcessStatus.UNKNOWN, engine.getStatus(""));
  }

  @Test
  public void testTryCancelTableProcess() {
    mockServer.createContext(
        "/spark/job/kill",
        exchange -> {
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":null}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    ProcessStatus status =
        engine.tryCancelTableProcess(createTestProcess(Collections.emptyMap()), "123");
    Assert.assertEquals(ProcessStatus.CANCELING, status);
  }

  @Test
  public void testTryCancelTableProcessFailure() {
    mockServer.createContext(
        "/spark/job/kill",
        exchange -> {
          String response = "{\"code\":1,\"msg\":\"作业不存在\",\"data\":null}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    ProcessStatus status =
        engine.tryCancelTableProcess(createTestProcess(Collections.emptyMap()), "999");
    Assert.assertEquals(ProcessStatus.UNKNOWN, status);
  }

  @Test
  public void testBuildSubmitRequestJsonWithFullParams() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");
    params.put("curUser", "sl_hiido_developer");
    params.put("logUser", "sl_hiido_developer");
    params.put("group", "sl_hiido_developer");
    params.put("userId", "470");
    params.put("sparkVersion", "321");
    params.put("sourceTag", "SCHEDULE");
    params.put("conf", "{\"sparkVersion\":\"321\"}");

    String json = engine.buildSubmitRequestJson(createTestProcess(params));

    // Parse and verify each field matches expected curl request body
    com.fasterxml.jackson.databind.ObjectMapper om =
        new com.fasterxml.jackson.databind.ObjectMapper();
    com.fasterxml.jackson.databind.JsonNode root = om.readTree(json);

    Assert.assertEquals("sql", root.get("jobType").asText());
    Assert.assertEquals("SELECT 1", root.get("hql").asText());
    Assert.assertEquals("sl_hiido_developer", root.get("curUser").asText());
    Assert.assertEquals("sl_hiido_developer", root.get("logUser").asText());
    Assert.assertEquals("sl_hiido_developer", root.get("group").asText());
    Assert.assertEquals(470, root.get("userId").asInt());
    Assert.assertEquals(321, root.get("sparkVersion").asInt());
    Assert.assertEquals("SCHEDULE", root.get("sourceTag").asText());
    Assert.assertEquals("{\"sparkVersion\":\"321\"}", root.get("conf").asText());
  }

  @Test
  public void testBuildSubmitRequestJsonDefaultValues() throws Exception {
    // Only hql provided, other fields should use engine defaults
    Map<String, String> params = new HashMap<>();
    params.put("hql", "SHOW TABLES");

    String json = engine.buildSubmitRequestJson(createTestProcess(params));

    com.fasterxml.jackson.databind.ObjectMapper om =
        new com.fasterxml.jackson.databind.ObjectMapper();
    com.fasterxml.jackson.databind.JsonNode root = om.readTree(json);

    Assert.assertEquals("sql", root.get("jobType").asText());
    Assert.assertEquals("SHOW TABLES", root.get("hql").asText());
    // Defaults from engine.open() config
    Assert.assertEquals("test-user", root.get("curUser").asText());
    Assert.assertEquals("schedule", root.get("sourceTag").asText());
    Assert.assertEquals(321, root.get("sparkVersion").asInt());
    // Optional fields should not be present
    Assert.assertNull(root.get("logUser"));
    Assert.assertNull(root.get("group"));
    Assert.assertNull(root.get("userId"));
  }

  @Test
  public void testSubmitWithFullParamsAndMockServer() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          // Read and verify the request body contains expected fields
          String body =
              new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
          Assert.assertTrue("Request should contain hql", body.contains("\"hql\":\"SELECT 1\""));
          Assert.assertTrue(
              "Request should contain curUser",
              body.contains("\"curUser\":\"sl_hiido_developer\""));
          Assert.assertTrue("Request should contain userId", body.contains("\"userId\":470"));

          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"987654321\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");
    params.put("curUser", "sl_hiido_developer");
    params.put("logUser", "sl_hiido_developer");
    params.put("group", "sl_hiido_developer");
    params.put("userId", "470");
    params.put("sparkVersion", "321");
    params.put("sourceTag", "SCHEDULE");
    params.put("conf", "{\"sparkVersion\":\"321\"}");

    String qid = engine.submitTableProcess(createTestProcess(params));
    Assert.assertEquals("987654321", qid);
  }

  @Test
  public void testFullLifecycleSubmitPollSuccess() {
    // Step 1: Submit returns qid
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"lifecycle-001\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");
    params.put("curUser", "sl_hiido_developer");
    params.put("logUser", "sl_hiido_developer");
    params.put("group", "sl_hiido_developer");
    params.put("userId", "470");
    params.put("sparkVersion", "321");
    params.put("sourceTag", "SCHEDULE");
    params.put("conf", "{\"sparkVersion\":\"321\"}");

    String qid = engine.submitTableProcess(createTestProcess(params));
    Assert.assertEquals("lifecycle-001", qid);

    // Step 2: Simulate polling through WAITING -> SUBMITTING -> RUNNING -> SUCCEEDED
    final int[] pollCount = {0};
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String[] statusSequence = {"WAITING", "SUBMITTING", "RUNNING", "SUCCEEDED"};
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

    // Verify status transitions match the mapping table
    Assert.assertEquals(ProcessStatus.PENDING, engine.getStatus(qid)); // WAITING -> PENDING
    Assert.assertEquals(ProcessStatus.SUBMITTED, engine.getStatus(qid)); // SUBMITTING -> SUBMITTED
    Assert.assertEquals(ProcessStatus.RUNNING, engine.getStatus(qid)); // RUNNING -> RUNNING
    Assert.assertEquals(ProcessStatus.SUCCESS, engine.getStatus(qid)); // SUCCEEDED -> SUCCESS
  }

  @Test
  public void testFullLifecycleSubmitPollKill() {
    mockServer.createContext(
        "/spark/job/submit",
        exchange -> {
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"kill-001\"}}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    Map<String, String> params = new HashMap<>();
    params.put("hql", "SELECT 1");
    String qid = engine.submitTableProcess(createTestProcess(params));

    // Simulate RUNNING state
    final boolean[] killed = {false};
    mockServer.createContext(
        "/spark/job/state",
        exchange -> {
          String status = killed[0] ? "KILLED" : "RUNNING";
          String response =
              String.format(
                  "{\"code\":0,\"msg\":\"操作成功\",\"data\":{\"qid\":\"%s\",\"status\":\"%s\"}}",
                  qid, status);
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    // Verify RUNNING
    Assert.assertEquals(ProcessStatus.RUNNING, engine.getStatus(qid));

    // Kill the job
    mockServer.createContext(
        "/spark/job/kill",
        exchange -> {
          killed[0] = true;
          String response = "{\"code\":0,\"msg\":\"操作成功\",\"data\":null}";
          byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    ProcessStatus cancelStatus =
        engine.tryCancelTableProcess(createTestProcess(Collections.emptyMap()), qid);
    Assert.assertEquals(ProcessStatus.CANCELING, cancelStatus);

    // After kill, polling should return KILLED
    Assert.assertEquals(ProcessStatus.KILLED, engine.getStatus(qid));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSubmitWithoutHqlThrows() {
    engine.buildSubmitRequestJson(createTestProcess(Collections.emptyMap()));
  }

  private TableProcess createTestProcess(Map<String, String> params) {
    return new TestingTableProcess(params);
  }

  private static class TestingTableProcess extends TableProcess {

    private final Map<String, String> params;

    TestingTableProcess(Map<String, String> params) {
      super(new TestingTableRuntime(), new NoopExecuteEngine());
      this.params = params;
    }

    @Override
    public Action getAction() {
      return Action.register("spark-test");
    }

    @Override
    public Map<String, String> getProcessParameters() {
      return params;
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
