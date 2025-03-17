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

import static org.mockito.Mockito.*;

import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.exception.ForbiddenException;
import org.apache.amoro.exception.SignatureCheckException;
import org.apache.amoro.server.AmoroManagementConf;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.resource.OptimizerManager;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.server.terminal.TerminalManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class TestDashboardServer {

  private DashboardServer dashboardServer;

  @Mock private Configurations mockConfig;

  @Mock private CatalogManager mockCatalogManager;

  @Mock private TableManager mockTableManager;

  @Mock private OptimizerManager mockOptimizerManager;

  @Mock private DefaultOptimizingService mockOptimizingService;

  @Mock private TerminalManager mockTerminalManager;

  @Mock private Context mockContext;

  @Mock private HttpServletRequest mockHttpRequest;

  @Before
  public void setup() {
    when(mockConfig.get(AmoroManagementConf.HTTP_SERVER_REST_AUTH_TYPE)).thenReturn("basic");
    when(mockConfig.get(AmoroManagementConf.ADMIN_USERNAME)).thenReturn("admin");
    when(mockConfig.get(AmoroManagementConf.ADMIN_PASSWORD)).thenReturn("password");

    dashboardServer =
        new DashboardServer(
            mockConfig,
            mockCatalogManager,
            mockTableManager,
            mockOptimizerManager,
            mockOptimizingService,
            mockTerminalManager);

    when(mockContext.req).thenReturn(mockHttpRequest);
  }

  @Test
  public void testPreHandleRequestWhiteList() {
    when(mockContext.path()).thenReturn("/api/ams/v1/login");

    dashboardServer.preHandleRequest(mockContext);

    verify(mockContext, times(1)).path();
    verify(mockContext, never()).header(anyString());
  }

  @Test
  public void testPreHandleRequestWebRequest() {
    when(mockContext.path()).thenReturn("/api/ams/v1/secured-endpoint");
    when(mockContext.header("X-Request-Source")).thenReturn("Web");
    when(mockContext.sessionAttribute("user")).thenReturn("testUser");

    dashboardServer.preHandleRequest(mockContext);

    verify(mockContext, times(1)).path();
    verify(mockContext, times(1)).header("X-Request-Source");
    verify(mockContext, times(1)).sessionAttribute("user");
  }

  @Test(expected = ForbiddenException.class)
  public void testPreHandleRequestWebRequestNoSession() {
    when(mockContext.path()).thenReturn("/api/ams/v1/secured-endpoint");
    when(mockContext.header("X-Request-Source")).thenReturn("Web");
    when(mockContext.sessionAttribute("user")).thenReturn(null);

    dashboardServer.preHandleRequest(mockContext);
  }

  @Test
  public void testPreHandleRequestBasicAuth() {
    when(mockContext.path()).thenReturn("/api/ams/v1/secured-endpoint");
    when(mockContext.header("X-Request-Source")).thenReturn(null);

    BasicAuthCredentials mockCredentials = mock(BasicAuthCredentials.class);
    when(mockCredentials.component1()).thenReturn("admin");
    when(mockCredentials.component2()).thenReturn("password");
    when(mockContext.basicAuthCredentials()).thenReturn(mockCredentials);

    dashboardServer.preHandleRequest(mockContext);

    verify(mockContext, times(1)).path();
    verify(mockContext, times(1)).header("X-Request-Source");
    verify(mockContext, times(1)).basicAuthCredentials();
  }

  @Test(expected = SignatureCheckException.class)
  public void testPreHandleRequestBasicAuthFailed() {
    when(mockContext.path()).thenReturn("/api/ams/v1/secured-endpoint");
    when(mockContext.header("X-Request-Source")).thenReturn(null);

    BasicAuthCredentials mockCredentials = mock(BasicAuthCredentials.class);
    when(mockCredentials.component1()).thenReturn("wrong");
    when(mockCredentials.component2()).thenReturn("wrong");
    when(mockContext.basicAuthCredentials()).thenReturn(mockCredentials);

    dashboardServer.preHandleRequest(mockContext);
  }

  @Test
  public void testHandleException() {
    when(mockContext.url()).thenReturn("/test-url");
    when(mockHttpRequest.getRequestURI()).thenReturn("/api/ams/v1/test");

    ArgumentCaptor<ErrorResponse> responseCaptor = ArgumentCaptor.forClass(ErrorResponse.class);

    dashboardServer.handleException(new ForbiddenException("Test forbidden"), mockContext);
    verify(mockContext).json(responseCaptor.capture());
    Assert.assertEquals(HttpCode.FORBIDDEN.getStatus(), responseCaptor.getValue().getCode());

    dashboardServer.handleException(new SignatureCheckException("Test signature"), mockContext);
    verify(mockContext, times(2)).json(responseCaptor.capture());
    Assert.assertEquals(
        HttpCode.FORBIDDEN.getStatus(), responseCaptor.getAllValues().get(1).getCode());

    dashboardServer.handleException(new RuntimeException("Test exception"), mockContext);
    verify(mockContext, times(3)).json(responseCaptor.capture());
    Assert.assertEquals(
        HttpCode.INTERNAL_SERVER_ERROR.getStatus(), responseCaptor.getAllValues().get(2).getCode());
  }

  @Test
  public void testHandleExceptionForNonApiRequest() {
    when(mockContext.url()).thenReturn("/some-page");
    when(mockHttpRequest.getRequestURI()).thenReturn("/some-page");

    dashboardServer.handleException(new ForbiddenException("Test forbidden"), mockContext);
    verify(mockContext).html(anyString());
  }
}
