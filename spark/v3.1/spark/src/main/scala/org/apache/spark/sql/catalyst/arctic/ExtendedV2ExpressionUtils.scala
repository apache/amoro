package org.apache.spark.sql.catalyst.expressions

import org.apache.spark.sql.catalyst.SQLConfHelper
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.connector.expressions.NamedReference

/**
 * A class that is inspired by V2ExpressionUtils in Spark but supports Iceberg transforms.
 */
object ExtendedV2ExpressionUtils extends SQLConfHelper {

  import org.apache.spark.sql.connector.catalog.CatalogV2Implicits.MultipartIdentifierHelper

  def resolveRef[T <: NamedExpression](ref: NamedReference, plan: LogicalPlan): T = {
    plan.resolve(ref.fieldNames.toSeq, conf.resolver) match {
      case Some(namedExpr) =>
        namedExpr.asInstanceOf[T]
      case None =>
        val name = ref.fieldNames.toSeq.quoted
        val outputString = plan.output.map(_.name).mkString(",")
        throw new UnsupportedOperationException(s"Unable to resolve $name given $outputString")
    }
  }
}
