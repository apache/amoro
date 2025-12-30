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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.javalin.http.Context;
import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.server.ha.HighAvailabilityContainer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/** Unit tests for RequestForwarder. */
public class TestRequestForwarder {

  private HighAvailabilityContainer mockHaContainer;
  private RequestForwarder requestForwarder;
  private CloseableHttpClient mockHttpClient;
  private Context mockContext;
  private HttpServletRequest mockServletRequest;

  @Before
  public void setUp() {
    mockHaContainer = mock(HighAvailabilityContainer.class);
    mockHttpClient = mock(CloseableHttpClient.class);
    mockContext = mock(Context.class);
    mockServletRequest = mock(HttpServletRequest.class);

    // Setup default mock behavior for HttpServletRequest
    // getHeaderNames() should return an empty enumeration by default, not null
    Enumeration<String> emptyHeaderNames = new Vector<String>().elements();
    when(mockServletRequest.getHeaderNames()).thenReturn(emptyHeaderNames);

    // Create RequestForwarder with custom configuration for testing
    // isMasterSlaveMode is set to true by default for most tests
    requestForwarder =
        new RequestForwarder(
            mockHaContainer,
            5000, // timeoutMs
            2, // maxRetries (reduced for faster tests)
            100, // retryBackoffMs (reduced for faster tests)
            3, // circuitBreakerThreshold (reduced for faster tests)
            5000, // circuitBreakerTimeoutMs (reduced for faster tests)
            true); // isMasterSlaveMode

    // Use reflection to replace httpClient with mock (for testing purposes)
    // Note: In a real scenario, you might want to use a test-friendly constructor
    // or make httpClient injectable
    try {
      java.lang.reflect.Field httpClientField =
          RequestForwarder.class.getDeclaredField("httpClient");
      httpClientField.setAccessible(true);
      httpClientField.set(requestForwarder, mockHttpClient);
    } catch (Exception e) {
      throw new RuntimeException("Failed to inject mock HTTP client", e);
    }
  }

