package com.netease.arctic.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeSet, Expression}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, UnaryNode}
import org.apache.spark.sql.catalyst.util.truncatedString

case class MergeRows(
                      isSourceRowPresent: Expression,
                      isTargetRowPresent: Expression,
                      matchedConditions: Seq[Expression],
                      matchedOutputs: Seq[Seq[Expression]],
                      notMatchedConditions: Seq[Expression],
                      notMatchedOutputs: Seq[Seq[Expression]],
                      targetOutput: Seq[Expression],
                      rowIdAttrs: Seq[Attribute],
                      performCardinalityCheck: Boolean,
                      emitNotMatchedTargetRows: Boolean,
                      output: Seq[Attribute],
                      child: LogicalPlan) extends UnaryNode {

  require(targetOutput.nonEmpty || !emitNotMatchedTargetRows)

  override lazy val producedAttributes: AttributeSet = {
    AttributeSet(output.filterNot(attr => inputSet.contains(attr)))
  }

  override lazy val references: AttributeSet = child.outputSet

  override def simpleString(maxFields: Int): String = {
    s"MergeRows${truncatedString(output, "[", ", ", "]", maxFields)}"
  }

}
