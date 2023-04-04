package com.netease.arctic.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.analysis.PartitionSpec
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, V2PartitionCommand}

case class AlterArcticTableDropPartition(
                                          table: LogicalPlan,
                                          parts: Seq[PartitionSpec],
                                          ifExists: Boolean,
                                          purge: Boolean) extends V2PartitionCommand {
  override protected def withNewChildInternal(newChild: LogicalPlan): LogicalPlan = {
    copy(table = newChild)
  }
}
