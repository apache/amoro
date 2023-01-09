package com.netease.arctic.spark.sql.catalyst.analysis

import com.netease.arctic.spark.SparkSQLProperties
import com.netease.arctic.spark.sql.catalyst.plans
import com.netease.arctic.spark.sql.catalyst.plans.WriteMerge
import com.netease.arctic.spark.sql.utils.FieldReference
import com.netease.arctic.spark.sql.utils.RowDeltaUtils.{DELETE_OPERATION, INSERT_OPERATION, OPERATION_COLUMN, UPDATE_OPERATION}
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.catalyst.analysis.EliminateSubqueryAliases
import org.apache.spark.sql.catalyst.expressions.Literal.TrueLiteral
import org.apache.spark.sql.catalyst.expressions.{Alias, Attribute, AttributeReference, ExprId, Expression, ExtendedV2ExpressionUtils, IsNotNull, Literal}
import org.apache.spark.sql.catalyst.plans.logical._
import org.apache.spark.sql.catalyst.plans.{Inner, MergeIntoArcticTable, RightOuter}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.NamedReference
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation
import org.apache.spark.sql.types.IntegerType

import scala.collection.mutable


object RewriteMergeIntoTable extends Rule[LogicalPlan] {

  private final val ROW_FROM_SOURCE = "__row_from_source"
  private final val ROW_FROM_TARGET = "__row_from_target"

  private final val ROW_FROM_SOURCE_REF = FieldReference(ROW_FROM_SOURCE)
  private final val ROW_FROM_TARGET_REF = FieldReference(ROW_FROM_TARGET)

  override def apply(plan: LogicalPlan): LogicalPlan = plan resolveOperators {
    case m@MergeIntoArcticTable(aliasedTable, source, cond, matchedActions, notMatchedActions, None) =>

      EliminateSubqueryAliases(aliasedTable) match {
        case r@DataSourceV2Relation(tbl, _, _, _, _) =>
          val rewritePlan =
            buildWriteDeltaPlan(r, tbl, source, cond, matchedActions, notMatchedActions)

          rewritePlan

        case p =>
          throw new UnsupportedOperationException(s"$p is not an Arctic table")
      }
  }

  def checkConditionIsPrimaryKey(table: Table, cond: Expression): Unit = {
    var validate: Boolean = true
    table match {
      case arctic: ArcticSparkTable =>
        val primarys = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
        val condRefs = cond.references.filter(f => primarys.contains(f.name))
        if (condRefs.isEmpty) {
          throw new UnsupportedOperationException(s"Condition ${cond.references}. is not allowed because is not a primary key")
        }
    }
  }

  def resolveRowIdAttrs(relation: DataSourceV2Relation, cond: Expression): Seq[Attribute] = {
    relation.table match {
      case arctic: ArcticSparkTable =>
        val primarys = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
        cond.references.filter(p => primarys.contains(p.name)).toSeq
    }
  }

