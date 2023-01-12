package com.netease.arctic.spark.writer;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

public class SimpleMergeRowDataWriter implements RowLevelWriter<InternalRow> {
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
    writer.write(new SparkInternalRowCastWrapper(row, ChangeAction.DELETE));
  }

  @Override
  public void update(InternalRow updateBefore, InternalRow updateAfter) throws IOException {
    SparkInternalRowCastWrapper delete;
    SparkInternalRowCastWrapper insert;
    if (isKeyedTable) {
      delete = new SparkInternalRowCastWrapper(updateBefore, ChangeAction.UPDATE_BEFORE);
      insert = new SparkInternalRowCastWrapper(updateAfter, ChangeAction.UPDATE_AFTER);
    } else {
      delete = new SparkInternalRowCastWrapper(updateBefore, ChangeAction.DELETE);
      insert = new SparkInternalRowCastWrapper(updateAfter, ChangeAction.INSERT);
    }
    writer.write(delete);
    writer.write(insert);

  }

  @Override
  public void insert(InternalRow row) throws IOException {
    writer.write(new SparkInternalRowCastWrapper(row, ChangeAction.INSERT));

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
