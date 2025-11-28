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

import io.javalin.http.Context;
import org.apache.amoro.client.AmsServerInfo;
import org.apache.amoro.server.HighAvailabilityContainer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced request forwarder for master-slave mode with retry mechanism, circuit breaker, and
 * better error handling.
 */
public class RequestForwarder {

  private static final Logger LOG = LoggerFactory.getLogger(RequestForwarder.class);

  // Default configuration values
  private static final int DEFAULT_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);
  private static final int DEFAULT_RETRY_COUNT = 3;
  private static final int DEFAULT_RETRY_BACKOFF_MS = 1000;
  private static final int DEFAULT_CIRCUIT_BREAKER_THRESHOLD = 5;
  private static final long DEFAULT_CIRCUIT_BREAKER_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1);

  private final HighAvailabilityContainer haContainer;
  private final CloseableHttpClient httpClient;
  private final int maxRetries;
  private final int retryBackoffMs;
  private final int circuitBreakerThreshold;
  private final long circuitBreakerTimeoutMs;

  // Circuit breaker state
  private final AtomicInteger failureCount = new AtomicInteger(0);
  private final AtomicLong lastFailureTime = new AtomicLong(0);
  private volatile boolean circuitOpen = false;

  public RequestForwarder(HighAvailabilityContainer haContainer) {
    this(
        haContainer,
        DEFAULT_TIMEOUT_MS,
        DEFAULT_RETRY_COUNT,
        DEFAULT_RETRY_BACKOFF_MS,
        DEFAULT_CIRCUIT_BREAKER_THRESHOLD,
        DEFAULT_CIRCUIT_BREAKER_TIMEOUT_MS);
  }

  public RequestForwarder(
      HighAvailabilityContainer haContainer,
      int timeoutMs,
      int maxRetries,
      int retryBackoffMs,
      int circuitBreakerThreshold,
      long circuitBreakerTimeoutMs) {
    this.haContainer = haContainer;
    this.maxRetries = maxRetries;
    this.retryBackoffMs = retryBackoffMs;
    this.circuitBreakerThreshold = circuitBreakerThreshold;
    this.circuitBreakerTimeoutMs = circuitBreakerTimeoutMs;

    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectTimeout(timeoutMs)
            .setSocketTimeout(timeoutMs)
            .setConnectionRequestTimeout(timeoutMs)
            .build();
    this.httpClient =
        HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .setMaxConnTotal(100)
            .setMaxConnPerRoute(20)
            .build();
  }

  /** Check if the request should be forwarded to the leader node. */
  public boolean shouldForward(Context ctx) {
    // Only forward in master-slave mode
    if (!haContainer.isMasterSlaveMode()) {
      return false;
    }

    // Only forward if current node is not the leader
    if (haContainer.hasLeadership()) {
      return false;
    }

    // Check circuit breaker state
    if (isCircuitBreakerOpen()) {
      LOG.warn("Circuit breaker is open, skipping request forwarding");
      return false;
    }

    // Only forward write operations (POST, PUT, DELETE)
    String method = ctx.method();
    return "POST".equalsIgnoreCase(method)
        || "PUT".equalsIgnoreCase(method)
        || "DELETE".equalsIgnoreCase(method);
  }

  /** Forward the request to the leader node with retry mechanism. */
  public boolean forwardRequest(Context ctx) throws IOException {
    String requestId = generateRequestId();
    LOG.info("[{}] Starting request forwarding to leader node", requestId);

    for (int attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        attemptForwardRequest(ctx, requestId, attempt);
        // If we reach here, forwarding was successful and response is set
        // Throw exception to stop all further processing
        LOG.debug(
            "[{}] Request forwarded successfully, throwing exception to stop processing",
            requestId);
        throw new RequestForwardedException("Request successfully forwarded to leader node");
      } catch (RequestForwardedException e) {
        // Request was successfully forwarded, re-throw to stop processing
        LOG.debug(
            "[{}] RequestForwardedException caught, re-throwing to stop local processing",
            requestId);
        throw e;
      } catch (IOException e) {
        handleForwardingFailure(e, requestId, attempt);

        if (attempt == maxRetries) {
          LOG.error(
              "[{}] Request forwarding failed after {} attempts", requestId, maxRetries + 1, e);
          updateCircuitBreakerState();
          throw new IOException(
              "Failed to forward request to leader node after " + (maxRetries + 1) + " attempts",
              e);
        }

        // Wait before retry
        if (attempt < maxRetries) {
          try {
            Thread.sleep(calculateBackoff(attempt));
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Request forwarding interrupted", ie);
          }
        }
      }
    }

    return false;
  }

  private void attemptForwardRequest(Context ctx, String requestId, int attempt)
      throws IOException, RequestForwardedException {
    AmsServerInfo leaderInfo = haContainer.getLeaderNodeInfo();
    if (leaderInfo == null
        || leaderInfo.getHost() == null
        || leaderInfo.getRestBindPort() == null) {
      LOG.warn("[{}] Leader node info is not available", requestId);
      throw new IOException("Leader node info is not available");
    }

    String leaderUrl = buildLeaderUrl(ctx, leaderInfo);
    LOG.debug(
        "[{}] Attempt {}: Forwarding {} request to {}",
        requestId,
        attempt + 1,
        ctx.method(),
        leaderUrl);

    HttpRequestBase httpRequest = createHttpRequest(ctx, leaderUrl, requestId);

    try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
      // handleResponse will throw RequestForwardedException with response data
      handleResponse(ctx, response, requestId);
      // Should not reach here
      throw new IOException("Failed to handle response from leader node");
    }
  }

  private String buildLeaderUrl(Context ctx, AmsServerInfo leaderInfo) {
    String leaderUrl =
        String.format(
            "http://%s:%d%s", leaderInfo.getHost(), leaderInfo.getRestBindPort(), ctx.path());

    // Add query string if present
    String queryString = ctx.queryString();
    if (queryString != null && !queryString.isEmpty()) {
      leaderUrl += "?" + queryString;
    }

    return leaderUrl;
  }

  private HttpRequestBase createHttpRequest(Context ctx, String url, String requestId)
      throws IOException {
    String method = ctx.method();
    HttpRequestBase request;

    switch (method.toUpperCase()) {
      case "POST":
        HttpPost postRequest = new HttpPost(url);
        setRequestBody(ctx, postRequest);
        request = postRequest;
        break;
      case "PUT":
        HttpPut putRequest = new HttpPut(url);
        setRequestBody(ctx, putRequest);
        request = putRequest;
        break;
      case "DELETE":
        request = new HttpDelete(url);
        break;
      default:
        throw new IllegalArgumentException("Unsupported HTTP method for forwarding: " + method);
    }

    // Add tracing headers
    request.addHeader("X-Request-Id", requestId);
    request.addHeader("X-Forwarded-By", "AMS-Follower");
    request.addHeader("X-Forwarded-From", getCurrentNodeInfo());

    copyHeaders(ctx, request);
    return request;
  }

  private void setRequestBody(Context ctx, HttpEntityEnclosingRequestBase request)
      throws IOException {
    // Try to get body from Javalin context first (if available)
    String body = ctx.body();
    if (body != null && !body.isEmpty()) {
      request.setEntity(
          new ByteArrayEntity(body.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
      // Set content type header
      String contentType = ctx.contentType();
      if (contentType != null) {
        request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
      }
      return;
    }

    // Fallback: Read body bytes directly from input stream
    HttpServletRequest servletRequest = ctx.req;
    try (InputStream inputStream = servletRequest.getInputStream()) {
      java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
      byte[] data = new byte[8192];
      int nRead;
      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      byte[] bodyBytes = buffer.toByteArray();
      if (bodyBytes.length > 0) {
        request.setEntity(new ByteArrayEntity(bodyBytes));
        // Set content type header
        String contentType = servletRequest.getContentType();
        if (contentType != null) {
          request.setHeader(HttpHeaders.CONTENT_TYPE, contentType);
        }
      }
    }
  }

  private void copyHeaders(Context ctx, HttpRequestBase httpRequest) {
    HttpServletRequest servletRequest = ctx.req;
    Enumeration<String> headerNames = servletRequest.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (shouldSkipHeader(headerName)) {
        continue;
      }
      Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
      while (headerValues.hasMoreElements()) {
        String headerValue = headerValues.nextElement();
        httpRequest.addHeader(headerName, headerValue);
      }
    }
  }

  private boolean handleResponse(Context ctx, CloseableHttpResponse response, String requestId)
      throws IOException, RequestForwardedException {
    // Copy response status
    int statusCode = response.getStatusLine().getStatusCode();
    ctx.status(statusCode);

    // Copy response body first (before headers) to ensure proper content length calculation
    HttpEntity entity = response.getEntity();
    byte[] responseBody = null;
    String contentType = null;
    java.util.Map<String, String> responseHeaders = new java.util.HashMap<>();

    // For 204 No Content and 304 Not Modified, do not set any response body
    if (statusCode == 204 || statusCode == 304) {
      // These status codes should not have a body
      LOG.debug("[{}] Status {} - no response body should be set", requestId, statusCode);
    } else {
      // For other status codes, always set response body
      if (entity != null) {
        responseBody = EntityUtils.toByteArray(entity);
        // Set response body - even if empty array, we still set it
        ctx.result(responseBody);
      } else {
        // If entity is null, this means Leader returned no body
        // For API endpoints, set empty JSON object to ensure proper response
        // This prevents "insufficient content written" and "Network error" issues
        if (ctx.path().startsWith("/api/")) {
          String emptyJson = "{}";
          responseBody = emptyJson.getBytes(java.nio.charset.StandardCharsets.UTF_8);
          ctx.result(responseBody);
          LOG.debug(
              "[{}] Entity is null, setting empty JSON for API endpoint: {}",
              requestId,
              ctx.path());
        } else {
          // For non-API endpoints, set empty string
          ctx.result("");
          responseBody = new byte[0];
        }
      }
    }

    // Copy response headers AFTER setting body to avoid Content-Length mismatch
    // Note: We skip Content-Length header and let Javalin calculate it automatically
    // Also collect headers for exception
    copyResponseHeaders(ctx, response, responseHeaders);

    // Set content type if available and not already set
    // Skip content type for 204/304 status codes as they should not have content
    if (statusCode != 204 && statusCode != 304) {
      if (response.getFirstHeader(HttpHeaders.CONTENT_TYPE) != null) {
        contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
      }

      if (contentType != null && !contentType.isEmpty()) {
        ctx.contentType(contentType);
      } else if (ctx.path().startsWith("/api/")) {
        // Always set JSON content type for API endpoints to ensure proper parsing
        contentType = "application/json";
        ctx.contentType(contentType);
      }
    }

    LOG.debug(
        "[{}] Request forwarded successfully, response status: {}, body-size: {}, content-type: {}",
        requestId,
        statusCode,
        responseBody != null ? responseBody.length : 0,
        contentType);

    // Throw exception with response data to ensure it's preserved
    throw new RequestForwardedException(
        "Request successfully forwarded to leader node",
        statusCode,
        responseBody,
        contentType,
        responseHeaders);
  }

  private void copyResponseHeaders(
      Context ctx, CloseableHttpResponse response, java.util.Map<String, String> headerMap) {
    org.apache.http.Header[] headers = response.getAllHeaders();
    for (org.apache.http.Header header : headers) {
      if (shouldSkipResponseHeader(header.getName())) {
        continue;
      }
      String headerName = header.getName();
      String headerValue = header.getValue();
      ctx.header(headerName, headerValue);
      // Also store in map for exception
      if (headerMap != null) {
        headerMap.put(headerName, headerValue);
      }
    }
  }

  private void handleForwardingFailure(IOException e, String requestId, int attempt) {
    failureCount.incrementAndGet();
    lastFailureTime.set(System.currentTimeMillis());

    if (attempt < maxRetries) {
      LOG.warn(
          "[{}] Request forwarding attempt {} failed, will retry: {}",
          requestId,
          attempt + 1,
          e.getMessage());
    } else {
      LOG.error("[{}] Request forwarding failed on final attempt", requestId, e);
    }
  }

  private void updateCircuitBreakerState() {
    long currentTime = System.currentTimeMillis();
    long lastFailure = lastFailureTime.get();

    if (failureCount.get() >= circuitBreakerThreshold
        && (currentTime - lastFailure) < circuitBreakerTimeoutMs) {
      circuitOpen = true;
      LOG.warn("Circuit breaker opened due to {} consecutive failures", failureCount.get());

      // Schedule circuit breaker reset
      new Thread(
              () -> {
                try {
                  Thread.sleep(circuitBreakerTimeoutMs);
                  circuitOpen = false;
                  failureCount.set(0);
                  LOG.info("Circuit breaker reset after timeout");
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              })
          .start();
    }
  }

  private boolean isCircuitBreakerOpen() {
    if (circuitOpen) {
      // Check if we should try to close the circuit breaker
      long currentTime = System.currentTimeMillis();
      if ((currentTime - lastFailureTime.get()) >= circuitBreakerTimeoutMs) {
        circuitOpen = false;
        failureCount.set(0);
        LOG.info("Circuit breaker closed after timeout period");
      }
    }
    return circuitOpen;
  }

  private long calculateBackoff(int attempt) {
    return retryBackoffMs * (1L << attempt); // Exponential backoff
  }

  private String generateRequestId() {
    return "req-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private String getCurrentNodeInfo() {
    // Return current node identifier for tracing
    return System.getProperty("node.id", "unknown");
  }

  private boolean shouldSkipHeader(String headerName) {
    String lowerHeaderName = headerName.toLowerCase();
    return lowerHeaderName.equals("connection")
        || lowerHeaderName.equals("keep-alive")
        || lowerHeaderName.equals("transfer-encoding")
        || lowerHeaderName.equals("content-length")
        || lowerHeaderName.equals("host")
        || lowerHeaderName.startsWith("x-forwarded");
  }

  private boolean shouldSkipResponseHeader(String headerName) {
    String lowerHeaderName = headerName.toLowerCase();
    // Skip headers that should not be forwarded or should be recalculated
    return lowerHeaderName.equals("connection")
        || lowerHeaderName.equals("keep-alive")
        || lowerHeaderName.equals("transfer-encoding")
        || lowerHeaderName.equals("content-length") // Let Javalin calculate this automatically
        || lowerHeaderName.equals("content-encoding"); // Skip content-encoding to avoid issues
  }

  public void close() {
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (IOException e) {
        LOG.warn("Failed to close HTTP client", e);
      }
    }
  }

  // Getters for monitoring
  public int getFailureCount() {
    return failureCount.get();
  }

  public boolean isCircuitBreakerOpenForMonitoring() {
    return circuitOpen;
  }

  public long getLastFailureTime() {
    return lastFailureTime.get();
  }
}
