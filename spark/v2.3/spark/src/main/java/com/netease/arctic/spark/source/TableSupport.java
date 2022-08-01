package com.netease.arctic.spark.source;

import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.types.StructType;

public interface TableSupport {

  DataSourceTable createTable(TableIdentifier identifier, StructType schema);


  DataSourceTable loadTable(TableIdentifier identifier);

  boolean dropTable(TableIdentifier identifier);
}
