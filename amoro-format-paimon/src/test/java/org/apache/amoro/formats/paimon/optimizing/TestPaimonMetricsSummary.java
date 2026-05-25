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

package org.apache.amoro.formats.paimon.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

@DisplayName("PaimonMetricsSummary basics")
public class TestPaimonMetricsSummary {

  @Test
  @DisplayName("equals / hashCode reflect all four numeric fields")
  void testEqualsHashCode() {
    PaimonMetricsSummary a = new PaimonMetricsSummary(5, 500, 1, 450);
    PaimonMetricsSummary b = new PaimonMetricsSummary(5, 500, 1, 450);
    PaimonMetricsSummary c = new PaimonMetricsSummary(5, 500, 1, 999);
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
  }

  @Test
  @DisplayName("summaryAsMap exposes the four conventional keys")
  void testSummaryKeys() {
    PaimonMetricsSummary m = new PaimonMetricsSummary(3, 123, 1, 100);
    Map<String, String> map = m.summaryAsMap();
    assertEquals("3", map.get(PaimonMetricsSummary.COMPACTED_FILES));
    assertEquals("123", map.get(PaimonMetricsSummary.COMPACTED_BYTES));
    assertEquals("1", map.get(PaimonMetricsSummary.PRODUCED_FILES));
    assertEquals("100", map.get(PaimonMetricsSummary.PRODUCED_BYTES));
  }

  @Test
  @DisplayName("toString mentions every field so that it is useful in failing assertions")
  void testToStringContainsKeys() {
    PaimonMetricsSummary m = new PaimonMetricsSummary(7, 77, 2, 60);
    String s = m.toString();
    assertTrue(s.contains("compactedFileCount"));
    assertTrue(s.contains("compactedFileSize"));
    assertTrue(s.contains("producedFileCount"));
    assertTrue(s.contains("producedFileSize"));
  }
}
