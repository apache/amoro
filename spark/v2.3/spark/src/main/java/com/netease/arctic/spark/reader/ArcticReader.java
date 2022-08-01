package com.netease.arctic.spark.reader;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.sources.v2.reader.DataReaderFactory;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.reader.Statistics;
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownFilters;
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownRequiredColumns;
import org.apache.spark.sql.sources.v2.reader.SupportsReportStatistics;
import org.apache.spark.sql.types.StructType;

import java.util.List;

public class ArcticReader implements DataSourceReader,
    SupportsPushDownFilters, SupportsPushDownRequiredColumns, SupportsReportStatistics {
  @Override
  public Filter[] pushFilters(Filter[] filters) {
    return new Filter[0];
  }

  @Override
  public Filter[] pushedFilters() {
    return new Filter[0];
  }

  @Override
  public void pruneColumns(StructType requiredSchema) {

  }

  @Override
  public Statistics getStatistics() {
    return null;
  }

  @Override
  public StructType readSchema() {
    return null;
  }

  @Override
  public List<DataReaderFactory<Row>> createDataReaderFactories() {
    return null;
  }
}
