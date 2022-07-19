
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

package com.netease.arctic.spark

import com.netease.arctic.spark.sql.catalyst.analysis.ResolveArcticCommand
import com.netease.arctic.spark.sql.catalyst.parser.ArcticSqlExtensionsParser
import com.netease.arctic.spark.sql.execution
import com.netease.arctic.spark.sql.optimize.OptimizeWriteRule
import org.apache.spark.sql.SparkSessionExtensions
import org.apache.spark.sql.catalyst.analysis.{AlignRowLevelOperations, RowLevelOperationsPredicateCheck}
import org.apache.spark.sql.catalyst.optimizer._

class ArcticSparkExtensions extends (SparkSessionExtensions => Unit) {

  override def apply(extensions: SparkSessionExtensions): Unit = {
    extensions.injectParser {
      case (_, parser) => new ArcticSqlExtensionsParser(parser)
    }
    // analyzer extensions
    extensions.injectPostHocResolutionRule { _ => AlignRowLevelOperations }
    extensions.injectResolutionRule{ spark => ResolveArcticCommand(spark) }
    extensions.injectCheckRule { _ => RowLevelOperationsPredicateCheck }

    // optimizer extensions
    extensions.injectOptimizerRule { _ => OptimizeConditionsInRowLevelOperations }
    extensions.injectOptimizerRule { _ => PullupCorrelatedPredicatesInRowLevelOperations }
    extensions.injectOptimizerRule { spark => RewriteDelete(spark) }
    extensions.injectOptimizerRule { spark => RewriteUpdate(spark) }
    extensions.injectOptimizerRule { spark => RewriteMergeInto(spark) }
    extensions.injectPreCBORule(OptimizeWriteRule)

    // planner extensions
    extensions.injectPlannerStrategy { spark => execution.ExtendedArcticUnkeyedStrategy(spark) }
    extensions.injectPlannerStrategy { spark => execution.ExtendedArcticCommandStrategy(spark) }
  }
}
