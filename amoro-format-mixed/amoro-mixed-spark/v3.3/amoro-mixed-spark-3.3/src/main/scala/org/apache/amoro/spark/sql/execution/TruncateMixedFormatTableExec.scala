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

import org.apache.iceberg.expressions.Expressions
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.execution.datasources.v2.LeafV2CommandExec

import org.apache.amoro.op.OverwriteBaseFiles
import org.apache.amoro.spark.table.{MixedSparkTable, UnkeyedSparkTable}

case class TruncateMixedFormatTableExec(table: Table) extends LeafV2CommandExec {
  override protected def run(): Seq[InternalRow] = {
    table match {
      case mixedSparkTable: MixedSparkTable =>
        if (mixedSparkTable.table().isKeyedTable) {
          val txId = mixedSparkTable.table().asKeyedTable().beginTransaction(null);
          val overwriteBaseFiles: OverwriteBaseFiles =
            mixedSparkTable.table().asKeyedTable().newOverwriteBaseFiles()
          overwriteBaseFiles.overwriteByRowFilter(Expressions.alwaysTrue())
          overwriteBaseFiles.updateOptimizedSequenceDynamically(txId)
          overwriteBaseFiles.commit()
        } else {
          val overwriteFiles = mixedSparkTable.table().asUnkeyedTable().newOverwrite()
          overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue())
          overwriteFiles.commit()
        }
      case unkeyedSparkTable: UnkeyedSparkTable =>
        val overwriteFiles = unkeyedSparkTable.table().newOverwrite()
        overwriteFiles.overwriteByRowFilter(Expressions.alwaysTrue())
        overwriteFiles.commit()
    }
    Nil
  }

  override def output: Seq[Attribute] = Nil
}
