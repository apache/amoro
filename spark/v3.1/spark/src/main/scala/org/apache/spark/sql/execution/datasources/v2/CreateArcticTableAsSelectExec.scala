package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.util.CharVarcharUtils
import org.apache.spark.sql.connector.catalog.{Identifier, TableCatalog}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.SparkPlan
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters

case class CreateArcticTableAsSelectExec(catalog: TableCatalog,
                                         ident: Identifier,
                                         partitioning: Seq[Transform],
                                         plan: LogicalPlan,
                                         query: SparkPlan,
                                         properties: Map[String, String],
                                         writeOptions: CaseInsensitiveStringMap,
                                         ifNotExists: Boolean) extends TableWriteExecHelper {
  override protected def run(): Seq[InternalRow] = {
    if (catalog.tableExists(ident)) {
      if (ifNotExists) {
        return Nil
      }

      throw new TableAlreadyExistsException(ident)
    }

    val schema = CharVarcharUtils.getRawSchema(query.schema).asNullable
    var propertiesMap: Map[String, String] = properties
    if (writeOptions.containsKey("primary.keys")) {
      propertiesMap += ("primary.keys" -> writeOptions.get("primary.keys"))
    }
    val table = catalog.createTable(ident, schema,
      partitioning.toArray, JavaConverters.mapAsJavaMap(propertiesMap))
    writeToTable(catalog, table, writeOptions, ident)
  }
}
