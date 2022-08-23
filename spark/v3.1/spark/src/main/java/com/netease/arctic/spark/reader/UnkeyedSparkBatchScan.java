package com.netease.arctic.spark.reader;

import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.spark.sql.connector.read.Batch;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;
import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.Statistics;
import org.apache.spark.sql.connector.read.SupportsReportStatistics;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.List;

// TODO: AdaptHiveRead/Writer
public class UnkeyedSparkBatchScan implements Scan, Batch, SupportsReportStatistics {

  UnkeyedSparkBatchScan(
      UnkeyedTable table, boolean caseSensitive,
      Schema expectedSchema, List<Expression> filters, CaseInsensitiveStringMap options) {

  }

  @Override
  public InputPartition[] planInputPartitions() {
    return new InputPartition[0];
  }

  @Override
  public PartitionReaderFactory createReaderFactory() {
    return null;
  }

  @Override
  public Statistics estimateStatistics() {
    return null;
  }

  @Override
  public StructType readSchema() {
    return null;
  }

  @Override
  public Batch toBatch() {
    return this;
  }
}
