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
import com.netease.arctic.io.ArcticHadoopFileIO;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.FileUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DataTask;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.InclusiveMetricsEvaluator;
import org.apache.iceberg.hadoop.HadoopTables;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class BaseChangeTableIncrementalScan implements ChangeTableIncrementalScan {

  private final ChangeTable table;
  private StructLikeMap<Long> fromPartitionTransactionId;
  private StructLikeMap<Long> fromPartitionLegacyTransactionId;
  private Expression dataFilter;

  private InclusiveMetricsEvaluator lazyMetricsEvaluator = null;
  private Map<String, Integer> lazyIndexOfDataFileType;

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
    Configuration hadoopConf = new Configuration();
    if (table.io() instanceof ArcticHadoopFileIO) {
      ArcticHadoopFileIO io = (ArcticHadoopFileIO) table.io();
      hadoopConf = io.conf();
    }
    HadoopTables tables = new HadoopTables(hadoopConf);
    Table entriesTable = tables.load(table.location() + "#" + MetadataTableType.ALL_ENTRIES);
    CloseableIterable<FileScanTask> manifestFileScanTasks = entriesTable.newScan().planFiles();

    CloseableIterable<StructLike> entries = CloseableIterable.concat(entriesOfManifest(manifestFileScanTasks));

    CloseableIterable<ArcticFileScanTask> allFileTasks =
        CloseableIterable.transform(entries, (entry -> {
          int status = entry.get(0, Integer.class);
          long sequence = entry.get(2, Long.class);
          StructLike dataFileRecord = entry.get(3, StructLike.class);
          Integer contentId = dataFileRecord.get(dataFileFieldIndex(DataFile.CONTENT.name()), Integer.class);
          String filePath = dataFileRecord.get(dataFileFieldIndex(DataFile.FILE_PATH.name()), String.class);
          StructLike partition;
          if (table.spec().isUnpartitioned()) {
            partition = null;
          } else {
            partition = dataFileRecord.get(dataFileFieldIndex(DataFile.PARTITION_NAME), StructLike.class);
          }
          if ((status == 0 || status == 1) && contentId != null && contentId == 0) {
            Boolean shouldKeep = shouldKeepFile.shouldKeep(partition, sequence);
            if (shouldKeep == null) {
              // if not sure should keep, use legacy transactionId to check
              if (shouldKeepFileWithLegacyTxId.shouldKeep(partition, FileUtil.parseFileTidFromFileName(filePath))) {
                DataFile dataFile = buildDataFile(dataFileRecord, filePath, partition);
                if (metricsEvaluator().eval(dataFile)) {
                  return new BaseArcticFileScanTask(new DefaultKeyedFile(dataFile), null, table.spec(), null);
                }
              }
            } else {
              if (shouldKeep) {
                DataFile dataFile = buildDataFile(dataFileRecord, filePath, partition);
                if (metricsEvaluator().eval(dataFile)) {
                  return new BaseArcticFileScanTask(new DefaultKeyedFile(dataFile), null, table.spec(), null);
                }
              }
            }
          }
          return null;
        }));
    return CloseableIterable.filter(allFileTasks, Objects::nonNull);
  }

  private Iterable<CloseableIterable<StructLike>> entriesOfManifest(CloseableIterable<FileScanTask> fileScanTasks) {
    return Iterables.transform(fileScanTasks, task -> {
      assert task != null;
      return ((DataTask) task).rows();
    });
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

  private DataFile buildDataFile(StructLike dataFile, String filePath, StructLike partition) {
    Long fileSize = dataFile.get(dataFileFieldIndex(DataFile.FILE_SIZE.name()), Long.class);
    Long recordCount = dataFile.get(dataFileFieldIndex(DataFile.RECORD_COUNT.name()), Long.class);
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

  @SuppressWarnings("unchecked")
  public Metrics buildMetrics(StructLike dataFile) {
    return new Metrics(dataFile.get(dataFileFieldIndex(DataFile.RECORD_COUNT.name()), Long.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.COLUMN_SIZES.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.VALUE_COUNTS.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.NULL_VALUE_COUNTS.name()), Map.class),
        (Map<Integer, Long>) dataFile.get(dataFileFieldIndex(DataFile.NAN_VALUE_COUNTS.name()), Map.class),
        (Map<Integer, ByteBuffer>) dataFile.get(dataFileFieldIndex(DataFile.LOWER_BOUNDS.name()), Map.class),
        (Map<Integer, ByteBuffer>) dataFile.get(dataFileFieldIndex(DataFile.UPPER_BOUNDS.name()), Map.class));
  }

  private int dataFileFieldIndex(String fieldName) {
    if (lazyIndexOfDataFileType == null) {
      Map<String, Integer> map = Maps.newHashMap();
      Types.StructType dataFileType = DataFile.getType(table.spec().partitionType());
      ArrayList<Types.NestedField> validFields = Lists.newArrayList(dataFileType.fields());
      if (table.spec().isUnpartitioned()) {
        // for no pk table, the partition field is not included
        validFields.removeIf(f -> f.name().equals(DataFile.PARTITION_NAME));
      }
      for (int i = 0; i < validFields.size(); i++) {
        map.put(validFields.get(i).name(), i);
      }
      lazyIndexOfDataFileType = map;
    }
    return lazyIndexOfDataFileType.get(fieldName);
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
