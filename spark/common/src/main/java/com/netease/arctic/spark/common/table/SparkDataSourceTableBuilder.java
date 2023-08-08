package com.netease.arctic.spark.common.table;

import com.netease.arctic.table.ArcticTable;
import org.apache.spark.sql.connector.catalog.Table;

public interface SparkDataSourceTableBuilder {
  Table build(ArcticTable table);
}
