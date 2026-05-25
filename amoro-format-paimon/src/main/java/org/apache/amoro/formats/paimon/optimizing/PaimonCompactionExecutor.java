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

import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.operation.BaseAppendFileStoreWrite;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.SpecialFields;
import org.apache.paimon.table.sink.AppendCompactTaskSerializer;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.CommitMessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Optimizer-side executor that performs Paimon BUCKET_UNAWARE compaction for one task.
 *
 * <p>Reuses the Paimon-native path: {@link AppendCompactTask#doCompact} internally calls {@code
 * BaseAppendFileStoreWrite#compactRewrite} and returns a ready-to-commit {@link CommitMessage}.
 * This executor only does the plumbing: deserialize the task, unwrap the table from {@link
 * PaimonCompactionInput#getTable()} (shipped from AMS à la Iceberg {@code RewriteFilesInput}),
 * invoke {@code doCompact}, then serialize the output via {@link CommitMessageSerializer} for the
 * AMS-side Committer to re-deserialize and commit.
 */
public class PaimonCompactionExecutor implements OptimizingExecutor<PaimonCompactionOutput> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(PaimonCompactionExecutor.class);

  private final PaimonCompactionInput input;

  public PaimonCompactionExecutor(PaimonCompactionInput input) {
    this.input = input;
  }

  @Override
  public PaimonCompactionOutput execute() {
    if (input == null || input.getTable() == null || input.getTaskBytes() == null) {
      throw new IllegalStateException(
          "PaimonCompactionInput is missing required fields (table / taskBytes).");
    }
    if (input.getCommitUser() == null || input.getCommitUser().isEmpty()) {
      throw new IllegalStateException("PaimonCompactionInput is missing commitUser.");
    }
    if (input.getCommitIdentifier() <= 0L) {
      throw new IllegalStateException(
          "PaimonCompactionInput has invalid commitIdentifier "
              + input.getCommitIdentifier()
              + ".");
    }

    AppendCompactTask task;
    try {
      task =
          new AppendCompactTaskSerializer()
              .deserialize(input.getSerializerVersion(), input.getTaskBytes());
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to deserialize AppendCompactTask (version=" + input.getSerializerVersion() + ")",
          e);
    }

    Object raw = input.getTable().originalTable();
    if (!(raw instanceof AppendOnlyFileStoreTable)) {
      throw new IllegalStateException(
          "PaimonCompactionExecutor requires AppendOnlyFileStoreTable, got "
              + (raw == null ? "null" : raw.getClass().getName()));
    }
    AppendOnlyFileStoreTable table = (AppendOnlyFileStoreTable) raw;

    BaseAppendFileStoreWrite write = table.store().newWrite(input.getCommitUser());
    CoreOptions coreOptions = table.coreOptions();
    if (coreOptions.rowTrackingEnabled()) {
      write.withWriteType(SpecialFields.rowTypeWithRowTracking(table.rowType()));
    }
    CommitMessage message;
    try {
      message = task.doCompact(table, write);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to execute Paimon AppendCompactTask for partition " + input.getPartitionPath(),
          e);
    } finally {
      try {
        write.close();
      } catch (Exception closeErr) {
        LOG.warn(
            "Error closing BaseAppendFileStoreWrite for partition {}: {}",
            input.getPartitionPath(),
            closeErr.getMessage());
      }
    }

    byte[] msgBytes;
    CommitMessageSerializer msgSerializer = new CommitMessageSerializer();
    int msgVersion = msgSerializer.getVersion();
    try {
      msgBytes = msgSerializer.serialize(message);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to serialize Paimon CommitMessage for partition " + input.getPartitionPath(), e);
    }

    long compactedFileCount = task.compactBefore() == null ? 0L : task.compactBefore().size();
    long compactedFileSize = sumFileSize(task.compactBefore());
    List<DataFileMeta> after = task.compactAfter();
    long producedFileCount = after == null ? 0L : after.size();
    long producedFileSize = sumFileSize(after);

    LOG.info(
        "Paimon compaction done: partition={} compactedFiles={} compactedBytes={} producedFiles={} producedBytes={}",
        input.getPartitionPath(),
        compactedFileCount,
        compactedFileSize,
        producedFileCount,
        producedFileSize);

    return new PaimonCompactionOutput(
        msgBytes,
        msgVersion,
        compactedFileCount,
        compactedFileSize,
        producedFileCount,
        producedFileSize);
  }

  private static long sumFileSize(List<DataFileMeta> files) {
    if (files == null || files.isEmpty()) {
      return 0L;
    }
    long total = 0L;
    for (DataFileMeta m : files) {
      total += m.fileSize();
    }
    return total;
  }
}
