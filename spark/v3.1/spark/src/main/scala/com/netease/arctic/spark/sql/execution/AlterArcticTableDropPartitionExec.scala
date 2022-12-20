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

import com.netease.arctic.spark.table.SupportDropPartitions
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.{PartitionSpec, UnresolvedPartitionSpec}
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.execution.datasources.v2.V2CommandExec

import scala.collection.JavaConverters.asJavaIterableConverter

case class AlterArcticTableDropPartitionExec(table: Table,
                                             parts: Seq[PartitionSpec],
                                             retainData: Boolean) extends V2CommandExec {
  override protected def run(): Seq[InternalRow] = {
    table match {
      case table: SupportDropPartitions =>
        val specs = parts.map {
          case part: UnresolvedPartitionSpec =>
            part.spec.map(s => s._1 + "=" + s._2).asJava
        }
        val ident = specs.mkString.replace(", ", "/")
          .replace("[", "")
          .replace("]", "")
        table.dropPartitions(ident)
      case _ =>
        throw new UnsupportedOperationException(
          s"table ${table.name()} can not drop multiple partitions.")
    }
    Nil
  }

  override def output: Seq[Attribute] = Nil
}
