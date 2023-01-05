package com.netease.arctic.spark.sql.catalyst.analysis

import org.apache.spark.sql.catalyst.expressions.{AttributeReference, ExprId}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

import scala.collection.mutable

trait RewriteRowLevelCommand extends Rule[LogicalPlan] {

  protected def buildRelationWithAttrs(
                                        relation: DataSourceV2Relation,
                                        table: Table): DataSourceV2Relation = {

    val attrs = dedupAttrs(relation.output)
    relation.copy(table = table, output = attrs)
  }

  protected def dedupAttrs(attrs: Seq[AttributeReference]): Seq[AttributeReference] = {
    val exprIds = mutable.Set.empty[ExprId]
    attrs.flatMap { attr =>
      if (exprIds.contains(attr.exprId)) {
        None
      } else {
        exprIds += attr.exprId
        Some(attr)
      }
    }
  }
}
