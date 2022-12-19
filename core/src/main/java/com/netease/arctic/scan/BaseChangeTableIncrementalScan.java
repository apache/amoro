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

import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;

public class BaseChangeTableIncrementalScan implements ChangeTableIncrementalScan {

  private final ChangeTable table;
  private StructLikeMap<Long> fromPartitionTransactionId;
  private StructLikeMap<Long> fromPartitionLegacyTransactionId;
  private Expression dataFilter;

  public BaseChangeTableIncrementalScan(ChangeTable table) {
    this.table = table;
  }

  @Override
  public ChangeTableIncrementalScan filter(Expression expr) {
    if (dataFilter == null) {
      dataFilter = expr;
    } else {
      dataFilter = Expressions.and(expr, dataFilter);
    }
    return this;
  }

  @Override
  public ChangeTableIncrementalScan fromTransaction(StructLikeMap<Long> partitionMaxTransactionId) {
    this.fromPartitionTransactionId = partitionMaxTransactionId;
    return this;
  }

  @Override
  public ChangeTableIncrementalScan fromLegacyTransaction(StructLikeMap<Long> partitionTransactionId) {
    this.fromPartitionLegacyTransactionId = partitionTransactionId;
    return this;
  }

  @Override
  public CloseableIterable<ArcticFileScanTask> planTasks() {
    return planTasks(this::shouldKeepFile, this::shouldKeepFileWithLegacyTxId);
  }

  public CloseableIterable<ArcticFileScanTask> planTasks(PartitionDataFilter shouldKeepFile,
                                                         PartitionDataFilter shouldKeepFileWithLegacyTxId) {
    Snapshot currentSnapshot = table.currentSnapshot();
    if (currentSnapshot == null) {
      // return no files for table without snapshot
      return CloseableIterable.empty();
    }
    TableEntriesScan manifestReader = TableEntriesScan.builder(table)
        .withAliveEntry(true)
        .withDataFilter(dataFilter)
        .includeFileContent(FileContent.DATA)
        .build();
    CloseableIterable<IcebergFileEntry> filteredEntry = CloseableIterable.filter(manifestReader.entries(), entry -> {
      StructLike partition = entry.getFile().partition();
      long sequenceNumber = entry.getSequenceNumber();
      Boolean shouldKeep = shouldKeepFile.shouldKeep(partition, sequenceNumber);
      if (shouldKeep == null) {
        String filePath = entry.getFile().path().toString();
        return shouldKeepFileWithLegacyTxId.shouldKeep(partition, TableFileUtils.parseFileTidFromFileName(filePath));
      } else {
        return shouldKeep;
      }
    });
    return CloseableIterable.transform(filteredEntry, e ->
        new BaseArcticFileScanTask(new DefaultKeyedFile((DataFile) e.getFile()), null, table.spec(), null));
  }

  private Boolean shouldKeepFile(StructLike partition, long txId) {
    if (fromPartitionTransactionId == null || fromPartitionTransactionId.isEmpty()) {
      // if fromPartitionTransactionId is not set or is empty, return null to check legacy transactionId
      return null;
    }
    if (table.spec().isUnpartitioned()) {
      Long fromTransactionId = fromPartitionTransactionId.entrySet().iterator().next().getValue();
      return txId > fromTransactionId;
    } else {
      if (!fromPartitionTransactionId.containsKey(partition)) {
        // return null to check legacy transactionId
        return null;
      } else {
        Long partitionTransactionId = fromPartitionTransactionId.get(partition);
        return txId > partitionTransactionId;
      }
    }
  }

  private boolean shouldKeepFileWithLegacyTxId(StructLike partition, long legacyTxId) {
    if (fromPartitionLegacyTransactionId == null || fromPartitionLegacyTransactionId.isEmpty()) {
      // if fromPartitionLegacyTransactionId is not set or is empty, return all files
      return true;
    }
    if (table.spec().isUnpartitioned()) {
      Long fromTransactionId = fromPartitionLegacyTransactionId.entrySet().iterator().next().getValue();
      return legacyTxId > fromTransactionId;
    } else {
      Long partitionTransactionId = fromPartitionLegacyTransactionId.getOrDefault(partition,
          TableProperties.PARTITION_MAX_TRANSACTION_ID_DEFAULT);
      return legacyTxId > partitionTransactionId;
    }
  }

  interface PartitionDataFilter {
    Boolean shouldKeep(StructLike partition, long transactionId);
  }
}
