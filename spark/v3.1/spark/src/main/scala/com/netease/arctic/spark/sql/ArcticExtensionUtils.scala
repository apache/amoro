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

package com.netease.arctic.spark.sql

import com.netease.arctic.spark.{ArcticSparkCatalog, ArcticSparkSessionCatalog}
import com.netease.arctic.spark.table.{ArcticSparkTable, SupportsUpsert}
import org.apache.spark.sql.connector.catalog.{Table, TableCatalog}
import org.apache.spark.sql.catalyst.plans.logical.{LogicalPlan, Project, SubqueryAlias}
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

import scala.annotation.tailrec


object ArcticExtensionUtils {

  implicit class ArcticTableHelper(table: Table) {
    def asArcticTable: ArcticSparkTable = {
      table match {
        case arcticTable: ArcticSparkTable => arcticTable
        case _ => throw new IllegalArgumentException(s"$table is not an arctic table")
      }
    }

    def asUpsertWrite: SupportsUpsert = {
      table match {
        case arcticTable: SupportsUpsert => arcticTable
        case _ => throw new IllegalArgumentException(s"$table is not an upsert-able table")
      }
    }
  }

  implicit class ArcticRelationHelper(plan: LogicalPlan) {
    def asTableRelation: DataSourceV2Relation = {
      ArcticExtensionUtils.asTableRelation(plan)
    }
  }
  def isArcticRelation(plan: LogicalPlan): Boolean = {
    def isArcticTable(relation: DataSourceV2Relation): Boolean = relation.table match {
      case _: ArcticSparkTable => true
      case _ => false
    }

    plan.collectLeaves().exists {
      case p: DataSourceV2Relation => isArcticTable(p)
      case s: SubqueryAlias => s.child.children.exists{ case p: DataSourceV2Relation => isArcticTable(p)}
    }
  }

  def isArcticCatalog(catalog: TableCatalog): Boolean = {
    catalog match {
      case _: ArcticSparkCatalog => true
      case _: ArcticSparkSessionCatalog[_] => true
      case _ => false
    }
  }

  def isArcticTable(table: Table): Boolean = table match {
    case _: ArcticSparkTable => true
    case _ => false
  }

  def asTableRelation(plan: LogicalPlan): DataSourceV2Relation = {
    plan match {
      case s: SubqueryAlias => asTableRelation(s.child)
      case p: Project => asTableRelation(p.child.children.head)
      case r: DataSourceV2Relation => r
      case _ => throw new IllegalArgumentException("Expected a DataSourceV2Relation")
    }
  }

}
