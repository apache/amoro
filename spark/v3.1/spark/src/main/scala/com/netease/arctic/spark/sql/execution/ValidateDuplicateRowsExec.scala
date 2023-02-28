package com.netease.arctic.spark.sql.execution

import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.codegen.GeneratePredicate
import org.apache.spark.sql.catalyst.expressions.{Ascending, Attribute, AttributeSet, BasePredicate, Expression, SortOrder, UnsafeProjection}
import org.apache.spark.sql.catalyst.util.truncatedString
import org.apache.spark.sql.execution.{SparkPlan, UnaryExecNode}

case class ValidateDuplicateRowsExec(rowIdAttrs: Seq[Attribute],
                                     output: Seq[Attribute],
                                     child: SparkPlan) extends UnaryExecNode {

  override def requiredChildOrdering: Seq[Seq[SortOrder]] = {
      // request a local sort by the row ID attrs to co-locate matches for the same target row
      Seq(output.map(attr => SortOrder(attr, Ascending)))

  }

  protected override def doExecute(): RDD[InternalRow] = {
    child.execute().mapPartitions(processPartition)
  }

  private def createProjection(exprs: Seq[Expression], attrs: Seq[Attribute]): UnsafeProjection = {
    UnsafeProjection.create(exprs, attrs)
  }

  private def processPartition(rowIterator: Iterator[InternalRow]): Iterator[InternalRow] = {
    val inputAttrs = child.output
    val rowIdProj = createProjection(rowIdAttrs, inputAttrs)
    var lastMatchedRowId: InternalRow = null
    var i2: Int = 0;
    System.out.println("init =============== i2 " + i2)
    def processRowWithMatchedOrUnMatchedRowCheck(inputRow: InternalRow): InternalRow = {

      System.out.println("final1 =============== i2 " + i2)
      val currentRowId = rowIdProj.apply(inputRow)
      if (currentRowId == lastMatchedRowId) {
        throw new SparkException(
          "The ON search condition of the MERGE statement matched a single row from " +
            "the target table with multiple rows of the source table. ")
      }
      lastMatchedRowId = currentRowId.copy()
      i2 = i2 + 1;
      System.out.println("final2 =============== i2 " + i2)
      inputRow
    }

    rowIterator
      .map(processRowWithMatchedOrUnMatchedRowCheck)
      .filter(row => row != null)
  }
}
