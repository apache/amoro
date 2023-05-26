package com.netease.arctic.spark.test.helper;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.List;

public class ScalaTestHelper {


  public static <T> Seq<T> seq(List<T> values) {
    return JavaConverters.asScalaBufferConverter(values).asScala().seq();
  }


}
