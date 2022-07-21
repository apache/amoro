package com.netease.arctic.spark.sql.execution

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.analysis.TableAlreadyExistsException
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.util.CharVarcharUtils
import org.apache.spark.sql.connector.catalog.{Identifier, Table, TableCatalog}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.v2.{V2CommandExec, WriteDataToArctic}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters

case class CreateArcticTableAsSelectExec(catalog: TableCatalog,
                                         ident: Identifier,
                                         partitioning: Seq[Transform],
                                         query: LogicalPlan,
                                         properties: Map[String, String],
                                         writeOptions: Map[String, String],
                                         ignoreIfExists: Boolean)  extends V2CommandExec {

  override protected def run(): Seq[InternalRow] = {
    val newWriteOptions = new CaseInsensitiveStringMap(JavaConverters.mapAsJavaMap(writeOptions))
    if (catalog.tableExists(ident)) {
      if (ignoreIfExists) {
        return Nil
      }

      throw new TableAlreadyExistsException(ident)
    }
    val schema = CharVarcharUtils.getRawSchema(query.schema)
    val table = catalog.createTable(ident, schema, partitioning.toArray, JavaConverters.mapAsJavaMap(properties))
    WriteDataToArctic(catalog, table, newWriteOptions, ident)
    Nil
  }


  override def output: Seq[Attribute] = Seq.empty
}
