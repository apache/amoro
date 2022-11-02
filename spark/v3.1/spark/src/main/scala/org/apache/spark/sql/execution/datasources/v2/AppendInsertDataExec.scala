package org.apache.spark.sql.execution.datasources.v2

import com.netease.arctic.spark.table.ArcticSparkTable
import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.catalog.SupportsWrite
import org.apache.spark.sql.connector.write.SupportsDynamicOverwrite
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class AppendInsertDataExec(table: ArcticSparkTable,
                                writeOptions: CaseInsensitiveStringMap,
                                queryInsert: SparkPlan,
                                validateQuery: SparkPlan,
                                refreshCache: () => Unit) extends ArcticTableWriteExec with BatchWriteHelper {
  override protected def run(): Seq[InternalRow] = {
    validateData()
    val writtenRows = writeInsert(newWriteBuilder().buildForBatch())
    refreshCache()
    writtenRows
  }


  override def query: SparkPlan = queryInsert

  override def left: SparkPlan = queryInsert

  override def right: SparkPlan = validateQuery
}
