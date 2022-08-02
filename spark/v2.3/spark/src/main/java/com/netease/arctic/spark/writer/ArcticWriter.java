package com.netease.arctic.spark.writer;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.sources.v2.writer.DataWriterFactory;
import org.apache.spark.sql.sources.v2.writer.SupportsWriteInternalRow;
import org.apache.spark.sql.sources.v2.writer.WriterCommitMessage;

public class ArcticWriter implements DataSourceWriter, SupportsWriteInternalRow {

  @Override
  public DataWriterFactory<InternalRow> createInternalRowWriterFactory() {
    return null;
  }

  @Override
  public void commit(WriterCommitMessage[] messages) {

  }

  @Override
  public void abort(WriterCommitMessage[] messages) {

  }
}
