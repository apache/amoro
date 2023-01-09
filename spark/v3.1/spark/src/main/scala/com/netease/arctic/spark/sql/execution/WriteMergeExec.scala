package com.netease.arctic.spark.sql.execution

import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.merge.MergeWriter
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.execution.datasources.v2.{BatchWriteHelper, ExtendedV2ExistingTableWriteExec}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

/**
 * Physical plan node to write a delta of rows to an existing table.
 */
case class WriteMergeExec(
                           table: ArcticSparkTable,
                           query: SparkPlan,
                           writeOptions: CaseInsensitiveStringMap,
                           refreshCache: () => Unit) extends ExtendedV2ExistingTableWriteExec[MergeWriter[InternalRow]] with BatchWriteHelper {

  override protected def run(): Seq[InternalRow] = {
    val writtenRows = writeWithV2(newWriteBuilder().buildForBatch())
    refreshCache()
    writtenRows
  }

  override def output: Seq[Attribute] = Nil

  override def child: SparkPlan = query
}
