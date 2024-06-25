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

package org.apache.amoro.spark

import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.analysis.{AlignedRowLevelIcebergCommandCheck, AlignRowLevelCommandAssignments, CheckMergeIntoTableConditions, MergeIntoIcebergTableResolutionCheck, ProcedureArgumentCoercion, ResolveMergeIntoTableReferences, ResolveProcedures, RewriteDeleteFromTable, RewriteMergeIntoTable, RewriteUpdateTable}
import org.apache.spark.sql.catalyst.optimizer._
import org.apache.spark.sql.catalyst.parser.extensions.IcebergSparkSqlExtensionsParser
import org.apache.spark.sql.execution.datasources.v2.{ExtendedDataSourceV2Strategy, ExtendedV2Writes, MixedFormatExtendedDataSourceV2Strategy, OptimizeMetadataOnlyDeleteFromTable, ReplaceRewrittenRowLevelCommand, RowLevelCommandScanRelationPushDown}
import org.apache.spark.sql.execution.dynamicpruning.RowLevelCommandDynamicPruning

import org.apache.amoro.spark.sql.catalyst.analysis._
import org.apache.amoro.spark.sql.catalyst.optimize.{OptimizeWriteRule, RewriteAppendMixedFormatTable, RewriteDeleteFromMixedFormatTable, RewriteUpdateMixedFormatTable}
import org.apache.amoro.spark.sql.catalyst.parser.MixedFormatSqlExtensionsParser
import org.apache.amoro.spark.sql.execution

class MixedFormatSparkExtensions extends (SparkSessionExtensions => Unit) {

  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectParser {
      case (_, parser) => new MixedFormatSqlExtensionsParser(parser)
    }
    // resolve mixed-format command
    extensions.injectResolutionRule { spark => ResolveMixedFormatCommand(spark) }
    extensions.injectResolutionRule { spark => ResolveMergeIntoMixedFormatTableReferences(spark) }
    extensions.injectResolutionRule { _ => MixedFormatAlignRowLevelCommandAssignments }
    extensions.injectResolutionRule { spark => RewriteMixedFormatMergeIntoTable(spark) }

    extensions.injectPostHocResolutionRule(spark => RewriteMixedFormatCommand(spark))

    // mixed-format optimizer rules
    extensions.injectPostHocResolutionRule { spark => QueryWithConstraintCheck(spark) }
    extensions.injectOptimizerRule { spark => RewriteAppendMixedFormatTable(spark) }
    extensions.injectOptimizerRule { spark => RewriteDeleteFromMixedFormatTable(spark) }
    extensions.injectOptimizerRule { spark => RewriteUpdateMixedFormatTable(spark) }

    // iceberg extensions
    extensions.injectResolutionRule { spark => ResolveMergeIntoTableReferences(spark) }
    extensions.injectResolutionRule { _ => CheckMergeIntoTableConditions }
    extensions.injectResolutionRule { _ => ProcedureArgumentCoercion }
    extensions.injectResolutionRule { _ => AlignRowLevelCommandAssignments }
    extensions.injectResolutionRule { _ => RewriteDeleteFromTable }
    extensions.injectResolutionRule { _ => RewriteUpdateTable }
    extensions.injectResolutionRule { _ => RewriteMergeIntoTable }
    extensions.injectCheckRule { _ => MergeIntoIcebergTableResolutionCheck }
    extensions.injectCheckRule { _ => AlignedRowLevelIcebergCommandCheck }

    extensions.injectOptimizerRule { _ => ExtendedSimplifyConditionalsInPredicate }
    extensions.injectOptimizerRule { _ => ExtendedReplaceNullWithFalseInPredicate }

    extensions.injectPreCBORule { _ => OptimizeMetadataOnlyDeleteFromTable }
    extensions.injectPreCBORule { _ => RowLevelCommandScanRelationPushDown }
    extensions.injectPreCBORule { _ => ExtendedV2Writes }
    extensions.injectPreCBORule { spark => RowLevelCommandDynamicPruning(spark) }
    extensions.injectPreCBORule { _ => ReplaceRewrittenRowLevelCommand }

    // planner extensions
    extensions.injectPlannerStrategy { spark => MixedFormatExtendedDataSourceV2Strategy(spark) }
    // mixed-format optimizer rules
    extensions.injectPreCBORule(OptimizeWriteRule)

    // mixed-format strategy rules
    extensions.injectPlannerStrategy { spark => execution.ExtendedMixedFormatStrategy(spark) }

    // iceberg sql parser extensions
    extensions.injectParser { case (_, parser) => new IcebergSparkSqlExtensionsParser(parser) }

    // iceberg procedure analyzer extensions
    extensions.injectResolutionRule { spark => ResolveProcedures(spark) }
  }

}
