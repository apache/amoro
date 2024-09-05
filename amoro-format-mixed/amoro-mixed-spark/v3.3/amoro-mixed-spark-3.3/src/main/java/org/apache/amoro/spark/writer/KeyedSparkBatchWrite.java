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

package org.apache.amoro.spark.writer;

import static org.apache.amoro.hive.op.UpdateHiveFiles.DELETE_UNTRACKED_HIVE_FILE;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;

import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.OperationConflictException;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.op.OverwriteBaseFiles;
import org.apache.amoro.op.RewritePartitions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.spark.io.TaskWriters;
import org.apache.amoro.spark.sql.utils.RowDeltaUtils;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.blocker.Blocker;
import org.apache.amoro.table.blocker.TableBlockerManager;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.Tasks;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.DataWriterFactory;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.PhysicalWriteInfo;
import org.apache.spark.sql.connector.write.Write;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyedSparkBatchWrite implements MixedFormatSparkWriteBuilder.MixedFormatWrite, Write {
  private final KeyedTable table;
  private final StructType dsSchema;

  private final long txId;
  private final String hiveSubdirectory;

  private final boolean orderedWriter;
  private final MixedFormatCatalog catalog;

  KeyedSparkBatchWrite(KeyedTable table, LogicalWriteInfo info, MixedFormatCatalog catalog) {
    this.table = table;
    this.dsSchema = info.schema();
    this.txId = table.beginTransaction(null);
    this.hiveSubdirectory = HiveTableUtil.newHiveSubdirectory(this.txId);
    this.orderedWriter =
        Boolean.parseBoolean(
            info.options().getOrDefault("writer.distributed-and-ordered", "false"));
    this.catalog = catalog;
  }

  @Override
  public BatchWrite asBatchAppend() {
    return new AppendWrite();
  }

  @Override
  public BatchWrite asDynamicOverwrite() {
    return new DynamicOverwrite();
  }

  @Override
  public BatchWrite asOverwriteByFilter(Expression overwriteExpr) {
    return new OverwriteByFilter(overwriteExpr);
  }

  @Override
  public BatchWrite asUpsertWrite() {
    return new UpsertWrite();
  }

  private abstract class BaseBatchWrite implements BatchWrite {

    protected TableBlockerManager tableBlockerManager;
    protected Blocker block;

    @Override
    public void abort(WriterCommitMessage[] messages) {
      try {
        Map<String, String> props = table.properties();
        Tasks.foreach(WriteTaskCommit.files(messages))
            .retry(
                PropertyUtil.propertyAsInt(props, COMMIT_NUM_RETRIES, COMMIT_NUM_RETRIES_DEFAULT))
            .exponentialBackoff(
                PropertyUtil.propertyAsInt(
                    props, COMMIT_MIN_RETRY_WAIT_MS, COMMIT_MIN_RETRY_WAIT_MS_DEFAULT),
                PropertyUtil.propertyAsInt(
                    props, COMMIT_MAX_RETRY_WAIT_MS, COMMIT_MAX_RETRY_WAIT_MS_DEFAULT),
                PropertyUtil.propertyAsInt(
                    props, COMMIT_TOTAL_RETRY_TIME_MS, COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT),
                2.0 /* exponential */)
            .throwFailureWhenFinished()
            .run(
                file -> {
                  table.io().deleteFile(file.path().toString());
                });
      } finally {
        tableBlockerManager.release(block);
      }
    }

    public void checkBlocker(TableBlockerManager tableBlockerManager) {
      List<String> blockerIds =
          tableBlockerManager.getBlockers().stream()
              .map(Blocker::blockerId)
              .collect(Collectors.toList());
      if (!blockerIds.contains(block.blockerId())) {
        throw new IllegalStateException("block is not in blockerManager");
      }
    }

    public void getBlocker() {
      this.tableBlockerManager = catalog.getTableBlockerManager(table.id());
      ArrayList<BlockableOperation> operations = Lists.newArrayList();
      operations.add(BlockableOperation.BATCH_WRITE);
      operations.add(BlockableOperation.OPTIMIZE);
      try {
        this.block = tableBlockerManager.block(operations);
      } catch (OperationConflictException e) {
        throw new IllegalStateException(
            "failed to block table " + table.id() + " with " + operations, e);
      }
    }
  }

  private class AppendWrite extends BaseBatchWrite {

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      getBlocker();
      return new ChangeWriteFactory(table, dsSchema, txId, orderedWriter);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      checkBlocker(tableBlockerManager);
      AppendFiles append = table.changeTable().newAppend();
      for (DataFile file : WriteTaskCommit.files(messages)) {
        append.appendFile(file);
      }
      append.commit();
      tableBlockerManager.release(block);
    }
  }

  private class DynamicOverwrite extends BaseBatchWrite {

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      getBlocker();
      return new BaseWriterFactory(table, dsSchema, txId, hiveSubdirectory, orderedWriter);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      checkBlocker(tableBlockerManager);
      RewritePartitions rewritePartitions = table.newRewritePartitions();
      rewritePartitions.updateOptimizedSequenceDynamically(txId);

      for (DataFile file : WriteTaskCommit.files(messages)) {
        rewritePartitions.addDataFile(file);
      }
      rewritePartitions.commit();
      tableBlockerManager.release(block);
    }
  }

  private class OverwriteByFilter extends BaseBatchWrite {
    private final Expression overwriteExpr;

    private OverwriteByFilter(Expression overwriteExpr) {
      this.overwriteExpr = overwriteExpr;
    }

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      getBlocker();
      return new BaseWriterFactory(table, dsSchema, txId, hiveSubdirectory, orderedWriter);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      checkBlocker(tableBlockerManager);
      OverwriteBaseFiles overwriteBaseFiles = table.newOverwriteBaseFiles();
      overwriteBaseFiles.overwriteByRowFilter(overwriteExpr);
      overwriteBaseFiles.updateOptimizedSequenceDynamically(txId);
      overwriteBaseFiles.set(DELETE_UNTRACKED_HIVE_FILE, "true");

      for (DataFile file : WriteTaskCommit.files(messages)) {
        overwriteBaseFiles.addFile(file);
      }
      overwriteBaseFiles.commit();
      tableBlockerManager.release(block);
    }
  }

  private class UpsertWrite extends BaseBatchWrite {

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      getBlocker();
      return new UpsertChangeFactory(table, dsSchema, txId, orderedWriter);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      checkBlocker(tableBlockerManager);
      AppendFiles append = table.changeTable().newAppend();
      for (DataFile file : WriteTaskCommit.files(messages)) {
        append.appendFile(file);
      }
      append.commit();
      tableBlockerManager.release(block);
    }
  }

  private abstract static class AbstractWriterFactory implements DataWriterFactory, Serializable {
    protected final KeyedTable table;
    protected final StructType dsSchema;
    protected final Long transactionId;
    protected final boolean orderedWrite;

    AbstractWriterFactory(
        KeyedTable table, StructType dsSchema, Long transactionId, boolean orderedWrite) {
      this.table = table;
      this.dsSchema = dsSchema;
      this.transactionId = transactionId;
      this.orderedWrite = orderedWrite;
    }

    public TaskWriter<InternalRow> newWriter(int partitionId, long taskId, StructType schema) {
      return TaskWriters.of(table)
          .withTransactionId(transactionId)
          .withPartitionId(partitionId)
          .withTaskId(taskId)
          .withDataSourceSchema(schema)
          .withOrderedWriter(orderedWrite)
          .newChangeWriter();
    }
  }

  private static class BaseWriterFactory extends AbstractWriterFactory {

    protected final String hiveSubdirectory;

    BaseWriterFactory(
        KeyedTable table,
        StructType dsSchema,
        Long transactionId,
        String hiveSubdirectory,
        boolean orderedWrite) {
      super(table, dsSchema, transactionId, orderedWrite);
      this.hiveSubdirectory = hiveSubdirectory;
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      TaskWriters writerBuilder =
          TaskWriters.of(table)
              .withTransactionId(transactionId)
              .withPartitionId(partitionId)
              .withTaskId(taskId)
              .withDataSourceSchema(dsSchema)
              .withOrderedWriter(orderedWrite)
              .withHiveSubdirectory(hiveSubdirectory);

      TaskWriter<InternalRow> writer = writerBuilder.newBaseWriter(true);
      return new SimpleInternalRowDataWriter(writer);
    }
  }

  private static class ChangeWriteFactory extends AbstractWriterFactory {

    ChangeWriteFactory(
        KeyedTable table, StructType dsSchema, long transactionId, boolean orderedWrite) {
      super(table, dsSchema, transactionId, orderedWrite);
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      return new SimpleRowLevelDataWriter(
          newWriter(partitionId, taskId, dsSchema),
          newWriter(partitionId, taskId, dsSchema),
          dsSchema,
          true);
    }
  }

  private static class UpsertChangeFactory extends AbstractWriterFactory {

    UpsertChangeFactory(
        KeyedTable table, StructType dsSchema, long transactionId, boolean orderedWrite) {
      super(table, dsSchema, transactionId, orderedWrite);
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      StructType schema =
          new StructType(
              Arrays.stream(dsSchema.fields())
                  .filter(field -> !field.name().equals(RowDeltaUtils.OPERATION_COLUMN()))
                  .toArray(StructField[]::new));
      return new SimpleRowLevelDataWriter(
          newWriter(partitionId, taskId, schema),
          newWriter(partitionId, taskId, schema),
          dsSchema,
          true);
    }
  }
}
