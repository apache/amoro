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

import org.apache.amoro.server.AmsEnvironment;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestDashboardAuthentication {

  private static final AmsEnvironment AMS = AmsEnvironment.getIntegrationInstances();
  private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  // MD5 of "catalogxdbxtablex"
  private static final String FORGED_SINGLE_PAGE_TOKEN = "b383ed05fbc9d4b7e3888a70ed19398c";
  private static final String FORGED_TOKEN_QUERY =
      "token=" + FORGED_SINGLE_PAGE_TOKEN + "&catalog=x&db=x&table=x";

  @BeforeAll
  public static void beforeAll() throws Exception {
    AMS.start();
  }

  @AfterAll
  public static void afterAll() throws IOException {
    AMS.stop();
  }

  @Test
  public void testForgedSinglePageTokenCannotAccessCatalogApi()
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(
                URI.create(AMS.getHttpUrl() + "/api/ams/v1/catalogs?" + FORGED_TOKEN_QUERY))
            .GET()
            .build();

    assertForbidden(
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()), "Signature check failed");
  }

  @Test
  public void testForgedSinglePageTokenCannotAccessTerminalApi()
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(
                URI.create(
                    AMS.getHttpUrl()
                        + "/api/ams/v1/terminal/catalogs/"
                        + AmsEnvironment.INTERNAL_ICEBERG_CATALOG
                        + "/execute?"
                        + FORGED_TOKEN_QUERY))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"sql\":\"select 1\"}"))
            .build();

    assertForbidden(
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()), "Signature check failed");
  }

  @Test
  public void testForgedSinglePageTokenCannotBypassWebSessionAuthentication()
      throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder(
                URI.create(AMS.getHttpUrl() + "/api/ams/v1/catalogs?" + FORGED_TOKEN_QUERY))
            .header("X-Request-Source", "Web")
            .GET()
            .build();

    assertForbidden(
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()), "Please login first");
  }

  private static void assertForbidden(HttpResponse<String> response, String expectedMessage)
      throws IOException {
    JsonNode responseBody = OBJECT_MAPPER.readTree(response.body());
    Assertions.assertEquals(403, responseBody.get("code").asInt());
    Assertions.assertEquals(expectedMessage, responseBody.get("message").asText());
  }
}
