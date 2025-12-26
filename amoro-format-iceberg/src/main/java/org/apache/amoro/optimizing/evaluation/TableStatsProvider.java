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

package org.apache.amoro.optimizing.evaluation;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.table.MixedTable;

public abstract class TableStatsProvider {
  /** Collect basic file statistics for the given table. */
  public abstract BasicFileStats collect(MixedTable table);

  /** Statistics representation of basic file metrics. */
  public static class BasicFileStats {
    /** Count of delete files. */
    int deleteFileCnt = 0;
    /** Count of data files. */
    int dataFileCnt = 0;
    /** Total size of all files in bytes. */
    long totalFileSize = 0;

    @VisibleForTesting
    BasicFileStats() {}

    public void accept(int dataFileCnt, int deleteFileCnt, long totalFileSize) {
      this.dataFileCnt += dataFileCnt;
      this.deleteFileCnt += deleteFileCnt;
      this.totalFileSize += totalFileSize;
    }
  }
}
