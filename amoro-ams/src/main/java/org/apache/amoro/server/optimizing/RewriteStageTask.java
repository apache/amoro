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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.server.dashboard.utils.OptimizingUtil;
import org.apache.amoro.server.persistence.TaskFilesPersistence;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

import java.util.Map;

public class RewriteStageTask
    extends StagedTaskDescriptor<RewriteFilesInput, RewriteFilesOutput, MetricsSummary> {

  private String partition;

  // only for mybatis and could be optimized by type handler afterward
  public RewriteStageTask() {}

  public RewriteStageTask(
      long tableId, String partition, RewriteFilesInput input, Map<String, String> properties) {
    super(tableId, input, properties);
    this.partition = partition;
  }

  @Override
  protected void calculateSummary() {
    if (input != null) {
      summary = new MetricsSummary(input);
      if (output != null) {
        summary.setNewDataFileCnt(OptimizingUtil.getFileCount(output.getDataFiles()));
        summary.setNewDataSize(OptimizingUtil.getFileSize(output.getDataFiles()));
        summary.setNewDataRecordCnt(OptimizingUtil.getRecordCnt(output.getDataFiles()));
        summary.setNewDeleteFileCnt(OptimizingUtil.getFileCount(output.getDeleteFiles()));
        summary.setNewDeleteSize(OptimizingUtil.getFileSize(output.getDeleteFiles()));
        summary.setNewDeleteRecordCnt(OptimizingUtil.getRecordCnt(output.getDeleteFiles()));
        summary.setNewFileSize(summary.getNewDataSize() + summary.getNewDeleteSize());
        summary.setNewFileCnt(summary.getNewDataFileCnt() + summary.getNewDeleteFileCnt());
      }
    }
  }

  @Override
  protected RewriteFilesOutput deserializeOutput(byte[] outputBytes) {
    return TaskFilesPersistence.loadTaskOutput(outputBytes);
  }

  public String getPartition() {
    return partition;
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
