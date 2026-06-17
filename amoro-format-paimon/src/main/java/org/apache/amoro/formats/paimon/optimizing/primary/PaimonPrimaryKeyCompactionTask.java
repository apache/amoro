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

import org.apache.amoro.formats.paimon.optimizing.PaimonMetricsSummary;
import org.apache.amoro.optimizing.TaskMetricsSummary;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.utils.SerializationUtil;

import java.nio.ByteBuffer;
import java.util.Map;

public class PaimonPrimaryKeyCompactionTask
    extends StagedTaskDescriptor<
        PaimonPrimaryKeyCompactionInput, PaimonPrimaryKeyCompactionOutput, PaimonMetricsSummary> {

  private String partition;

  public PaimonPrimaryKeyCompactionTask() {}

  public PaimonPrimaryKeyCompactionTask(
      long tableId,
      String partition,
      PaimonPrimaryKeyCompactionInput input,
      Map<String, String> properties) {
    super(tableId, input, properties);
    this.partition = partition;
  }

  public static PaimonPrimaryKeyCompactionTask buildTask(
      long tableId,
      String partition,
      PaimonPrimaryKeyCompactionInput input,
      Map<String, String> properties) {
    return new PaimonPrimaryKeyCompactionTask(tableId, partition, input, properties);
  }

  public String getPartition() {
    return partition;
  }

  @Override
  public TaskMetricsSummary toMetricsSummary() {
    return ensureTypedSummary().toMetricsSummary();
  }

  @Override
  protected void calculateSummary() {
    PaimonMetricsSummary metricsSummary = new PaimonMetricsSummary();
    if (output != null) {
      metricsSummary.setCompactedFileCount(output.getCompactedFileCount());
      metricsSummary.setCompactedFileSize(output.getCompactedFileSize());
      metricsSummary.setProducedFileCount(output.getProducedFileCount());
      metricsSummary.setProducedFileSize(output.getProducedFileSize());
    }
    summary = metricsSummary;
  }

  private PaimonMetricsSummary ensureTypedSummary() {
    if (!(summary instanceof PaimonMetricsSummary)) {
      calculateSummary();
    }
    return summary;
  }

  public void setInputBytes(byte[] inputBytes) {
    setInput(deserializeInput(inputBytes));
  }

  public void setOutput(ByteBuffer outputBuffer) {
    ByteBuffer bytes = outputBuffer.slice();
    byte[] outputBytes = new byte[bytes.remaining()];
    bytes.get(outputBytes);
    setOutputBytes(outputBytes);
  }

  public PaimonPrimaryKeyCompactionInput deserializeInput(byte[] inputBytes) {
    return SerializationUtil.simpleDeserialize(inputBytes);
  }

  @Override
  protected PaimonPrimaryKeyCompactionOutput deserializeOutput(byte[] outputBytes) {
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
