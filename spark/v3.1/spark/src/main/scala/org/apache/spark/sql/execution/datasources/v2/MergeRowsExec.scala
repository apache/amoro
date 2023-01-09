package org.apache.spark.sql.execution.datasources.v2

import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.codegen.GeneratePredicate
import org.apache.spark.sql.catalyst.expressions.{Ascending, Attribute, AttributeSet, BasePredicate, Expression, SortOrder, UnsafeProjection}
import org.apache.spark.sql.catalyst.util.truncatedString
import org.apache.spark.sql.execution.{SparkPlan, UnaryExecNode}

case class MergeRowsExec(
                          isSourceRowPresent: Expression,
                          isTargetRowPresent: Expression,
                          matchedConditions: Seq[Expression],
                          matchedOutputs: Seq[Seq[Expression]],
                          notMatchedConditions: Seq[Expression],
                          notMatchedOutputs: Seq[Seq[Expression]],
                          targetOutput: Seq[Expression],
                          rowIdAttrs: Seq[Attribute],
                          performCardinalityCheck: Boolean,
                          unMatchedRowCheck: Boolean,
                          emitNotMatchedTargetRows: Boolean,
                          output: Seq[Attribute],
                          child: SparkPlan) extends UnaryExecNode {

  override def requiredChildOrdering: Seq[Seq[SortOrder]] = {
    if (performCardinalityCheck) {
      // request a local sort by the row ID attrs to co-locate matches for the same target row
      Seq(rowIdAttrs.map(attr => SortOrder(attr, Ascending)))
    } else {
      Seq(Nil)
    }
  }

  @transient override lazy val producedAttributes: AttributeSet = {
    AttributeSet(output.filterNot(attr => inputSet.contains(attr)))
  }

  @transient override lazy val references: AttributeSet = child.outputSet

  override def simpleString(maxFields: Int): String = {
    s"MergeRowsExec${truncatedString(output, "[", ", ", "]", maxFields)}"
  }

  protected override def doExecute(): RDD[InternalRow] = {
    child.execute().mapPartitions(processPartition)
  }

  private def createProjection(exprs: Seq[Expression], attrs: Seq[Attribute]): UnsafeProjection = {
    UnsafeProjection.create(exprs, attrs)
  }

  private def createPredicate(expr: Expression, attrs: Seq[Attribute]): BasePredicate = {
    GeneratePredicate.generate(expr, attrs)
  }

  private def applyProjection(
                               actions: Seq[(BasePredicate, Option[UnsafeProjection])],
                               inputRow: InternalRow): InternalRow = {

    // find the first action where the predicate evaluates to true
    // if there are overlapping conditions in actions, use the first matching action
    // in the example below, when id = 5, both actions match but the first one is applied
    //   WHEN MATCHED AND id > 1 AND id < 10 UPDATE *
    //   WHEN MATCHED AND id = 5 OR id = 21 DELETE

    val pair = actions.find {
      case (predicate, _) => predicate.eval(inputRow)
    }

    // apply the projection to produce an output row, or return null to suppress this row
    pair match {
      case Some((_, Some(projection))) =>
        val row = projection.apply(inputRow)
        row
      case _ =>
        null
    }
  }

  private def processPartition(rowIterator: Iterator[InternalRow]): Iterator[InternalRow] = {
    val inputAttrs = child.output

    val isSourceRowPresentPred = createPredicate(isSourceRowPresent, inputAttrs)
    val isTargetRowPresentPred = createPredicate(isTargetRowPresent, inputAttrs)

    val matchedPreds = matchedConditions.map(createPredicate(_, inputAttrs))
    val matchedProjs = matchedOutputs.map {
      case output if output.nonEmpty =>
        Some(createProjection(output, inputAttrs))
      case _ => None
    }
    val matchedPairs = matchedPreds zip matchedProjs

    val notMatchedPreds = notMatchedConditions.map(createPredicate(_, inputAttrs))
    val notMatchedProjs = notMatchedOutputs.map {
      case output if output.nonEmpty => Some(createProjection(output, inputAttrs))
      case _ => None
    }
    val nonMatchedPairs = notMatchedPreds zip notMatchedProjs

    val projectTargetCols = createProjection(targetOutput, inputAttrs)
    val rowIdProj = createProjection(rowIdAttrs, inputAttrs)

    // This method is responsible for processing a input row to emit the resultant row with an
    // additional column that indicates whether the row is going to be included in the final
    // output of merge or not.
    // 1. Found a target row for which there is no corresponding source row (join condition not met)
    //    - Only project the target columns if we need to output unchanged rows
    // 2. Found a source row for which there is no corresponding target row (join condition not met)
    //    - Apply the not matched actions (i.e INSERT actions) if non match conditions are met.
    // 3. Found a source row for which there is a corresponding target row (join condition met)
    //    - Apply the matched actions (i.e DELETE or UPDATE actions) if match conditions are met.
    def processRow(inputRow: InternalRow): InternalRow = {
      if (emitNotMatchedTargetRows && !isSourceRowPresentPred.eval(inputRow)) {
        projectTargetCols.apply(inputRow)
      } else if (!isTargetRowPresentPred.eval(inputRow)) {
        applyProjection(nonMatchedPairs, inputRow)
      } else {
        applyProjection(matchedPairs, inputRow)
      }
    }

    var lastMatchedRowId: InternalRow = null

    def processRowWithCardinalityCheck(inputRow: InternalRow): InternalRow = {
      val isSourceRowPresent = isSourceRowPresentPred.eval(inputRow)
      val isTargetRowPresent = isTargetRowPresentPred.eval(inputRow)

      if ((isSourceRowPresent && isTargetRowPresent) ||
        (isSourceRowPresent && !isTargetRowPresent && unMatchedRowCheck)) {
        val currentRowId = rowIdProj.apply(inputRow)
        if (currentRowId == lastMatchedRowId) {
          throw new SparkException(
            "The ON search condition of the MERGE statement matched a single row from " +
              "the target table with multiple rows of the source table. This could result " +
              "in the target row being operated on more than once with an update or delete " +
              "operation and is not allowed.")
        }
        lastMatchedRowId = currentRowId.copy()
      } else {
        lastMatchedRowId = null
      }

      if (emitNotMatchedTargetRows && !isSourceRowPresent) {
        projectTargetCols.apply(inputRow)
      } else if (!isTargetRowPresent) {
        applyProjection(nonMatchedPairs, inputRow)
      } else {
        applyProjection(matchedPairs, inputRow)
      }
    }

    val processFunc: InternalRow => InternalRow = if (performCardinalityCheck) {
      processRowWithCardinalityCheck
    } else {
      processRow
    }

    rowIterator
      .map(processFunc)
      .filter(row => row != null)
  }
}
