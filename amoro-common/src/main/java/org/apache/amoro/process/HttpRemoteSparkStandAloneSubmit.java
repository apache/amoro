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

package org.apache.amoro.process;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-based execution engine that submits Spark SQL jobs to a remote Spark StandAlone cluster via
 * REST APIs provided by dremel-httpserver.
 */
public class HttpRemoteSparkStandAloneSubmit implements ExecuteEngine {

  private static final Logger LOG = LoggerFactory.getLogger(HttpRemoteSparkStandAloneSubmit.class);

  public static final String ENGINE_NAME = "sl-spark-http";
  public static final String SUMMARY_KEY_QIDS = "qids";

  private static final String PROP_BASE_URL = "base-url";
  private static final String PROP_CONNECT_TIMEOUT = "connect-timeout-ms";
  private static final String PROP_READ_TIMEOUT = "read-timeout-ms";
  private static final String PROP_DEFAULT_SPARK_VERSION = "default-spark-version";
  private static final String PROP_SOURCE_TAG = "source-tag";
  private static final String PROP_CUR_USER = "cur-user";

  private static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
  private static final int DEFAULT_READ_TIMEOUT_MS = 30000;
  private static final int DEFAULT_SPARK_VERSION = 321;
  private static final String DEFAULT_BASE_URL = "http://spark-server-api-pre.eclytics.com";
  private static final String DEFAULT_SOURCE_TAG = "schedule";
  private static final String DEFAULT_CUR_USER = "amoro";

  private static final String SUBMIT_PATH = "/spark/job/submit";
  private static final String STATE_PATH = "/spark/job/state";
  private static final String KILL_PATH = "/spark/job/kill";

  private static final String PARAM_HQL = "hql";
  private static final String PARAM_JOB_TYPE = "jobType";
  private static final String PARAM_CUR_USER = "curUser";
  private static final String PARAM_LOG_USER = "logUser";
  private static final String PARAM_USER_NAME = "userName";
  private static final String PARAM_GROUP = "group";
  private static final String PARAM_USER_ID = "userId";
  private static final String PARAM_SPARK_VERSION = "sparkVersion";
  private static final String PARAM_SOURCE_TAG = "sourceTag";
  private static final String PARAM_SCHEDULE_ID = "scheduleId";
  private static final String PARAM_CONF = "conf";
  private static final String PARAM_CLIENT_IP = "clientIp";

  private final ObjectMapper objectMapper = new ObjectMapper();

  private String baseUrl;
  private int connectTimeoutMs;
  private int readTimeoutMs;
  private int defaultSparkVersion;
  private String sourceTag;
  private String curUser;

  @Override
  public String name() {
    return ENGINE_NAME;
  }

  @Override
  public EngineType engineType() {
    return EngineType.of(ENGINE_NAME);
  }

  @Override
  public void open(Map<String, String> properties) {
    Map<String, String> engineProperties = properties == null ? Collections.emptyMap() : properties;
    this.baseUrl = engineProperties.get(PROP_BASE_URL);
    if (baseUrl == null || baseUrl.isEmpty()) {
      baseUrl = DEFAULT_BASE_URL;
    }
    if (baseUrl.endsWith("/")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    }

    this.connectTimeoutMs =
        parseInt(engineProperties.get(PROP_CONNECT_TIMEOUT), DEFAULT_CONNECT_TIMEOUT_MS);
    this.readTimeoutMs = parseInt(engineProperties.get(PROP_READ_TIMEOUT), DEFAULT_READ_TIMEOUT_MS);
    this.defaultSparkVersion =
        parseInt(engineProperties.get(PROP_DEFAULT_SPARK_VERSION), DEFAULT_SPARK_VERSION);
    this.sourceTag = engineProperties.getOrDefault(PROP_SOURCE_TAG, DEFAULT_SOURCE_TAG);
    this.curUser = engineProperties.getOrDefault(PROP_CUR_USER, DEFAULT_CUR_USER);

    LOG.info(
        "HttpRemoteSparkStandAloneSubmit engine opened with baseUrl={}, connectTimeout={}ms, readTimeout={}ms",
        baseUrl,
        connectTimeoutMs,
        readTimeoutMs);
  }

  @Override
  public void close() {
    LOG.info("HttpRemoteSparkStandAloneSubmit engine closed");
  }