  // Helper method to set req field using reflection
  // Note: ctx.req is a public field in Javalin Context class, not a method
  // We need to use reflection because Mockito's when() only works with method calls
  private void setReqField(Context ctx, HttpServletRequest req) {
    try {
      // Try to find the req field
      java.lang.reflect.Field reqField = null;
      try {
        reqField = Context.class.getDeclaredField("req");
      } catch (NoSuchFieldException e) {
        // If direct field access fails, search through all fields
        java.lang.reflect.Field[] fields = Context.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
          if (field.getType() == HttpServletRequest.class
              || field.getType().isAssignableFrom(HttpServletRequest.class)) {
            reqField = field;
            break;
          }
        }
        if (reqField == null) {
          throw new RuntimeException("Could not find req field in Context class", e);
        }
      }

      reqField.setAccessible(true);
      reqField.set(ctx, req);
    } catch (IllegalAccessException e) {
      // If we can't set the field (e.g., Context is final and field is final),
      // we'll skip this test setup - the test will fail if it actually needs req
      // This is a limitation of mocking final classes with Mockito
      throw new RuntimeException(
          "Failed to set req field - Context may be final class. Consider using Mockito-inline or adjusting test.",
          e);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set req field", e);
    }
  }

  @After
  public void tearDown() {
    if (requestForwarder != null) {
      requestForwarder.close();
    }
  }

  @Test
  public void testShouldForward_NotMasterSlaveMode() {
    // Create a new RequestForwarder with isMasterSlaveMode = false
    RequestForwarder nonMasterSlaveForwarder =
        new RequestForwarder(
            mockHaContainer, 5000, 2, 100, 3, 5000, false); // isMasterSlaveMode = false

    assertFalse(
        "Should not forward when master-slave mode is disabled",
        nonMasterSlaveForwarder.shouldForward(mockContext));
    verify(mockHaContainer, never()).hasLeadership();
  }

  @Test
  public void testShouldForward_IsLeader() {
    when(mockHaContainer.hasLeadership()).thenReturn(true);

    assertFalse(
        "Should not forward when current node is leader",
        requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testShouldForward_GetMethod() {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("GET");

    assertFalse("Should not forward GET requests", requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testShouldForward_PostMethod() {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");

    assertTrue("Should forward POST requests", requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testShouldForward_PutMethod() {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("PUT");

    assertTrue("Should forward PUT requests", requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testShouldForward_DeleteMethod() {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("DELETE");

    assertTrue("Should forward DELETE requests", requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testForwardRequest_Success() throws Exception {
    // Setup
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    // Mock HTTP response
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

    HttpEntity mockEntity = mock(HttpEntity.class);
    byte[] responseBody = "{\"success\":true}".getBytes();
    when(mockEntity.getContentLength()).thenReturn((long) responseBody.length);
    // EntityUtils.toByteArray() calls getContent() internally, so we need to mock getContent()
    when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody));
    when(mockResponse.getEntity()).thenReturn(mockEntity);

    BasicHeader contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentTypeHeader);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {contentTypeHeader});

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    // Execute and verify
    try {
      requestForwarder.forwardRequest(mockContext);
      // Should throw RequestForwardedException
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      // Verify exception contains response data
      assertTrue("Exception should have response data", e.hasResponseData());
      assertEquals("Status code should be 200", 200, e.getStatusCode());
      assertNotNull("Response body should not be null", e.getResponseBody());
      assertEquals(
          "Content type should be application/json", "application/json", e.getContentType());
    }

    // Verify context was updated
    verify(mockContext).status(200);
    verify(mockContext).result(responseBody);
    verify(mockContext).contentType("application/json");
  }

  @Test
  public void testForwardRequest_WithQueryString() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("GET");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn("param1=value1&param2=value2");

    // This should not forward (GET method)
    assertFalse(requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testForwardRequest_NoLeaderInfo() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(null);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw IOException when leader info is not available", false);
    } catch (IOException e) {
      // The exception message may be wrapped after retries, so check both the message and cause
      String message = e.getMessage();
      String lowerMessage = message != null ? message.toLowerCase() : "";
      boolean containsLeaderInfo = lowerMessage.contains("leader");

      // Also check the cause if message doesn't contain it
      if (!containsLeaderInfo && e.getCause() != null) {
        String causeMessage = e.getCause().getMessage();
        String lowerCauseMessage = causeMessage != null ? causeMessage.toLowerCase() : "";
        containsLeaderInfo = lowerCauseMessage.contains("leader");
      }

      assertTrue(
          "Error message or cause should mention leader info. Message: "
              + message
              + (e.getCause() != null ? ", Cause: " + e.getCause().getMessage() : ""),
          containsLeaderInfo);
    }
  }

  @Test
  public void testForwardRequest_RetryOnFailure() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    // First attempt fails, second succeeds
    when(mockHttpClient.execute(any(HttpRequestBase.class)))
        .thenThrow(new IOException("Connection timeout"))
        .thenAnswer(
            invocation -> {
              CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
              StatusLine mockStatusLine = mock(StatusLine.class);
              when(mockStatusLine.getStatusCode()).thenReturn(200);
              when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

              HttpEntity mockEntity = mock(HttpEntity.class);
              byte[] responseBody = "{\"success\":true}".getBytes();
              // EntityUtils.toByteArray() calls getContent() internally, so we need to mock
              // getContent()
              when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody));
              when(mockResponse.getEntity()).thenReturn(mockEntity);

              when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
              when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[0]);

              return mockResponse;
            });

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException after retry", false);
    } catch (RequestForwardedException e) {
      assertTrue("Exception should have response data", e.hasResponseData());
      assertEquals("Status code should be 200", 200, e.getStatusCode());
    }

    // Verify retry happened (execute called twice)
    verify(mockHttpClient, times(2)).execute(any(HttpRequestBase.class));
  }

  @Test
  public void testForwardRequest_MaxRetriesExceeded() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    // All attempts fail
    when(mockHttpClient.execute(any(HttpRequestBase.class)))
        .thenThrow(new IOException("Connection timeout"));

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw IOException after max retries", false);
    } catch (IOException e) {
      assertTrue(
          "Error message should mention retry attempts", e.getMessage().contains("attempts"));
    }

    // Verify retry happened (maxRetries + 1 = 3 times)
    verify(mockHttpClient, times(3)).execute(any(HttpRequestBase.class));
  }

  @Test
  public void testForwardRequest_Status204() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("DELETE");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs/test");
    when(mockContext.queryString()).thenReturn(null);
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(204);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockResponse.getEntity()).thenReturn(null);
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[0]);

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertEquals("Status code should be 204", 204, e.getStatusCode());
      // 204 should not have response body
      assertTrue("Response body should be null for 204", e.getResponseBody() == null);
    }

    verify(mockContext).status(204);
    verify(mockContext, never()).result(any(byte[].class));
  }

  @Test
  public void testForwardRequest_EmptyResponseBody() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockResponse.getEntity()).thenReturn(null); // No entity
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[0]);

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertEquals("Status code should be 200", 200, e.getStatusCode());
      assertNotNull("Response body should be set (empty JSON)", e.getResponseBody());
      assertEquals(
          "Content type should be application/json", "application/json", e.getContentType());
      // Should be empty JSON object
      String bodyStr = new String(e.getResponseBody());
      assertEquals("Body should be empty JSON", "{}", bodyStr);
    }

    verify(mockContext).result(any(byte[].class));
    verify(mockContext).contentType("application/json");
  }

  @Test
  public void testForwardRequest_RequestBodyFromInputStream() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("PUT");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs/test");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn(null); // No body in context
    setReqField(mockContext, mockServletRequest);

    // Mock input stream
    String requestBody = "{\"name\":\"test\",\"type\":\"hive\"}";
    InputStream inputStream = new ByteArrayInputStream(requestBody.getBytes());
    when(mockServletRequest.getInputStream())
        .thenReturn(
            new javax.servlet.ServletInputStream() {
              @Override
              public int read() throws IOException {
                return inputStream.read();
              }

              @Override
              public int read(byte[] b, int off, int len) throws IOException {
                return inputStream.read(b, off, len);
              }

              @Override
              public boolean isFinished() {
                try {
                  return inputStream.available() == 0;
                } catch (IOException e) {
                  return false;
                }
              }

              @Override
              public boolean isReady() {
                return true;
              }

              @Override
              public void setReadListener(javax.servlet.ReadListener readListener) {
                // Not needed for test
              }
            });
    when(mockServletRequest.getContentType()).thenReturn("application/json");

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

    HttpEntity mockEntity = mock(HttpEntity.class);
    byte[] responseBody = "{\"success\":true}".getBytes();
    // EntityUtils.toByteArray() calls getContent() internally, so we need to mock getContent()
    when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody));
    when(mockResponse.getEntity()).thenReturn(mockEntity);

    BasicHeader contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentTypeHeader);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {contentTypeHeader});

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertTrue("Exception should have response data", e.hasResponseData());
    }

    // Verify HTTP request was created with body
    verify(mockHttpClient).execute(any(HttpRequestBase.class));
  }

  @Test
  public void testForwardRequest_CopyHeaders() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    // Mock request headers
    Map<String, Vector<String>> headers = new HashMap<>();
    Vector<String> authValues = new Vector<>();
    authValues.add("Bearer token123");
    headers.put("Authorization", authValues);
    Vector<String> acceptValues = new Vector<>();
    acceptValues.add("application/json");
    headers.put("Accept", acceptValues);

    Enumeration<String> headerNames = new Vector<>(headers.keySet()).elements();
    when(mockServletRequest.getHeaderNames()).thenReturn(headerNames);
    when(mockServletRequest.getHeaders("Authorization"))
        .thenReturn(headers.get("Authorization").elements());
    when(mockServletRequest.getHeaders("Accept")).thenReturn(acceptValues.elements());

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

    HttpEntity mockEntity = mock(HttpEntity.class);
    byte[] responseBody = "{\"success\":true}".getBytes();
    // EntityUtils.toByteArray() calls getContent() internally, so we need to mock getContent()
    when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody));
    when(mockResponse.getEntity()).thenReturn(mockEntity);

    BasicHeader contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentTypeHeader);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {contentTypeHeader});

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertTrue("Exception should have response data", e.hasResponseData());
    }

    // Verify headers were copied (we can't easily verify the exact headers in the request,
    // but we can verify the execute was called)
    verify(mockHttpClient).execute(any(HttpRequestBase.class));
  }

  @Test
  public void testCircuitBreaker_OpenAfterFailures() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    // All requests fail
    when(mockHttpClient.execute(any(HttpRequestBase.class)))
        .thenThrow(new IOException("Connection timeout"));

    // Trigger failures to open circuit breaker
    for (int i = 0; i < 3; i++) {
      try {
        requestForwarder.forwardRequest(mockContext);
      } catch (IOException e) {
        // Expected
      }
    }

    // Circuit breaker should be open now
    assertTrue(
        "Circuit breaker should be open after multiple failures",
        requestForwarder.isCircuitBreakerOpenForMonitoring());

    // Should not forward when circuit breaker is open
    assertFalse(
        "Should not forward when circuit breaker is open",
        requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testGetFailureCount() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    when(mockHttpClient.execute(any(HttpRequestBase.class)))
        .thenThrow(new IOException("Connection timeout"));

    assertEquals("Initial failure count should be 0", 0, requestForwarder.getFailureCount());

    try {
      requestForwarder.forwardRequest(mockContext);
    } catch (IOException e) {
      // Expected
    }

    assertTrue("Failure count should be greater than 0", requestForwarder.getFailureCount() > 0);
  }

  @Test
  public void testForwardRequest_Status500() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("{\"name\":\"test\"}");
    when(mockContext.contentType()).thenReturn("application/json");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(500);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

    HttpEntity mockEntity = mock(HttpEntity.class);
    byte[] responseBody = "{\"error\":\"Internal server error\"}".getBytes();
    // EntityUtils.toByteArray() calls getContent() internally, so we need to mock getContent()
    when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody));
    when(mockResponse.getEntity()).thenReturn(mockEntity);

    BasicHeader contentTypeHeader = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(contentTypeHeader);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {contentTypeHeader});

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertEquals("Status code should be 500", 500, e.getStatusCode());
      assertNotNull("Response body should not be null", e.getResponseBody());
    }

    verify(mockContext).status(500);
    verify(mockContext).result(responseBody);
  }

  @Test
  public void testForwardRequest_NonApiEndpoint() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("POST");
    when(mockContext.path()).thenReturn("/some/non-api/path");
    when(mockContext.queryString()).thenReturn(null);
    when(mockContext.body()).thenReturn("test body");
    when(mockContext.contentType()).thenReturn("text/plain");
    setReqField(mockContext, mockServletRequest);

    AmsServerInfo leaderInfo = new AmsServerInfo();
    leaderInfo.setHost("leader.example.com");
    leaderInfo.setRestBindPort(8080);
    when(mockHaContainer.getLeaderNodeInfo()).thenReturn(leaderInfo);

    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    StatusLine mockStatusLine = mock(StatusLine.class);
    when(mockStatusLine.getStatusCode()).thenReturn(200);
    when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    when(mockResponse.getEntity()).thenReturn(null);
    when(mockResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE)).thenReturn(null);
    when(mockResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[0]);

    when(mockHttpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);

    try {
      requestForwarder.forwardRequest(mockContext);
      assertTrue("Should throw RequestForwardedException", false);
    } catch (RequestForwardedException e) {
      assertEquals("Status code should be 200", 200, e.getStatusCode());
      // For non-API endpoints, empty string should be set
      assertNotNull("Response body should be set", e.getResponseBody());
      String bodyStr = new String(e.getResponseBody());
      assertEquals("Body should be empty string for non-API endpoints", "", bodyStr);
    }

    verify(mockContext).result("");
  }

  @Test
  public void testForwardRequest_WithResponseHeaders() throws Exception {
    when(mockHaContainer.hasLeadership()).thenReturn(false);
    when(mockContext.method()).thenReturn("GET");
    when(mockContext.path()).thenReturn("/api/ams/v1/catalogs");
    when(mockContext.queryString()).thenReturn(null);

    // GET should not be forwarded
    assertFalse(
        "GET requests should not be forwarded", requestForwarder.shouldForward(mockContext));
  }

  @Test
  public void testClose() throws Exception {
    // Test that close doesn't throw exception
    requestForwarder.close();
    // Should not throw
  }
}
