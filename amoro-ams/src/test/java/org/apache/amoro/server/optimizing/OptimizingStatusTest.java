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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OptimizingStatusTest {
  @Test
  public void testOptimizingStatusCodeValue() {
    assertEquals(7, OptimizingStatus.values().length);

    assertEquals(OptimizingStatus.FULL_OPTIMIZING, OptimizingStatus.ofCode(100));
    assertEquals(OptimizingStatus.MAJOR_OPTIMIZING, OptimizingStatus.ofCode(200));
    assertEquals(OptimizingStatus.MINOR_OPTIMIZING, OptimizingStatus.ofCode(300));
    assertEquals(OptimizingStatus.COMMITTING, OptimizingStatus.ofCode(400));
    assertEquals(OptimizingStatus.PLANNING, OptimizingStatus.ofCode(500));
    assertEquals(OptimizingStatus.PENDING, OptimizingStatus.ofCode(600));
    assertEquals(OptimizingStatus.IDLE, OptimizingStatus.ofCode(700));
  }
}
