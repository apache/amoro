/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.spark.sql.catalyst.analysis

import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.catalyst.analysis.MixedFormatAssignmentAlignmentSupport
import org.apache.spark.sql.catalyst.expressions.{Alias, AttributeReference, Expression, ExtractValue, GetStructField}
import org.apache.spark.sql.catalyst.plans.logical.Assignment
import org.apache.spark.sql.catalyst.plans.logical.DeleteAction
import org.apache.spark.sql.catalyst.plans.logical.InsertAction
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.plans.logical.UpdateAction
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.catalyst.util.CharVarcharUtils

import org.apache.amoro.spark.sql.catalyst.plans.{MergeIntoMixedFormatTable, UnresolvedMergeIntoMixedFormatTable}

/**
 * A rule that aligns assignments in UPDATE and MERGE operations.
 *
 * Note that this rule must be run before rewriting row-level commands.
 */
object MixedFormatAlignRowLevelCommandAssignments
  extends Rule[LogicalPlan] with MixedFormatAssignmentAlignmentSupport {

  override def apply(plan: LogicalPlan): LogicalPlan = plan resolveOperators {
    case m: MergeIntoMixedFormatTable if m.resolved && !m.aligned =>
      val alignedMatchedActions = m.matchedActions.map {
        case u @ UpdateAction(_, assignments) =>
          u.copy(assignments = alignAssignments(m.targetTable, assignments))
        case d: DeleteAction =>
          d
        case _ =>
          throw new AnalysisException(
            "Matched actions can only contain UPDATE or DELETE",
            Map.empty[String, String])
      }

      val alignedNotMatchedActions = m.notMatchedActions.map {
        case i @ InsertAction(_, assignments) =>
          // check no nested columns are present
          val refs = assignments.map(_.key).map(toAssignmentRef)
          refs.foreach { ref =>
            if (ref.size > 1) {
              throw new AnalysisException(
                "Nested fields are not supported inside INSERT clauses of MERGE operations: " +
                  s"${ref.mkString("`", "`.`", "`")}",
                Map.empty[String, String])
            }
          }

          val colNames = refs.map(_.head)

          // check there are no duplicates
          val duplicateColNames = colNames.groupBy(identity).collect {
            case (name, matchingNames) if matchingNames.size > 1 => name
          }

          if (duplicateColNames.nonEmpty) {
            throw new AnalysisException(
              s"Duplicate column names inside INSERT clause: ${duplicateColNames.mkString(", ")}",
              Map.empty[String, String])
          }

          // reorder assignments by the target table column order
          val assignmentMap = colNames.zip(assignments).toMap
          i.copy(assignments = alignInsertActionAssignments(m.targetTable, assignmentMap))

        case _ =>
          throw new AnalysisException(
            "Not matched actions can only contain INSERT",
            Map.empty[String, String])
      }

      m.copy(matchedActions = alignedMatchedActions, notMatchedActions = alignedNotMatchedActions)
  }

  private def alignInsertActionAssignments(
      targetTable: LogicalPlan,
      assignmentMap: Map[String, Assignment]): Seq[Assignment] = {

    val resolver = conf.resolver

    targetTable.output.map { targetAttr =>
      val assignment = assignmentMap
        .find { case (name, _) => resolver(name, targetAttr.name) }
        .map { case (_, assignment) => assignment }

      if (assignment.isEmpty) {
        throw new AnalysisException(
          s"Cannot find column '${targetAttr.name}' of the target table among " +
            s"the INSERT columns: ${assignmentMap.keys.mkString(", ")}. " +
            "INSERT clauses must provide values for all columns of the target table.",
          Map.empty[String, String])
      }

      val key = assignment.get.key
      val value = castIfNeeded(targetAttr, assignment.get.value, resolver)
      handleCharVarcharLimits(Assignment(key, value))
    }
  }

  private def toAssignmentRef(expr: Expression): Seq[String] = expr match {
    case attr: AttributeReference =>
      Seq(attr.name)
    case Alias(child, _) =>
      toAssignmentRef(child)
    case GetStructField(child, _, Some(name)) =>
      toAssignmentRef(child) :+ name
    case other: ExtractValue =>
      throw new AnalysisException(
        s"Updating nested fields is only supported for structs: $other",
        Map.empty[String, String])
    case other =>
      throw new AnalysisException(
        s"Cannot convert to a reference, unsupported expression: $other",
        Map.empty[String, String])
  }

  private def handleCharVarcharLimits(assignment: Assignment): Assignment = {
    val key = assignment.key
    val value = assignment.value

    val rawKeyType = key.transform {
      case attr: AttributeReference =>
        CharVarcharUtils.getRawType(attr.metadata)
          .map(attr.withDataType)
          .getOrElse(attr)
    }.dataType

    if (CharVarcharUtils.hasCharVarchar(rawKeyType)) {
      val newKey = key.transform {
        case attr: AttributeReference => CharVarcharUtils.cleanAttrMetadata(attr)
      }
      val newValue = CharVarcharUtils.stringLengthCheck(value, rawKeyType)
      Assignment(newKey, newValue)
    } else {
      assignment
    }
  }
}
