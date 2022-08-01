package com.netease.arctic.spark

import com.netease.arctic.spark.sql.catalyst.analysis.ArcticResolutionDelegateHiveRule
import org.apache.spark.sql.SparkSessionExtensions

class ArcticSparkExtensions extends (SparkSessionExtensions => Unit){
  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectResolutionRule(ArcticResolutionDelegateHiveRule)
  }
}
