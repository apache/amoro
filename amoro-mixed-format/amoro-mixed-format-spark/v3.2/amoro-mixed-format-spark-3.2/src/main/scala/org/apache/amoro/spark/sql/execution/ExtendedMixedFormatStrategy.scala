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

package org.apache.amoro.spark.sql.execution

import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.apache.spark.sql.{SparkSession, Strategy}
import org.apache.spark.sql.catalyst.analysis.{NamedRelation, ResolvedTable}
import org.apache.spark.sql.catalyst.expressions.PredicateHelper
import org.apache.spark.sql.catalyst.plans.logical.{DescribeRelation, LogicalPlan}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2._
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import org.apache.amoro.spark.sql.MixedFormatExtensionUtils.{isMixedFormatTable, MixedFormatTableHelper}
import org.apache.amoro.spark.sql.catalyst.plans._

case class ExtendedMixedFormatStrategy(spark: SparkSession) extends Strategy with PredicateHelper {

  override def apply(plan: LogicalPlan): Seq[SparkPlan] = plan match {
    case DescribeRelation(r: ResolvedTable, partitionSpec, isExtended, _)
        if isMixedFormatTable(r.table) =>
      if (partitionSpec.nonEmpty) {
        throw new RuntimeException("DESCRIBE does not support partition for v2 tables.")
      }
      DescribeKeyedTableExec(r.table, r.catalog, r.identifier, isExtended) :: Nil

    case MigrateToMixedFormatLogicalPlan(command) =>
      MigrateToMixedFormatExec(command) :: Nil

    case MixedFormatRowLevelWrite(
          table: DataSourceV2Relation,
          query,
          options,
          projs,
          Some(write)) =>
      MixedFormatRowLevelWriteExec(
        table.table.asMixedSparkTable,
        planLater(query),
        new CaseInsensitiveStringMap(options.asJava),
        projs,
        refreshCache(table),
        write) :: Nil

    case MergeRows(
          isSourceRowPresent,
          isTargetRowPresent,
          matchedConditions,
          matchedOutputs,
          notMatchedConditions,
          notMatchedOutputs,
          rowIdAttrs,
          matchedRowCheck,
          unMatchedRowCheck,
          emitNotMatchedTargetRows,
          output,
          child) =>
      MergeRowsExec(
        isSourceRowPresent,
        isTargetRowPresent,
        matchedConditions,
        matchedOutputs,
        notMatchedConditions,
        notMatchedOutputs,
        rowIdAttrs,
        matchedRowCheck,
        unMatchedRowCheck,
        emitNotMatchedTargetRows,
        output,
        planLater(child)) :: Nil

    case d @ AlterMixedFormatTableDropPartition(r: ResolvedTable, _, _, _) =>
      AlterMixedFormatTableDropPartitionExec(r.table, d.parts) :: Nil

    case QueryWithConstraintCheckPlan(scanPlan, fileFilterPlan) =>
      QueryWithConstraintCheckExec(planLater(scanPlan), planLater(fileFilterPlan)) :: Nil

    case TruncateMixedFormatTable(r: ResolvedTable) =>
      TruncateMixedFormatTableExec(r.table) :: Nil

    case _ => Nil
  }

  private def refreshCache(r: NamedRelation)(): Unit = {
    spark.sharedState.cacheManager.recacheByPlan(spark, r)
  }

}
