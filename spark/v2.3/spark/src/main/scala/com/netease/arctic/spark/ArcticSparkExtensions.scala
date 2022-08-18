package com.netease.arctic.spark

import com.netease.arctic.spark.sql.catalyst.analysis.ArcticResolutionDelegateHiveRule
import com.netease.arctic.spark.sql.catalyst.parser.ArcticSqlExtensionsParser
import org.apache.spark.sql.SparkSessionExtensions

class ArcticSparkExtensions extends (SparkSessionExtensions => Unit){
  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectParser {
      case (_, parser) => new ArcticSqlExtensionsParser(parser)
    }

    extensions.injectResolutionRule(ArcticResolutionDelegateHiveRule)
  }
}