  @Override
  public String submitTableProcess(TableProcess tableProcess) {
    String requestBody = buildSubmitRequestJson(tableProcess);
    LOG.info("Submitting spark job to {}{}", baseUrl, SUBMIT_PATH);

    String responseBody = doPost(SUBMIT_PATH, requestBody);
    JsonNode response = parseResponse(responseBody);

    int code = response.get("code").asInt(-1);
    if (code != 0) {
      String msg = response.has("msg") ? response.get("msg").asText() : "unknown error";
      throw new RuntimeException(
          String.format("Failed to submit spark job: code=%d, msg=%s", code, msg));
    }

    JsonNode data = response.get("data");
    if (data == null || data.isNull() || !data.has("qid")) {
      throw new RuntimeException("Submit response missing 'data.qid' field: " + responseBody);
    }

    String qid = data.get("qid").asText();
    LOG.info("Spark job submitted successfully, qid={}", qid);
    return qid;
  }

  @Override
  public ProcessStatus getStatus(String processIdentifier) {
    return getStatusInfo(processIdentifier).getStatus();
  }

  @Override
  public ProcessStatusInfo getStatusInfo(String processIdentifier) {
    if (processIdentifier == null || processIdentifier.isEmpty()) {
      return ProcessStatusInfo.of(ProcessStatus.UNKNOWN);
    }

    try {
      String responseBody = doGet(STATE_PATH + "?qid=" + processIdentifier);
      JsonNode response = parseResponse(responseBody);

      int code = response.get("code").asInt(-1);
      if (code != 0) {
        LOG.warn(
            "Failed to query spark job state for qid={}, code={}, msg={}",
            processIdentifier,
            code,
            response.has("msg") ? response.get("msg").asText() : "unknown");
        return ProcessStatusInfo.of(
            ProcessStatus.UNKNOWN,
            response.has("msg") ? response.get("msg").asText() : "unknown error");
      }

      JsonNode data = response.get("data");
      if (data == null || data.isNull() || !data.has("status")) {
        LOG.warn("State response missing 'data.status' for qid={}", processIdentifier);
        return ProcessStatusInfo.of(ProcessStatus.UNKNOWN, "Missing data.status");
      }

      String remoteStatus = data.get("status").asText();
      String errMsg = data.hasNonNull("errMsg") ? data.get("errMsg").asText() : "";
      return ProcessStatusInfo.of(mapRemoteState(remoteStatus), errMsg);
    } catch (Exception e) {
      LOG.warn("Error querying spark job state for qid={}", processIdentifier, e);
      return ProcessStatusInfo.of(ProcessStatus.UNKNOWN, e.getMessage());
    }
  }

  @Override
  public ProcessStatus tryCancelTableProcess(TableProcess tableProcess, String processIdentifier) {
    try {
      ObjectNode killRequest = objectMapper.createObjectNode();
      killRequest.put("qid", processIdentifier);
      String requestBody = objectMapper.writeValueAsString(killRequest);

      LOG.info("Killing spark job qid={}", processIdentifier);
      String responseBody = doPost(KILL_PATH, requestBody);
      JsonNode response = parseResponse(responseBody);

      int code = response.get("code").asInt(-1);
      if (code == 0) {
        LOG.info("Kill request sent successfully for qid={}", processIdentifier);
        return ProcessStatus.CANCELING;
      } else {
        String msg = response.has("msg") ? response.get("msg").asText() : "unknown error";
        LOG.warn("Failed to kill spark job qid={}, code={}, msg={}", processIdentifier, code, msg);
        return ProcessStatus.UNKNOWN;
      }
    } catch (Exception e) {
      LOG.warn("Error killing spark job qid={}", processIdentifier, e);
      return ProcessStatus.UNKNOWN;
    }
  }

  @Override
  public Map<String, String> buildRetrySummary(
      String currentIdentifier, Map<String, String> currentSummary) {
    Map<String, String> result =
        currentSummary != null ? new HashMap<>(currentSummary) : new HashMap<>();

    if (currentIdentifier == null || currentIdentifier.isEmpty()) {
      return result;
    }

    try {
      List<String> qids;
      String existingQids = result.get(SUMMARY_KEY_QIDS);
      if (existingQids != null && !existingQids.isEmpty()) {
        qids = objectMapper.readValue(existingQids, new TypeReference<List<String>>() {});
      } else {
        qids = new ArrayList<>();
      }
      qids.add(currentIdentifier);
      result.put(SUMMARY_KEY_QIDS, objectMapper.writeValueAsString(qids));
    } catch (Exception e) {
      LOG.warn("Failed to archive qid {} into summary", currentIdentifier, e);
    }
    return result;
  }

