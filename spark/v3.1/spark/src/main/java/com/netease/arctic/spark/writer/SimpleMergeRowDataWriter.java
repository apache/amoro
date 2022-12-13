package com.netease.arctic.spark.writer;

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

  public SimpleMergeRowDataWriter(TaskWriter<InternalRow> writer, StructType schema) {
    this.writer = writer;
    this.schema = schema;
  }

  @Override
  public void delete(InternalRow row) throws IOException {
    // TODO: 2022/12/13  Constructing a delete row
    writer.write(row);
  }

  @Override
  public void update(InternalRow row) throws IOException {
    // TODO: 2022/12/13 Constructing a update row 
    writer.write(row);
  }

  @Override
  public void insert(InternalRow row) throws IOException {
    // TODO: 2022/12/13 Constructing a insert row 
    writer.write(row);
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
