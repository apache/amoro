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

import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.spark.io.TaskWriters;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.IdGenerator;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.Tasks;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.DataWriterFactory;
import org.apache.spark.sql.connector.write.PhysicalWriteInfo;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import static com.netease.arctic.spark.writer.WriteTaskCommit.files;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;

public class UnkeyedSparkBatchWrite implements ArcticSparkWriteBuilder.ArcticWrite {

  private final UnkeyedTable table;
  private final StructType dsSchema;
  private final long transactionId = IdGenerator.randomId();
  private final String hiveSubdirectory = HiveTableUtil.newHiveSubdirectory(transactionId);

  public UnkeyedSparkBatchWrite(UnkeyedTable table, StructType dsSchema) {
    this.table = table;
    this.dsSchema = dsSchema;
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

  private class AppendWrite extends BaseBatchWrite {

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      return new WriterFactory(table, dsSchema, false, transactionId, null);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      AppendFiles appendFiles = table.newAppend();
      for (DataFile file : files(messages)) {
        appendFiles.appendFile(file);
      }
      appendFiles.commit();
    }
  }

  private class DynamicOverwrite extends BaseBatchWrite {

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      return new WriterFactory(table, dsSchema, true, transactionId, hiveSubdirectory);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      ReplacePartitions replacePartitions = table.newReplacePartitions();
      for (DataFile file : files(messages)) {
        replacePartitions.addFile(file);
      }
      replacePartitions.commit();
    }
  }

  private class OverwriteByFilter extends BaseBatchWrite {
    private final Expression overwriteExpr;

    private OverwriteByFilter(Expression overwriteExpr) {
      this.overwriteExpr = overwriteExpr;
    }

    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      return new WriterFactory(table, dsSchema, true, transactionId, hiveSubdirectory);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      OverwriteFiles overwriteFiles = table.newOverwrite();
      overwriteFiles.overwriteByRowFilter(overwriteExpr);
      for (DataFile file : files(messages)) {
        overwriteFiles.addFile(file);
      }
      overwriteFiles.commit();
    }
  }

  private class UpsertWrite extends BaseBatchWrite {
    @Override
    public DataWriterFactory createBatchWriterFactory(PhysicalWriteInfo info) {
      return new DeltaUpsertWriteFactory(table, dsSchema, transactionId);
    }

    @Override
    public void commit(WriterCommitMessage[] messages) {
      RowDelta rowDelta = table.newRowDelta();
      if (WriteTaskDeleteFilesCommit.deleteFiles(messages).iterator().hasNext()) {
        for (DeleteFile file : WriteTaskDeleteFilesCommit.deleteFiles(messages)) {
          rowDelta.addDeletes(file);
        }
        rowDelta.commit();
      }


      AppendFiles appendFiles = table.newAppend();
      if (WriteTaskDeleteFilesCommit.dataFiles(messages).iterator().hasNext()) {
        for (DataFile file : WriteTaskDeleteFilesCommit.dataFiles(messages)) {
          appendFiles.appendFile(file);
        }
        appendFiles.commit();
      }
    }
  }

  private static class WriterFactory implements DataWriterFactory, Serializable {
    protected final UnkeyedTable table;
    protected final StructType dsSchema;

    protected final long transactionId;
    protected final String hiveSubdirectory;

    protected final boolean isOverwrite;

    WriterFactory(UnkeyedTable table,
                  StructType dsSchema,
                  boolean isOverwrite,
                  long transactionId,
                  String hiveSubdirectory) {
      this.table = table;
      this.dsSchema = dsSchema;
      this.isOverwrite = isOverwrite;
      this.transactionId = transactionId;
      this.hiveSubdirectory = hiveSubdirectory;
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      TaskWriter<InternalRow> writer = TaskWriters.of(table)
          .withPartitionId(partitionId)
          .withTransactionId(transactionId)
          .withTaskId(taskId)
          .withDataSourceSchema(dsSchema)
          .withHiveSubdirectory(hiveSubdirectory)
          .newBaseWriter(this.isOverwrite);
      return new SimpleInternalRowDataWriter(writer);
    }
  }

  private static class DeltaUpsertWriteFactory extends WriterFactory {

    DeltaUpsertWriteFactory(UnkeyedTable table, StructType dsSchema, long transactionId) {
      super(table, dsSchema, false, transactionId, null);
    }

    @Override
    public DataWriter<InternalRow> createWriter(int partitionId, long taskId) {
      // TODO: issues-173 - support upsert data writer
      StructType schema = new StructType(Arrays.stream(dsSchema.fields()).filter(f -> !f.name().equals("_file") &&
          !f.name().equals("_pos") && !f.name().equals("_arctic_upsert_op")).toArray(StructField[]::new));
      UnkeyedPosDeleteSparkWriter<InternalRow> internalRowUnkeyedPosDeleteSparkWriter = TaskWriters.of(table)
          .withPartitionId(partitionId)
          .withTransactionId(transactionId)
          .withTaskId(taskId)
          .withDataSourceSchema(schema)
          .newBasePosDeleteWriter();

      return new SimpleUnkeyedUpsertDataWriter(internalRowUnkeyedPosDeleteSparkWriter, dsSchema);
    }
  }
}
