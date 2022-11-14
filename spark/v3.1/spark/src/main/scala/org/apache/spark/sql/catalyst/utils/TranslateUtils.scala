package org.apache.spark.sql.catalyst.utils

import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.execution.datasources.DataSourceStrategy
import org.apache.spark.sql.sources.Filter

object TranslateUtils {
  def translateFilter(deleteExpr: Expression): Option[Filter] = {
    DataSourceStrategy.translateFilter(deleteExpr, supportNestedPredicatePushdown = true)
  }
}
