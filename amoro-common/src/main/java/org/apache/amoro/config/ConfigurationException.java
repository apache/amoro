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

package org.apache.amoro.config;

/**
 * Exception thrown when a configuration value causes an exception, typically when converting
 * Duration to milliseconds. This exception can be caught to trigger process exit.
 */
public class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String configKey;

  public ConfigurationException(String configKey, String message) {
    super(message);
    this.configKey = configKey;
  }

  public ConfigurationException(String configKey, String message, Throwable cause) {
    super(message, cause);
    this.configKey = configKey;
  }

  /**
   * Returns the configuration key that caused the overflow.
   *
   * @return the configuration key
   */
  public String getConfigKey() {
    return configKey;
  }
}
