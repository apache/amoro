package com.netease.arctic.spark.source;

import org.apache.spark.sql.sources.DataSourceRegister;

public class ArcticSource implements DataSourceRegister {
  @Override
  public String shortName() {
    return "arctic";
  }
}
