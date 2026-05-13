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

import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.utils.SerializationUtil;

import java.util.Map;

/**
 * Staged task descriptor for one Paimon BUCKET_UNAWARE compaction task, wrapping one Paimon {@code
 * AppendCompactTask}. Counterpart of {@code RewriteStageTask} on the Iceberg side.
 */
public class PaimonCompactionTask
    extends StagedTaskDescriptor<
        PaimonCompactionInput, PaimonCompactionOutput, PaimonMetricsSummary> {

  private String partition;

  /** Required for reflective construction (e.g. mybatis / deserialization paths). */
  public PaimonCompactionTask() {}

  public PaimonCompactionTask(
      long tableId, String partition, PaimonCompactionInput input, Map<String, String> properties) {
    super(tableId, input, properties);
    this.partition = partition;
  }

  public String getPartition() {
    return partition;
  }

  @Override
  protected void calculateSummary() {
    if (summary == null) {
      summary = new PaimonMetricsSummary();
    }
    if (output != null) {
      summary.setCompactedFileCount(output.getCompactedFileCount());
      summary.setCompactedFileSize(output.getCompactedFileSize());
      summary.setProducedFileCount(output.getProducedFileCount());
      summary.setProducedFileSize(output.getProducedFileSize());
    }
  }

  @Override
  protected PaimonCompactionOutput deserializeOutput(byte[] outputBytes) {
    return SerializationUtil.simpleDeserialize(outputBytes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableId", tableId)
        .add("partition", partition)
        .add("input", input)
        .add("output", output)
        .add("properties", properties)
        .toString();
  }
}
