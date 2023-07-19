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

import com.netease.arctic.scan.ArcticFileScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import org.apache.iceberg.io.CloseableIterator;

/**
 * Flink data iterator that reads {@link ArcticFileScanTask} or {@link KeyedTableScanTask}
 * into a {@link CloseableIterator}
 *
 * @param <T> is the output data type returned by this iterator.
 */
public interface ScanTaskDataIterator<T> extends CloseableIterator<T> {

  /**
   * (startingFileOffset, startingRecordOffset) points to the next row that reader should resume from.
   * E.g., if the seek position is (file=0, record=1), seek moves the iterator position to the 2nd row
   * in file 0. When next() is called after seek, 2nd row from file 0 should be returned.
   */
  void seek(int startingFileOffset, long startingRecordOffset);

  boolean currentFileHasNext();

  int fileOffset();

  long recordOffset();
}
