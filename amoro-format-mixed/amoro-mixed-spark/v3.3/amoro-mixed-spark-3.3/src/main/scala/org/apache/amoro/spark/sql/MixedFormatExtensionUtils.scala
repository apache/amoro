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

package org.apache.amoro.spark.sql

import scala.collection.JavaConverters.seqAsJavaList

import org.apache.iceberg.spark.Spark3Util
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.analysis.Resolver
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, Project, SubqueryAlias}
import org.apache.spark.sql.connector.catalog.{CatalogPlugin, Identifier, Table, TableCatalog}
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation
import org.apache.spark.sql.types.{ArrayType, MapType, StructField, StructType}

import org.apache.amoro.spark.{MixedFormatSparkCatalog, MixedFormatSparkSessionCatalog}
import org.apache.amoro.spark.table.{MixedSparkTable, SupportsRowLevelOperator, UnkeyedSparkTable}

object MixedFormatExtensionUtils {

  implicit class MixedFormatTableHelper(table: Table) {
    def asMixedFormatTable: MixedSparkTable = {
      table match {
        case mixedSparkTable: MixedSparkTable => mixedSparkTable
        case _ => throw new IllegalArgumentException(s"$table is not an mixed-format table")
      }
    }

    def asUpsertWrite: SupportsRowLevelOperator = {
      table match {
        case table: SupportsRowLevelOperator => table
        case _ => throw new IllegalArgumentException(s"$table is not an upsert-able table")
      }
    }
  }

  implicit class MixedFormatRelationHelper(plan: LogicalPlan) {
    def asTableRelation: DataSourceV2Relation = {
      MixedFormatExtensionUtils.asTableRelation(plan)
    }
  }

  def isMixedFormatRelation(plan: LogicalPlan): Boolean = {
    def isMixedFormatTable(relation: DataSourceV2Relation): Boolean = relation.table match {
      case _: MixedSparkTable => true
      case _ => false
    }

    plan.collectLeaves().exists {
      case p: DataSourceV2Relation => isMixedFormatTable(p)
      case s: SubqueryAlias => s.child.children.exists { case p: DataSourceV2Relation =>
          isMixedFormatTable(p)
        }
      case _ => false
    }
  }

  def isKeyedRelation(plan: LogicalPlan): Boolean = {
    def isKeyedTable(relation: DataSourceV2Relation): Boolean = relation.table match {
      case a: MixedSparkTable =>
        a.table().isKeyedTable
      case _ => false
    }

    plan.collectLeaves().exists {
      case p: DataSourceV2Relation => isKeyedTable(p)
      case s: SubqueryAlias => s.child.children.exists { case p: DataSourceV2Relation =>
          isKeyedTable(p)
        }
      case _ => false
    }
  }

  def isUpsert(relation: DataSourceV2Relation): Boolean = {
    val upsertWrite = relation.table.asUpsertWrite
    upsertWrite.appendAsUpsert()
  }

  def isUnkeyedRelation(plan: LogicalPlan): Boolean = {
    def isUnkeyedTable(relation: DataSourceV2Relation): Boolean = relation.table match {
      case _: UnkeyedSparkTable => true
      case _ => false
    }

    plan.collectLeaves().exists {
      case p: DataSourceV2Relation => isUnkeyedTable(p)
      case s: SubqueryAlias => s.child.children.exists {
          case p: DataSourceV2Relation => isUnkeyedTable(p)
        }
    }
  }

  def isMixedFormatCatalog(catalog: TableCatalog): Boolean = {
    catalog match {
      case _: MixedFormatSparkCatalog => true
      case _: MixedFormatSparkSessionCatalog[_] => true
      case _ => false
    }
  }

  def isMixedFormatCatalog(catalog: CatalogPlugin): Boolean = {
    catalog match {
      case _: MixedFormatSparkCatalog => true
      case _: MixedFormatSparkSessionCatalog[_] => true
      case _ => false
    }
  }

  def isMixedFormatTable(table: Table): Boolean = table match {
    case _: MixedSparkTable => true
    case _: UnkeyedSparkTable => true
    case _ => false
  }

  def asTableRelation(plan: LogicalPlan): DataSourceV2Relation = {
    plan match {
      case s: SubqueryAlias => asTableRelation(s.child)
      case p: Project => asTableRelation(p.child.children.head)
      case r: DataSourceV2Relation => r
      case _ => throw new IllegalArgumentException("Expected a DataSourceV2Relation")
    }
  }

  def isKeyedTable(relation: DataSourceV2Relation): Boolean = {
    relation.table match {
      case mixedSparkTable: MixedSparkTable =>
        mixedSparkTable.table().isKeyedTable
      case _ => false
    }
  }

  def buildCatalogAndIdentifier(
      sparkSession: SparkSession,
      originIdentifier: TableIdentifier): (TableCatalog, Identifier) = {
    var identifier: Seq[String] = Seq.empty[String]
    identifier :+= originIdentifier.database.getOrElse(sparkSession.catalog.currentDatabase)
    identifier :+= originIdentifier.table
    val catalogAndIdentifier =
      Spark3Util.catalogAndIdentifier(sparkSession, seqAsJavaList(identifier))
    catalogAndIdentifier.catalog() match {
      case a: TableCatalog => (a, catalogAndIdentifier.identifier())
      case _ =>
        throw new UnsupportedOperationException("Only support TableCatalog or its implementation")
    }
  }
}
