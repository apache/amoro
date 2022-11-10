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

package com.netease.arctic.scan;

import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;

public interface ChangeTableIncrementalScan {
  /**
   * Config this scan with filter by the {@link Expression}.
   *
   * @param expr a filter expression
   * @return scan based on this with results filtered by the expression
   */
  ChangeTableIncrementalScan filter(Expression expr);

  /**
   * Plan the {@link ArcticFileScanTask tasks} for this scan.
   *
   * @return an Iterable of tasks for this scan
   */
  CloseableIterable<ArcticFileScanTask> planTasks();

  /**
   * Config this scan to read data from {@code partitionTransactionId} exclusive to
   * the current Transaction inclusive.
   * @param partitionTransactionId from TransactionId for each partition
   * @return this for method chaining
   */
  ChangeTableIncrementalScan fromTransactionId(StructLikeMap<Long> partitionTransactionId);


  /**
   * Config this scan to read data from legacy {@code partitionTransactionId} exclusive to
   * the current Transaction inclusive.
   * For partitions set both TransactionId and LegacyTransactionId, LegacyTransactionId will
   * be ignored.
   * @param partitionTransactionId from TransactionId for each partition
   * @return this for method chaining
   */
  ChangeTableIncrementalScan fromLegacyTransactionId(StructLikeMap<Long> partitionTransactionId);
}
