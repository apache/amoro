package com.netease.arctic.spark.sql.optimize

import com.netease.arctic.spark.sql.ArcticExtensionUtils.{ArcticTableHelper, asTableRelation, isArcticRelation}
import com.netease.arctic.spark.sql.catalyst.plans.{ReplaceArcticData, WriteMerge}
import com.netease.arctic.spark.sql.utils.ArcticRewriteHelper
import com.netease.arctic.spark.table.ArcticSparkTable
import com.netease.arctic.spark.writer.WriteMode
import org.apache.spark.sql.catalyst.analysis.EliminateSubqueryAliases
import org.apache.spark.sql.catalyst.expressions.{Alias, And, AttributeReference, Cast, EqualNullSafe, EqualTo, Expression, Literal, NamedExpression, Not}
import org.apache.spark.sql.catalyst.plans.logical.{Assignment, DeleteAction, Filter, HintInfo, InsertAction, Join, JoinHint, LogicalPlan, NO_BROADCAST_HASH, Project, Union, UpdateAction}
import org.apache.spark.sql.catalyst.plans.{LeftAnti, LeftOuter, MergeIntoArcticTable}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.connector.expressions.NamedReference
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Implicits.TableHelper
import org.apache.spark.sql.execution.datasources.v2.{DataSourceV2Relation, DataSourceV2ScanRelation}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.sql.{AnalysisException, SparkSession}

import java.util

