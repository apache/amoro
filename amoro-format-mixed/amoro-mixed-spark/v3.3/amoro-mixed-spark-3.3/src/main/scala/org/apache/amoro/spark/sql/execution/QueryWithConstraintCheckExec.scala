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

package org.apache.amoro.spark.sql.execution

import org.apache.spark.SparkException
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.{Attribute, SortOrder}
import org.apache.spark.sql.catalyst.plans.physical
import org.apache.spark.sql.execution.{BinaryExecNode, SparkPlan}
import org.apache.spark.sql.vectorized.ColumnarBatch

case class QueryWithConstraintCheckExec(
    scanExec: SparkPlan,
    fileFilterExec: SparkPlan)
  extends BinaryExecNode {

  override protected def doPrepare(): Unit = {
    val rows = fileFilterExec.executeCollect()
    if (rows.length > 0) {
      throw new SparkException(
        "There are multiple duplicate primary key data in the inserted data, " +
          "which cannot guarantee the uniqueness of the primary key. ")
    }
  }

  override def left: SparkPlan = scanExec

  override def right: SparkPlan = fileFilterExec

  override def output: Seq[Attribute] = scanExec.output

  override def outputPartitioning: physical.Partitioning = scanExec.outputPartitioning

  override def outputOrdering: Seq[SortOrder] = scanExec.outputOrdering

  override def supportsColumnar: Boolean = scanExec.supportsColumnar

  override protected def withNewChildrenInternal(
      newLeft: SparkPlan,
      newRight: SparkPlan): SparkPlan = {
    copy(scanExec = newLeft, fileFilterExec = newRight)
  }

  override protected def doExecute(): RDD[InternalRow] = {
    val result = scanExec.execute()
    if (result.partitions.length == 0) {
      sparkContext.parallelize(Array.empty[InternalRow], 1)
    } else {
      result
    }
  }

  override protected def doExecuteColumnar(): RDD[ColumnarBatch] = {
    val result = scanExec.executeColumnar()
    if (result.partitions.length == 0) {
      sparkContext.parallelize(Array.empty[ColumnarBatch], 1)
    } else {
      result
    }
  }
}
