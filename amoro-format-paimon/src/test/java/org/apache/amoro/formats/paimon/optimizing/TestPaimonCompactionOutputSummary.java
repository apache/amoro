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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Verifies the C5 dual-write contract for {@link PaimonCompactionOutput#summary()}:
 *
 * <ol>
 *   <li>The Paimon-native keys ({@code compacted-*} / {@code produced-*}) remain unchanged so
 *       Paimon-side tooling keeps the raw view.
 *   <li>The dashboard-compatible keys match the exact names that {@code
 *       org.apache.amoro.optimizing.MetricsSummary#fromMap(Map)} reads — so the AMS dashboard
 *       pipeline renders Paimon summaries without format-specific branches. Note that Iceberg uses
 *       suffixed input names ({@code input-data-files(rewrite)} / {@code input-data-size(rewrite)})
 *       while output names are plain.
 *   <li>Dual-write means the same raw long values appear under both key families.
 * </ol>
 *
 * <p>The fromMap round-trip assertion lives in {@code TestPaimonDashboardSummary} (amoro-ams),
 * because {@code MetricsSummary} is packaged in amoro-format-iceberg and is not on the
 * amoro-format-paimon test classpath.
 */
@DisplayName("PaimonCompactionOutput summary — C5 dual-write contract")
public class TestPaimonCompactionOutputSummary {

  private static final long COMPACTED_FILES = 7L;
  private static final long COMPACTED_BYTES = 1_024L * 1_024L * 42L; // 42 MiB
  private static final long PRODUCED_FILES = 1L;
  private static final long PRODUCED_BYTES = 1_024L * 1_024L * 40L; // 40 MiB

  private PaimonCompactionOutput newOutput() {
    return new PaimonCompactionOutput(
        new byte[] {1, 2, 3}, 3, COMPACTED_FILES, COMPACTED_BYTES, PRODUCED_FILES, PRODUCED_BYTES);
  }

  @Test
  @DisplayName("summary() emits all 8 keys — 4 Paimon-native + 4 dashboard-compat")
  void testSummaryContainsAllEightKeys() {
    Map<String, String> summary = newOutput().summary();
    assertNotNull(summary);
    assertEquals(8, summary.size(), "expected 4 Paimon-native + 4 dashboard-compat keys");

    // Paimon-native keys.
    assertTrue(summary.containsKey(PaimonCompactionOutput.COMPACTED_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.COMPACTED_BYTES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.PRODUCED_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.PRODUCED_BYTES));

    // Dashboard-compat keys.
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_SIZE));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE));
  }

  @Test
  @DisplayName("Dashboard-compat key names exactly match what MetricsSummary.fromMap reads")
  void testDashboardKeyNamingContract() {
    // Hard-code the literal names so a rename in MetricsSummary would have to be reflected here.
    // (We cannot import MetricsSummary directly — amoro-format-paimon does not depend on
    // amoro-format-iceberg. See TestPaimonDashboardSummary for the round-trip assertion.)
    assertEquals("input-data-files(rewrite)", PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES);
    assertEquals("input-data-size(rewrite)", PaimonCompactionOutput.DASHBOARD_INPUT_DATA_SIZE);
    assertEquals("output-data-files", PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES);
    assertEquals("output-data-size", PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE);
  }

  @Test
  @DisplayName("Values are stringified longs that match the underlying counters")
  void testSummaryValuesAreStringifiedLongs() {
    Map<String, String> summary = newOutput().summary();

    // Paimon-native side carries the raw counters verbatim.
    assertEquals(
        Long.toString(COMPACTED_FILES), summary.get(PaimonCompactionOutput.COMPACTED_FILES));
    assertEquals(
        Long.toString(COMPACTED_BYTES), summary.get(PaimonCompactionOutput.COMPACTED_BYTES));
    assertEquals(Long.toString(PRODUCED_FILES), summary.get(PaimonCompactionOutput.PRODUCED_FILES));
    assertEquals(Long.toString(PRODUCED_BYTES), summary.get(PaimonCompactionOutput.PRODUCED_BYTES));

    // Every value is a well-formed long (no human-readable "42 MB" strings here).
    for (String v : summary.values()) {
      Long.parseLong(v);
    }
  }

  @Test
  @DisplayName("Dual-write keeps both key families strictly equal — same raw values, no transform")
  void testDualWriteValuesAreConsistent() {
    Map<String, String> summary = newOutput().summary();

    assertEquals(
        summary.get(PaimonCompactionOutput.COMPACTED_FILES),
        summary.get(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES),
        "dashboard input file count must mirror Paimon compacted file count");
    assertEquals(
        summary.get(PaimonCompactionOutput.COMPACTED_BYTES),
        summary.get(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_SIZE),
        "dashboard input data size must mirror Paimon compacted bytes");
    assertEquals(
        summary.get(PaimonCompactionOutput.PRODUCED_FILES),
        summary.get(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES),
        "dashboard output file count must mirror Paimon produced file count");
    assertEquals(
        summary.get(PaimonCompactionOutput.PRODUCED_BYTES),
        summary.get(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE),
        "dashboard output data size must mirror Paimon produced bytes");
  }

  @Test
  @DisplayName("Empty (no-arg) output still emits all 8 keys with zero values")
  void testEmptyOutputEmitsZeros() {
    Map<String, String> summary = new PaimonCompactionOutput().summary();
    assertEquals(8, summary.size());
    assertEquals("0", summary.get(PaimonCompactionOutput.COMPACTED_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.COMPACTED_BYTES));
    assertEquals("0", summary.get(PaimonCompactionOutput.PRODUCED_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.PRODUCED_BYTES));
    assertEquals("0", summary.get(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_SIZE));
    assertEquals("0", summary.get(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE));
  }
}
