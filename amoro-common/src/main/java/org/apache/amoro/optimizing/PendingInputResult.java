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

package org.apache.amoro.optimizing;

import org.apache.amoro.table.FormatPendingInput;

/** Result of a format-specific pending input evaluation. */
public class PendingInputResult {

  private final FormatPendingInput pendingInput;
  private final FormatPendingInput optimizingPendingInput;
  private final boolean optimizingNecessary;

  public PendingInputResult(FormatPendingInput pendingInput, boolean optimizingNecessary) {
    this(pendingInput, pendingInput, optimizingNecessary);
  }

  public PendingInputResult(
      FormatPendingInput pendingInput,
      FormatPendingInput optimizingPendingInput,
      boolean optimizingNecessary) {
    this.pendingInput = pendingInput;
    this.optimizingPendingInput =
        optimizingPendingInput == null ? pendingInput : optimizingPendingInput;
    this.optimizingNecessary = optimizingNecessary;
  }

  public FormatPendingInput pendingInput() {
    return pendingInput;
  }

  public FormatPendingInput optimizingPendingInput() {
    return optimizingPendingInput;
  }

  public boolean optimizingNecessary() {
    return optimizingNecessary;
  }
}
