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

package com.netease.arctic.spark.actions.optimizing;

import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

public class IcebergTableFileScanHelper implements TableFileScanHelper {
  private final Table table;
  private PartitionFilter partitionFilter;
  private Expression filter;
  private final long snapshotId;

  public IcebergTableFileScanHelper(Table table, long snapshotId) {
    this.table = table;
    this.snapshotId = snapshotId;
  }

  @Override
  public CloseableIterable<FileScanResult> scan() {
    if (snapshotId == TableSnapshot.INVALID_SNAPSHOT_ID) {
      return CloseableIterable.empty();
    }
    PartitionSpec partitionSpec = table.spec();
    return CloseableIterable.transform(
        CloseableIterable.filter(
            table.newScan().useSnapshot(snapshotId).filter(filter).planFiles(),
            fileScanTask -> {
              if (partitionFilter != null) {
                StructLike partition = fileScanTask.file().partition();
                String partitionPath = partitionSpec.partitionToPath(partition);
                return partitionFilter.test(partitionPath);
              }
              return true;
            }), this::buildFileScanResult);
  }

  protected FileScanResult buildFileScanResult(FileScanTask fileScanTask) {
    return new FileScanResult(fileScanTask.file(), Lists.newArrayList(fileScanTask.deletes()));
  }

  @Override
  public TableFileScanHelper withPartitionFilter(PartitionFilter partitionFilter) {
    this.partitionFilter = partitionFilter;
    return this;
  }

  @Override
  public TableFileScanHelper withFilter(Expression filter) {
    this.filter = filter;
    return this;
  }
}
