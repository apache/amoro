package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.optimize.{ClusteredDistribution, Expression => V2Expression}
import com.netease.arctic.spark.source.ArcticSparkTable
import com.netease.arctic.spark.sql.expressions.IdentityTransform
import com.netease.arctic.spark.sql.plan.OverwriteArcticTableDynamic
import com.netease.arctic.spark.util.ArcticSparkUtil
import org.apache.spark.sql.catalyst.optimizer.PropagateEmptyRelation.conf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.arctic.AnalysisException
import org.apache.spark.sql.catalyst.expressions.{Expression, NamedExpression}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, RepartitionByExpression}
import org.apache.spark.sql.catalyst.rules.Rule

case class OptimizeWriteRule(spark : SparkSession) extends Rule[LogicalPlan] {

  override def apply(plan: LogicalPlan): LogicalPlan = plan transformDown {
    case a @ OverwriteArcticTableDynamic(t, table, query) =>
      table match {
        case table: ArcticSparkTable =>
          val newQuery = distributionQuery(query, table)
          val optimizedAppend = a.copy(query = newQuery)
          optimizedAppend
        case _ =>
          a
      }
  }

  private def distributionQuery(query: LogicalPlan, table: ArcticSparkTable): LogicalPlan =  {
    val distribution = ArcticSparkUtil.buildRequiredDistribution(table) match {
      case d: ClusteredDistribution =>
        d.clustering.map(e => toCatalyst(e, query))
      case _ =>
        Array.empty[Expression]
    }
    val queryWithDistribution = if (distribution.nonEmpty) {
      val partitionNum = conf.numShufflePartitions
      val pp = RepartitionByExpression(distribution, query, partitionNum)
      pp
    } else {
      query
    }
    queryWithDistribution
  }

  def toCatalyst(expr: V2Expression, query: LogicalPlan): Expression =  {
    val resolver = conf.resolver
    def resolve(parts: Seq[String]): NamedExpression = {
      query.resolve(parts, resolver) match {
        case Some(attr) =>
          attr
        case None =>
          val ref = parts.map(quoteIfNeeded).mkString(".")
          throw AnalysisException.message(s"Cannot resolve '$ref' using ${query.output}")
      }
    }

    expr match {
      case it: IdentityTransform =>
        resolve(it.ref.fieldNames)
      case _ =>
        throw new RuntimeException(s"$expr is not currently supported")
    }
  }

  def quoteIfNeeded(part: String): String = {
    if (part.contains(".") || part.contains("`")) {
      s"`${part.replace("`", "``")}`"
    } else {
      part
    }
  }

}
