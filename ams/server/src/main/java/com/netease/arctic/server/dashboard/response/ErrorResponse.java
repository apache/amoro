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

package com.netease.arctic.server.dashboard.response;

import io.javalin.http.HttpStatus;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

public class ErrorResponse extends Response {
  private String requestId;

  public ErrorResponse(int code, String message, String requestId) {
    super(code, message);
    this.requestId = requestId;
  }

  public ErrorResponse(HttpStatus httpStatus, String message, String requestId) {
    super(httpStatus.getCode(), message);
    this.requestId = requestId;
  }

  public ErrorResponse(String message) {
    super(HttpStatus.BAD_REQUEST.getCode(), message);
    this.requestId = null;
  }

  public static ErrorResponse of(String message) {
    return new ErrorResponse(message);
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("requestId", requestId)
        .toString();
  }
}
