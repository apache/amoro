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

package org.apache.amoro.spark.reader;

import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScan;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.spark.util.Stats;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.read.Batch;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReader;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;
import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.Statistics;
import org.apache.spark.sql.connector.read.SupportsReportStatistics;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class KeyedSparkBatchScan implements Scan, Batch, SupportsReportStatistics {
  private static final Logger LOG = LoggerFactory.getLogger(KeyedSparkBatchScan.class);

  private final KeyedTable table;
  private final boolean caseSensitive;
  private final Schema expectedSchema;
  private final List<Expression> filterExpressions;
  private StructType readSchema = null;
  private List<CombinedScanTask> tasks = null;

  KeyedSparkBatchScan(
      KeyedTable table,
      boolean caseSensitive,
      Schema expectedSchema,
      List<Expression> filters,
      CaseInsensitiveStringMap options) {
    Preconditions.checkNotNull(table, "table must not be null");
    Preconditions.checkNotNull(expectedSchema, "expectedSchema must not be null");
    Preconditions.checkNotNull(filters, "filters must not be null");

    this.table = table;
    this.caseSensitive = caseSensitive;
    this.expectedSchema = expectedSchema;
    this.filterExpressions = filters;
  }

  @Override
  public Batch toBatch() {
    return this;
  }

  @Override
  public StructType readSchema() {
    if (readSchema == null) {
      this.readSchema = SparkSchemaUtil.convert(expectedSchema);
    }
    return readSchema;
  }

  @Override
  public InputPartition[] planInputPartitions() {
    List<CombinedScanTask> scanTasks = tasks();
    MixedFormatInputPartition[] readTasks = new MixedFormatInputPartition[scanTasks.size()];
    for (int i = 0; i < scanTasks.size(); i++) {
      readTasks[i] =
          new MixedFormatInputPartition(scanTasks.get(i), table, expectedSchema, caseSensitive);
    }
    return readTasks;
  }

  @Override
  public PartitionReaderFactory createReaderFactory() {
    return new ReaderFactory();
  }

  @Override
  public Statistics estimateStatistics() {
    long sizeInBytes = 0L;
    long numRows = 0L;

    for (CombinedScanTask combinedScanTask : tasks()) {
      for (KeyedTableScanTask fileScanTask : combinedScanTask.tasks()) {

        sizeInBytes += fileScanTask.cost();
        numRows += fileScanTask.recordCount();
      }
    }

    return new Stats(sizeInBytes, numRows);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    KeyedSparkBatchScan that = (KeyedSparkBatchScan) o;
    return table.id().equals(that.table.id())
        && readSchema().equals(that.readSchema())
        && // compare Spark schemas to ignore field ids
        filterExpressions.toString().equals(that.filterExpressions.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(table.id(), readSchema());
  }

  private List<CombinedScanTask> tasks() {
    if (tasks == null) {
      KeyedTableScan scan = table.newScan();

      for (Expression filter : filterExpressions) {
        scan = scan.filter(filter);
      }
      long startTime = System.currentTimeMillis();
      LOG.info("mor statistics plan task start");
      try (CloseableIterable<CombinedScanTask> tasksIterable = scan.planTasks()) {
        this.tasks = Lists.newArrayList(tasksIterable);
        LOG.info(
            "mor statistics plan task end, cost time {}, tasks num {}",
            System.currentTimeMillis() - startTime,
            tasks.size());
      } catch (IOException e) {
        throw new UncheckedIOException("Failed to close table scan: %s", e);
      }
    }
    return tasks;
  }

  @Override
  public String description() {
    String filters =
        filterExpressions.stream().map(Spark3Util::describe).collect(Collectors.joining(", "));
    return String.format("%s [filters=%s]", table, filters);
  }

  @Override
  public String toString() {
    return String.format(
        "IcebergScan(table=%s, type=%s, filters=%s, caseSensitive=%s)",
        table, expectedSchema.asStruct(), filterExpressions, caseSensitive);
  }

  private static class ReaderFactory implements PartitionReaderFactory {
    @Override
    public PartitionReader<InternalRow> createReader(InputPartition partition) {
      if (partition instanceof MixedFormatInputPartition) {
        return new RowReader((MixedFormatInputPartition) partition);
      } else {
        throw new UnsupportedOperationException("Incorrect input partition type: " + partition);
      }
    }
  }

  private static class RowReader implements PartitionReader<InternalRow> {

    SparkKeyedDataReader reader;
    Iterator<KeyedTableScanTask> scanTasks;
    KeyedTableScanTask currentScanTask;
    CloseableIterator<InternalRow> currentIterator = CloseableIterator.empty();
    InternalRow current;

    RowReader(MixedFormatInputPartition task) {
      reader =
          new SparkKeyedDataReader(
              task.io,
              task.tableSchema,
              task.expectedSchema,
              task.keySpec,
              task.nameMapping,
              task.caseSensitive);
      scanTasks = task.combinedScanTask.tasks().iterator();
    }

    @Override
    public boolean next() throws IOException {
      while (true) {
        if (currentIterator.hasNext()) {
          this.current = currentIterator.next();
          return true;
        } else if (scanTasks.hasNext()) {
          this.currentIterator.close();
          this.currentScanTask = scanTasks.next();
          this.currentIterator = reader.readData(this.currentScanTask);
        } else {
          this.currentIterator.close();
          return false;
        }
      }
    }

    @Override
    public InternalRow get() {
      return this.current;
    }

    @Override
    public void close() throws IOException {
      this.currentIterator.close();
      while (scanTasks.hasNext()) {
        scanTasks.next();
      }
    }
  }

  private static class MixedFormatInputPartition implements InputPartition, Serializable {
    final CombinedScanTask combinedScanTask;
    final AuthenticatedFileIO io;
    final boolean caseSensitive;
    final Schema expectedSchema;
    final Schema tableSchema;
    final PrimaryKeySpec keySpec;
    final String nameMapping;

    MixedFormatInputPartition(
        CombinedScanTask combinedScanTask,
        KeyedTable table,
        Schema expectedSchema,
        boolean caseSensitive) {
      this.combinedScanTask = combinedScanTask;
      this.expectedSchema = expectedSchema;
      this.tableSchema = table.schema();
      this.caseSensitive = caseSensitive;
      this.io = table.io();
      this.keySpec = table.primaryKeySpec();
      this.nameMapping = table.properties().get(TableProperties.DEFAULT_NAME_MAPPING);
    }
  }
}
