package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.sql.utils.WriteQueryProjections
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.merge.MergeWriter
import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2.{BatchWriteHelper, ExtendedV2ExistingTableWriteExec, WritingSparkTask}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

/**
 * Physical plan node to write a delta of rows to an existing table.
 */
case class WriteMergeExec(
                           table: ArcticSparkTable,
                           query: SparkPlan,
                           writeOptions: CaseInsensitiveStringMap,
                           projections: WriteQueryProjections,
                           refreshCache: () => Unit) extends ExtendedV2ExistingTableWriteExec[MergeWriter[InternalRow]] with BatchWriteHelper {

  override protected def run(): Seq[InternalRow] = {
    val writtenRows = writeWithV2(newWriteBuilder().buildForBatch())
    refreshCache()
    writtenRows
  }

  override def output: Seq[Attribute] = Nil

  override def child: SparkPlan = query

  override lazy val writingTask: WritingSparkTask[MergeWriter[InternalRow]] = {
    DeltaWithMetadataWritingSparkTask(projections)
  }
}

case class DeltaWithMetadataWritingSparkTask(
                                              projs: WriteQueryProjections) extends WritingSparkTask[MergeWriter[InternalRow]] {

  private lazy val frontRowProjection = projs.frontRowProjection.orNull
  private lazy val backRowProjection = projs.backRowProjection

  override protected def writeFunc(writer: MergeWriter[InternalRow], row: InternalRow): Unit = {
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
