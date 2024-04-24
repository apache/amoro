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

package org.apache.amoro.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Period;

public class TestTimeUtil {
  @Test
  public void testParse() {
    Assertions.assertEquals(Duration.ofMinutes(10), TimeUtil.parseDuration("10min"));
    Assertions.assertEquals(Duration.ofDays(3), TimeUtil.parseDuration("3d"));
    Assertions.assertEquals(Period.ofMonths(13), TimeUtil.parsePeriod("13m"));
    Assertions.assertEquals(Duration.ofDays(13 * 30).toMillis(), TimeUtil.estimatedMills("13m"));
  }

  @Test
  public void testWrongUnit() {
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> TimeUtil.parsePeriod("10min"));
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> TimeUtil.parseDuration("7w"));
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> TimeUtil.parseDuration("30q"));
  }

  @Test
  public void testNegativeValue() {
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> TimeUtil.parsePeriod("-10d"));
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> TimeUtil.parseDuration("-7h"));
  }
}
