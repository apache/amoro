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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.properties.CatalogMetaProperties;
import org.apache.amoro.server.AmsEnvironment;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.model.ApiTokens;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestCatalogDashboardApis {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static AmsEnvironment ams;
  private static APITokenManager apiTokenManager;
  private static String apiKey;
  private static String secret;

  @BeforeAll
  public static void beforeAll() throws Exception {
    ams = AmsEnvironment.getIntegrationInstances();
    ams.start();
    apiTokenManager = new APITokenManager();
    ApiTokens token = new ApiTokens("testApiKey", "testSecret");
    apiTokenManager.insertApiToken(token);
    apiKey = token.getApikey();
    secret = token.getSecret();
  }

  @AfterAll
  public static void afterAll() throws Exception {
    if (apiTokenManager != null && apiKey != null) {
      apiTokenManager.deleteApiTokenByKey(apiKey);
    }
    if (ams != null) {
      ams.stop();
    }
  }

  private String httpGet(String pathWithQuery) throws Exception {
    URL url = new URL(ams.getHttpUrl() + pathWithQuery);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setDoInput(true);
    int code = conn.getResponseCode();
    Assertions.assertEquals(200, code);
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } finally {
      conn.disconnect();
    }
  }

  private String httpPost(String pathWithQuery, String body) throws Exception {
    URL url = new URL(ams.getHttpUrl() + pathWithQuery);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setDoInput(true);
    conn.setRequestProperty("Content-Type", "application/json");
    if (body != null) {
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes(StandardCharsets.UTF_8));
      }
    }
    int code = conn.getResponseCode();
    Assertions.assertEquals(200, code);
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } finally {
      conn.disconnect();
    }
  }

  private String calculateSignature(Map<String, String> params) throws Exception {
    StringBuilder sb = new StringBuilder("/api/ams/v1/api/token/calculate/signature?");
    sb.append("apiKey=").append(encode(apiKey));
    sb.append("&secret=").append(encode(secret));
    for (Map.Entry<String, String> entry : params.entrySet()) {
      sb.append("&").append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
    }
    String body = httpPost(sb.toString(), null);
    JsonNode root = MAPPER.readTree(body);
    Assertions.assertEquals(200, root.get("code").asInt());
    return root.get("result").asText();
  }

  private static String encode(String value) throws Exception {
    return URLEncoder.encode(value, "UTF-8");
  }

  private String signedGet(String path, Map<String, String> queryParams) throws Exception {
    Map<String, String> signParams = new HashMap<>(queryParams);
    String signature = calculateSignature(signParams);
    StringBuilder sb = new StringBuilder(path);
    sb.append("?");
    sb.append("apiKey=").append(encode(apiKey));
    sb.append("&signature=").append(encode(signature));
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      sb.append("&").append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
    }
    return httpGet(sb.toString());
  }

  private String signedPost(String path, Map<String, String> queryParams, String body)
      throws Exception {
    Map<String, String> signParams = new HashMap<>(queryParams);
    String signature = calculateSignature(signParams);
    StringBuilder sb = new StringBuilder(path);
    sb.append("?");
    sb.append("apiKey=").append(encode(apiKey));
    sb.append("&signature=").append(encode(signature));
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      sb.append("&").append(encode(entry.getKey())).append("=").append(encode(entry.getValue()));
    }
    return httpPost(sb.toString(), body);
  }

  private JsonNode parseResult(String json) throws Exception {
    JsonNode root = MAPPER.readTree(json);
    Assertions.assertEquals(200, root.get("code").asInt());
    return root.get("result");
  }

  @Test
  public void testCatalogTypeListFilesystemValue() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("nonce", "type-list");
    String json = signedGet("/api/ams/v1/catalogs/metastore/types", params);
    JsonNode result = parseResult(json);
    boolean foundFilesystem = false;
    for (JsonNode node : result) {
      if ("Filesystem".equals(node.get("display").asText())) {
        foundFilesystem = true;
        Assertions.assertEquals(
            CatalogMetaProperties.CATALOG_TYPE_FILESYSTEM, node.get("value").asText());
      }
    }
    Assertions.assertTrue(foundFilesystem, "Filesystem type entry should exist");
  }

  @Test
  public void testCreateCatalogWithFilesystemAndHadoopTypes() throws Exception {
    createAndVerifyFilesystemCatalog(
        "fs_catalog_filesystem", CatalogMetaProperties.CATALOG_TYPE_FILESYSTEM);
    createAndVerifyFilesystemCatalog(
        "fs_catalog_hadoop", CatalogMetaProperties.CATALOG_TYPE_HADOOP);
  }

  private void createAndVerifyFilesystemCatalog(String name, String type) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("nonce", "create-" + name);

    ObjectNode root = MAPPER.createObjectNode();
    root.put("name", name);
    root.put("type", type);
    root.put("optimizerGroup", "default");
    ArrayNode tableFormats = root.putArray("tableFormatList");
    tableFormats.add("ICEBERG");

    ObjectNode storageConfig = root.putObject("storageConfig");
    storageConfig.put("storage.type", "Hadoop");

    ObjectNode authConfig = root.putObject("authConfig");
    authConfig.put("auth.type", "SIMPLE");
    authConfig.put("auth.simple.hadoop_username", "test");

    ObjectNode properties = root.putObject("properties");
    properties.put("warehouse", "/tmp/" + name);

    root.putObject("tableProperties");

    String body = root.toString();
    String response = signedPost("/api/ams/v1/catalogs", params, body);
    JsonNode respRoot = MAPPER.readTree(response);
    Assertions.assertEquals(200, respRoot.get("code").asInt());

    CatalogManager catalogManager = ams.serviceContainer().getCatalogManager();
    CatalogMeta meta = catalogManager.getCatalogMeta(name);
    Assertions.assertEquals(CatalogMetaProperties.CATALOG_TYPE_FILESYSTEM, meta.getCatalogType());

    String detail =
        signedGet(
            "/api/ams/v1/catalogs/" + name, Collections.singletonMap("nonce", "detail-" + name));
    JsonNode detailResult = parseResult(detail);
    Assertions.assertNotNull(detailResult);
    Assertions.assertEquals(
        CatalogMetaProperties.CATALOG_TYPE_FILESYSTEM, detailResult.get("type").asText());
  }

  @Test
  public void testMetastoreMatrixForFilesystemAndHadoop() throws Exception {
    assertMetastoreMatrix("filesystem");
    assertMetastoreMatrix("hadoop");
  }

  private void assertMetastoreMatrix(String type) throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("nonce", "matrix-" + type);

    String tableFormatsJson =
        signedGet("/api/ams/v1/catalogs/metastore/" + type + "/table-formats", params);
    JsonNode tableFormatsNode = parseResult(tableFormatsJson);
    Assertions.assertTrue(tableFormatsNode.size() > 0);
    Set<String> tableFormats = new HashSet<>();
    for (JsonNode n : tableFormatsNode) {
      tableFormats.add(n.asText());
    }
    Assertions.assertEquals(tableFormats.size(), tableFormatsNode.size());

    String storageTypesJson =
        signedGet("/api/ams/v1/catalogs/metastore/" + type + "/storage-types", params);
    JsonNode storageTypesNode = parseResult(storageTypesJson);
    Assertions.assertTrue(storageTypesNode.size() > 0);
    Set<String> storageTypes = new HashSet<>();
    for (JsonNode n : storageTypesNode) {
      storageTypes.add(n.asText());
    }
    Assertions.assertEquals(storageTypes.size(), storageTypesNode.size());
  }
}
