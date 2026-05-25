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

package org.apache.amoro.server.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.optimizing.MetricsSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * End-to-end C5 dashboard-compat assertion: the AMS dashboard renders Paimon task summaries by
 * calling {@link MetricsSummary#fromMap(Map)} on whatever {@link PaimonCompactionOutput#summary()}
 * emits. This test pins down that round-trip so a future change to either side that breaks the
 * pipeline is caught here rather than in a runtime dashboard surprise.
 *
 * <p>Key insight documented: {@code MetricsSummary.fromMap} reads suffixed input keys ({@code
 * input-data-files(rewrite)} / {@code input-data-size(rewrite)}) but plain output keys ({@code
 * output-data-files} / {@code output-data-size}). The Plan §3.1 prescription of plain input keys
 * would have silently zeroed the input side of the dashboard. {@code PaimonCompactionOutput} emits
 * both Paimon-native and the exact-naming dashboard keys, so this test verifies the dashboard side
 * only.
 */
@DisplayName("Paimon dashboard summary — MetricsSummary.fromMap(output.summary()) round-trip")
public class TestPaimonDashboardSummary {

  private static final long COMPACTED_FILES = 12L;
  private static final long COMPACTED_BYTES = 1_234_567L;
  private static final long PRODUCED_FILES = 2L;
  private static final long PRODUCED_BYTES = 1_200_000L;

  private PaimonCompactionOutput paimonOutput() {
    return new PaimonCompactionOutput(
        new byte[] {9, 8, 7}, 3, COMPACTED_FILES, COMPACTED_BYTES, PRODUCED_FILES, PRODUCED_BYTES);
  }

  @Test
  @DisplayName("fromMap recovers input/output file counts and sizes from Paimon summary map")
  void testFromMapRoundTrip() {
    Map<String, String> summary = paimonOutput().summary();
    MetricsSummary metrics = MetricsSummary.fromMap(summary);
    assertNotNull(metrics);

    // Input side (routed through MetricsSummary's rewriteData* fields).
    assertEquals(
        (int) COMPACTED_FILES,
        metrics.getRewriteDataFileCnt(),
        "dashboard inputFileCount must match Paimon compacted file count");
    assertEquals(
        COMPACTED_BYTES,
        metrics.getRewriteDataSize(),
        "dashboard inputFileSize must match Paimon compacted bytes");

    // Output side (routed through MetricsSummary's newData* fields).
    assertEquals(
        (int) PRODUCED_FILES,
        metrics.getNewDataFileCnt(),
        "dashboard outputFileCount must match Paimon produced file count");
    assertEquals(
        PRODUCED_BYTES,
        metrics.getNewDataSize(),
        "dashboard outputFileSize must match Paimon produced bytes");
  }

  @Test
  @DisplayName("FilesStatistics surfaces the same Paimon counters through the dashboard API")
  void testFilesStatistics() {
    Map<String, String> summary = paimonOutput().summary();
    MetricsSummary metrics = MetricsSummary.fromMap(summary);

    // getInputFilesStatistics aggregates rewrite + delete; for Paimon BUCKET_UNAWARE there are no
    // deletes, so it must exactly equal the compacted counters.
    assertEquals(COMPACTED_FILES, metrics.getInputFilesStatistics().getFileCnt());
    assertEquals(COMPACTED_BYTES, metrics.getInputFilesStatistics().getTotalSize());

    assertEquals(PRODUCED_FILES, metrics.getOutputFilesStatistics().getFileCnt());
    assertEquals(PRODUCED_BYTES, metrics.getOutputFilesStatistics().getTotalSize());
  }

  @Test
  @DisplayName("Paimon-native keys coexist with dashboard keys — dashboard path ignores them")
  void testPaimonNativeKeysDoNotLeakIntoFromMap() {
    Map<String, String> summary = paimonOutput().summary();
    // Sanity: both key families are present in the raw summary.
    assertTrue(summary.containsKey(PaimonCompactionOutput.COMPACTED_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES));

    // And fromMap consumes only the dashboard-compat keys.
    MetricsSummary metrics = MetricsSummary.fromMap(summary);
    assertEquals((int) COMPACTED_FILES, metrics.getRewriteDataFileCnt());
  }

  @Test
  @DisplayName("Empty Paimon output → fromMap returns zero-valued MetricsSummary (no NPE)")
  void testEmptyOutputFromMap() {
    Map<String, String> summary = new PaimonCompactionOutput().summary();
    MetricsSummary metrics = MetricsSummary.fromMap(summary);
    assertNotNull(metrics);
    assertEquals(0, metrics.getRewriteDataFileCnt());
    assertEquals(0L, metrics.getRewriteDataSize());
    assertEquals(0, metrics.getNewDataFileCnt());
    assertEquals(0L, metrics.getNewDataSize());
  }
}
