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

package org.apache.amoro.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.expressions.{Attribute, AttributeReference}
import org.apache.spark.sql.catalyst.plans.logical.{Command, LogicalPlan}
import org.apache.spark.sql.catalyst.util.truncatedString

import org.apache.amoro.spark.command.{MigrateToMixedFormatCommand, MixedFormatSparkCommand}

abstract class MixedFormatCommandLogicalPlan(command: MixedFormatSparkCommand) extends Command {
  override def output: Seq[Attribute] = {
    command.outputType().map(f => AttributeReference(f.name, f.dataType, f.nullable, f.metadata)())
  }

  override def simpleString(maxFields: Int): String = {
    s"${command.name()}LogicPlan${truncatedString(output, "[", ",", "]", maxFields)} ${command.execInfo}"
  }
}

case class MigrateToMixedFormatLogicalPlan(command: MigrateToMixedFormatCommand)
  extends MixedFormatCommandLogicalPlan(command) {
  override def children: Seq[LogicalPlan] = Nil

  override protected def withNewChildrenInternal(newChildren: IndexedSeq[LogicalPlan])
      : LogicalPlan = null
}
