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

package com.netease.arctic.spark.sql.catalyst.rule

import com.netease.arctic.spark.source.ArcticSparkTable
import com.netease.arctic.spark.sql.plan.OverwriteArcticTableDynamic
import com.netease.arctic.table.ArcticTable
import org.apache.spark.sql.{AnalysisException, SparkSession}
import org.apache.spark.sql.catalyst.plans.logical.{InsertIntoTable, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule


/**
 * process insert overwrite or insert into for arctic table.
 * 1. check query column size match with table schema.
 * 2. cast query column data type by projection to match table fields data type
 */
case class PreprocessArcticTableInsertionRule(spark: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case i @ OverwriteArcticTableDynamic(table, query) if query.resolved =>
      val newQuery = process(table, query)
      i
  }


  def process(table: ArcticSparkTable, query: LogicalPlan): LogicalPlan = {
    query
  }
}
