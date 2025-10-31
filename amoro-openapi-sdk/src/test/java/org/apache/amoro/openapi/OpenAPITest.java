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

package org.apache.amoro.openapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.amoro.openapi.api.HealthApi;
import org.apache.amoro.openapi.invoker.ApiClient;
import org.apache.amoro.openapi.model.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenAPITest {
  private WireMockServer wireMockServer;
  private String basePath;

  @Before
  public void setup() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    int port = wireMockServer.port();
    configureFor("localhost", port);
    basePath = "http://localhost:" + port;
  }

  @After
  public void teardown() {
    wireMockServer.stop();
  }

  @Test
  public void testSdkCall() throws Exception {
    String json = "{\"message\":\"string\",\"code\":0,\"result\":{}}";
    stubFor(
        get(urlEqualTo("/api/ams/v1/health/status"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("{\"message\":\"\",\"code\":0,\"result\":{}}")));

    ApiClient apiClient = new ApiClient().setBasePath(basePath);
    HealthApi api = new HealthApi(apiClient);
    Response healthResponse = api.apiAmsV1HealthStatusGet();
    assert healthResponse.getCode() == 0;
    assert healthResponse.getResult() instanceof java.util.Map;
    assert ((java.util.Map<?, ?>) healthResponse.getResult()).isEmpty();
  }
}
