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

import org.apache.amoro.optimizing.OptimizingExecutor;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.disk.IOManager;
import org.apache.paimon.io.CompactIncrement;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.BucketMode;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.sink.BatchTableWrite;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.CommitMessageImpl;
import org.apache.paimon.table.sink.CommitMessageSerializer;
import org.apache.paimon.utils.SerializationUtils;

import java.util.List;

public class PaimonPrimaryKeyCompactionExecutor
    implements OptimizingExecutor<PaimonPrimaryKeyCompactionOutput> {

  private static final long serialVersionUID = 1L;

  private final PaimonPrimaryKeyCompactionInput input;

  public PaimonPrimaryKeyCompactionExecutor(PaimonPrimaryKeyCompactionInput input) {
    this.input = input;
  }

  @Override
  public PaimonPrimaryKeyCompactionOutput execute() {
    validateInput();

    Object raw = input.getTable().originalTable();
    if (!(raw instanceof FileStoreTable) || raw instanceof AppendOnlyFileStoreTable) {
      throw new IllegalStateException(
          "PaimonPrimaryKeyCompactionExecutor requires non-append FileStoreTable, got "
              + (raw == null ? "null" : raw.getClass().getName()));
    }
    FileStoreTable table = (FileStoreTable) raw;
    if (table.primaryKeys() == null
        || table.primaryKeys().isEmpty()
        || (table.bucketMode() != BucketMode.HASH_FIXED
            && table.bucketMode() != BucketMode.HASH_DYNAMIC)) {
      throw new IllegalStateException(
          "PaimonPrimaryKeyCompactionExecutor requires primary-key HASH_FIXED/HASH_DYNAMIC"
              + " FileStoreTable, got bucketMode="
              + table.bucketMode()
              + ", primaryKeys="
              + table.primaryKeys());
    }

    try (IOManager ioManager = IOManager.create(System.getProperty("java.io.tmpdir"));
        BatchTableWrite write = table.newBatchWriteBuilder().newWrite().withIOManager(ioManager)) {
      for (PaimonBucketCompactionUnit unit : input.getUnits()) {
        BinaryRow partition = SerializationUtils.deserializeBinaryRow(unit.getPartitionBytes());
        write.compact(partition, unit.getBucket(), input.isFullCompaction());
      }
      List<CommitMessage> messages = write.prepareCommit();
      return new PaimonPrimaryKeyCompactionOutput(
          CommitMessageSerializer.serializeAll(messages),
          input.getUnits().size(),
          compactedFileCount(),
          compactedFileSize(),
          compactedRecordCount(),
          producedFileCount(messages),
          producedFileSize(messages));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to execute Paimon primary-key compaction.", e);
    }
  }

  private void validateInput() {
    if (input == null || input.getTable() == null || input.getUnits() == null) {
      throw new IllegalStateException(
          "PaimonPrimaryKeyCompactionInput is missing required fields (table / units).");
    }
    if (input.getUnits().isEmpty()) {
      throw new IllegalStateException("PaimonPrimaryKeyCompactionInput has empty units.");
    }
    if (input.getCommitUser() == null || input.getCommitUser().isEmpty()) {
      throw new IllegalStateException("PaimonPrimaryKeyCompactionInput is missing commitUser.");
    }
    if (input.getCommitIdentifier() <= 0L) {
      throw new IllegalStateException(
          "PaimonPrimaryKeyCompactionInput has invalid commitIdentifier "
              + input.getCommitIdentifier()
              + ".");
    }
    if (input.getOptimizingType() == null) {
      throw new IllegalStateException("PaimonPrimaryKeyCompactionInput is missing optimizingType.");
    }
    if (input.getOptimizingType() == OptimizingType.MINOR && input.isFullCompaction()) {
      throw new IllegalStateException(
          "Paimon primary-key MINOR compaction requires fullCompaction=false.");
    }
    if (input.getOptimizingType() != OptimizingType.MINOR && !input.isFullCompaction()) {
      throw new IllegalStateException(
          "Paimon primary-key "
              + input.getOptimizingType()
              + " compaction requires fullCompaction=true.");
    }
  }

  private long compactedFileCount() {
    return input.getUnits().stream().mapToLong(PaimonBucketCompactionUnit::getFileCount).sum();
  }

  private long compactedFileSize() {
    return input.getUnits().stream()
        .mapToLong(PaimonBucketCompactionUnit::getFileSizeInBytes)
        .sum();
  }

  private long compactedRecordCount() {
    return input.getUnits().stream().mapToLong(PaimonBucketCompactionUnit::getRecordCount).sum();
  }

  private static long producedFileCount(List<CommitMessage> messages) {
    long count = 0L;
    for (CommitMessage message : messages) {
      count += compactAfter(message).size();
    }
    return count;
  }

  private static long producedFileSize(List<CommitMessage> messages) {
    long size = 0L;
    for (CommitMessage message : messages) {
      for (DataFileMeta file : compactAfter(message)) {
        size += file.fileSize();
      }
    }
    return size;
  }

  private static List<DataFileMeta> compactAfter(CommitMessage message) {
    if (!(message instanceof CommitMessageImpl)) {
      throw new IllegalStateException(
          "Paimon primary-key compact message must be CommitMessageImpl, got "
              + (message == null ? "null" : message.getClass().getName()));
    }
    CompactIncrement increment = ((CommitMessageImpl) message).compactIncrement();
    if (increment == null) {
      throw new IllegalStateException("Paimon primary-key compact message has null increment.");
    }
    List<DataFileMeta> compactAfter = increment.compactAfter();
    if (compactAfter == null) {
      throw new IllegalStateException("Paimon primary-key compact message has null compactAfter.");
    }
    return compactAfter;
  }
}
