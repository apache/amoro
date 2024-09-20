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

import org.apache.iceberg.TableScan;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.util.StructLikeMap;

public interface ChangeTableIncrementalScan extends TableScan {

  /**
   * Config this scan to read data from a particular sequence (exclusive) for each partition.
   *
   * @param partitionSequence - sequence for each partition
   * @return this for method chaining
   */
  ChangeTableIncrementalScan fromSequence(StructLikeMap<Long> partitionSequence);

  /**
   * Config this scan to read data from a particular sequence (exclusive).
   *
   * @param sequence - sequence (exclusive) scan from
   * @return this for method chaining
   */
  ChangeTableIncrementalScan fromSequence(long sequence);

  /**
   * Config this scan to read data up to a particular sequence (inclusive).
   *
   * @param sequence - sequence (inclusive) scan to
   * @return this for method chaining
   */
  ChangeTableIncrementalScan toSequence(long sequence);

  @Override
  ChangeTableIncrementalScan useSnapshot(long snapshotId);

  @Override
  ChangeTableIncrementalScan filter(Expression filter);
}
