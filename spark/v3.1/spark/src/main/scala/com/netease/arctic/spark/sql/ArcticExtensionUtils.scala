package com.netease.arctic.spark.sql

import com.netease.arctic.spark.table.ArcticSparkTable
import org.apache.spark.sql.catalog.Table
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, SubqueryAlias}
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation


object ArcticExtensionUtils {

  implicit class ArcticTableHelper(table: Table) {

  }

  def isArcticRelation(plan: LogicalPlan): Boolean = {
    def isArcticTable(relation: DataSourceV2Relation): Boolean = relation.table match {
      case _: ArcticSparkTable => true
      case _ => false
    }

    plan match {
      case s: SubqueryAlias => isArcticRelation(s.child)
      case r: DataSourceV2Relation => isArcticTable(r)
      case _ => false
    }
  }
}
