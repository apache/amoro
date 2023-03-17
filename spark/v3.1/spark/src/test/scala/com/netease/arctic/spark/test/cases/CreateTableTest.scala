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

package com.netease.arctic.spark.test.cases

import com.netease.arctic.table.PrimaryKeySpec
import org.apache.iceberg.relocated.com.google.common.collect.Lists
import org.apache.iceberg.types.Types
import org.apache.iceberg.types.Types.NestedField
import org.apache.iceberg.{PartitionSpec, Schema}
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import scala.language.postfixOps


object CreateTableTest {


  def id(isPrimaryKey: Boolean): NestedField = {
    NestedField.of(1, !isPrimaryKey, "id", Types.IntegerType.get())
  }

  def commonFields: java.util.List[NestedField] = Lists.newArrayList(
    NestedField.required(2, "data", Types.StringType.get()),
    NestedField.optional(
      3, "point", Types.StructType.of(
        NestedField.required(4, "x", Types.DoubleType.get()),
        NestedField.required(5, "y", Types.DoubleType.get())
      )),
    NestedField.optional(
      6, "maps", Types.MapType.ofOptional(
        7, 8,
        Types.StringType.get(), Types.StringType.get()
      )),
    NestedField.optional(
      9, "arrays", Types.ListType.ofOptional(
        10, Types.StringType.get()
      )),
  )

  def partitionField(isHiveTable: Boolean, useTimestampWithoutZoneInNewTable: Boolean): NestedField = {
    if (isHiveTable) {
      NestedField.optional(20, "pt", Types.StringType.get())
    } else if (useTimestampWithoutZoneInNewTable) {
      NestedField.optional(20, "pt", Types.TimestampType.withoutZone)
    } else {
      NestedField.optional(20, "pt", Types.TimestampType.withZone())
    }
  }

  def schema(keyField: NestedField, partitionField: NestedField, isHive: Boolean, timestampWithoutZone: Boolean): Schema = {
    val fields: java.util.List[NestedField] = Lists.newArrayList()
    fields.add(keyField)
    fields.addAll(commonFields)
    val tsField = if (isHive || timestampWithoutZone) {
      Types.NestedField.optional(11, "ts", Types.TimestampType.withoutZone)
    } else {
      Types.NestedField.optional(11, "ts", Types.TimestampType.withZone())
    }

    fields.add(tsField)

    fields.add(partitionField)

    new Schema(fields)
  }

  private val hiveStructDDL =
    """
      |id INT,
      |data string NOT NULL,
      |point struct<x: double NOT NULL, y: double NOT NULL>,
      |maps map<string, string>,
      |arrays array<string>,
      |ts timestamp,
      |pt string
      |""".stripMargin

  private val mixedIcebergStructDDL =
    """
      |id INT,
      |data string NOT NULL,
      |point struct<x: double NOT NULL, y: double NOT NULL>,
      |maps map<string, string>,
      |arrays array<string>,
      |ts timestamp,
      |pt timestamp
      |""".stripMargin

  private val primaryKeyDDL =
    """,
      |PRIMARY KEY(id)""".stripMargin


  def buildArgs(
    catalogType: String, withKey: Boolean, withPartition: Boolean, timestampWithoutZone: Boolean = false
  ): CreateTableTestArgument = {
    val (structDDL, isHiveTable) = catalogType toUpperCase match {
      case "SESSION" | "HIVE" => (hiveStructDDL, true)
      case "ARCTIC" => (mixedIcebergStructDDL, false)
    }
    val expectSchema = schema(
      id(withKey),
      partitionField(isHiveTable, timestampWithoutZone),
      isHiveTable, timestampWithoutZone
    )


    val (primaryKeyDDL, expectKeySpec) = if (withKey) {
      (this.primaryKeyDDL, PrimaryKeySpec.builderFor(expectSchema).addColumn("id").build())
    } else {
      ("", PrimaryKeySpec.noPrimaryKey())
    }

    val (partitionDDL, expectPartitionSpec) = catalogType toUpperCase match {
      case "SESSION" | "HIVE" if withPartition =>
        ("PARTITIONED BY (pt)", PartitionSpec.builderFor(expectSchema).identity("pt").build())
      case "ARCTIC" if withPartition =>
        ("PARTITIONED BY (days(pt))", PartitionSpec.builderFor(expectSchema).day("pt").build())
      case _ =>
        ("", PartitionSpec.unpartitioned())
    }


    CreateTableTestArgument(
      catalogType, structDDL, primaryKeyDDL, partitionDDL,
      expectSchema, expectKeySpec, expectPartitionSpec, timestampWithoutZone
    )
  }


  @Parameters(name = "{0}")
  def parameters(): java.util.List[CreateTableTestArgument] = {
    Lists.newArrayList(
      // session catalog
      buildArgs("session", withKey = true, withPartition = true),
      buildArgs("session", withKey = true, withPartition = false),
      buildArgs("session", withKey = false, withPartition = true),
      buildArgs("session", withKey = false, withPartition = false),

      buildArgs("hive", withKey = true, withPartition = true),
      buildArgs("hive", withKey = true, withPartition = false),
      buildArgs("hive", withKey = false, withPartition = true),
      buildArgs("hive", withKey = false, withPartition = false),

      buildArgs("arctic", withKey = true, withPartition = true, timestampWithoutZone = true),
      buildArgs("arctic", withKey = true, withPartition = false, timestampWithoutZone = true),
      buildArgs("arctic", withKey = false, withPartition = true, timestampWithoutZone = true),
      buildArgs("arctic", withKey = false, withPartition = false, timestampWithoutZone = true),

      buildArgs("arctic", withKey = true, withPartition = true),
      buildArgs("arctic", withKey = true, withPartition = false),
      buildArgs("arctic", withKey = false, withPartition = true),
      buildArgs("arctic", withKey = false, withPartition = false),
    )
  }
}


case class CreateTableTestArgument(
  catalogType: String,
  structDDL: String,
  primaryKeyDDL: String,
  partitionDDL: String,
  expectSchema: Schema,
  expectKeySpec: PrimaryKeySpec,
  expectPartitionSpec: PartitionSpec,
  timestampWithoutZone: Boolean = false
) {
  override def toString: String = {
    val withKey = if (expectKeySpec.primaryKeyExisted()) {
      "WithPrimaryKey"
    } else {
      "WithoutPrimaryKey"
    }

    val withPartition = if (expectPartitionSpec.isPartitioned) {
      "WithPartition"
    } else {
      "WithoutPartition"
    }

    s"${this.getClass.getSimpleName}[CatalogType: ${this.catalogType} ${withKey} ${withPartition}" +
      s" timestampWithoutZone=${this.timestampWithoutZone}]"
  }
}

@RunWith(classOf[Parameterized])
case class CreateTableTest(
  testArgument: CreateTableTestArgument
) {

  // override protected def catalogType(): String = testArgument.catalogType

  //
  //  @Test
  //  def test(): Unit = {
  //    sql(s"SET `${SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES}`=${
  //      testArgument
  //        .timestampWithoutZone
  //    }")
  //    val createTableSqlText =
  //      s"""
  //         |CREATE TABLE ${database}.${table} ( ${this.testArgument.structDDL} ${this.testArgument.primaryKeyDDL})
  //         |using arctic
  //         |${testArgument.partitionDDL}
  //         |""".stripMargin
  //    sql(createTableSqlText)
  //
  //    val arcticTable = loadTable()
  //    assertTableSchema(
  //      arcticTable,
  //      testArgument.expectSchema,
  //      testArgument.expectPartitionSpec,
  //      testArgument.expectKeySpec)
  //
  //  }


}
