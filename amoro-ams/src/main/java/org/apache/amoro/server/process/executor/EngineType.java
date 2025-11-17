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

package org.apache.amoro.server.process.executor;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.Objects;

/** EngineType models a simple execution engine identifier used by process executors. */
public final class EngineType {

  private static final int MAX_NAME_LENGTH = 16;

  private final String engineName;

  /**
   * Construct with the given engine name.
   *
   * @param engineName engine name
   */
  public EngineType(String engineName) {
    Preconditions.checkArgument(
        engineName.length() <= MAX_NAME_LENGTH,
        "EngineType name length should be less than " + MAX_NAME_LENGTH);
    this.engineName = engineName;
  }

  /**
   * Get engine name.
   *
   * @return engine name
   */
  public String getEngineName() {
    return engineName;
  }

  /**
   * Create an EngineType from string name.
   *
   * @param engineType engine name
   * @return EngineType
   */
  public static EngineType of(String engineType) {
    return new EngineType(engineType);
  }

  /**
   * Compare equality by engine name.
   *
   * @param o other object
   * @return true if same engine name
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EngineType engineType = (EngineType) o;
    return Objects.equals(engineName, engineType.getEngineName());
  }

  /**
   * Hash code based on engine name.
   *
   * @return hash code
   */
  @Override
  public int hashCode() {
    return Objects.hash(engineName);
  }
}
