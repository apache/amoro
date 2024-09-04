/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.spark.sql.catalyst.optimize

import java.util

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.amoro.catalyst.MixedFormatSpark32Helper
import org.apache.spark.sql.catalyst.expressions.{Alias, And, Attribute, AttributeReference, Cast, EqualTo, Expression, GreaterThan, Literal}
import org.apache.spark.sql.catalyst.expressions.aggregate.{AggregateExpression, Complete, Count}
import org.apache.spark.sql.catalyst.plans.RightOuter
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation
import org.apache.spark.sql.types.LongType

import org.apache.amoro.spark.sql.catalyst.plans._
import org.apache.amoro.spark.sql.utils.{ProjectingInternalRow, WriteQueryProjections}
import org.apache.amoro.spark.sql.utils.RowDeltaUtils.{OPERATION_COLUMN, UPDATE_OPERATION}
import org.apache.amoro.spark.table.MixedSparkTable
import org.apache.amoro.spark.writer.WriteMode

case class RewriteAppendMixedFormatTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import org.apache.amoro.spark.sql.MixedFormatExtensionUtils._

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case AppendData(r: DataSourceV2Relation, query, writeOptions, _, _)
        if isMixedFormatRelation(r) && isUpsert(r) =>
      val upsertQuery = rewriteAppendAsUpsertQuery(r, query)
      val insertQuery = Project(
        Seq(Alias(Literal(UPDATE_OPERATION), OPERATION_COLUMN)()) ++ upsertQuery.output,
        upsertQuery)
      val insertAttribute =
        insertQuery.output.filter(_.name.contains("_mixed_before_"))
      val projections = buildInsertProjections(insertQuery, insertAttribute, isUpsert = true)
      val upsertOptions = writeOptions + (WriteMode.WRITE_MODE_KEY -> WriteMode.UPSERT.mode)
      val writeBuilder =
        MixedFormatSpark32Helper.newWriteBuilder(r.table, query.schema, upsertOptions)
      val write = writeBuilder.build()
      MixedFormatRowLevelWrite(r, insertQuery, upsertOptions, projections, Some(write))
  }

  def buildInsertProjections(
      plan: LogicalPlan,
      targetRowAttrs: Seq[Attribute],
      isUpsert: Boolean): WriteQueryProjections = {
    val (frontRowProjection, backRowProjection) = if (isUpsert) {
      val frontRowProjection =
        Some(ProjectingInternalRow.newProjectInternalRow(plan, targetRowAttrs, isFront = true, 0))
      val backRowProjection =
        ProjectingInternalRow.newProjectInternalRow(plan, targetRowAttrs, isFront = false, 0)
      (frontRowProjection, backRowProjection)
    } else {
      val backRowProjection =
        ProjectingInternalRow.newProjectInternalRow(plan, targetRowAttrs, isFront = true, 0)
      (null, backRowProjection)
    }
    WriteQueryProjections(frontRowProjection, backRowProjection)
  }

  def buildValidatePrimaryKeyDuplication(
      r: DataSourceV2Relation,
      query: LogicalPlan): LogicalPlan = {
    r.table match {
      case mixedSparkTable: MixedSparkTable =>
        if (mixedSparkTable.table().isKeyedTable) {
          val primaries = mixedSparkTable.table().asKeyedTable().primaryKeySpec().fieldNames()
          val than = GreaterThan(
            AggregateExpression(Count(Literal(1)), Complete, isDistinct = false),
            Cast(Literal(1), LongType))
          val alias = Alias(than, "count")()
          val attributes = query.output.filter(p => primaries.contains(p.name))
          Aggregate(attributes, Seq(alias), query)
        } else {
          throw new UnsupportedOperationException(s"UnKeyed table can not validate")
        }
    }
  }

  def isUpsert(relation: DataSourceV2Relation): Boolean = {
    val upsertWrite = relation.table.asUpsertWrite
    upsertWrite.appendAsUpsert()
  }

  def buildJoinCondition(
      primaries: util.List[String],
      tableScan: LogicalPlan,
      insertPlan: LogicalPlan): Expression = {
    var i = 0
    var joinCondition: Expression = null
    val expressions = new util.ArrayList[Expression]
    while (i < primaries.size) {
      val primary = primaries.get(i)
      val primaryAttr = insertPlan.output.find(_.name == primary).get
      val joinAttribute =
        tableScan.output.find(_.name.replace("_mixed_before_", "") == primary).get
      val experssion = EqualTo(primaryAttr, joinAttribute)
      expressions.add(experssion)
      i += 1
    }
    expressions.forEach(experssion => {
      if (joinCondition == null) {
        joinCondition = experssion
      } else {
        joinCondition = And(joinCondition, experssion)
      }
    })
    joinCondition
  }

  def rewriteAppendAsUpsertQuery(
      r: DataSourceV2Relation,
      query: LogicalPlan): LogicalPlan = {
    r.table match {
      case mixedSparkTable: MixedSparkTable =>
        if (mixedSparkTable.table().isKeyedTable) {
          val primaries = mixedSparkTable.table().asKeyedTable().primaryKeySpec().fieldNames()
          val tablePlan = buildKeyedTableBeforeProject(r)
          // val insertPlan = buildKeyedTableInsertProjection(query)
          val joinCondition = buildJoinCondition(primaries, tablePlan, query)
          Join(tablePlan, query, RightOuter, Some(joinCondition), JoinHint.NONE)
        } else {
          query
        }
    }
  }

  private def buildKeyedTableInsertProjection(relation: LogicalPlan): LogicalPlan = {
    val output = relation.output
    val outputWithValues = output.map(a => {
      Alias(a, "_mixed_after_" + a.name)()
    })
    Project(outputWithValues, relation)
  }

  private def buildKeyedTableBeforeProject(relation: DataSourceV2Relation): LogicalPlan = {
    val output = relation.output
    val outputWithValues = output.map(a => {
      Alias(a, "_mixed_before_" + a.name)()
    })
    Project(outputWithValues, relation)
  }
}
