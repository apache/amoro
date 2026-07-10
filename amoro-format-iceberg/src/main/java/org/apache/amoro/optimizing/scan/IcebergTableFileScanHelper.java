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

package org.apache.amoro.optimizing.scan;

import org.apache.amoro.iceberg.Constants;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.utils.IcebergThreadPools;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;

import java.util.Map;

public class IcebergTableFileScanHelper implements TableFileScanHelper {
  private final Table table;
  private Expression partitionFilter = Expressions.alwaysTrue();
  private final long snapshotId;
  private final Map<Integer, PartitionSpec> specs;

  public IcebergTableFileScanHelper(Table table, long snapshotId) {
    this.table = table;
    this.snapshotId = snapshotId;
    this.specs = table.specs();
  }

  @Override
  public CloseableIterable<FileScanResult> scan() {
    if (snapshotId == Constants.INVALID_SNAPSHOT_ID) {
      return CloseableIterable.empty();
    }
    return CloseableIterable.transform(
        table
            .newScan()
            .planWith(IcebergThreadPools.getPlanningExecutor())
            .useSnapshot(snapshotId)
            .filter(partitionFilter)
            .planFiles(),
        this::buildFileScanResult);
  }

  protected FileScanResult buildFileScanResult(FileScanTask fileScanTask) {
    return new FileScanResult(fileScanTask.file(), Lists.newArrayList(fileScanTask.deletes()));
  }

  @Override
  public TableFileScanHelper withPartitionFilter(Expression partitionFilter) {
    this.partitionFilter = partitionFilter;
    return this;
  }

  @Override
  public PartitionSpec getSpec(int specId) {
    return specs.get(specId);
  }
}
