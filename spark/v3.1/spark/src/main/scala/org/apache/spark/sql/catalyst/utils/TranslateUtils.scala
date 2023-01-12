package org.apache.spark.sql.catalyst.utils

import com.netease.arctic.spark.sql.utils.ProjectingInternalRow
import org.apache.spark.sql.catalyst.expressions.{Attribute, Expression}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.datasources.DataSourceStrategy
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType

object TranslateUtils {
  def translateFilter(deleteExpr: Expression): Option[Filter] = {
    DataSourceStrategy.translateFilter(deleteExpr, supportNestedPredicatePushdown = true)
  }

  def newLazyProjection(plan: LogicalPlan,
                        attrs: Seq[Attribute],
                        isFront: Boolean,
                        offset: Int): ProjectingInternalRow = {

    val colOrdinals = attrs.map(attr => plan.output.indexWhere(_.name == attr.name)).filter(p => p.!=(-1))
    val planAttrs = colOrdinals.map(plan.output(_))
    val schema = StructType.fromAttributes(planAttrs)
    if (!isFront) {
      val backColOrdinals = colOrdinals.map(c => c + colOrdinals.size + offset)
      ProjectingInternalRow(schema, backColOrdinals)
    } else {
      ProjectingInternalRow(schema, colOrdinals)
    }
  }
}
