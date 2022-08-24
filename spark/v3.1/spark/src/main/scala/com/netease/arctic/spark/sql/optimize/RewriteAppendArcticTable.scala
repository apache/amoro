package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.table.SupportsUpsert
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.plans.Inner
import org.apache.spark.sql.catalyst.plans.logical.{AppendData, Join, JoinHint, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

case class RewriteAppendArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import com.netease.arctic.spark.sql.ArcticExtensionUtils._

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case i @ AppendData(r: DataSourceV2Relation, query, writeOptions, isByName) if isArcticRelation(r) =>
      val upsertWrite = r.table.asUpsertWrite
      if(upsertWrite.appendAsUpsert()){

      }
      i
  }

  def rewriteAppendAsUpsertQuery(r: DataSourceV2Relation,
                                 upsertWrite: SupportsUpsert,
                                 query: LogicalPlan): LogicalPlan = {
    // val joinCondition = upsertWrite.joinCondition()
    val join = Join(r, query, Inner, None, JoinHint.NONE)
    query
  }

}
