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

package org.apache.iceberg;

import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.scan.BasicArcticFileScanTask;
import com.netease.arctic.scan.ChangeTableIncrementalScan;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;

/**
 * Table scan for {@link com.netease.arctic.table.ChangeTable}, support filter files with data
 * sequence number and return {@link BasicArcticFileScanTask}.
 */
public class MixedChangeTableScan extends DataTableScan implements ChangeTableIncrementalScan {
  private StructLikeMap<Long> fromPartitionSequence;
  private StructLikeMap<Long> fromPartitionLegacyTransactionId;
  private Long toSequence;
  private Long fromSequence;

  public MixedChangeTableScan(Table table, Schema schema) {
    super(table, schema, ImmutableTableScanContext.builder().build());
  }

  protected MixedChangeTableScan(Table table, Schema schema, TableScanContext context) {
    super(table, schema, context);
  }

  @Override
  public MixedChangeTableScan useSnapshot(long scanSnapshotId) {
    TableScan scan = super.useSnapshot(scanSnapshotId);
    return newRefinedScan(table(), scan.schema(), context().useSnapshotId(scanSnapshotId));
  }

  @Override
  public MixedChangeTableScan filter(Expression expr) {
    TableScan scan = super.filter(expr);
    return newRefinedScan(table(), scan.schema(), context().filterRows(scan.filter()));
  }

  @Override
  protected MixedChangeTableScan newRefinedScan(
      Table table, Schema schema, TableScanContext context) {
    MixedChangeTableScan scan = new MixedChangeTableScan(table, schema, context);
    scan.fromPartitionSequence = this.fromPartitionSequence;
    scan.fromPartitionLegacyTransactionId = this.fromPartitionLegacyTransactionId;
    scan.toSequence = this.toSequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan fromSequence(StructLikeMap<Long> partitionSequence) {
    MixedChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.fromPartitionSequence = partitionSequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan fromSequence(long sequence) {
    MixedChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.fromSequence = sequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan toSequence(long sequence) {
    MixedChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.toSequence = sequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan fromLegacyTransaction(
      StructLikeMap<Long> partitionTransactionId) {
    MixedChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.fromPartitionLegacyTransactionId = partitionTransactionId;
    return scan;
  }

  @Override
  public CloseableIterable<FileScanTask> doPlanFiles() {
    CloseableIterable<FileScanTask> filteredTasks =
        CloseableIterable.filter(
            super.doPlanFiles(),
            fileScanTask -> {
              StructLike partition = fileScanTask.file().partition();
              long sequenceNumber = fileScanTask.file().dataSequenceNumber();
              Boolean shouldKeep = shouldKeepFile(partition, sequenceNumber);
              if (shouldKeep == null) {
                String filePath = fileScanTask.file().path().toString();
                return shouldKeepFileWithLegacyTxId(
                    partition, FileNameRules.parseChange(filePath, sequenceNumber).transactionId());
              } else {
                return shouldKeep;
              }
            });
    return CloseableIterable.transform(
        filteredTasks,
        fileScanTask ->
            new BasicArcticFileScanTask(
                DefaultKeyedFile.parseChange(fileScanTask.file()), null, table().spec(), null));
  }

  private Boolean shouldKeepFile(StructLike partition, long sequence) {
    if (biggerThanToSequence(sequence)) {
      return false;
    }
    if (fromSequence == null
        && (fromPartitionSequence == null || fromPartitionSequence.isEmpty())) {
      // if fromPartitionSequence is not set or is empty, return null to check legacy transactionId
      return null;
    }
    Long fromSequence;
    if (table().spec().isUnpartitioned()) {
      fromSequence = scanFromSequence(TablePropertyUtil.EMPTY_STRUCT);
    } else {
      fromSequence = scanFromSequence(partition);
    }
    if (fromSequence != null) {
      return sequence > fromSequence;
    } else {
      return null;
    }
  }

  private Long scanFromSequence(StructLike partitionData) {
    Long fromSequence = null;
    if (fromPartitionSequence != null) {
      fromSequence = fromPartitionSequence.get(partitionData);
    }
    if (fromSequence == null) {
      fromSequence = this.fromSequence;
    }
    return fromSequence;
  }

  private boolean biggerThanToSequence(long sequence) {
    return this.toSequence != null && sequence > this.toSequence;
  }

  private boolean shouldKeepFileWithLegacyTxId(StructLike partition, long legacyTxId) {
    if (fromPartitionLegacyTransactionId == null || fromPartitionLegacyTransactionId.isEmpty()) {
      // if fromPartitionLegacyTransactionId is not set or is empty, return all files
      return true;
    }
    if (table().spec().isUnpartitioned()) {
      Long fromTransactionId =
          fromPartitionLegacyTransactionId.entrySet().iterator().next().getValue();
      return legacyTxId > fromTransactionId;
    } else {
      if (!fromPartitionLegacyTransactionId.containsKey(partition)) {
        // if fromPartitionLegacyTransactionId not contains this partition, return all files of this
        // partition
        return true;
      } else {
        Long partitionTransactionId = fromPartitionLegacyTransactionId.get(partition);
        return legacyTxId > partitionTransactionId;
      }
    }
  }
}
