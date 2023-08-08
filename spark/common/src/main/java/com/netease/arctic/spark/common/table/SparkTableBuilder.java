package com.netease.arctic.spark.common.table;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.spark.sql.connector.catalog.Table;

import java.util.Map;

public class SparkTableBuilder {

  Map<TableFormat, SparkDataSourceTableBuilder> tableBuilderMap = Maps.newConcurrentMap();
  ArcticCatalog catalog;

  public SparkTableBuilder(ArcticCatalog catalog) {
    this.catalog = catalog;
  }

  public void registerTableBuilder(TableFormat format, SparkDataSourceTableBuilder builder) {
    Preconditions.checkNotNull(builder, "table builder is null");
    tableBuilderMap.put(format, builder);
  }

  public Table build(ArcticTable table) {
    SparkDataSourceTableBuilder builder = tableBuilderMap.get(table.format());
    if (builder != null) {
      return builder.build(table);
    }
    throw new IllegalArgumentException("table format " + table.format() + " is not supported");
  }
}
