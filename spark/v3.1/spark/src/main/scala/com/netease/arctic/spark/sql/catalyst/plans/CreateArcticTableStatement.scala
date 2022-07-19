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

package com.netease.arctic.spark.sql.catalyst.plans

import org.apache.spark.sql.catalyst.catalog.BucketSpec
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.plans.logical.{Command, LogicalPlan, SerdeInfo}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType

case class CreateArcticTableStatement(tableName: Seq[String],
                                      tableSchema: StructType,
                                      partitioning: Seq[Transform],
                                      bucketSpec: Option[BucketSpec],
                                      properties: Map[String, String],
                                      provider: Option[String],
                                      options: Map[String, String],
                                      location: Option[String],
                                      comment: Option[String],
                                      serde: Option[SerdeInfo],
                                      primary: Seq[String],
                                      external: Boolean,
                                      ifNotExists: Boolean) extends Command {

  override lazy val output: Seq[Attribute] = Nil

  override def children: Seq[LogicalPlan] = Seq.empty

}
