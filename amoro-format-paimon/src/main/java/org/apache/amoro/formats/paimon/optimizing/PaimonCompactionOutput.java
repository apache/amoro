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

import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Compaction output for Paimon BUCKET_UNAWARE tables. Carries the serialized {@code CommitMessage}
 * produced by {@code AppendCompactTask#doCompact} plus the basic file-level statistics that feed
 * {@link PaimonMetricsSummary}.
 */
public class PaimonCompactionOutput implements TableOptimizing.OptimizingOutput {

  private static final long serialVersionUID = 1L;

  public static final String COMPACTED_FILES = "compacted-files";
  public static final String COMPACTED_BYTES = "compacted-bytes";
  public static final String PRODUCED_FILES = "produced-files";
  public static final String PRODUCED_BYTES = "produced-bytes";

  /*
   * Dashboard-compatible keys — chosen to match what {@code
   * org.apache.amoro.optimizing.MetricsSummary#fromMap(Map)} in {@code amoro-format-iceberg}
   * actually reads, so that the existing dashboard pipeline renders Paimon task summaries
   * without special-casing:
   *
   *  - input side uses the Iceberg suffixed names {@code input-data-*(rewrite)} because
   *    {@code MetricsSummary.INPUT_DATA_FILES} / {@code INPUT_DATA_SIZE} carry the "(rewrite)"
   *    suffix — fromMap won't pick up plain {@code input-data-files}.
   *  - output side uses plain {@code output-data-*} because that's already the Iceberg
   *    constant naming.
   *
   * This deliberately differs from {@link PaimonMetricsSummary#toMetricsSummary()} which
   * emits plain {@code input-data-files} — that path is consumed by
   * {@code MetricsSummary.aggregate(...)}'s normalize() hook, not fromMap(). Dual-write keeps
   * both consumer paths happy without requiring the Paimon output layer to know which one
   * runs downstream.
   */
  public static final String DASHBOARD_INPUT_DATA_FILES = "input-data-files(rewrite)";
  public static final String DASHBOARD_INPUT_DATA_SIZE = "input-data-size(rewrite)";
  public static final String DASHBOARD_OUTPUT_DATA_FILES = "output-data-files";
  public static final String DASHBOARD_OUTPUT_DATA_SIZE = "output-data-size";

  private byte[] commitMessageBytes;
  private int commitMessageVersion;
  private long compactedFileCount;
  private long compactedFileSize;
  private long producedFileCount;
  private long producedFileSize;

  public PaimonCompactionOutput() {}

  public PaimonCompactionOutput(
      byte[] commitMessageBytes,
      int commitMessageVersion,
      long compactedFileCount,
      long compactedFileSize,
      long producedFileCount,
      long producedFileSize) {
    this.commitMessageBytes = commitMessageBytes;
    this.commitMessageVersion = commitMessageVersion;
    this.compactedFileCount = compactedFileCount;
    this.compactedFileSize = compactedFileSize;
    this.producedFileCount = producedFileCount;
    this.producedFileSize = producedFileSize;
  }

  public byte[] getCommitMessageBytes() {
    return commitMessageBytes;
  }

  public int getCommitMessageVersion() {
    return commitMessageVersion;
  }

  public long getCompactedFileCount() {
    return compactedFileCount;
  }

  public long getCompactedFileSize() {
    return compactedFileSize;
  }

  public long getProducedFileCount() {
    return producedFileCount;
  }

  public long getProducedFileSize() {
    return producedFileSize;
  }

  @Override
  public Map<String, String> summary() {
    Map<String, String> summary = new LinkedHashMap<>();
    // Paimon-native keys — preserved so downstream Paimon tooling / logs keep their raw view.
    summary.put(COMPACTED_FILES, Long.toString(compactedFileCount));
    summary.put(COMPACTED_BYTES, Long.toString(compactedFileSize));
    summary.put(PRODUCED_FILES, Long.toString(producedFileCount));
    summary.put(PRODUCED_BYTES, Long.toString(producedFileSize));
    // Dashboard-compatible keys — same raw values, keyed under the names that
    // {@code MetricsSummary.fromMap} reads so the AMS dashboard renders without
    // format-specific branches.
    summary.put(DASHBOARD_INPUT_DATA_FILES, Long.toString(compactedFileCount));
    summary.put(DASHBOARD_INPUT_DATA_SIZE, Long.toString(compactedFileSize));
    summary.put(DASHBOARD_OUTPUT_DATA_FILES, Long.toString(producedFileCount));
    summary.put(DASHBOARD_OUTPUT_DATA_SIZE, Long.toString(producedFileSize));
    return summary;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("commitMessageBytesLen", commitMessageBytes == null ? 0 : commitMessageBytes.length)
        .add("commitMessageVersion", commitMessageVersion)
        .add("compactedFileCount", compactedFileCount)
        .add("compactedFileSize", compactedFileSize)
        .add("producedFileCount", producedFileCount)
        .add("producedFileSize", producedFileSize)
        .toString();
  }
}