  // build a rewrite plan for sources that support row deltas
  private def buildWriteDeltaPlan(
                                   relation: DataSourceV2Relation,
                                   operationTable: Table,
                                   source: LogicalPlan,
                                   cond: Expression,
                                   matchedActions: Seq[MergeAction],
                                   notMatchedActions: Seq[MergeAction]): WriteMerge = {
    checkConditionIsPrimaryKey(relation.table, cond)
    // construct a scan relation and include all required metadata columns
    val keyAttrs = resolveRowIdAttrs(relation, cond)
    val readRelation = buildRelationWithAttrs(relation, operationTable)
    val readAttrs = readRelation.output

    // project an extra column to check if a target row exists after the join
    val targetTableProjExprs = readAttrs :+ Alias(TrueLiteral, ROW_FROM_TARGET)()
    val targetTableProj = Project(targetTableProjExprs, readRelation)

    // project an extra column to check if a source row exists after the join
    val sourceTableProjExprs = source.output :+ Alias(TrueLiteral, ROW_FROM_SOURCE)()
    val sourceTableProj = Project(sourceTableProjExprs, source)

    // use inner join if there is no NOT MATCHED action, unmatched source rows can be discarded
    // use right outer join in all other cases, unmatched source rows may be needed
    // also disable broadcasts for the target table to perform the cardinality check
    val joinType = if (notMatchedActions.isEmpty) Inner else RightOuter
    val joinHint = JoinHint(leftHint = Some(HintInfo(Some(NO_BROADCAST_HASH))), rightHint = None)
    val joinPlan = Join(targetTableProj, sourceTableProj, joinType, Some(cond), joinHint)

    val matchedConditions = matchedActions.map(actionCondition)
    val matchedOutputs = matchedActions.map(deltaActionOutput(_, relation.output, source.output))

    val notMatchedConditions = notMatchedActions.map(actionCondition)
    val notMatchedOutputs = notMatchedActions.map(deltaActionOutput(_, relation.output, source.output))

    val operationTypeAttr = AttributeReference(OPERATION_COLUMN, IntegerType, nullable = false)()
    val rowFromSourceAttr = resolveAttrRef(ROW_FROM_SOURCE_REF, joinPlan)
    val rowFromTargetAttr = resolveAttrRef(ROW_FROM_TARGET_REF, joinPlan)

    // merged rows must contain values for the operation type and all read attrs
    val mergeRowsOutput = buildMergeRowsOutput(matchedOutputs, notMatchedOutputs, operationTypeAttr +: readAttrs)

    val mergeRows = plans.MergeRows(
      isSourceRowPresent = IsNotNull(rowFromSourceAttr),
      isTargetRowPresent = if (notMatchedActions.isEmpty) TrueLiteral else IsNotNull(rowFromTargetAttr),
      matchedConditions = matchedConditions,
      matchedOutputs = matchedOutputs,
      notMatchedConditions = notMatchedConditions,
      notMatchedOutputs = notMatchedOutputs,
      // only needed if emitting unmatched target rows
      targetOutput = Nil,
      rowIdAttrs = keyAttrs,
      performCardinalityCheck = isCardinalityCheckNeeded(matchedActions),
      unMatchedRowCheck = java.lang.Boolean.valueOf(SparkSQLProperties.CHECK_DATA_DUPLICATES_ENABLE_DEFAULT),
      emitNotMatchedTargetRows = false,
      output = mergeRowsOutput,
      joinPlan)

    // build a plan to write the row delta to the table
    val writeRelation = relation.copy(table = operationTable)
    var options: Map[String, String] = Map.empty
    options += (WriteMode.WRITE_MODE_KEY -> WriteMode.MERGE.toString)
    WriteMerge(writeRelation, mergeRows, options)
  }

  private def actionCondition(action: MergeAction): Expression = {
    action.condition.getOrElse(TrueLiteral)
  }

  def buildRelationWithAttrs(
                                        relation: DataSourceV2Relation,
                                        table: Table): DataSourceV2Relation = {

    val attrs = dedupAttrs(relation.output)
    relation.copy(table = table, output = attrs)
  }

  def dedupAttrs(attrs: Seq[AttributeReference]): Seq[AttributeReference] = {
    val exprIds = mutable.Set.empty[ExprId]
    attrs.flatMap { attr =>
      if (exprIds.contains(attr.exprId)) {
        None
      } else {
        exprIds += attr.exprId
        Some(attr)
      }
    }
  }


  private def deltaActionOutput(
                                 action: MergeAction,
                                 targetOutput: Seq[Expression],
                                 sourceOutput: Seq[Attribute]): Seq[Expression] = {

    action match {
      case u: UpdateAction =>
        Seq(Literal(UPDATE_OPERATION)) ++ targetOutput ++ sourceOutput

      case _: DeleteAction =>
        Seq(Literal(DELETE_OPERATION)) ++ targetOutput ++ sourceOutput

      case i: InsertAction =>
        Seq(Literal(INSERT_OPERATION)) ++ targetOutput ++ sourceOutput

      case other =>
        throw new UnsupportedOperationException(s"Unexpected action: $other")
    }
  }

  private def buildMergeRowsOutput(
                                    matchedOutputs: Seq[Seq[Expression]],
                                    notMatchedOutputs: Seq[Seq[Expression]],
                                    attrs: Seq[Attribute]): Seq[Attribute] = {

    // collect all outputs from matched and not matched actions (ignoring DELETEs)
    val outputs = matchedOutputs.filter(_.nonEmpty) ++ notMatchedOutputs.filter(_.nonEmpty)

    // build a correct nullability map for output attributes
    // an attribute is nullable if at least one matched or not matched action may produce null
    val nullabilityMap = attrs.indices.map { index =>
      index -> outputs.exists(output => output(index).nullable)
    }.toMap

    attrs.zipWithIndex.map { case (attr, index) =>
      attr.withNullability(nullabilityMap(index))
    }
  }

  private def isCardinalityCheckNeeded(actions: Seq[MergeAction]): Boolean = actions match {
    case Seq(DeleteAction(None)) => false
    case _ => true
  }


  private def resolveAttrRef(ref: NamedReference, plan: LogicalPlan): AttributeReference = {
    ExtendedV2ExpressionUtils.resolveRef[AttributeReference](ref, plan)
  }
}
