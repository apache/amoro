package com.netease.arctic.spark.sql.optimize

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.logical.{AppendData, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule

case class RewriteAppendArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import com.netease.arctic.spark.sql.ArcticExtensionUtils._

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case i @ AppendData(table, query, writeOptions, isByName) if isArcticRelation(table) =>
      // TODO: support arctic insert rules
      i
  }
}
