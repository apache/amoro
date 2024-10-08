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

import org.apache.amoro.ActionStage;

/** The stage of the optimizing process. */
public class OptimizingStages {

  /** Minor optimizing executing phase */
  public static final ActionStage RUNNING = new ActionStage("running", 19);

  /** Committing phase of optimizing */
  public static final ActionStage COMMITTING = new ActionStage("committing", 18);

  /** Planning phase of optimizing */
  public static final ActionStage PLANNING = new ActionStage("planning", 17);
  public static final ActionStage EVALUATING = new ActionStage("evaluating", 16);

  /** When input data has been collected but waiting for quota available(not scheduled yet) */
  public static final ActionStage PENDING = new ActionStage("pending", 9);

  /** When the process has been scheduled but being waiting for quota available */
  public static final ActionStage SUSPENDING = new ActionStage("suspending", 9);

  /** When waiting for input data */
  public static final ActionStage IDLE = new ActionStage("idle", 8);
}
