package com.netease.arctic.spark.optimize;


public interface Transform extends Expression{
  String name();

  NamedReference[] references();

  Expression[] arguments();

}
