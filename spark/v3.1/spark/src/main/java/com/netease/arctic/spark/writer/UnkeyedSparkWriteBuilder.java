package com.netease.arctic.spark.writer;

import com.netease.arctic.table.UnkeyedTable;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.SupportsDynamicOverwrite;
import org.apache.spark.sql.connector.write.SupportsOverwrite;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.sources.Filter;

// TODO: AdaptHiveRead/Writer
public class UnkeyedSparkWriteBuilder implements WriteBuilder, SupportsDynamicOverwrite, SupportsOverwrite {

  public UnkeyedSparkWriteBuilder(UnkeyedTable table, LogicalWriteInfo info) {

  }

  @Override
  public WriteBuilder overwriteDynamicPartitions() {
    return null;
  }

  @Override
  public WriteBuilder overwrite(Filter[] filters) {
    return null;
  }

  @Override
  public BatchWrite buildForBatch() {
    return null;
  }
}
