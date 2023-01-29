package org.apache.spark.sql.catalyst.utils

import com.netease.arctic.spark.sql.utils.ProjectingInternalRow
import org.apache.spark.sql.catalyst.expressions.{Attribute, Expression}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.datasources.DataSourceStrategy
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType

object TranslateUtils {
  def translateFilter(deleteExpr: Expression): Option[Filter] = {
    DataSourceStrategy.translateFilter(deleteExpr, supportNestedPredicatePushdown = true)
  }
}
