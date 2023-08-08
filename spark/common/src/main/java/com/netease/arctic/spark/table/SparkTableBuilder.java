package com.netease.arctic.spark.table;

import com.netease.arctic.table.ArcticTable;
import org.apache.spark.sql.connector.catalog.Table;

public interface SparkTableBuilder {
  Table build(ArcticTable table, String internalTable);

}
