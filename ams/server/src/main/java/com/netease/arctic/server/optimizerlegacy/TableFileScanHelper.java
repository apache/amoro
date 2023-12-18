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

package com.netease.arctic.server.optimizerlegacy;

import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.io.CloseableIterable;

import java.util.List;

public interface TableFileScanHelper {
  class FileScanResult {
    private final DataFile file;
    private final List<ContentFile<?>> deleteFiles;

    public FileScanResult(DataFile file, List<ContentFile<?>> deleteFiles) {
      this.file = file;
      this.deleteFiles = deleteFiles;
    }

    public DataFile file() {
      return file;
    }

    public List<ContentFile<?>> deleteFiles() {
      return deleteFiles;
    }
  }

  interface PartitionFilter {
    /**
     * If we should keep or skip this partition
     *
     * @param partition -
     * @return true for keep this partition
     */
    boolean test(String partition);
  }

  CloseableIterable<FileScanResult> scan();

  TableFileScanHelper withPartitionFilter(PartitionFilter partitionFilter);
}
