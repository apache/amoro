package com.netease.arctic.spark.sql.catalyst.analysis

import com.netease.arctic.spark.ArcticSparkCatalog
import com.netease.arctic.spark.sql.ArcticExtensionUtils.isArcticRelation
import com.netease.arctic.spark.sql.catalyst.plans.AlterArcticTableDropPartition
import com.netease.arctic.spark.sql.execution.AlterArcticTableDropPartitionExec
import com.netease.arctic.spark.table.ArcticSparkTable
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.ResolvedTable
import org.apache.spark.sql.catalyst.plans.logical.{AlterTableDropPartition, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule

/**
 * Rule for rewrite some spark commands to arctic's implementation.
 * @param sparkSession
 */
case class ArcticPostAnalysisRule(sparkSession: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = {
    plan match {
      // Rewrite the AlterTableDropPartitionCommand to AlterArcticTableDropPartitionCommand
      case AlterTableDropPartition(child, specs, ifExists, purge, retainData) =>
        AlterArcticTableDropPartition(child, specs, ifExists, purge, retainData)
      case _ => plan
    }
  }
}