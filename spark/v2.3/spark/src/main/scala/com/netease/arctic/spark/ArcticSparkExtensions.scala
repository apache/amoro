package com.netease.arctic.spark

import org.apache.spark.sql.SparkSessionExtensions

class ArcticSparkExtensions extends (SparkSessionExtensions => Unit){
  override def apply(extensions: SparkSessionExtensions): Unit = {

  }
}
