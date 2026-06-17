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

import org.apache.amoro.optimizing.TableOptimizing;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaimonPrimaryKeyCompactionOutput implements TableOptimizing.OptimizingOutput {

  private static final long serialVersionUID = 1L;

  public static final String COMPACTED_BUCKETS = "compacted-buckets";
  public static final String COMPACTED_FILES = "compacted-files";
  public static final String COMPACTED_BYTES = "compacted-bytes";
  public static final String COMPACTED_RECORDS = "compacted-records";
  public static final String PRODUCED_FILES = "produced-files";
  public static final String PRODUCED_BYTES = "produced-bytes";

  public static final String DASHBOARD_INPUT_DATA_FILES = "input-data-files(rewrite)";
  public static final String DASHBOARD_INPUT_DATA_SIZE = "input-data-size(rewrite)";
  public static final String DASHBOARD_OUTPUT_DATA_FILES = "output-data-files";
  public static final String DASHBOARD_OUTPUT_DATA_SIZE = "output-data-size";

  private List<byte[]> commitMessageBytesList;
  private int compactedBucketCount;
  private long compactedFileCount;
  private long compactedFileSize;
  private long compactedRecordCount;
  private long producedFileCount;
  private long producedFileSize;

  public PaimonPrimaryKeyCompactionOutput() {}

  public PaimonPrimaryKeyCompactionOutput(
      List<byte[]> commitMessageBytesList,
      int compactedBucketCount,
      long compactedFileCount,
      long compactedFileSize,
      long compactedRecordCount,
      long producedFileCount,
      long producedFileSize) {
    this.commitMessageBytesList = commitMessageBytesList;
    this.compactedBucketCount = compactedBucketCount;
    this.compactedFileCount = compactedFileCount;
    this.compactedFileSize = compactedFileSize;
    this.compactedRecordCount = compactedRecordCount;
    this.producedFileCount = producedFileCount;
    this.producedFileSize = producedFileSize;
  }

  public List<byte[]> getCommitMessageBytesList() {
    return commitMessageBytesList == null ? Collections.emptyList() : commitMessageBytesList;
  }

  public int getCompactedBucketCount() {
    return compactedBucketCount;
  }

  public long getCompactedFileCount() {
    return compactedFileCount;
  }

  public long getCompactedFileSize() {
    return compactedFileSize;
  }

  public long getCompactedRecordCount() {
    return compactedRecordCount;
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
    summary.put(COMPACTED_BUCKETS, Integer.toString(compactedBucketCount));
    summary.put(COMPACTED_FILES, Long.toString(compactedFileCount));
    summary.put(COMPACTED_BYTES, Long.toString(compactedFileSize));
    summary.put(COMPACTED_RECORDS, Long.toString(compactedRecordCount));
    summary.put(PRODUCED_FILES, Long.toString(producedFileCount));
    summary.put(PRODUCED_BYTES, Long.toString(producedFileSize));
    summary.put(DASHBOARD_INPUT_DATA_FILES, Long.toString(compactedFileCount));
    summary.put(DASHBOARD_INPUT_DATA_SIZE, Long.toString(compactedFileSize));
    summary.put(DASHBOARD_OUTPUT_DATA_FILES, Long.toString(producedFileCount));
    summary.put(DASHBOARD_OUTPUT_DATA_SIZE, Long.toString(producedFileSize));
    return summary;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("commitMessageCount", getCommitMessageBytesList().size())
        .add("compactedBucketCount", compactedBucketCount)
        .add("compactedFileCount", compactedFileCount)
        .add("compactedFileSize", compactedFileSize)
        .add("compactedRecordCount", compactedRecordCount)
        .add("producedFileCount", producedFileCount)
        .add("producedFileSize", producedFileSize)
        .toString();
  }
}
