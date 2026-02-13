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

import java.time.Duration;

/** Process trigger strategy. */
public final class ProcessTriggerStrategy {

  private final Duration triggerInterval;

  private final boolean triggerOnNewSnapshot;

  private final int triggerParallelism;

  public ProcessTriggerStrategy(
      Duration triggerInterval, boolean triggerOnNewSnapshot, int triggerParallelism) {
    this.triggerInterval = triggerInterval;
    this.triggerOnNewSnapshot = triggerOnNewSnapshot;
    this.triggerParallelism = triggerParallelism;
  }

  public static ProcessTriggerStrategy triggerAtFixRate(Duration triggerInterval) {
    return new ProcessTriggerStrategy(triggerInterval, false, 1);
  }

  public Duration getTriggerInterval() {
    return triggerInterval;
  }

  public boolean isTriggerOnNewSnapshot() {
    return triggerOnNewSnapshot;
  }

  public int getTriggerParallelism() {
    return triggerParallelism;
  }
}
