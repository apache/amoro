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

package org.apache.amoro.maintainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Base implementation of MaintainerOutput. */
public class BaseMaintainerOutput implements MaintainerOutput {

  private static final long serialVersionUID = 1L;

  private final Map<String, String> summary;
  private final boolean success;
  private final String errorMessage;

  /** Create a successful maintainer output. */
  public BaseMaintainerOutput() {
    this(true, null);
  }

  /**
   * Create a maintainer output with specified status.
   *
   * @param success whether the operation succeeded
   * @param errorMessage error message if failed, null otherwise
   */
  public BaseMaintainerOutput(boolean success, String errorMessage) {
    this.summary = new HashMap<>();
    this.success = success;
    this.errorMessage = errorMessage;
  }

  @Override
  public Map<String, String> summary() {
    return Collections.unmodifiableMap(summary);
  }

  /**
   * Add a summary entry.
   *
   * @param key summary key
   * @param value summary value
   */
  public void putSummary(String key, String value) {
    summary.put(key, value);
  }

  @Override
  public boolean isSuccess() {
    return success;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }
}
