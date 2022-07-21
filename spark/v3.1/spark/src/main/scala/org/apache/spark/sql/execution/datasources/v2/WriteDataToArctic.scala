package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.SparkException
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.catalog.CatalogV2Implicits.IdentifierHelper
import org.apache.spark.sql.connector.catalog.{Identifier, StagedTable, SupportsWrite, Table, TableCatalog}
import org.apache.spark.sql.connector.write.{LogicalWriteInfoImpl, V1WriteBuilder}
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.util.Utils

import java.util.UUID

case class WriteDataToArctic(catalog: TableCatalog,
                             table: Table,
                             writeOptions: CaseInsensitiveStringMap,
                             ident: Identifier) extends V2TableWriteExec{
  override protected def run(): Seq[InternalRow] = {
    Utils.tryWithSafeFinallyAndFailureCallbacks({
      table match {
        case table: SupportsWrite =>
          val info = LogicalWriteInfoImpl(
            queryId = UUID.randomUUID().toString,
            query.schema,
            writeOptions)
          val writeBuilder = table.newWriteBuilder(info)

          val writtenRows = writeBuilder match {
            case v2 => writeWithV2(v2.buildForBatch())
          }

          table match {
            case st: StagedTable => st.commitStagedChanges()
            case _ =>
          }
          writtenRows

        case _ =>
          // Table does not support writes - staged changes are also rolled back below if table
          // is staging.
          throw new SparkException(
            s"Table implementation does not support writes: ${ident.quoted}")
      }
    })(catchBlock = {
      table match {
        // Failure rolls back the staged writes and metadata changes.
        case st: StagedTable => st.abortStagedChanges()
        case _ => catalog.dropTable(ident)
      }
    })
  }

  override def query: SparkPlan = null

}
