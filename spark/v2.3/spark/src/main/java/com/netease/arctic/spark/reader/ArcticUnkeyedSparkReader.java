package com.netease.arctic.spark.reader;

import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.sources.v2.reader.DataReaderFactory;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.reader.Statistics;
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownFilters;
import org.apache.spark.sql.sources.v2.reader.SupportsPushDownRequiredColumns;
import org.apache.spark.sql.sources.v2.reader.SupportsReportStatistics;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ArcticUnkeyedSparkReader implements DataSourceReader,
    SupportsPushDownFilters, SupportsPushDownRequiredColumns, SupportsReportStatistics {

  private static final Logger LOG = LoggerFactory.getLogger(ArcticUnkeyedSparkReader.class);
  private static final Filter[] NO_FILTERS = new Filter[0];
  private final UnkeyedTable table;
  private Schema schema = null;
  private StructType requestedProjection;
  private final boolean caseSensitive;
  private List<Expression> filterExpressions = null;
  private Filter[] pushedFilters = NO_FILTERS;

  private StructType readSchema = null;
  private List<CombinedScanTask> tasks = null;
  private final Schema expectedSchema;

  public ArcticUnkeyedSparkReader(SparkSession spark, UnkeyedTable table) {
    this.table = table;
    this.caseSensitive = Boolean.parseBoolean(spark.conf().get("spark.sql.caseSensitive"));
    this.expectedSchema = lazySchema();
  }

  private Schema lazySchema() {
    if (schema == null) {
      if (requestedProjection != null) {
        // the projection should include all columns that will be returned,
        // including those only used in filters
        this.schema = SparkSchemaUtil.prune(table.schema(),
            requestedProjection, filterExpression(), caseSensitive);
      } else {
        this.schema = table.schema();
      }
    }
    return schema;
  }

  private Expression filterExpression() {
    if (filterExpressions != null) {
      return filterExpressions.stream().reduce(Expressions.alwaysTrue(), Expressions::and);
    }
    return Expressions.alwaysTrue();
  }


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