case class RewriteArcticMergeInto(spark: SparkSession) extends Rule[LogicalPlan] with ArcticRewriteHelper{

  private var ROW_INDEX: Int = 1


  def checkConditionIsPrimaryKey(arctic: ArcticSparkTable, cond: Expression): Unit = {
    val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
    if (!primaries.contains(cond.references.head.name)) {
      throw new UnsupportedOperationException("condition must be primary key")
    }
  }

  override def apply(plan: LogicalPlan): LogicalPlan = plan transform {
    case m @MergeIntoArcticTable(aliasedTable, source, cond, matchedActions, notMatchedActions, None)
      if matchedActions.nonEmpty && isArcticRelation(aliasedTable)=>
        EliminateSubqueryAliases(aliasedTable) match {
          case r@DataSourceV2Relation(_, _, _, _, _) => {
            if (r.table.isInstanceOf[ArcticSparkTable]) {
              val arctic = r.table.asArcticTable
              checkConditionIsPrimaryKey(arctic, cond)
              var joinPlanDelete: LogicalPlan = null
              var joinPlanUpdate: LogicalPlan = null
              var joinPlanInsert: LogicalPlan = null
              var mergeQuery: LogicalPlan = null
              var condition: Expression = null
              matchedActions.foreach {
                case DeleteAction(Some(deleteCond)) =>
                  val scanBuilder = r.table.asReadable.newScanBuilder(CaseInsensitiveStringMap.empty())
                  val matchingRowsPlanBuilder = scanRelation => Filter(deleteCond, scanRelation)
                  val query = DataSourceV2ScanRelation(r, scanBuilder.build(), r.output)
                  val matchingRowsPlan = matchingRowsPlanBuilder(query)
                  if (ROW_INDEX != 1) {
                    val deleteProjection = buildDeleteProjection(r, matchingRowsPlan)
                    val joinPlan = Join(deleteProjection, source, LeftOuter, Some(cond), JoinHint.NONE)
                    joinPlanDelete = Filter(condition, joinPlan)
                    mergeQuery = Union(mergeQuery, joinPlanDelete)
                    condition = And(condition, Not(deleteCond))
                  } else {
                    condition = Not(deleteCond)
                    val deleteProjection = buildDeleteProjection(r, matchingRowsPlan)
                    joinPlanDelete = Join(deleteProjection, source, LeftOuter, Some(cond), JoinHint.NONE)
                    mergeQuery = joinPlanDelete
                  }
                case UpdateAction(Some(updateCond), assignments) =>
                  val scanBuilder = r.table.asReadable.newScanBuilder(CaseInsensitiveStringMap.empty())
                  val matchingRowsPlanBuilder = scanRelation => Filter(updateCond, scanRelation)
                  val query = DataSourceV2ScanRelation(r, scanBuilder.build(), r.output)
                  val matchingRowsPlan = matchingRowsPlanBuilder(query)
                  if (ROW_INDEX != 1) {
                    val updateProjection = buildUpdateProjection(r, matchingRowsPlan)
                    val plan1 = buildUpdateInsertProjection(source, source, assignments)
                    val joinPlan = Join(updateProjection, plan1, LeftOuter, Some(cond), JoinHint.NONE)
                    joinPlanUpdate = Filter(condition, joinPlan)
                    mergeQuery = Union(mergeQuery, joinPlanUpdate)
                    condition = And(condition, Not(updateCond))
                  } else {
                    condition = Not(updateCond)
                    val updateProjection = buildUpdateProjection(r, matchingRowsPlan)
                    joinPlanUpdate = Join(updateProjection, source, LeftOuter, Some(cond), JoinHint.NONE)
                    mergeQuery = joinPlanUpdate
                  }
                case _ =>
              }
              val insert = notMatchedActions match {
                case Seq(InsertAction(Some(insertCond), _)) =>
                  val insertProjection = buildInsertProjection(source)
                  val join = Filter(insertCond, insertProjection)
                  val joinHint = JoinHint(leftHint = Some(HintInfo(Some(NO_BROADCAST_HASH))), rightHint = None)
                  joinPlanInsert = Join(join, r, LeftOuter, Some(cond), joinHint)
                  val primaries = arctic.table().asKeyedTable().primaryKeySpec().fieldNames()
                  val joinCondition = buildJoinCondition(primaries, r, joinPlanInsert)
                  joinPlanInsert = Filter(Not(joinCondition), joinPlanInsert)
                  joinPlanInsert
              }
              mergeQuery = Union(mergeQuery, insert)
              var options: Map[String, String] = Map.empty
              options += (WriteMode.WRITE_MODE_KEY -> WriteMode.MERGE.toString)
              WriteMerge(r, mergeQuery, options)
//              mergeQuery
            } else {
              throw new UnsupportedOperationException("Table does not support row level operations")
            }
          }
        }

      case c@MergeIntoArcticTable(aliasedTable, source, cond, matchedActions, notMatchedActions, None)
        if matchedActions.isEmpty && notMatchedActions.size == 1 && isArcticRelation(aliasedTable)=>
        EliminateSubqueryAliases(aliasedTable) match {
          case r: DataSourceV2Relation =>
            val insertAction = notMatchedActions.head.asInstanceOf[InsertAction]
            var joinPlanInsert: LogicalPlan = null
            val insertProjection = buildInsertProjection(source)
            val join = insertAction.condition match {
              case Some(insertCond) => Filter(insertCond, insertProjection)
              case None => insertProjection
            }
            val joinHint = JoinHint(leftHint = Some(HintInfo(Some(NO_BROADCAST_HASH))), rightHint = None)
            joinPlanInsert = Join(join, r, LeftOuter, Some(cond), joinHint)
            val insertExp = EqualNullSafe(r.output.find(_.name.equals("id")).get, join.output.find(_.name.equals("id")).get)
            joinPlanInsert = Filter(Not(insertExp), joinPlanInsert)
            joinPlanInsert
          case _ =>
            throw new UnsupportedOperationException("Table does not support row level operations")
        }
  }

  def resolveRef[T <: NamedExpression](ref: NamedReference, plan: LogicalPlan): T = {
    plan.resolve(ref.fieldNames.toSeq, conf.resolver) match {
      case Some(namedExpr) =>
        namedExpr.asInstanceOf[T]
      case None =>
        throw new UnsupportedOperationException("Table does not support row level operations")
    }
  }

  def buildInsertProjection(matchingRowsPlan: LogicalPlan): LogicalPlan = {
    val projection = Project(Seq(Alias(Literal("I"), "_arctic_upsert_op")()) ++ matchingRowsPlan.output, matchingRowsPlan)
    projection
  }

  def buildDeleteProjection(
                             table: LogicalPlan,
                             matchingRowsPlan: LogicalPlan): LogicalPlan = {
    val deleteProjection = Project(Seq(Alias(Literal("D"), "_arctic_upsert_op")()) ++ table.output, matchingRowsPlan)
    ROW_INDEX += 1
    deleteProjection
  }

  private def buildUpdateProjection(
                                     relation: DataSourceV2Relation,
                                     scanPlan: LogicalPlan): LogicalPlan = {
    val project = Project(Seq(Alias(Literal("U"), "_arctic_upsert_op")()) ++ relation.output, scanPlan)
    ROW_INDEX += 1
    project
  }

  def buildJoinCondition(primaries: util.List[String], r: DataSourceV2Relation, insertPlan: LogicalPlan): Expression = {
    var i = 0
    var joinCondition: Expression = null
    val expressions = new util.ArrayList[Expression]
    while (i < primaries.size) {
      val primary = primaries.get(i)
      val primaryAttr = r.output.find(_.name == primary).get
      val joinAttribute = insertPlan.output.find(_.name == primary).get
      val experssion = EqualNullSafe(primaryAttr, joinAttribute)
      expressions.add(experssion)
      i += 1
    }
    expressions.forEach(experssion => {
      if (joinCondition == null) {
        joinCondition = experssion
      } else {
        joinCondition = And(joinCondition, experssion)
      }
    });
    joinCondition
  }


  private def buildUpdateInsertProjection(relation: LogicalPlan,
                                                    scanPlan: LogicalPlan,
                                                    assignments: Seq[Assignment]): LogicalPlan = {
    val output = relation.output
    val assignmentMap = assignments.map(
      a =>
        if (a.value.dataType.catalogString.equals(a.key.dataType.catalogString)) {
          a.key.asInstanceOf[AttributeReference].name -> a.value
        } else {
          a.key.asInstanceOf[AttributeReference].name -> Cast(a.value, a.key.dataType)
        }
    ).toMap
    val outputWithValues = output.map(a => {
      if (assignmentMap.contains(a.name)) {
        Alias(assignmentMap(a.name), a.name)()
      } else {
        a
      }
    })
    Project(outputWithValues, scanPlan)
  }


}
