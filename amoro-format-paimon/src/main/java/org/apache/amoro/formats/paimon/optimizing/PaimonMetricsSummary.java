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

import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Task-level metrics summary for Paimon BUCKET_UNAWARE compaction. Kept intentionally lean for the
 * first version — file count/size covers the 90% case and avoids bleeding Iceberg-specific fields
 * (position/equality deletes, record counts via ContentFile) into the Paimon path.
 */
public class PaimonMetricsSummary {

  public static final String COMPACTED_FILES = "compacted-files";
  public static final String COMPACTED_BYTES = "compacted-bytes";
  public static final String PRODUCED_FILES = "produced-files";
  public static final String PRODUCED_BYTES = "produced-bytes";

  // Iceberg-compatible keys — used when adapting to the shared TaskMetricsSummary view.
  static final String ICEBERG_INPUT_DATA_FILES = "input-data-files";
  static final String ICEBERG_INPUT_DATA_SIZE = "input-data-size";
  static final String ICEBERG_OUTPUT_DATA_FILES = "output-data-files";
  static final String ICEBERG_OUTPUT_DATA_SIZE = "output-data-size";

  private long compactedFileCount;
  private long compactedFileSize;
  private long producedFileCount;
  private long producedFileSize;

  public PaimonMetricsSummary() {}

  public PaimonMetricsSummary(
      long compactedFileCount,
      long compactedFileSize,
      long producedFileCount,
      long producedFileSize) {
    this.compactedFileCount = compactedFileCount;
    this.compactedFileSize = compactedFileSize;
    this.producedFileCount = producedFileCount;
    this.producedFileSize = producedFileSize;
  }

  public long getCompactedFileCount() {
    return compactedFileCount;
  }

  public void setCompactedFileCount(long compactedFileCount) {
    this.compactedFileCount = compactedFileCount;
  }

  public long getCompactedFileSize() {
    return compactedFileSize;
  }

  public void setCompactedFileSize(long compactedFileSize) {
    this.compactedFileSize = compactedFileSize;
  }

  public long getProducedFileCount() {
    return producedFileCount;
  }

  public void setProducedFileCount(long producedFileCount) {
    this.producedFileCount = producedFileCount;
  }

  public long getProducedFileSize() {
    return producedFileSize;
  }

  public void setProducedFileSize(long producedFileSize) {
    this.producedFileSize = producedFileSize;
  }

  public Map<String, String> summaryAsMap() {
    Map<String, String> summary = new LinkedHashMap<>();
    summary.put(COMPACTED_FILES, Long.toString(compactedFileCount));
    summary.put(COMPACTED_BYTES, Long.toString(compactedFileSize));
    summary.put(PRODUCED_FILES, Long.toString(producedFileCount));
    summary.put(PRODUCED_BYTES, Long.toString(producedFileSize));
    return summary;
  }

  /**
   * Adapt this Paimon summary to the shared {@link TaskMetricsSummary} view consumed by {@code
   * OptimizingQueue.getSummary()}. Emits only the Iceberg-compatible {@code input-data-*} / {@code
   * output-data-*} keys — record counts are not surfaced by Paimon's {@code AppendCompactTask}, so
   * we intentionally omit those fields rather than lie with zero.
   */
  public TaskMetricsSummary toMetricsSummary() {
    // Snapshot values at call-time to keep the returned adapter independent of later mutations.
    final long compactedFiles = compactedFileCount;
    final long compactedBytes = compactedFileSize;
    final long producedFiles = producedFileCount;
    final long producedBytes = producedFileSize;
    return humanReadable -> {
      Map<String, String> out = new LinkedHashMap<>();
      out.put(ICEBERG_INPUT_DATA_FILES, Long.toString(compactedFiles));
      out.put(ICEBERG_INPUT_DATA_SIZE, Long.toString(compactedBytes));
      out.put(ICEBERG_OUTPUT_DATA_FILES, Long.toString(producedFiles));
      out.put(ICEBERG_OUTPUT_DATA_SIZE, Long.toString(producedBytes));
      return out;
    };
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PaimonMetricsSummary)) {
      return false;
    }
    PaimonMetricsSummary that = (PaimonMetricsSummary) o;
    return compactedFileCount == that.compactedFileCount
        && compactedFileSize == that.compactedFileSize
        && producedFileCount == that.producedFileCount
        && producedFileSize == that.producedFileSize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(compactedFileCount, compactedFileSize, producedFileCount, producedFileSize);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("compactedFileCount", compactedFileCount)
        .add("compactedFileSize", compactedFileSize)
        .add("producedFileCount", producedFileCount)
        .add("producedFileSize", producedFileSize)
        .toString();
  }
}
