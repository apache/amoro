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

package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.ArcticSparkCatalog
import com.netease.arctic.spark.sql.catalyst.plans.{AppendArcticData, OverwriteArcticData, ReplaceArcticData}
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.util.ArcticSparkUtils
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.aggregate.{AggregateExpression, Complete, Count}
import org.apache.spark.sql.catalyst.expressions.{Alias, And, ArcticExpressionUtils, Cast, EqualTo, Expression, GreaterThan, Literal}
import org.apache.spark.sql.catalyst.plans.RightOuter
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, _}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.iceberg.distributions.ClusteredDistribution
import org.apache.spark.sql.execution.datasources.LogicalRelation
import org.apache.spark.sql.execution.datasources.v2.{CreateTableAsSelectExec, DataSourceV2Relation, OverwritePartitionsDynamicExec}
import org.apache.spark.sql.types.LongType

import java.util

case class RewriteAppendArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import com.netease.arctic.spark.sql.ArcticExtensionUtils._

  def buildValidatePrimaryKeyDuplication(r: DataSourceV2Relation, query: LogicalPlan): LogicalPlan = {
    r.table match {
      case arctic: ArcticSparkTable =>
        if (arctic.table().isKeyedTable) {
          val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
          val than = GreaterThan(AggregateExpression(Count(Literal(1)), Complete, isDistinct = false), Cast(Literal(1), LongType))
          val alias = Alias(than, "count")()
          val attributes = query.output.filter(p => primaries.contains(p.name))
          Aggregate(attributes, Seq(alias), query)
        } else {
          throw new UnsupportedOperationException(s"UnKeyed table can not validate")
        }
    }
  }


  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case AppendData(r: DataSourceV2Relation, query, writeOptions, isByName) if isArcticRelation(r) =>
      val arcticRelation = asTableRelation(r)
      val upsertWrite = arcticRelation.table.asUpsertWrite
      val (newQuery, options) = if (upsertWrite.appendAsUpsert()) {
        val upsertQuery = rewriteAppendAsUpsertQuery(r, query)
        val upsertOptions = writeOptions + (WriteMode.WRITE_MODE_KEY -> WriteMode.UPSERT.mode)
        (upsertQuery, upsertOptions)
      } else {
        (query, writeOptions)
      }
      arcticRelation.table match {
        case a: ArcticSparkTable =>
          if (a.table().isKeyedTable) {
            val validateQuery = buildValidatePrimaryKeyDuplication(r, query)
            AppendArcticData(arcticRelation, newQuery, validateQuery, options)
          } else {
            ReplaceArcticData(arcticRelation, query, writeOptions)
          }
      }
    case a@OverwritePartitionsDynamic(r: DataSourceV2Relation, query, writeOptions, _) =>
      val arcticRelation = asTableRelation(r)
      arcticRelation.table match {
        case table: ArcticSparkTable =>
          if (table.table().isKeyedTable) {
            val validateQuery = buildValidatePrimaryKeyDuplication(r, query)
            OverwriteArcticData(arcticRelation, query, validateQuery, writeOptions)
          } else {
            a
          }
      }
  }

  def buildJoinCondition(primaries: util.List[String], r: DataSourceV2Relation, insertPlan: LogicalPlan): Expression =  {
    var i = 0
    var joinCondition: Expression = null
    val expressions = new util.ArrayList[Expression]
    while ( i < primaries.size) {
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
    });
    joinCondition
  }

  def rewriteAppendAsUpsertQuery(r: DataSourceV2Relation,
                                 query: LogicalPlan): LogicalPlan = {
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
    val outputWithValues = output.map( a => {
        Alias(a, "_arctic_after_" + a.name)()
    })
    Project(outputWithValues, relation)
  }

  private def distributionQuery(query: LogicalPlan, table: ArcticSparkTable): LogicalPlan =  {
    val distribution = ArcticSparkUtils.buildRequiredDistribution(table) match {
      case d: ClusteredDistribution =>
        d.clustering.map(e => ArcticExpressionUtils.toCatalyst(e, query))
      case _ =>
        Array.empty[Expression]
    }
    val queryWithDistribution = if (distribution.nonEmpty) {
      val partitionNum = conf.numShufflePartitions
      val pp = RepartitionByExpression(distribution, query, partitionNum)
      pp
    } else {
      query
    }
    queryWithDistribution
  }
}
