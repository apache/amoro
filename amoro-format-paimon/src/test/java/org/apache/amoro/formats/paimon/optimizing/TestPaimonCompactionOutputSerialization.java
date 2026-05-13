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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Map;

@DisplayName("PaimonCompactionOutput serialization & summary")
public class TestPaimonCompactionOutputSerialization {

  @Test
  @DisplayName("Empty Output (no-arg ctor) still exposes a complete summary map with zeros")
  void testEmptyOutputSummary() {
    PaimonCompactionOutput output = new PaimonCompactionOutput();
    Map<String, String> summary = output.summary();
    assertNotNull(summary);
    assertEquals("0", summary.get(PaimonCompactionOutput.COMPACTED_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.COMPACTED_BYTES));
    assertEquals("0", summary.get(PaimonCompactionOutput.PRODUCED_FILES));
    assertEquals("0", summary.get(PaimonCompactionOutput.PRODUCED_BYTES));
  }

  @Test
  @DisplayName("Fully-populated Output round-trips every field")
  void testFullOutputRoundTrip() {
    byte[] msgBytes = new byte[] {10, 20, 30};
    PaimonCompactionOutput output =
        new PaimonCompactionOutput(msgBytes, 3, 7L, 1_000_000L, 1L, 950_000L);

    ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    PaimonCompactionOutput out = SerializationUtil.simpleDeserialize(bytes);

    assertNotNull(out);
    assertArrayEquals(msgBytes, out.getCommitMessageBytes());
    assertEquals(3, out.getCommitMessageVersion());
    assertEquals(7L, out.getCompactedFileCount());
    assertEquals(1_000_000L, out.getCompactedFileSize());
    assertEquals(1L, out.getProducedFileCount());
    assertEquals(950_000L, out.getProducedFileSize());
  }

  @Test
  @DisplayName("summary() keys cover Paimon-native + dashboard-compat entries, all numeric")
  void testSummaryKeys() {
    PaimonCompactionOutput output = new PaimonCompactionOutput(null, 2, 5L, 500L, 1L, 450L);
    Map<String, String> summary = output.summary();
    // 4 Paimon-native + 4 dashboard-compat (see PaimonCompactionOutput.summary javadoc for why).
    assertEquals(8, summary.size());
    assertTrue(summary.containsKey(PaimonCompactionOutput.COMPACTED_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.COMPACTED_BYTES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.PRODUCED_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.PRODUCED_BYTES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_INPUT_DATA_SIZE));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES));
    assertTrue(summary.containsKey(PaimonCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE));
    // Ensure numeric values (parseable as long).
    for (String v : summary.values()) {
      Long.parseLong(v);
    }
  }
}
