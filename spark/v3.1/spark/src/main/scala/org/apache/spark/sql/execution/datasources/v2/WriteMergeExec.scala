/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.spark.sql.execution.datasources.v2

import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.merge.MergeWriter
import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.AttributeSet
import org.apache.spark.sql.connector.write._
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util.UUID

/**
 * Physical plan node to write a delta of rows to an existing table.
 */
case class WriteMergeExec(
                           table: ArcticSparkTable,
                           query: SparkPlan,
                           writeOptions: CaseInsensitiveStringMap,
                           refreshCache: () => Unit) extends ExtendedV2ExistingTableWriteExec[MergeWriter[InternalRow]]{

  override lazy val references: AttributeSet = query.outputSet

  override lazy val writingTask: WritingSparkTask[MergeWriter[InternalRow]] = {
    DeltaWithMetadataWritingSparkTask()
  }

  override protected def run(): Seq[InternalRow] = {
    prepare()
    val info = LogicalWriteInfoImpl(
      queryId = UUID.randomUUID().toString,
      query.schema,
      writeOptions)
    val writtenRows = writeWithV2(table.newWriteBuilder(info).buildForBatch())
    refreshCache()
    writtenRows
  }

  case class DeltaWithMetadataWritingSparkTask() extends WritingSparkTask[MergeWriter[InternalRow]] {


    override protected def writeFunc(writer: MergeWriter[InternalRow], row: InternalRow): Unit = {
      val operation = row.getString(0)

      operation match {
        case "D" =>
          writer.delete(row)

        case "U" =>
          writer.update(row)

        case "I" =>
          writer.insert(row)

        case other =>
          throw new SparkException(s"Unexpected operation ID: $other")
      }
    }
  }
}
