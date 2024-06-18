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

package org.apache.amoro.flink.read.hybrid.reader;

import org.apache.amoro.flink.read.source.DataIterator;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.connector.base.source.reader.RecordsWithSplitIds;
import org.apache.flink.connector.file.src.util.Pool;
import org.apache.flink.table.data.RowData;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * {@link RecordsWithSplitIds} is used to pass a batch of records from fetcher to source reader.
 * Batching is to improve the efficiency for records handover.
 *
 * <p>{@link RecordsWithSplitIds} interface can encapsulate batches from multiple splits. This is
 * the case for Kafka source where fetchers can retrieve records from multiple Kafka partitions at
 * the same time.
 *
 * <p>For file-based sources like Iceberg, readers always read one split/file at a time. Hence, we
 * will only have a batch of records for one split here.
 *
 * <p>This class uses array to store a batch of records from the same file (with the same
 * fileOffset).
 */
class ArrayBatchRecords<T> implements RecordsWithSplitIds<MixedFormatRecordWithOffset<T>> {
  @Nullable private String splitId;
  @Nullable private final Pool.Recycler<T[]> recycler;
  @Nullable private final T[] records;
  private final int numberOfRecords;
  private final Set<String> finishedSplits;
  private final MixedFormatRecordWithOffset<T> recordWithOffset;

  // point to current read position within the records array
  private int position;

  private RecordPosition[] recordPositions;

  private ArrayBatchRecords(
      @Nullable String splitId,
      @Nullable Pool.Recycler<T[]> recycler,
      @Nullable T[] records,
      int numberOfRecords,
      int fileOffset,
      long startingRecordOffset,
      Set<String> finishedSplits) {
    Preconditions.checkArgument(numberOfRecords >= 0, "numberOfRecords can't be negative");
    Preconditions.checkArgument(fileOffset >= 0, "fileOffset can't be negative");
    Preconditions.checkArgument(startingRecordOffset >= 0, "numberOfRecords can't be negative");

    this.splitId = splitId;
    this.recycler = recycler;
    this.records = records;
    this.numberOfRecords = numberOfRecords;
    this.finishedSplits =
        Preconditions.checkNotNull(finishedSplits, "finishedSplits can be empty but not null");
    this.recordWithOffset = new MixedFormatRecordWithOffset<>();

    this.position = 0;
  }

  private ArrayBatchRecords(
      @Nullable String splitId,
      @Nullable Pool.Recycler<T[]> recycler,
      @Nullable T[] records,
      int numberOfRecords,
      RecordPosition[] positions,
      Set<String> finishedSplits) {
    Preconditions.checkArgument(numberOfRecords >= 0, "numberOfRecords can't be negative");

    this.splitId = splitId;
    this.recycler = recycler;
    this.records = records;
    this.numberOfRecords = numberOfRecords;
    this.recordPositions = Preconditions.checkNotNull(positions, "recordPositions can't be null");
    this.finishedSplits =
        Preconditions.checkNotNull(finishedSplits, "finishedSplits can be empty but not null");
    this.recordWithOffset = new MixedFormatRecordWithOffset<>();

    this.position = 0;
  }

  @Nullable
  @Override
  public String nextSplit() {
    String nextSplit = this.splitId;
    // set the splitId to null to indicate no more splits
    // this class only contains record for one split
    this.splitId = null;
    return nextSplit;
  }

  @Nullable
  @Override
  public MixedFormatRecordWithOffset<T> nextRecordFromSplit() {
    if (position < numberOfRecords) {
      setRecordWithOffset();
      position++;
      return recordWithOffset;
    } else {
      return null;
    }
  }

  private void setRecordWithOffset() {
    assert records != null;
    assert recordPositions[position] != null;
    RecordPosition offset = recordPositions[position];
    Preconditions.checkArgument(
        offset.currentInsertFileOffset() >= 0 || offset.currentDeleteFileOffset() >= 0,
        "fileOffset can't be negative");
    Preconditions.checkArgument(
        offset.currentInsertRecordOffset() >= 0, "numberOfRecords can't be negative");
    Preconditions.checkArgument(
        offset.currentDeleteRecordOffset() >= 0, "numberOfRecords can't be negative");
    recordWithOffset.set(
        records[position],
        offset.currentInsertFileOffset(),
        offset.currentInsertRecordOffset(),
        offset.currentDeleteFileOffset(),
        offset.currentDeleteRecordOffset());
  }

  /**
   * This method is called when all records from this batch has been emitted. If recycler is set, it
   * should be called to return the records array back to pool.
   */
  @Override
  public void recycle() {
    if (recycler != null) {
      recycler.recycle(records);
    }
  }

  @Override
  public Set<String> finishedSplits() {
    return finishedSplits;
  }

  @VisibleForTesting
  T[] records() {
    return records;
  }

  @VisibleForTesting
  int numberOfRecords() {
    return numberOfRecords;
  }

  /**
   * Create a ArrayBatchRecords backed up an array with records from the same file
   *
   * @param splitId Iceberg source only read from one split a time. We never have multiple records
   *     from multiple splits.
   * @param recycler Because {@link DataIterator} with {@link RowData} returns an iterator of reused
   *     RowData object, we need to clone RowData eagerly when constructing a batch of records. We
   *     can use object pool to reuse the RowData array object which can be expensive to create.
   *     This recycler can be provided to recycle the array object back to pool after read is
   *     exhausted. If the {@link DataIterator} returns an iterator of non-reused objects, we don't
   *     need to clone objects. It is cheap to just create the batch array. Hence, we don't need
   *     object pool and recycler can be set to null.
   * @param records an array (maybe reused) holding a batch of records
   * @param numberOfRecords actual number of records in the array
   * @param positions fileOffset and recordOffset for all records in this batch
   * @param <T> record type
   */
  public static <T> RecordsWithSplitIds<MixedFormatRecordWithOffset<T>> forRecords(
      String splitId,
      Pool.Recycler<T[]> recycler,
      T[] records,
      int numberOfRecords,
      RecordPosition[] positions) {
    return new ArrayBatchRecords<>(
        splitId, recycler, records, numberOfRecords, positions, Collections.emptySet());
  }

  /**
   * Create ab ArrayBatchRecords with only finished split id
   *
   * @param splitId for the split that is just exhausted
   */
  public static <T> ArrayBatchRecords<T> finishedSplit(String splitId) {
    return new ArrayBatchRecords<>(null, null, null, 0, 0, 0, Collections.singleton(splitId));
  }
}
