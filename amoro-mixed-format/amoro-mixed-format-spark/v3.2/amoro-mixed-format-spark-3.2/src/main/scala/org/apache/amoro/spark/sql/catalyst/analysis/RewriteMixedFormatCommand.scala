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

package org.apache.amoro.spark.sql.catalyst.analysis

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.analysis.ResolvedTable
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.catalog.TableCatalog
import org.apache.spark.sql.execution.command.CreateTableLikeCommand

import org.apache.amoro.spark.{MixedFormatSparkCatalog, MixedFormatSparkSessionCatalog}
import org.apache.amoro.spark.mixed.MixedSessionCatalogBase
import org.apache.amoro.spark.sql.MixedFormatExtensionUtils.buildCatalogAndIdentifier
import org.apache.amoro.spark.sql.catalyst.plans.{AlterMixedFormatTableDropPartition, TruncateMixedFormatTable}
import org.apache.amoro.spark.table.MixedSparkTable
import org.apache.amoro.spark.writer.WriteMode

/**
 * Rule for rewrite some spark commands to mixed-format's implementation.
 */
case class RewriteMixedFormatCommand(sparkSession: SparkSession) extends Rule[LogicalPlan] {

  private def isCreateMixedFormatTableLikeCommand(
      targetTable: TableIdentifier,
      provider: Option[String]): Boolean = {
    val (targetCatalog, _) = buildCatalogAndIdentifier(sparkSession, targetTable)
    isCreateMixedFormatTable(targetCatalog, provider)
  }

  private def isCreateMixedFormatTable(catalog: TableCatalog, provider: Option[String]): Boolean = {
    catalog match {
      case _: MixedFormatSparkCatalog => true
      case _: MixedFormatSparkSessionCatalog[_] =>
        provider.isDefined && MixedSessionCatalogBase.SUPPORTED_PROVIDERS.contains(
          provider.get.toLowerCase)
      case _ => false
    }
  }

  override def apply(plan: LogicalPlan): LogicalPlan = {
    import org.apache.amoro.spark.sql.MixedFormatExtensionUtils._
    plan match {
      // Rewrite the AlterTableDropPartition to AlterMixedFormatTableDropPartition
      case DropPartitions(r: ResolvedTable, parts, ifExists, purge)
          if isMixedFormatTable(r.table) =>
        AlterMixedFormatTableDropPartition(r, parts, ifExists, purge)
      case t @ TruncateTable(r: ResolvedTable)
          if isMixedFormatTable(r.table) =>
        TruncateMixedFormatTable(t.child)

      case c @ CreateTableAsSelect(catalog, _, _, _, props, options, _)
          if isCreateMixedFormatTable(catalog, props.get(TableCatalog.PROP_PROVIDER)) =>
        var propertiesMap: Map[String, String] = props
        var optionsMap: Map[String, String] = options
        if (options.contains("primary.keys")) {
          propertiesMap += ("primary.keys" -> options("primary.keys"))
        }
        optionsMap += (WriteMode.WRITE_MODE_KEY -> WriteMode.OVERWRITE_DYNAMIC.mode)
        c.copy(properties = propertiesMap, writeOptions = optionsMap)
      case c @ CreateTableLikeCommand(
            targetTable,
            sourceTable,
            storage,
            provider,
            properties,
            ifNotExists)
          if isCreateMixedFormatTableLikeCommand(targetTable, provider) => {
        val (sourceCatalog, sourceIdentifier) = buildCatalogAndIdentifier(sparkSession, sourceTable)
        val (targetCatalog, targetIdentifier) = buildCatalogAndIdentifier(sparkSession, targetTable)
        val table = sourceCatalog.loadTable(sourceIdentifier)
        var targetProperties = properties
        table match {
          case mixedSparkTable: MixedSparkTable if mixedSparkTable.table().isKeyedTable =>
            targetProperties += ("primary.keys" ->
              String.join(
                ",",
                mixedSparkTable.table().asKeyedTable().primaryKeySpec().fieldNames()))
          case _ =>
        }
        targetProperties += ("provider" -> "arctic")
        CreateV2Table(
          targetCatalog,
          targetIdentifier,
          table.schema(),
          table.partitioning(),
          targetProperties,
          ifNotExists)
      }
      case _ => plan
    }
  }
}
