package com.netease.arctic.spark.source;

import org.apache.spark.sql.sources.v2.ReadSupport;
import org.apache.spark.sql.sources.v2.WriteSupport;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.types.StructType;

public interface DataSourceTable extends ReadSupport, WriteSupport {

  DataSourceWriter createOverwriteWriter(StructType schema);

  DataSourceWriter createAppendWriter(StructType schema);
}
