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

import java.util.Map;

/**
 * Exception thrown when a request has been forwarded to the leader node. This exception is used to
 * stop further processing of the request in the current node. It also carries the response
 * information to ensure the response is properly returned to the client.
 */
public class RequestForwardedException extends RuntimeException {
  private final int statusCode;
  private final byte[] responseBody;
  private final String contentType;
  private final Map<String, String> responseHeaders;

  public RequestForwardedException(
      String message,
      int statusCode,
      byte[] responseBody,
      String contentType,
      Map<String, String> responseHeaders) {
    super(message);
    this.statusCode = statusCode;
    this.responseBody = responseBody;
    this.contentType = contentType;
    this.responseHeaders = responseHeaders;
  }

  public RequestForwardedException(String message) {
    this(message, 0, null, null, null);
  }

  public int getStatusCode() {
    return statusCode;
  }

  public byte[] getResponseBody() {
    return responseBody;
  }

  public String getContentType() {
    return contentType;
  }

  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }

  public boolean hasResponseData() {
    return statusCode > 0;
  }
}
