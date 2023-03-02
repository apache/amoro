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

package com.netease.arctic.spark.sql.catalyst.optimize

import com.netease.arctic.spark.sql.catalyst.plans._
import com.netease.arctic.spark.sql.utils.RowDeltaUtils.{INSERT_OPERATION, OPERATION_COLUMN, UPDATE_OPERATION}
import com.netease.arctic.spark.sql.utils.{ProjectingInternalRow, WriteQueryProjections}
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.WriteMode
import com.netease.arctic.spark.{ArcticSparkCatalog, SparkSQLProperties}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.aggregate.{AggregateExpression, Complete, Count}
import org.apache.spark.sql.catalyst.expressions.{Alias, And, Attribute, AttributeReference, EqualNullSafe, EqualTo, Expression, GreaterThan, Literal}
import org.apache.spark.sql.catalyst.plans.RightOuter
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.DataSourceAnalysis.resolver
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

import java.util

case class RewriteAppendArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import com.netease.arctic.spark.sql.ArcticExtensionUtils._

  def buildInsertProjections(plan: LogicalPlan, targetRowAttrs: Seq[AttributeReference],
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

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case a @ AppendData(r: DataSourceV2Relation, query, writeOptions, _) if isArcticRelation(r) =>
      val arcticRelation = asTableRelation(r)
      val upsertWrite = arcticRelation.table.asUpsertWrite
      val (newQuery, options, projections) = if (upsertWrite.appendAsUpsert()) {
        val upsertQuery = rewriteAppendAsUpsertQuery(r, query)
        val insertQuery = Project(Seq(Alias(Literal(UPDATE_OPERATION), OPERATION_COLUMN)()) ++ upsertQuery.output, upsertQuery)
        val projections = buildInsertProjections(insertQuery, r.output, isUpsert = true)
        val upsertOptions = writeOptions + (WriteMode.WRITE_MODE_KEY -> WriteMode.UPSERT.mode)
        (insertQuery, upsertOptions, projections)
      } else {
        val insertQuery = Project(Seq(Alias(Literal(INSERT_OPERATION), OPERATION_COLUMN)()) ++ query.output, query)
        val projections = buildInsertProjections(insertQuery, r.output, isUpsert = false)
        (insertQuery, writeOptions, projections)
      }

      arcticRelation.table match {
        case tbl: ArcticSparkTable =>
          if (tbl.table().isKeyedTable) {
            if (checkDuplicatesEnabled()) {
              val writeOption = options + ("optimize.enabled" -> "true")
              val validateQuery = buildValidatePrimaryKeyDuplication(r, query)
              val checkDataQuery = DynamicArcticFilterWithCardinalityCheck(newQuery, validateQuery)
              ArcticRowLevelWrite(arcticRelation, checkDataQuery, writeOption, projections)
            } else {
              ArcticRowLevelWrite(arcticRelation, newQuery, options, projections)
            }
          } else {
            a
          }
      }
    case a @ OverwritePartitionsDynamic(r: DataSourceV2Relation, query, writeOptions, _)
      if checkDuplicatesEnabled() =>
      val arcticRelation = asTableRelation(r)
      arcticRelation.table match {
        case table: ArcticSparkTable =>
          if (table.table().isKeyedTable) {
            val validateQuery = buildValidatePrimaryKeyDuplication(r, query)
            val checkDataQuery = DynamicArcticFilterWithCardinalityCheck(query, validateQuery)
            a.copy(query = checkDataQuery)
          } else {
            a
          }
        case _ =>
          a
      }

    case a @ OverwriteByExpression(r: DataSourceV2Relation, deleteExpr, query, writeOptions, _)
      if checkDuplicatesEnabled() =>
      val arcticRelation = asTableRelation(r)
      arcticRelation.table match {
        case table: ArcticSparkTable =>
          if (table.table().isKeyedTable) {
            val validateQuery = buildValidatePrimaryKeyDuplication(r, query)
            var finalExpr: Expression = deleteExpr
            deleteExpr match {
              case expr: EqualNullSafe =>
                finalExpr = expr.copy(query.output.last, expr.right)
              case _ =>
            }
            val checkDataQuery = DynamicArcticFilterWithCardinalityCheck(query, validateQuery)
            a.copy(query = checkDataQuery)

          } else {
            a
          }
        case _ =>
          a
      }

    case c @ CreateTableAsSelect(catalog, ident, parts, query, props, options, ifNotExists)
      if checkDuplicatesEnabled() =>
      catalog match {
        case _: ArcticSparkCatalog =>
          if (props.contains("primary.keys")) {
            val primaries = props("primary.keys").split(",")
            val validateQuery = buildValidatePrimaryKeyDuplication(primaries, query)
            val checkDataQuery = DynamicArcticFilterWithCardinalityCheck(query, validateQuery)
            c.copy(query = checkDataQuery)
          } else {
            c
          }
        case _ =>
          c
      }
  }

  def buildValidatePrimaryKeyDuplication(r: DataSourceV2Relation, query: LogicalPlan): LogicalPlan = {
    r.table match {
      case arctic: ArcticSparkTable =>
        if (arctic.table().isKeyedTable) {
          val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
          val attributes = query.output.filter(p => primaries.contains(p.name))
          val aggSumCol = Alias(AggregateExpression(Count(Literal(1)), Complete, isDistinct = false), SUM_ROW_ID_ALIAS_NAME)()
          val aggPlan = Aggregate(attributes, Seq(aggSumCol), query)
          val sumAttr = findOutputAttr(aggPlan.output, SUM_ROW_ID_ALIAS_NAME)
          val havingExpr = GreaterThan(sumAttr, Literal(1L))
          Filter(havingExpr, aggPlan)
        } else {
          throw new UnsupportedOperationException(s"UnKeyed table can not validate")
        }
    }
  }

  def buildValidatePrimaryKeyDuplication(primaries: Array[String], query: LogicalPlan): LogicalPlan = {
    val attributes = query.output.filter(p => primaries.contains(p.name))
    val aggSumCol = Alias(AggregateExpression(Count(Literal(1)), Complete, isDistinct = false), SUM_ROW_ID_ALIAS_NAME)()
    val aggPlan = Aggregate(attributes, Seq(aggSumCol), query)
    val sumAttr = findOutputAttr(aggPlan.output, SUM_ROW_ID_ALIAS_NAME)
    val havingExpr = GreaterThan(sumAttr, Literal(1L))
    Filter(havingExpr, aggPlan)
  }

  protected def findOutputAttr(attrs: Seq[Attribute], attrName: String): Attribute = {
    attrs.find(attr => resolver(attr.name, attrName)).getOrElse {
      throw new UnsupportedOperationException(s"Cannot find $attrName in $attrs")
    }
  }

  private final val SUM_ROW_ID_ALIAS_NAME = "_sum_"

  def checkDuplicatesEnabled(): Boolean = {
    java.lang.Boolean.valueOf(spark.sessionState.conf.
      getConfString(
        SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE,
        SparkSQLProperties.CHECK_SOURCE_DUPLICATES_ENABLE_DEFAULT))
  }

  def buildJoinCondition(primaries: util.List[String], r: DataSourceV2Relation, insertPlan: LogicalPlan): Expression = {
    var i = 0
    var joinCondition: Expression = null
    val expressions = new util.ArrayList[Expression]
    while (i < primaries.size) {
      val primary = primaries.get(i)
      val primaryAttr = r.output.find(_.name == primary).get
      val joinAttribute = insertPlan.output.find(_.name.replace("_arctic_after_", "") == primary).get
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
    query: LogicalPlan
  ): LogicalPlan = {
    r.table match {
      case arctic: ArcticSparkTable =>
        if (arctic.table().isKeyedTable) {
          val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
          val insertPlan = buildKeyedTableInsertProjection(query)
          val joinCondition = buildJoinCondition(primaries, r, insertPlan)
          Join(r, insertPlan, RightOuter, Some(joinCondition), JoinHint.NONE)
        } else {
          query
        }
    }
  }

  private def buildKeyedTableInsertProjection(relation: LogicalPlan): LogicalPlan = {
    val output = relation.output
    val outputWithValues = output.map(a => {
      Alias(a, "_arctic_after_" + a.name)()
    })
    Project(outputWithValues, relation)
  }
}
