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

package org.apache.amoro.scan;

import org.apache.amoro.table.KeyedTable;

import java.io.Serializable;
import java.util.List;

/**
 * A scan task for {@link KeyedTable} over some base files, positional delete files, insert files
 * and equality delete files.
 */
public interface KeyedTableScanTask extends Serializable {

  /**
   * Returns the estimated cost of reading the task, used to split and combine task to {@link
   * CombinedScanTask}
   */
  long cost();

  /** Returns the estimated count of record reading from the task */
  long recordCount();

  /** Returns a list of {@link MixedFileScanTask} for base files */
  List<MixedFileScanTask> baseTasks();

  /** Returns a list of {@link MixedFileScanTask} for insert files */
  List<MixedFileScanTask> insertTasks();

  /** Returns a list of {@link MixedFileScanTask} for equality delete files */
  List<MixedFileScanTask> mixedEquityDeletes();

  /** Returns a list of {@link MixedFileScanTask} for insert files and base files */
  List<MixedFileScanTask> dataTasks();
}
