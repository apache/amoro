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

package com.netease.arctic.spark.sql.catalyst.analysis

import com.netease.arctic.spark.command.MigrateToArcticCommand
import com.netease.arctic.spark.sql.catalyst.plans
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.catalyst.plans.logical.{InsertIntoStatement, LogicalPlan, Project}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

import scala.collection.JavaConverters.seqAsJavaList



case class ResolveArcticCommand(spark: SparkSession) extends Rule[LogicalPlan] {

  override def apply(plan: LogicalPlan): LogicalPlan = plan resolveOperators {
    case plans.MigrateToArcticStatement(source, target) =>
      val command = MigrateToArcticCommand.newBuilder(spark)
        .withSource(seqAsJavaList(source))
        .withTarget(seqAsJavaList(target))
        .build()
      plans.MigrateToArcticLogicalPlan(command)
    // set the nullable is false to the primary key field
    case i@InsertIntoStatement(r: DataSourceV2Relation,
    partition, cols, query, overwrite, ifPartitionNotExists) =>
      val output = query.output.map(f =>
        if(r.output.filter(p => !p.nullable).map(t=>t.name).contains(f.name)) {
          AttributeReference(f.name, f.dataType, nullable = false)(f.exprId,f.qualifier)
        } else {
          f
        }
      )
      val finalQuery = Project(output, query)
      i.copy(i.table, partition, cols, finalQuery, overwrite, ifPartitionNotExists)
  }
}
