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

package org.apache.amoro.flink.read.source;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.flink.table.data.RowData;
import org.apache.flink.util.Preconditions;
import org.apache.iceberg.io.CloseableIterator;

import java.io.IOException;

/**
 * Iterator for reading data in a Merge on Read (MOR) way. This iterator handles reading data from
 * an Amoro mix-format table while keeping track of file and record offsets for efficient data
 * retrieval.
 */
public class MergeOnReadDataIterator extends DataIterator<RowData> {
  private int fileOffset;
  private long recordOffset;
  private final CloseableIterator<RowData> iterator;

  public MergeOnReadDataIterator(
      FlinkKeyedMORDataReader flinkKeyedMORDataReader,
      KeyedTableScanTask keyedTableScanTask,
      AuthenticatedFileIO io) {
    super();
    this.iterator =
        IteratorWithIO.of(io, io.doAs(() -> flinkKeyedMORDataReader.readData(keyedTableScanTask)));
  }

  @Override
  public void seek(int startingFileOffset, long startingRecordOffset) {
    // startingFileOffset is not used, because we only have one file per task
    Preconditions.checkNotNull(iterator, "iterator is null in the MergeOnReadDataIterator.");
    // skip records within the file
    for (long i = 0; i < startingRecordOffset; ++i) {
      if (hasNext()) {
        next();
      } else {
        throw new IllegalStateException(
            String.format(
                "Invalid starting record offset %d for file %d from KeyedTableScanTask.",
                startingRecordOffset, startingFileOffset));
      }
    }
    this.fileOffset = startingFileOffset;
    this.recordOffset = startingRecordOffset;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public RowData next() {
    return iterator.next();
  }

  public boolean currentFileHasNext() {
    return iterator.hasNext();
  }

  @Override
  public int fileOffset() {
    return fileOffset;
  }

  @Override
  public long recordOffset() {
    return recordOffset;
  }

  @Override
  public void close() throws IOException {
    // close the current iterator
    if (iterator != null) {
      iterator.close();
    }
  }

  static class IteratorWithIO implements CloseableIterator<RowData> {
    private final AuthenticatedFileIO io;
    private final CloseableIterator<RowData> iterator;

    private IteratorWithIO(AuthenticatedFileIO io, CloseableIterator<RowData> iterator) {
      this.io = io;
      this.iterator = iterator;
    }

    static IteratorWithIO of(AuthenticatedFileIO io, CloseableIterator<RowData> iterator) {
      Preconditions.checkNotNull(io);
      return new IteratorWithIO(io, iterator);
    }

    @Override
    public void close() throws IOException {
      io.doAs(
          () -> {
            iterator.close();
            return null;
          });
    }

    @Override
    public boolean hasNext() {
      return io.doAs(iterator::hasNext);
    }

    @Override
    public RowData next() {
      return io.doAs(iterator::next);
    }
  }
}
