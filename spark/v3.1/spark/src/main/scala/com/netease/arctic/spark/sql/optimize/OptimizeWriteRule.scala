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

import com.netease.arctic.spark.SupportSparkAdapter
import com.netease.arctic.spark.sql.catalyst.plans.{AppendArcticData, OverwriteArcticPartitionsDynamic, ReplaceArcticData}
import com.netease.arctic.spark.table.{ArcticIcebergSparkTable, ArcticSparkTable}
import com.netease.arctic.spark.util.DistributionAndOrderingUtil
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.{Expression, SortOrder}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, OverwritePartitionsDynamic, RepartitionByExpression, Sort}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

case class OptimizeWriteRule(spark: SparkSession) extends Rule[LogicalPlan] with SupportSparkAdapter {

  override def apply(plan: LogicalPlan): LogicalPlan = plan transformDown {
    case overwrite @ OverwritePartitionsDynamic(r: DataSourceV2Relation, query, writeOptions, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table, rowLevelOperation = false)
          val optimizedAppend = overwrite.copy(query = newQuery)
          optimizedAppend
        case _ =>
          overwrite
      }
    case append @ AppendArcticData(r: DataSourceV2Relation, query, _, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table, rowLevelOperation = false)
          val optimizedAppend = append.copy(query = newQuery)
          optimizedAppend
        case _ =>
          append
      }
    case replace @ ReplaceArcticData(r: DataSourceV2Relation, query, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table, rowLevelOperation = true)
          val optimizedAppend = replace.copy(query = newQuery)
          optimizedAppend
        case _ =>
          replace
      }
    case overwrite @ OverwriteArcticPartitionsDynamic(r: DataSourceV2Relation, query, _, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table, rowLevelOperation = false)
          val optimizedAppend = overwrite.copy(query = newQuery)
          optimizedAppend
        case _ =>
          overwrite
      }
  }

  private def distributionQuery(query: LogicalPlan, table: Table, rowLevelOperation: Boolean): LogicalPlan = {
    import org.apache.spark.sql.connector.expressions.{Expression => Expr}

    def toCatalyst(expr: Expr): Expression = sparkAdapter.expressions().toCatalyst(expr, query)

    val arcticTable = table match {
      case t: ArcticSparkTable => t.table()
      case t: ArcticIcebergSparkTable => t.table()
    }

    val distribution = DistributionAndOrderingUtil.buildTableRequiredDistribution(arcticTable)
      .toSeq.map(e => toCatalyst(e))
      .asInstanceOf[Seq[Expression]]

    val queryWithDistribution = if (distribution.nonEmpty) {
      val partitionNum = conf.numShufflePartitions
      val pp = RepartitionByExpression(distribution, query, partitionNum)
      pp
    } else {
      query
    }

    val ordering = DistributionAndOrderingUtil.buildTableRequiredSortOrder(arcticTable, rowLevelOperation)
      .toSeq
      .map(e => toCatalyst(e))
      .asInstanceOf[Seq[SortOrder]]

    val queryWithDistributionAndOrdering = if (ordering.nonEmpty) {
      Sort(ordering, global = false, child = queryWithDistribution)
    } else {
      queryWithDistribution
    }
    queryWithDistributionAndOrdering
  }

}




