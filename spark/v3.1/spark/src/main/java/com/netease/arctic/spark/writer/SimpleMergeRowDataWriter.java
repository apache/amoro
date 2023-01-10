package com.netease.arctic.spark.writer;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import com.netease.arctic.spark.writer.merge.MergeWriter;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

public class SimpleMergeRowDataWriter implements MergeWriter<InternalRow> {
  final TaskWriter<InternalRow> writer;
  
  final StructType schema;
  final boolean isKeyedTable;

  public SimpleMergeRowDataWriter(TaskWriter<InternalRow> writer, StructType schema, boolean isKeyedTable) {
    this.writer = writer;
    this.schema = schema;
    this.isKeyedTable = isKeyedTable;
  }

  @Override
  public void delete(InternalRow row) throws IOException {
    SparkInternalRowCastWrapper delete = new SparkInternalRowCastWrapper(
        true, row, schema, ChangeAction.DELETE, isKeyedTable);
    writer.write(delete);
  }

  @Override
  public void update(InternalRow row) throws IOException {
    SparkInternalRowCastWrapper delete;
    SparkInternalRowCastWrapper insert;
    if (isKeyedTable) {
      delete = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.UPDATE_BEFORE, true);
      insert = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.UPDATE_AFTER, true);
    } else {
      delete = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.DELETE, false);
      insert = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.INSERT, false);
    }
    writer.write(delete);
    writer.write(insert);

  }

  @Override
  public void insert(InternalRow row) throws IOException {
    SparkInternalRowCastWrapper insert;
    if (isKeyedTable) {
      insert = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.INSERT, true);
    } else {
      insert = new SparkInternalRowCastWrapper(
          true, row, schema, ChangeAction.INSERT, false);
    }
    writer.write(insert);

  }

  @Override
  public WriterCommitMessage commit() throws IOException {
    WriteResult result = writer.complete();
    if (!isKeyedTable) {
      return new WriteTaskDeleteFilesCommit(result.deleteFiles(), result.dataFiles());
    }
    return new WriteTaskCommit(result.dataFiles());
  }

  @Override
  public void abort() throws IOException {
    if (this.writer != null) {
      this.writer.abort();
    }
  }

  @Override
  public void close() throws IOException {
    if (this.writer != null) {
      writer.close();
    }
  }
}
