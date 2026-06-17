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

package org.apache.amoro.formats.paimon.optimizing.primary;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.utils.SerializationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@DisplayName("PaimonPrimaryKeyCompactionOutput")
class TestPaimonPrimaryKeyCompactionOutput {

  @Test
  @DisplayName("summary exposes primary-key native metrics and dashboard compatible metrics")
  void summaryContainsBucketAndFileMetrics() {
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(
            Arrays.asList(new byte[] {1}, new byte[] {2}), 2, 9L, 900L, 30L, 3L, 300L);

    Map<String, String> summary = output.summary();

    assertEquals("2", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BUCKETS));
    assertEquals("9", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_FILES));
    assertEquals("900", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BYTES));
    assertEquals("30", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_RECORDS));
    assertEquals("3", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_FILES));
    assertEquals("300", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_BYTES));
    assertEquals("9", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_INPUT_DATA_FILES));
    assertEquals("900", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_INPUT_DATA_SIZE));
    assertEquals("3", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES));
    assertEquals("300", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE));
    for (String value : summary.values()) {
      Long.parseLong(value);
    }
  }

  @Test
  @DisplayName("empty output emits complete zero summary")
  void emptyOutputSummaryUsesZeros() {
    Map<String, String> summary = new PaimonPrimaryKeyCompactionOutput().summary();

    assertEquals(10, summary.size());
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BUCKETS));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_FILES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_BYTES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.COMPACTED_RECORDS));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_FILES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.PRODUCED_BYTES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_INPUT_DATA_FILES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_INPUT_DATA_SIZE));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_OUTPUT_DATA_FILES));
    assertEquals("0", summary.get(PaimonPrimaryKeyCompactionOutput.DASHBOARD_OUTPUT_DATA_SIZE));
  }

  @Test
  @DisplayName("output serializes and deserializes every field")
  void outputRoundTripsEveryField() {
    List<byte[]> commitMessages = Arrays.asList(new byte[] {1, 2}, new byte[] {3, 4});
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(commitMessages, 2, 9L, 900L, 30L, 3L, 300L);

    ByteBuffer buffer = SerializationUtil.simpleSerialize(output);
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    PaimonPrimaryKeyCompactionOutput deserialized = SerializationUtil.simpleDeserialize(bytes);

    assertNotNull(deserialized);
    assertEquals(2, deserialized.getCommitMessageBytesList().size());
    assertArrayEquals(new byte[] {1, 2}, deserialized.getCommitMessageBytesList().get(0));
    assertArrayEquals(new byte[] {3, 4}, deserialized.getCommitMessageBytesList().get(1));
    assertEquals(2, deserialized.getCompactedBucketCount());
    assertEquals(9L, deserialized.getCompactedFileCount());
    assertEquals(900L, deserialized.getCompactedFileSize());
    assertEquals(30L, deserialized.getCompactedRecordCount());
    assertEquals(3L, deserialized.getProducedFileCount());
    assertEquals(300L, deserialized.getProducedFileSize());
  }

  @Test
  @DisplayName("constructor accepts an empty commit message list")
  void emptyCommitMessageListIsPreserved() {
    PaimonPrimaryKeyCompactionOutput output =
        new PaimonPrimaryKeyCompactionOutput(Collections.emptyList(), 0, 0L, 0L, 0L, 0L, 0L);

    assertTrue(output.getCommitMessageBytesList().isEmpty());
  }
}
