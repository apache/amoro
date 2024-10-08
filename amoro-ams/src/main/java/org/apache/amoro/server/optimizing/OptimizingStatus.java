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

package org.apache.amoro.server.optimizing;

public enum OptimizingStatus {
  FULL_OPTIMIZING("full", true, 100),
  MAJOR_OPTIMIZING("major", true, 200),
  MINOR_OPTIMIZING("minor", true, 300),
  COMMITTING("committing", true, 400),
  PLANNING("planning", false, 500),
  PENDING("pending", false, 600),
  IDLE("idle", false, 700);
  private final String displayValue;

  private final boolean isProcessing;

  private final int code;

  OptimizingStatus(String displayValue, boolean isProcessing, int code) {
    this.displayValue = displayValue;
    this.isProcessing = isProcessing;
    this.code = code;
  }

  public boolean isProcessing() {
    return isProcessing;
  }

  public String displayValue() {
    return displayValue;
  }

  public int getCode() {
    return code;
  }

  public static OptimizingStatus ofCode(int code) {
    for (OptimizingStatus status : values()) {
      if (status.getCode() == code) {
        return status;
      }
    }
    return null;
  }
}
