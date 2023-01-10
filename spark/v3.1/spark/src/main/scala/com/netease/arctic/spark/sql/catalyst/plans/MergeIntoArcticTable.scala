package com.netease.arctic.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.expressions.{AssignmentUtils, Expression}
import org.apache.spark.sql.catalyst.plans.logical._

case class MergeIntoArcticTable(
                                 targetTable: LogicalPlan,
                                 sourceTable: LogicalPlan,
                                 mergeCondition: Expression,
                                 matchedActions: Seq[MergeAction],
                                 notMatchedActions: Seq[MergeAction],
                                 rewritePlan: Option[LogicalPlan] = None) extends Command {

  lazy val aligned: Boolean = {
    val matchedActionsAligned = matchedActions.forall {
      case UpdateAction(_, assignments) =>
        AssignmentUtils.aligned(targetTable, assignments)
      case _: DeleteAction =>
        true
      case _ =>
        false
    }

    val notMatchedActionsAligned = notMatchedActions.forall {
      case InsertAction(_, assignments) =>
        AssignmentUtils.aligned(targetTable, assignments)
      case _ =>
        false
    }

    matchedActionsAligned && notMatchedActionsAligned
  }

  def condition: Option[Expression] = Some(mergeCondition)

  override def children: Seq[LogicalPlan] = if (rewritePlan.isDefined) {
    targetTable :: sourceTable :: rewritePlan.get :: Nil
  } else {
    targetTable :: sourceTable :: Nil
  }
}
