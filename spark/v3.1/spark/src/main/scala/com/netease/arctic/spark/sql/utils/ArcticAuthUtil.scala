/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.sql.utils

import com.netease.arctic.spark.sql.utils.ArcticAuthUtil.ArcticActionType.ArcticActionType
import com.netease.arctic.spark.sql.utils.ArcticAuthUtil.ArcticCommandType.ArcticCommandType
import com.netease.arctic.spark.sql.utils.ArcticAuthUtil.ArcticOperationType.ArcticOperationType

object ArcticAuthUtil {

  def operationType(command: String): ArcticOperationType = {
    command match {
      case "ReplaceArcticData" |
          "AppendArcticData" |
          "OverwriteArcticData" |
          "OverwriteArcticDataByExpression" => ArcticOperationType.QUERY
      case "CreateArcticTableAsSelect" => ArcticOperationType.CREATETABLE_AS_SELECT
    }
  }

  def actionType(command: String): ArcticActionType = {
    command match {
      case "ReplaceArcticData" |
          "OverwriteArcticData" |
          "OverwriteArcticDataByExpression" => ArcticActionType.UPDATE
      case "AppendArcticData" => ArcticActionType.INSERT
      case "CreateArcticTableAsSelect" => ArcticActionType.OTHER
    }
  }

  def commandType(command: String): Seq[ArcticCommandType] = {
    command match {
      case "ReplaceArcticData" | "AppendArcticData" =>
        Seq(ArcticCommandType.HasTableAsIdentifierOption, ArcticCommandType.HasQueryAsLogicalPlan)
      case "OverwriteArcticData" |
          "OverwriteArcticDataByExpression" =>
        Seq(ArcticCommandType.HasTableAsIdentifierOption, ArcticCommandType.HasQueryAsLogicalPlan)
      case "CreateArcticTableAsSelect" =>
        Seq(ArcticCommandType.HasTableNameAsIdentifier, ArcticCommandType.HasQueryAsLogicalPlan)
    }
  }

  def isArcticCommand(command: String): Boolean = {
    command.contains("arctic")
  }

  object ArcticCommandType extends Enumeration {
    type ArcticCommandType = Value
    val HasChildAsIdentifier, HasQueryAsLogicalPlan, HasTableAsIdentifier,
        HasTableAsIdentifierOption, HasTableNameAsIdentifier = Value
  }

  object ArcticActionType extends Enumeration {
    type ArcticActionType = Value

    val OTHER, INSERT, INSERT_OVERWRITE, UPDATE, DELETE = Value
  }

  object ArcticOperationType extends Enumeration {
    type ArcticOperationType = Value

    val QUERY, CREATETABLE_AS_SELECT = Value
  }

}
