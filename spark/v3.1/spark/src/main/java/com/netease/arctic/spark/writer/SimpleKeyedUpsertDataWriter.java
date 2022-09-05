package com.netease.arctic.spark.writer;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.spark.SparkInternalRowCastWrapper;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.WriterCommitMessage;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;
import java.util.Arrays;

public class SimpleKeyedUpsertDataWriter implements DataWriter<InternalRow> {
  final TaskWriter<InternalRow> writer;
  final StructType schema;

  public SimpleKeyedUpsertDataWriter(TaskWriter<InternalRow> writer, StructType schemaNum) {
    this.writer = writer;
    this.schema = schemaNum;
  }

  @Override
  public void write(InternalRow record) throws IOException {
    if (schema != null && !isDelete(schema)) {
      SparkInternalRowCastWrapper insert = new SparkInternalRowCastWrapper(record, schema, ChangeAction.INSERT, false);
      SparkInternalRowCastWrapper delete = new SparkInternalRowCastWrapper(record, schema, ChangeAction.DELETE, false);
      if (delete.getRow() != null) {
        writer.write(delete);
      }
      writer.write(insert);
    } else if (schema != null && isDelete(schema)) {
      SparkInternalRowCastWrapper delete = new SparkInternalRowCastWrapper(record, schema, ChangeAction.DELETE, true);
      writer.write(delete);
    } else {
      writer.write(record);
    }
  }

  private boolean isDelete(StructType schema) {
    return Arrays.stream(schema.fieldNames()).findFirst().get().equals("_arctic_upsert_op");
  }

  @Override
  public WriterCommitMessage commit() throws IOException {
    WriteResult result = writer.complete();
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