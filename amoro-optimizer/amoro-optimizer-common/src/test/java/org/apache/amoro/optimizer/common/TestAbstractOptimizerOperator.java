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

package org.apache.amoro.optimizer.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAbstractOptimizerOperator {

  @Test
  public void testStrayInterruptWhileStartedIsSwallowed() {
    AbstractOptimizerOperator operator = new AbstractOptimizerOperator(new OptimizerConfig());
    Thread.currentThread().interrupt();
    try {
      operator.waitAShortTime(10);
      // A stray interrupt while the operator is still running must not leave the flag set,
      // otherwise every subsequent sleep returns instantly and the poll/retry loops busy-spin
      // for the remaining lifetime of the process.
      Assertions.assertFalse(
          Thread.currentThread().isInterrupted(),
          "Interrupt flag must be swallowed while the operator is started");
    } finally {
      Thread.interrupted();
    }
  }

  @Test
  public void testInterruptDuringShutdownIsPreserved() {
    AbstractOptimizerOperator operator = new AbstractOptimizerOperator(new OptimizerConfig());
    operator.stop();
    Thread.currentThread().interrupt();
    try {
      operator.waitAShortTime(10);
      Assertions.assertTrue(
          Thread.currentThread().isInterrupted(),
          "Interrupt flag must be preserved after stop so shutdown skips residual sleeps");
    } finally {
      Thread.interrupted();
    }
  }
}
