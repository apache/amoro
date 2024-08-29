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

package org.apache.amoro.process;

/** The stage of the optimizing process. */
public enum OptimizingStage {

  /** Full optimizing executing phase */
  FULL_OPTIMIZING("full", true),

  /** Major optimizing executing phase */
  MAJOR_OPTIMIZING("major", true),

  /** Minor optimizing executing phase */
  MINOR_OPTIMIZING("minor", true),

  /** Committing phase of optimizing */
  COMMITTING("committing", true),

  /** Planning phase of optimizing */
  PLANNING("planning", false),

  /** When input data has been collected but waiting for quota available(not scheduled yet) */
  PENDING("pending", false),

  /** When waiting for input data */
  IDLE("idle", false),

  /** When the process has been scheduled but being waiting for quota available */
  SUSPENDING("suspending", false),

  /** Mainly for external process submitting to external resources */
  SUBMITTING("submitting", false);

  /** The display description of the stage. */
  private final String displayValue;

  /*
   * Whether the stage is an optimizing executing stage.
   */
  private final boolean isOptimizing;

  OptimizingStage(String displayValue, boolean isProcessing) {
    this.displayValue = displayValue;
    this.isOptimizing = isProcessing;
  }

  public boolean isOptimizing() {
    return isOptimizing;
  }

  public String displayValue() {
    return displayValue;
  }
}
