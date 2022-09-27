package com.netease.arctic.spark.optimize;

import org.apache.spark.annotation.InterfaceStability;

@InterfaceStability.Evolving
public interface Expression {

  String describe();
}
