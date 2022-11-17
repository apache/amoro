package org.apache.spark.sql.execution.datasources.v2

import com.netease.arctic.spark.table.ArcticSparkTable
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.util.CharVarcharUtils
import org.apache.spark.sql.connector.catalog.{Identifier, Table, TableCatalog}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters.mapAsJavaMapConverter


case class CreateArcticTableAsSelectExec(catalog: TableCatalog,
                                         ident: Identifier,
                                         partitioning: Seq[Transform],
                                         plan: LogicalPlan,
                                         queryInsert: SparkPlan,
                                         validateQuery: SparkPlan,
                                         properties: Map[String, String],
                                         writeOptions: CaseInsensitiveStringMap,
                                         ifNotExists: Boolean) extends ArcticTableWriteExec {

  var arcticTable: ArcticSparkTable = _

  override protected def run(): Seq[InternalRow] = {
    if (catalog.tableExists(ident)) {
      if (ifNotExists) {
        return Nil
      }

      throw new TableAlreadyExistsException(ident)
    }

    val schema = CharVarcharUtils.getRawSchema(queryInsert.schema).asNullable
    val table = catalog.createTable(ident, schema,
      partitioning.toArray, properties.asJava)
    table match {
      case table: ArcticSparkTable =>
        arcticTable = table
      case _ =>
        throw new UnsupportedOperationException(s"Unsupported others table")
    }
    validateData()
    writeToTable(catalog, table, writeOptions, ident)
  }

  override def table: ArcticSparkTable = arcticTable

  override def left: SparkPlan = queryInsert

  override def right: SparkPlan = validateQuery
}
