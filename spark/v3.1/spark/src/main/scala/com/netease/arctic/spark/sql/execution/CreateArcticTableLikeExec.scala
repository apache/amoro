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

package com.netease.arctic.spark.sql.execution

import scala.collection.JavaConverters
import scala.collection.JavaConverters.seqAsJavaList

import com.netease.arctic.spark.{ArcticSparkCatalog, ArcticSparkSessionCatalog}
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.table.KeyedTable
import org.apache.iceberg.spark.Spark3Util
import org.apache.iceberg.spark.Spark3Util.CatalogAndIdentifier
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.{InternalRow, TableIdentifier}
import org.apache.spark.sql.catalyst.catalog.CatalogStorageFormat
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.TableCatalog
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec

case class CreateArcticTableLikeExec(
    sparkSession: SparkSession,
    targetTable: TableIdentifier,
    sourceTable: TableIdentifier,
    fileFormat: CatalogStorageFormat,
    provider: Option[String],
    properties: Map[String, String] = Map.empty,
    ifNotExists: Boolean) extends V2CommandExec {
  protected def run(): Seq[InternalRow] = {
    val sourceIdentifier = buildArcticIdentifier(sparkSession, sourceTable)
    val targetIdentifier = buildArcticIdentifier(sparkSession, targetTable)
    val arcticCatalog = buildCatalog(sourceIdentifier)
    val table = arcticCatalog.loadTable(sourceIdentifier.identifier())
    var targetProperties = properties
    targetProperties += ("provider" -> "arctic")
    table match {
      case keyedTable: ArcticSparkTable =>
        keyedTable.table() match {
          case table: KeyedTable =>
            targetProperties += ("primary.keys" ->
              String.join(",", table.primaryKeySpec().fieldNames()))
          case _ =>
        }
      case _ =>
    }
    arcticCatalog.createTable(
      targetIdentifier.identifier(),
      table.schema(),
      table.partitioning(),
      JavaConverters.mapAsJavaMap(targetProperties))
    Seq.empty[InternalRow]
  }

  private def buildArcticIdentifier(
      sparkSession: SparkSession,
      originIdentifier: TableIdentifier): CatalogAndIdentifier = {
    var identifier: Seq[String] = Seq.empty[String]
    identifier :+= originIdentifier.database.get
    identifier :+= originIdentifier.table
    Spark3Util.catalogAndIdentifier(sparkSession, seqAsJavaList(identifier))
  }

  private def buildCatalog(catalogAndIdentifier: CatalogAndIdentifier): TableCatalog = {
    catalogAndIdentifier.catalog() match {
      case arcticCatalog: ArcticSparkCatalog =>
        arcticCatalog.asInstanceOf[ArcticSparkCatalog]
      case arcticCatalog: ArcticSparkSessionCatalog[_] =>
        arcticCatalog.asInstanceOf[ArcticSparkSessionCatalog[_]]
      case _ =>
        throw new UnsupportedOperationException("Only support arctic catalog")
    }
  }

  override def output: Seq[Attribute] = Nil

  override def children: Seq[SparkPlan] = Nil

}
