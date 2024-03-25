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

package com.netease.arctic.io.reader;

import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;

/** Copy from {@link org.apache.iceberg.data.DeleteLoader} to adapt return {@link RecordWithLsn}. */
public interface CombinedDeleteLoader {

  /**
   * Loads the content of equality delete files into a set.
   *
   * @param deleteFiles equality delete file
   * @param projection a projection of columns to load
   * @return a set of equality deletes
   */
  Iterable<RecordWithLsn> loadEqualityDeletes(Iterable<DeleteFile> deleteFiles, Schema projection);

  class RecordWithLsn {
    private final Long lsn;
    private Record record;

    public RecordWithLsn(Long lsn, Record record) {
      this.lsn = lsn;
      this.record = record;
    }

    public Long getLsn() {
      return lsn;
    }

    public Record getRecord() {
      return record;
    }

    public RecordWithLsn recordCopy() {
      record = record.copy();
      return this;
    }
  }
}
