package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.sql.ArcticExtensionUtils.isArcticRelation
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.logical.{AppendData, DeleteFromTable, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule

case class RewriteDeleteFromArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case d @ DeleteFromTable(table, condition) if isArcticRelation(table) =>
      // TODO: support arctic delete rules
      d
  }
}
