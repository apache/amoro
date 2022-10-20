package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.catalog.SupportsWrite
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class AppendInsertDataExec(table: SupportsWrite,
                                writeOptions: CaseInsensitiveStringMap,
                                queryInsert: SparkPlan,
                                queryValidate: SparkPlan,
                                refreshCache: () => Unit) extends ArcticTableWriteExec with BatchWriteHelper {
  override protected def run(): Seq[InternalRow] = {
    validateData()
    val writtenRowsA = writeInsert(newWriteBuilder().buildForBatch())
    refreshCache()
    writtenRowsA
  }

  override def left: SparkPlan = queryInsert

  override def right: SparkPlan = queryValidate

  override def query: SparkPlan = queryInsert
}
