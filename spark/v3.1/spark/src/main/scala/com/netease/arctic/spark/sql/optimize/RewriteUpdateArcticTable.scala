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
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.plans.logical.{AppendData, Assignment, LogicalPlan, UpdateTable}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation


/**
 * rewrite update table plan as append upsert data.
 */
case class RewriteUpdateArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case u@UpdateTable(table, assignments, condition) if isArcticRelation(table) =>
      val arcticRelation = asTableRelation(table)
      val upsertWrite = arcticRelation.table.asUpsertWrite
      val upsertQuery = buildUpsertQuery(arcticRelation, assignments, condition)
      val options = Map.empty[String, String]
      AppendData.byPosition(arcticRelation, upsertQuery, options)
      u
  }


  def buildUpsertQuery(r: DataSourceV2Relation, assignments: Seq[Assignment],
                       condition: Option[Expression]): LogicalPlan = {
    // TODO: support arctic upsert query
    r
  }

}
