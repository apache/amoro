package com.netease.arctic.spark.source;

import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.types.StructType;

import java.util.Optional;

public class ArcticSparkTable implements DataSourceTable {
  private final ArcticTable arcticTable;
  private final StructType requestedSchema;
  private final boolean refreshEagerly;
  private StructType lazyTableSchema = null;

  public static DataSourceTable ofArcticTable(ArcticTable table) {
    return new ArcticSparkTable(table, false);
  }

  public ArcticSparkTable(ArcticTable arcticTable, boolean refreshEagerly) {
    this(arcticTable, null, refreshEagerly);
  }

  public ArcticSparkTable(ArcticTable arcticTable, StructType requestedSchema, boolean refreshEagerly) {
    this.arcticTable = arcticTable;
    this.requestedSchema = requestedSchema;
    this.refreshEagerly = refreshEagerly;
    if (requestedSchema != null) {
      // convert the requested schema to throw an exception if any requested fields are unknown
      SparkSchemaUtil.convert(arcticTable.schema(), requestedSchema);
    }
  }


  @Override
  public StructType schema() {
    if (lazyTableSchema == null) {
      Schema tableSchema = arcticTable.schema();
      if (requestedSchema != null) {
        Schema prunedSchema = SparkSchemaUtil.prune(tableSchema, requestedSchema);
        this.lazyTableSchema = SparkSchemaUtil.convert(prunedSchema);
      } else {
        this.lazyTableSchema = SparkSchemaUtil.convert(tableSchema);
      }
    }
    return lazyTableSchema;
  }

  @Override
  public DataSourceReader createReader(DataSourceOptions options) {
    return null;
  }

  @Override
  public Optional<DataSourceWriter> createWriter(String jobId, StructType schema,
                                                 SaveMode mode, DataSourceOptions options) {
    return Optional.empty();
  }
}
