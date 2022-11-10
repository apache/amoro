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

import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.iceberg.optimize.InternalRecordWrapper;
import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.ManifestEntryFields;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.InclusiveMetricsEvaluator;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

public class BaseChangeTableIncrementalScan implements ChangeTableIncrementalScan {

  private final ChangeTable table;
  private StructLikeMap<Long> fromPartitionTransactionId;
  private Expression dataFilter;

  private InclusiveMetricsEvaluator lazyMetricsEvaluator = null;

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
  public ChangeTableIncrementalScan fromTransactionId(StructLikeMap<Long> partitionMaxTransactionId) {
    this.fromPartitionTransactionId = partitionMaxTransactionId;
    return this;
  }

  @Override
  public CloseableIterable<ArcticFileScanTask> planTasks() {
    return planTasks((partition, sequence) -> {
      if (fromPartitionTransactionId == null || fromPartitionTransactionId.isEmpty()) {
        // if fromPartitionTransactionId is not set or is empty, return all files
        return true;
      }
      if (table.spec().isUnpartitioned()) {
        Long fromTransactionId = fromPartitionTransactionId.entrySet().iterator().next().getValue();
        return sequence > fromTransactionId;
      } else {
        Long partitionTransactionId =
            fromPartitionTransactionId.getOrDefault(partition, TableProperties.PARTITION_MAX_TRANSACTION_ID_DEFAULT);
        return sequence > partitionTransactionId;
      }
    });
  }

  public CloseableIterable<ArcticFileScanTask> planTasks(BiPredicate<StructLike, Long> partitionTransactionFilter) {
    Snapshot currentSnapshot = table.currentSnapshot();
    if (currentSnapshot == null) {
      // return no files for table without snapshot
      return CloseableIterable.empty();
    }
    long currentSnapshotSequence = table.currentSnapshot().sequenceNumber();
    Configuration hadoopConf = new Configuration();
    if (table.io() instanceof ArcticHadoopFileIO) {
      ArcticHadoopFileIO io = (ArcticHadoopFileIO) table.io();
      hadoopConf = io.conf();
    }
    HadoopTables tables = new HadoopTables(hadoopConf);
    Table entriesTable = tables.load(table.location() + "#ENTRIES");
    CloseableIterable<Record> entries = IcebergGenerics.read(entriesTable)
        .useSnapshot(currentSnapshot.snapshotId())
        .build();

    CloseableIterable<ArcticFileScanTask> allFileTasks =
        CloseableIterable.transform(entries, (entry -> {
          int status = (int) entry.getField(ManifestEntryFields.STATUS.name());
          GenericRecord dataFileRecord = (GenericRecord) entry.getField(ManifestEntryFields.DATA_FILE_FIELD_NAME);
          Integer contentId = (Integer) dataFileRecord.getField(DataFile.CONTENT.name());
          // metricsEvaluator.eval()
          // status == 0 means EXISTING, status == 1 means ADDED
          // contentId == 0 means FileContent.DATA
          if ((status == 0 || status == 1) && contentId != null && contentId == 0) {
            long sequence;
            if (status == 1) {
              // for files ADDED in this snapshot, sequence is equals to the current snapshot sequence
              sequence = currentSnapshotSequence;
            } else {
              // for EXISTING files, sequence is record in entry
              sequence = (long) entry.getField(ManifestEntryFields.SEQUENCE_NUMBER.name());
            }
            String filePath = (String) dataFileRecord.getField(DataFile.FILE_PATH.name());
            StructLike partition = getPartition(dataFileRecord);
            if (partitionTransactionFilter.test(partition, sequence)) {
              DataFile dataFile = buildDataFile(dataFileRecord, filePath, partition);
              if (metricsEvaluator().eval(dataFile)) {
                return new BaseArcticFileScanTask(new DefaultKeyedFile(dataFile), null, table.spec(), null);
              } else {
                // ignore files not match dataFilter
                return null;
              }
            } else {
              // ignore files not match partitionTransactionFilter
              return null;
            }
          } else {
            // ignore files not EXISTING/ADDED or not DataFile
            return null;
          }
        }));
    return CloseableIterable.filter(allFileTasks, Objects::nonNull);
  }

  private DataFile buildDataFile(GenericRecord dataFile, String filePath, StructLike partition) {
    Long fileSize = (Long) dataFile.getField(DataFile.FILE_SIZE.name());
    Long recordCount = (Long) dataFile.getField(DataFile.RECORD_COUNT.name());
    DataFile file;
    if (table.spec().isUnpartitioned()) {
      file = DataFiles.builder(table.spec())
          .withPath(filePath)
          .withFileSizeInBytes(fileSize)
          .withRecordCount(recordCount)
          .withMetrics(buildMetrics(dataFile))
          .build();
    } else {
      file = DataFiles.builder(table.spec())
          .withPath(filePath)
          .withFileSizeInBytes(fileSize)
          .withRecordCount(recordCount)
          .withPartition(partition)
          .withMetrics(buildMetrics(dataFile))
          .build();
    }
    return file;
  }

  private StructLike getPartition(GenericRecord dataFile) {
    GenericRecord parRecord = (GenericRecord) dataFile.getField(DataFile.PARTITION_NAME);
    if (parRecord != null) {
      InternalRecordWrapper wrapper = new InternalRecordWrapper(parRecord.struct());
      return wrapper.wrap(parRecord);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public Metrics buildMetrics(GenericRecord dataFile) {
    return new Metrics((Long) dataFile.getField(DataFile.RECORD_COUNT.name()),
        (Map<Integer, Long>) dataFile.getField(DataFile.COLUMN_SIZES.name()),
        (Map<Integer, Long>) dataFile.getField(DataFile.VALUE_COUNTS.name()),
        (Map<Integer, Long>) dataFile.getField(DataFile.NULL_VALUE_COUNTS.name()),
        (Map<Integer, Long>) dataFile.getField(DataFile.NAN_VALUE_COUNTS.name()),
        (Map<Integer, ByteBuffer>) dataFile.getField(DataFile.LOWER_BOUNDS.name()),
        (Map<Integer, ByteBuffer>) dataFile.getField(DataFile.UPPER_BOUNDS.name()));
  }

  private InclusiveMetricsEvaluator metricsEvaluator() {
    if (lazyMetricsEvaluator == null) {
      if (dataFilter != null) {
        this.lazyMetricsEvaluator =
            new InclusiveMetricsEvaluator(table.spec().schema(), dataFilter, true);
      } else {
        this.lazyMetricsEvaluator =
            new InclusiveMetricsEvaluator(table.spec().schema(), Expressions.alwaysTrue(), true);
      }
    }
    return lazyMetricsEvaluator;
  }

}
