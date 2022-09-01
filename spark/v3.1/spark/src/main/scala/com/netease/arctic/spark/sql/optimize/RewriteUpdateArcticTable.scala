/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.sql.ArcticExtensionUtils.{ArcticTableHelper, asTableRelation, isArcticRelation}
import com.netease.arctic.spark.sql.catalyst.plans.ReplaceArcticData
import com.netease.arctic.spark.table.{ArcticSparkTable, SupportsExtendIdentColumns, SupportsUpsert}
import com.netease.arctic.spark.util.ArcticSparkUtils
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.{Alias, ArcticExpressionUtils, AttributeReference, EqualTo, Expression, Literal}
import org.apache.spark.sql.catalyst.plans.Inner
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.iceberg.distributions.ClusteredDistribution
import org.apache.spark.sql.execution.datasources.v2.{DataSourceV2Relation, DataSourceV2ScanRelation}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.utils.ArcticRewriteHelper


/**
 * rewrite update table plan as append upsert data.
 */
case class RewriteUpdateArcticTable(spark: SparkSession) extends Rule[LogicalPlan] with ArcticRewriteHelper{

  override def apply(plan: LogicalPlan): LogicalPlan = plan match {
    case UpdateTable(table: DataSourceV2Relation, assignments, condition) =>
      val pre = Filter(condition.get, table)
      val after = Filter(condition.get, table)

      val newQuery = Union(pre, after)
      newQuery
    case u: UpdateTable if isArcticRelation(u.table) =>
      val arcticRelation = asTableRelation(u.table)
      val upsertWrite = arcticRelation.table.asUpsertWrite
      val scanBuilder = upsertWrite.newUpsertScanBuilder(arcticRelation.options)
      pushFilter(scanBuilder, u.condition.get, arcticRelation.output)
      val upsertQuery = buildUpsertQuery(arcticRelation, upsertWrite, scanBuilder, u.assignments, u.condition)
      var query = upsertQuery
      var options: Map[String, String] = Map.empty
      arcticRelation.table match {
        case a: ArcticSparkTable =>
          if (a.table().isKeyedTable) {
            val newQuery = distributionQuery(upsertQuery, a)
            query = newQuery
          }
      }
      options +=(WriteMode.WRITE_MODE_KEY -> WriteMode.UPSERT.toString)
      ReplaceArcticData(arcticRelation, query, options)

    case _ => plan
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

  def buildUpsertQuery(r: DataSourceV2Relation, upsert: SupportsUpsert, scanBuilder: SupportsExtendIdentColumns,
                       assignments: Seq[Assignment],
                       condition: Option[Expression]): LogicalPlan = {
    if (upsert.requireAdditionIdentifierColumns()) {
      scanBuilder.withIdentifierColumns()
    }
    val scan = scanBuilder.build()
    val outputAttr = toOutputAttrs(scan.readSchema(), r.output)
    val valuesRelation = DataSourceV2ScanRelation(r, scan, outputAttr)
    val matchedRowsQuery = if (condition.isDefined) {
      Filter(condition.get, valuesRelation)
    } else {
      valuesRelation
    }
    r.table match {
      case a: ArcticSparkTable =>
        if (a.table().isKeyedTable) {
          val updatedRowsQuery = buildKeyedTableUpdateInsertProjection(valuesRelation, matchedRowsQuery, assignments)
          Join(matchedRowsQuery, updatedRowsQuery, Inner,
            Some(EqualTo(matchedRowsQuery.output.head, updatedRowsQuery.output.head)), JoinHint.NONE)
        } else {
          val updatedRowsQuery = buildUnKeyedTableUpdateInsertProjection(valuesRelation, matchedRowsQuery, assignments)
          val deleteQuery = Project(Seq(Alias(Literal(SupportsUpsert.UPSERT_OP_VALUE_DELETE), SupportsUpsert.UPSERT_OP_COLUMN_NAME)())
            ++ matchedRowsQuery.output.iterator,
            matchedRowsQuery)
          val insertQuery = Project(Seq(Alias(Literal(SupportsUpsert.UPSERT_OP_VALUE_INSERT), SupportsUpsert.UPSERT_OP_COLUMN_NAME)())
            ++ updatedRowsQuery.output.iterator,
            updatedRowsQuery)
          Union(deleteQuery, insertQuery)
        }
    }
  }

  protected def toOutputAttrs(schema: StructType, attrs: Seq[AttributeReference]): Seq[AttributeReference] = {
    val nameToAttr = attrs.map(_.name).zip(attrs).toMap
    schema.map(f => AttributeReference(f.name, f.dataType, f.nullable, f.metadata)()).map {
      a =>
        nameToAttr.get(a.name) match {
          case Some(ref) =>
            // keep the attribute id if it was present in the relation
            a.withExprId(ref.exprId)
          case _ =>
            // if the field is new, create a new attribute
            AttributeReference(a.name, a.dataType, a.nullable, a.metadata)()
        }
    }
  }

  private def buildKeyedTableUpdateInsertProjection(relation: LogicalPlan,
                                    scanPlan: LogicalPlan,
                                    assignments: Seq[Assignment]): LogicalPlan = {
    val output = relation.output
    val assignmentMap = assignments.map(a =>
      a.key.asInstanceOf[AttributeReference].name -> a.value
    ).toMap
    val outputWithValues = output.map( a => {
      if(assignmentMap.contains(a.name)) {
        Alias(assignmentMap(a.name), "_arctic_after_" + a.name)()
      } else {
        Alias(a, "_arctic_after_" + a.name)()
      }
    })
    Project(outputWithValues, scanPlan)
  }

  private def buildUnKeyedTableUpdateInsertProjection(relation: LogicalPlan,
                                                    scanPlan: LogicalPlan,
                                                    assignments: Seq[Assignment]): LogicalPlan = {
    val output = relation.output
    val assignmentMap = assignments.map(a =>
      a.key.asInstanceOf[AttributeReference].name -> a.value
    ).toMap
    val outputWithValues = output.map(a => {
      if (assignmentMap.contains(a.name)) {
        Alias(assignmentMap(a.name), a.name)()
      } else {
        a
      }
    })
    Project(outputWithValues, scanPlan)
  }

}