  @VisibleForTesting
  public ProcessStatus mapRemoteState(String remoteState) {
    if (remoteState == null || remoteState.isEmpty()) {
      return ProcessStatus.UNKNOWN;
    }
    switch (remoteState.toUpperCase()) {
      case "WAITING":
        return ProcessStatus.PENDING;
      case "SUBMITTING":
      case "PENDING":
        return ProcessStatus.SUBMITTED;
      case "RUNNING":
        return ProcessStatus.RUNNING;
      case "SUCCEEDED":
        return ProcessStatus.SUCCESS;
      case "FAILED":
      case "SUBMIT_TIMEOUT":
        return ProcessStatus.FAILED;
      case "KILLED":
        return ProcessStatus.KILLED;
      default:
        LOG.warn("Unknown remote spark job state: {}", remoteState);
        return ProcessStatus.UNKNOWN;
    }
  }

  @VisibleForTesting
  public String buildSubmitRequestJson(TableProcess tableProcess) {
    Map<String, String> params = tableProcess.getProcessParameters();

    String hql = params.get(PARAM_HQL);
    Preconditions.checkArgument(
        hql != null && !hql.isEmpty(), "Process parameter '%s' is required", PARAM_HQL);

    ObjectNode requestNode = objectMapper.createObjectNode();
    requestNode.put(PARAM_JOB_TYPE, "sql");
    requestNode.put(PARAM_HQL, hql);
    requestNode.put(PARAM_CUR_USER, params.getOrDefault(PARAM_CUR_USER, curUser));
    requestNode.put(PARAM_SOURCE_TAG, params.getOrDefault(PARAM_SOURCE_TAG, sourceTag));
    requestNode.put(
        PARAM_SPARK_VERSION, parseInt(params.get(PARAM_SPARK_VERSION), defaultSparkVersion));

    putIfPresent(requestNode, params, PARAM_LOG_USER);
    putIfPresent(requestNode, params, PARAM_USER_NAME);
    putIfPresent(requestNode, params, PARAM_GROUP);
    putIfPresent(requestNode, params, PARAM_SCHEDULE_ID);
    putIfPresent(requestNode, params, PARAM_CONF);
    putIfPresent(requestNode, params, PARAM_CLIENT_IP);

    if (params.containsKey(PARAM_USER_ID)) {
      requestNode.put(PARAM_USER_ID, parseInt(params.get(PARAM_USER_ID), 0));
    }

    try {
      return objectMapper.writeValueAsString(requestNode);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize submit request", e);
    }
  }

  private String doPost(String path, String jsonBody) {
    HttpURLConnection connection = null;
    try {
      connection = openConnection(path, "POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);
      byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
      connection.setFixedLengthStreamingMode(body.length);
      try (OutputStream output = connection.getOutputStream()) {
        output.write(body);
      }
      return readResponse(path, connection);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("HTTP POST " + path + " failed", e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private String doGet(String path) {
    HttpURLConnection connection = null;
    try {
      connection = openConnection(path, "GET");
      return readResponse(path, connection);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("HTTP GET " + path + " failed", e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private HttpURLConnection openConnection(String path, String method) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
    connection.setRequestMethod(method);
    connection.setConnectTimeout(connectTimeoutMs);
    connection.setReadTimeout(readTimeoutMs);
    return connection;
  }

  private String readResponse(String path, HttpURLConnection connection) throws IOException {
    int statusCode = connection.getResponseCode();
    String body =
        readBody(statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream());
    if (statusCode != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException(
          String.format("HTTP %s returned status %d: %s", path, statusCode, body));
    }
    return body;
  }

  private String readBody(InputStream inputStream) throws IOException {
    if (inputStream == null) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
    }
    return builder.toString();
  }

  private JsonNode parseResponse(String responseBody) {
    try {
      return objectMapper.readTree(responseBody);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse response: " + responseBody, e);
    }
  }

  private void putIfPresent(ObjectNode node, Map<String, String> params, String key) {
    String value = params.get(key);
    if (value != null && !value.isEmpty()) {
      node.put(key, value);
    }
  }

  private static int parseInt(String value, int defaultValue) {
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
