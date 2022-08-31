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

package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.sql.catalyst.plans.MigrateToArcticLogicalPlan
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.catalyst.analysis.ResolvedTable
import org.apache.spark.sql.catalyst.plans.logical.{CreateTableAsSelect, DescribeRelation, LogicalPlan}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.command.CreateTableLikeCommand
import org.apache.spark.sql.execution.datasources.v2.CreateTableAsSelectExec
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.sql.{SparkSession, Strategy}

import scala.collection.JavaConverters

case class ExtendedArcticStrategy(spark: SparkSession) extends Strategy {

  override def apply(plan: LogicalPlan): Seq[SparkPlan] = plan match {
    case CreateTableAsSelect(catalog, ident, parts, query, props, options, ifNotExists) =>
      var propertiesMap: Map[String, String] = props
      var optionsMap: Map[String, String] = options
      if (options.contains("primary.keys")) {
        propertiesMap += ("primary.keys" -> options("primary.keys"))
      }
      if(propertiesMap.contains("primary.keys")) {
        optionsMap += (WriteMode.WRITE_MODE_KEY -> WriteMode.OVERWRITE_DYNAMIC.mode)
      }

      val writeOptions = new CaseInsensitiveStringMap(JavaConverters.mapAsJavaMap(optionsMap))
      CreateTableAsSelectExec(catalog, ident, parts, query, planLater(query),
        propertiesMap, writeOptions, ifNotExists) :: Nil

    case CreateTableLikeCommand(targetTable, sourceTable, storage, provider, properties, ifNotExists) =>
      CreateArcticTableLikeExec(spark, targetTable, sourceTable, storage, provider, properties, ifNotExists) :: Nil

    case DescribeRelation(r: ResolvedTable, partitionSpec, isExtended) =>
      if (partitionSpec.nonEmpty) {
        throw new RuntimeException("DESCRIBE does not support partition for v2 tables.")
      }
      DescribeKeyedTableExec(r.table, r.catalog, r.identifier, isExtended) :: Nil

    case MigrateToArcticLogicalPlan(command) =>
      println("create migrate to arctic command logical")
      MigrateToArcticExec(command)::Nil

    case _ => Nil
  }

}
