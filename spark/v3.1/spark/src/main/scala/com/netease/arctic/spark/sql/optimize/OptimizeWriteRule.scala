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

import com.netease.arctic.spark.sql.catalyst.plans.{AppendArcticData, OverwriteArcticData, ReplaceArcticData}
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.util.ArcticSparkUtils
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.{ArcticExpressionUtils, Expression}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, OverwritePartitionsDynamic, RepartitionByExpression}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.iceberg.distributions.ClusteredDistribution
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

case class OptimizeWriteRule(spark: SparkSession) extends Rule[LogicalPlan] {

  override def apply(plan: LogicalPlan): LogicalPlan = plan transformDown {
    case a@OverwritePartitionsDynamic(r: DataSourceV2Relation, query, writeOptions, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table)
          val optimizedAppend = a.copy(query = newQuery)
          optimizedAppend
        case _ =>
          a
      }
    case a@AppendArcticData(r: DataSourceV2Relation, query, _, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table)
          val optimizedAppend = a.copy(query = newQuery)
          optimizedAppend
        case _ =>
          a
      }
    case a@ReplaceArcticData(r: DataSourceV2Relation, query, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table)
          val optimizedAppend = a.copy(query = newQuery)
          optimizedAppend
        case _ =>
          a
      }
    case a@OverwriteArcticData(r: DataSourceV2Relation, query, _, _) =>
      r.table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table)
          val optimizedAppend = a.copy(query = newQuery)
          optimizedAppend
        case _ =>
          a
      }
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




