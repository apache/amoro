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

import com.netease.arctic.spark.source.ArcticSource
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.arctic.AnalysisException
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.execution.command.RunnableCommand
import org.apache.spark.sql.internal.StaticSQLConf

case class DropArcticTableCommand(
    arctic: ArcticSource,
    tableIdentifier: TableIdentifier,
    ignoreIfExists: Boolean,
    purge: Boolean)
  extends RunnableCommand {
  override def run(sparkSession: SparkSession): Seq[Row] = {
    val spark = SparkSession.getActiveSession.get
    val sparkCatalogImpl = spark.conf.get(StaticSQLConf.CATALOG_IMPLEMENTATION.key)
    if (!"hive".equalsIgnoreCase(sparkCatalogImpl)) {
      throw AnalysisException.message(
        s"failed to create table ${tableIdentifier} not use hive catalog")
    }
    arctic.dropTable(tableIdentifier, purge)
    Seq.empty[Row]
  }
}
