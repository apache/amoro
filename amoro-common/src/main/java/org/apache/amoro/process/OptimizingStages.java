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

import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

/** The stage of the optimizing process. */
public class OptimizingStages {

  /** minor optimizing executing phase */
  public static final ProcessStage MINOR = new ProcessStage("minor", 13);
  /** major optimizing executing phase */
  public static final ProcessStage MAJOR = new ProcessStage("major", 14);
  /** full optimizing executing phase */
  public static final ProcessStage FULL = new ProcessStage("full", 15);
  /** Committing phase of optimizing */
  public static final ProcessStage COMMITTING = new ProcessStage("committing", 18);
  /** Planning phase of optimizing */
  public static final ProcessStage PLANNING = new ProcessStage("planning", 17);
  /** evaluating phase of optimizing */
  public static final ProcessStage EVALUATING = new ProcessStage("evaluating", 16);
  /** When input data has been collected but waiting for quota available(not scheduled yet) */
  public static final ProcessStage PENDING = new ProcessStage("pending", 9);
  /** When the process has been scheduled but being waiting for quota available */
  public static final ProcessStage SUSPENDING = new ProcessStage("suspending", 9);
  /** When waiting for input data */
  public static final ProcessStage IDLE = new ProcessStage("idle", 1);

  private static final Map<String, ProcessStage> STAGES =
      ImmutableMap.<String, ProcessStage>builder()
          .put(MINOR.getDesc(), MINOR)
          .put(MAJOR.getDesc(), MAJOR)
          .put(FULL.getDesc(), FULL)
          .put(COMMITTING.getDesc(), COMMITTING)
          .put(PLANNING.getDesc(), PLANNING)
          .put(EVALUATING.getDesc(), EVALUATING)
          .put(PENDING.getDesc(), PENDING)
          .put(SUSPENDING.getDesc(), SUSPENDING)
          .put(IDLE.getDesc(), IDLE)
          .build();

  public static ProcessStage of(String desc) {
    return Optional.ofNullable(STAGES.get(desc))
        .orElseThrow(() -> new IllegalArgumentException("No optimizing stage with desc: " + desc));
  }
}
