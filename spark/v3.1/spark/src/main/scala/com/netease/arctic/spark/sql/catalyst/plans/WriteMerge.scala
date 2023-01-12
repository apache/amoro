package com.netease.arctic.spark.sql.catalyst.plans

import com.netease.arctic.spark.sql.utils.WriteQueryProjections
import org.apache.spark.sql.catalyst.analysis.NamedRelation
import org.apache.spark.sql.catalyst.plans.logical.{Command, LogicalPlan, V2WriteCommand}

case class WriteMerge(table: NamedRelation,
                      query: LogicalPlan,
                      options: Map[String, String],
                      projections: WriteQueryProjections) extends V2WriteCommand {

  def isByName: Boolean = false

  def withNewQuery(newQuery: LogicalPlan): WriteMerge = copy(query = newQuery)

  def withNewTable(newTable: NamedRelation): WriteMerge = copy(table = newTable)

  override def outputResolved = true
}
