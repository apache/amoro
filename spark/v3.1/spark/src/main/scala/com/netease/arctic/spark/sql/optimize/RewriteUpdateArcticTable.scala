package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.sql.ArcticExtensionUtils.isArcticRelation
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, UpdateTable}
import org.apache.spark.sql.catalyst.rules.Rule

case class RewriteUpdateArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case d@UpdateTable(table, assignments, condition) if isArcticRelation(table) =>
      // TODO: support arctic update rules
      d
  }
}
