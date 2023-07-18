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

package com.netease.arctic.flink.read.source;

import com.netease.arctic.scan.KeyedTableScanTask;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.io.CloseableIterator;

import java.io.IOException;

public class MergeOnReadDataIterator implements ScanTaskDataIterator<RowData> {
  private int fileOffset;
  private long recordOffset;
  private CloseableIterator<RowData> iterator;

  public MergeOnReadDataIterator(
      FlinkArcticMORDataReader flinkArcticMORDataReader,
      KeyedTableScanTask keyedTableScanTask) {
    this.iterator = flinkArcticMORDataReader.readData(keyedTableScanTask);
  }

  @Override
  public void seek(int startingFileOffset, long startingRecordOffset) {
    // skip records within the file
    for (long i = 0; i < startingRecordOffset; ++i) {
      if (hasNext()) {
        next();
      } else {
        throw new IllegalStateException(String.format(
            "Invalid starting record offset %d for file %d from KeyedTableScanTask List.",
            startingRecordOffset, startingFileOffset));
      }
    }
    this.fileOffset = startingFileOffset;
    this.recordOffset = startingRecordOffset;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public RowData next() {
    return this.iterator.next();
  }

  public boolean currentFileHasNext() {
    return this.iterator.hasNext();
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
    this.iterator.close();
    this.iterator = null;
  }
}
