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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class OptimizingStatusTest {
  @Test
  public void testNumberOfOptimizingStatuses() {
    assertEquals(7, OptimizingStatus.values().length);
  }

  static Stream<Arguments> codeToStatusProvider() {
    return Stream.of(
        Arguments.of(100, OptimizingStatus.FULL_OPTIMIZING),
        Arguments.of(200, OptimizingStatus.MAJOR_OPTIMIZING),
        Arguments.of(300, OptimizingStatus.MINOR_OPTIMIZING),
        Arguments.of(400, OptimizingStatus.COMMITTING),
        Arguments.of(500, OptimizingStatus.PLANNING),
        Arguments.of(600, OptimizingStatus.PENDING),
        Arguments.of(700, OptimizingStatus.IDLE));
  }

  @ParameterizedTest
  @MethodSource("codeToStatusProvider")
  public void testOptimizingStatusCodeValue(int code, OptimizingStatus expectedStatus) {
    assertEquals(expectedStatus, OptimizingStatus.ofCode(code));
  }

  static Stream<Arguments> displayValueToStatusProvider() {
    return Stream.of(
        Arguments.of("full", OptimizingStatus.FULL_OPTIMIZING),
        Arguments.of("major", OptimizingStatus.MAJOR_OPTIMIZING),
        Arguments.of("minor", OptimizingStatus.MINOR_OPTIMIZING),
        Arguments.of("committing", OptimizingStatus.COMMITTING),
        Arguments.of("planning", OptimizingStatus.PLANNING),
        Arguments.of("pending", OptimizingStatus.PENDING),
        Arguments.of("idle", OptimizingStatus.IDLE));
  }

  @ParameterizedTest
  @MethodSource("displayValueToStatusProvider")
  public void testOptimizingStatusDisplayValue(
      String displayValue, OptimizingStatus expectedStatus) {
    assertEquals(expectedStatus, OptimizingStatus.ofDisplayValue(displayValue));
  }
}
