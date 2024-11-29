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

import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.write.Write
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2.{V2ExistingTableWriteExec, WritingSparkTask}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import org.apache.amoro.spark.sql.utils.WriteQueryProjections
import org.apache.amoro.spark.table.MixedSparkTable
import org.apache.amoro.spark.writer.RowLevelWriter

/**
 * Physical plan node to write a delta of rows to an existing table.
 */
case class MixedFormatRowLevelWriteExec(
    table: MixedSparkTable,
    query: SparkPlan,
    writeOptions: CaseInsensitiveStringMap,
    projections: WriteQueryProjections,
    refreshCache: () => Unit,
    write: Write) extends V2ExistingTableWriteExec {

  override protected def run(): Seq[InternalRow] = {
    val writtenRows = writeWithV2(write.toBatch)
    refreshCache()
    writtenRows
  }

  override def output: Seq[Attribute] = Nil

  override def child: SparkPlan = query

  override lazy val writingTask: WritingSparkTask[RowLevelWriter[InternalRow]] = {
    DeltaWithMetadataWritingSparkTask(projections)
  }

  override protected def withNewChildInternal(newChild: SparkPlan): MixedFormatRowLevelWriteExec = {
    copy(query = newChild)
  }
}

case class DeltaWithMetadataWritingSparkTask(
    projs: WriteQueryProjections) extends WritingSparkTask[RowLevelWriter[InternalRow]] {

  private lazy val frontRowProjection = projs.frontRowProjection.orNull
  private lazy val backRowProjection = projs.backRowProjection

  override protected def write(writer: RowLevelWriter[InternalRow], row: InternalRow): Unit = {
    val operation = row.getString(0)

    operation match {
      case "D" =>
        frontRowProjection.project(row)
        writer.delete(frontRowProjection)

      case "U" =>
        frontRowProjection.project(row)
        backRowProjection.project(row)
        writer.update(frontRowProjection, backRowProjection)

      case "I" =>
        backRowProjection.project(row)
        writer.insert(backRowProjection)

      case other =>
        throw new SparkException(s"Unexpected operation ID: $other")
    }
  }
}
