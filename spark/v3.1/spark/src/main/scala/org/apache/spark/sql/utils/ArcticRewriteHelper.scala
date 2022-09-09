package org.apache.spark.sql.utils

import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.expressions.{AttributeReference, AttributeSet, Expression, PredicateHelper}
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.execution.datasources.DataSourceStrategy
import org.apache.spark.sql.execution.datasources.v2.PushDownUtils

trait ArcticRewriteHelper extends PredicateHelper with Logging{
  protected def pushFilter(
                           scanBuilder: ScanBuilder,
                           cond: Expression,
                           tableAttrs: Seq[AttributeReference]): Unit = {
    val predicates = extractFilters(cond, tableAttrs)
    if (predicates.nonEmpty) {
      val normalizedPredicates = DataSourceStrategy.normalizeExprs(predicates, tableAttrs)
      PushDownUtils.pushFilters(scanBuilder, normalizedPredicates)
    }
  }

  private def extractFilters(cond: Expression, tableAttrs: Seq[AttributeReference]): Seq[Expression] = {
    val tableAttrSet = AttributeSet(tableAttrs)
    splitConjunctivePredicates(cond).filter(_.references.subsetOf(tableAttrSet))
  }
}
