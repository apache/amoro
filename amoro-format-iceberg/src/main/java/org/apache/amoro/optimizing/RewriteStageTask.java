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

package org.apache.amoro.optimizing;

import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.utils.SerializationUtil;
import org.apache.iceberg.ContentFile;

import java.util.Map;

/**
 * RewriteStageTask is a task which contains the input and output of a rewrite stage task. Other
 * stage task would include plan, evaluate, commit, etc.
 */
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
        summary.setNewDataFileCnt(getFileCount(output.getDataFiles()));
        summary.setNewDataSize(getFileSize(output.getDataFiles()));
        summary.setNewDataRecordCnt(getRecordCnt(output.getDataFiles()));
        summary.setNewDeleteFileCnt(getFileCount(output.getDeleteFiles()));
        summary.setNewDeleteSize(getFileSize(output.getDeleteFiles()));
        summary.setNewDeleteRecordCnt(getRecordCnt(output.getDeleteFiles()));
        summary.setNewFileSize(summary.getNewDataSize() + summary.getNewDeleteSize());
        summary.setNewFileCnt(summary.getNewDataFileCnt() + summary.getNewDeleteFileCnt());
      }
    }
  }

  @Override
  protected RewriteFilesOutput deserializeOutput(byte[] outputBytes) {
    return SerializationUtil.simpleDeserialize(outputBytes);
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

  private static long getFileSize(ContentFile<?>[] contentFiles) {
    long size = 0;
    if (contentFiles != null) {
      for (ContentFile<?> contentFile : contentFiles) {
        size += contentFile.fileSizeInBytes();
      }
    }
    return size;
  }

  private static int getFileCount(ContentFile<?>[] contentFiles) {
    return contentFiles == null ? 0 : contentFiles.length;
  }

  private static long getRecordCnt(ContentFile<?>[] contentFiles) {
    long recordCnt = 0;
    if (contentFiles != null) {
      for (ContentFile<?> contentFile : contentFiles) {
        recordCnt += contentFile.recordCount();
      }
    }
    return recordCnt;
  }
}
