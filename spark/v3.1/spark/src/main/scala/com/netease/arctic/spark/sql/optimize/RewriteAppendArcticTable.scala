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

import com.netease.arctic.spark.table.{ArcticSparkTable, SupportsUpsert}
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.EqualTo
import org.apache.spark.sql.catalyst.plans.Inner
import org.apache.spark.sql.catalyst.plans.logical.{AppendData, Join, JoinHint, LogicalPlan, Project, Union}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

case class RewriteAppendArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  import com.netease.arctic.spark.sql.ArcticExtensionUtils._

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case i@AppendData(r: DataSourceV2Relation, query, writeOptions, isByName) if isArcticRelation(r) =>
      val upsertWrite = r.table.asUpsertWrite
      val (newQuery, options) = if (upsertWrite.appendAsUpsert()) {
        val upsertQuery = rewriteAppendAsUpsertQuery(r, upsertWrite, query)
        val upsertOptions = writeOptions + (WriteMode.WRITE_MODE_KEY -> WriteMode.UPSERT.mode)
        (upsertQuery, upsertOptions)
      } else {
        (query, writeOptions)
      }
      i.copy(query = newQuery, writeOptions = options)
  }

  def rewriteAppendAsUpsertQuery(r: DataSourceV2Relation,
                                 upsertWrite: SupportsUpsert,
                                 query: LogicalPlan): LogicalPlan = {
    // val joinCondition = upsertWrite.joinCondition()
//    r.table match {
//      case arctic: ArcticSparkTable =>
//        val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
//    }
    val join = Join(r, query, Inner, Some(EqualTo(r.output.head, query.output.head)), JoinHint.NONE)
    val project = Project(query.output, join)
    val value = Union(project, query)
    query
  }

}
