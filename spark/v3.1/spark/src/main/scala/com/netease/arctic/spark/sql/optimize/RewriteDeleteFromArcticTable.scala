package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.sql.ArcticExtensionUtils.{ArcticTableHelper, asTableRelation, isArcticRelation}
import com.netease.arctic.spark.table.SupportsUpsert
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.{Alias, AttributeReference, Expression, Literal}
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.{DataSourceV2Relation, DataSourceV2ScanRelation}
import org.apache.spark.sql.types.StructType

case class RewriteDeleteFromArcticTable(spark: SparkSession) extends Rule[LogicalPlan] {

  private val opCol = SupportsUpsert.UPSERT_OP_COLUMN_NAME
  private val opDel = SupportsUpsert.UPSERT_OP_VALUE_DELETE

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case _@DeleteFromTable(table, condition) if isArcticRelation(table) =>
      val r = asTableRelation(table)
      val upsert = r.table.asUpsertWrite
      val query = createUpsertQuery(r, upsert, condition)
//      val options = Map.empty[String, String] ++ (WriteMode.WRITE_MODE_KEY, WriteMode.UPSERT.mode)
      val options = Map.empty()
      AppendData.byPosition(r, query, options)
  }


  def createUpsertQuery(r: DataSourceV2Relation, upsert: SupportsUpsert, condition: Option[Expression]): LogicalPlan = {
    val scanBuilder = upsert.newScanBuilder()
    if (upsert.requireAdditionIdentifierColumns()) {
      scanBuilder.withIdentifierColumns()
    }
    val scan = scanBuilder.build()
    val outputAttr = toOutputAttrs(scan.readSchema(), r.output)
    val valuesRelation = DataSourceV2ScanRelation(r, scan, outputAttr)

    val matchValueQuery = if (condition.isDefined) {
      Filter(condition.get, valuesRelation)
    } else {
      valuesRelation
    }
    val withOperation = Seq(Alias(Literal(opDel), opCol)()) ++ matchValueQuery.output
    val deleteQuery = Project(withOperation, matchValueQuery)
    deleteQuery
  }

  def toOutputAttrs(schema: StructType, attrs: Seq[AttributeReference]): Seq[AttributeReference] = {
    val nameToAttr = attrs.map(_.name).zip(attrs).toMap
    schema.map(f => AttributeReference(f.name, f.dataType, f.nullable, f.metadata)()).map {
      a =>
        nameToAttr.get(a.name) match {
          case Some(ref) =>
            // keep the attribute id if it was present in the relation
            a.withExprId(ref.exprId)
          case _ =>
            // if the field is new, create a new attribute
            AttributeReference(a.name, a.dataType, a.nullable, a.metadata)()
        }
    }
  }
}
