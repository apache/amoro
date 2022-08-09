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

package com.netease.arctic.spark.writer;

import com.netease.arctic.op.OverwriteBaseFiles;
import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.Tasks;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.DataWriterFactory;
import org.apache.spark.sql.connector.write.PhysicalWriteInfo;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;



public class SparkWrite {
  private final KeyedTable table;
  private final StructType dsSchema;

  private final long transactionId;

  SparkWrite(KeyedTable table, StructType dsSchema) {
    this.table = table;
    this.dsSchema = dsSchema;
    this.transactionId = table.beginTransaction(null);
  }

  BatchWrite asBatchAppend() {
    return new BatchAppend();
  }

  BatchWrite asDynamicOverwrite() {
    return new DynamicOverwrite();
  }

  BatchWrite asOverwriteByFilter(Expression overwriteExpr) {
    return new OverwriteByFilter(overwriteExpr);
  }

  private abstract class BaseBatchWrite implements BatchWrite {
    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      return new WriterFactory(table, dsSchema, transactionId);
    }

    @Override
    public void abort(WriterCommitMessage[] messages) {
      Map<String, String> props = table.properties();
      Tasks.foreach(files(messages))
          .retry(PropertyUtil.propertyAsInt(props, COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
          .exponentialBackoff(
              PropertyUtil.propertyAsInt(props, COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
              PropertyUtil.propertyAsInt(props, COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
              PropertyUtil.propertyAsInt(props, COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
              2.0 /* exponential */)
          .throwFailureWhenFinished()
          .run(file -> {
            table.io().deleteFile(file.path().toString());
          });
    }
  }

  private class BatchAppend extends BaseBatchWrite {
    @Override
    public void commit(WriterCommitMessage[] messages) {
      AppendFiles append = table.baseTable().newAppend();

      for (DataFile file : files(messages)) {
        append.appendFile(file);
      }

      append.commit();
    }
  }

  private class DynamicOverwrite extends BaseBatchWrite {
    @Override
    public void commit(WriterCommitMessage[] messages) {
      RewritePartitions rewritePartitions = table.baseTable().newRewritePartition();
      rewritePartitions.withTransactionId(transactionId);

      for (DataFile file : files(messages)) {
        rewritePartitions.addDataFile(file);
      }
      rewritePartitions.commit();
    }
  }

  private class OverwriteByFilter extends BaseBatchWrite {
    private final Expression overwriteExpr;

    private OverwriteByFilter(Expression overwriteExpr) {
      this.overwriteExpr = overwriteExpr;
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      OverwriteBaseFiles overwriteBaseFiles = table.baseTable().newOverwriteBaseFiles();
      overwriteBaseFiles.overwriteByRowFilter(overwriteExpr);
      overwriteBaseFiles.withTransactionId(transactionId);

      for (DataFile file : files(messages)) {
        overwriteBaseFiles.addFile(file);
      }

      overwriteBaseFiles.commit();
    }
  }

  private static class WriterFactory implements DataWriterFactory, Serializable {
    private final KeyedTable table;
    private final StructType dsSchema;
    private final long transactionId;

    WriterFactory(KeyedTable table, StructType dsSchema, long transactionId) {
      this.table = table;
      this.dsSchema = dsSchema;
      this.transactionId = transactionId;
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      TaskWriter<InternalRow> writer = ArcticSparkTaskWriters.buildFor(table)
          .withTransactionId(transactionId)
          .withPartitionId(partitionId)
          .withTaskId(taskId)
          .withDataSourceSchema(dsSchema)
          .buildBaseWriter();

      return new SparkInternalRowWriter(writer);
    }
  }

  public static class TaskCommit implements WriterCommitMessage {
    private final DataFile[] taskFiles;

    TaskCommit(DataFile[] taskFiles) {
      this.taskFiles = taskFiles;
    }

    DataFile[] files() {
      return taskFiles;
    }
  }

  private static Iterable<DataFile> files(WriterCommitMessage[] messages) {
    if (messages.length > 0) {
      return Iterables.concat(Iterables.transform(Arrays.asList(messages), message -> message != null ?
          ImmutableList.copyOf(((TaskCommit) message).files()) :
          ImmutableList.of()));
    }
    return ImmutableList.of();
  }
}
