package com.netease.arctic.spark.optimize;

public interface NamedReference extends Expression {
  String[] fieldNames();
}
