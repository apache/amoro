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

package com.netease.arctic.spark.tests

import com.netease.arctic.ams.api.properties.TableFormat
import com.netease.arctic.catalog.CatalogLoader
import com.netease.arctic.table.{ArcticTable, PrimaryKeySpec, TableIdentifier}
import org.apache.iceberg.types.Types.NestedField
import org.apache.iceberg.types.{Type, Types}
import org.apache.iceberg.{PartitionSpec, Schema}
import org.junit.{After, Assert, Before}

import scala.collection.JavaConverters._
import scala.language.postfixOps

trait TableTestSupport extends SparkCatalogTestSupport {

  def database = "test_db"

  def table = "test_table"

  def withKey: Boolean

  def withPartition: Boolean

  lazy val tableFormat: TableFormat = catalogType toUpperCase match {
    case "SESSION" | "HIVE" => TableFormat.MIXED_HIVE
    case "ARCTIC" => TableFormat.MIXED_ICEBERG
  }

  var _fieldId: Int = 0

  def nextId(): Int = {
    _fieldId = _fieldId + 1
    _fieldId
  }

  def timestampNestedField(name: String, optional: Boolean = true): NestedField = tableFormat match {
    case TableFormat.MIXED_ICEBERG if !useTimestampWithoutZoneInNewTable =>
      NestedField.of(nextId(), optional, name, Types.TimestampType.withZone())
    case _ => NestedField.optional(nextId(), name, Types.TimestampType.withoutZone())
  }

  lazy val partitionField: NestedField = tableFormat match {
    case TableFormat.MIXED_ICEBERG => timestampNestedField("pt")
    case TableFormat.MIXED_HIVE => NestedField.optional(nextId(), "pt", Types.StringType.get())
  }

  lazy val schema: Schema = new Schema(
    NestedField.required(nextId(), "id", Types.IntegerType.get()),
    NestedField.optional(nextId(), "data", Types.StringType.get()),
    partitionField
  )

  lazy val partitionSpec: PartitionSpec = tableFormat match {
    case TableFormat.MIXED_ICEBERG if withPartition =>
      PartitionSpec.builderFor(schema).day("pt").build()
    case TableFormat.MIXED_HIVE if withPartition =>
      PartitionSpec.builderFor(schema).identity("pt").build()
    case _ => PartitionSpec.unpartitioned()
  }


  lazy val keySpec: PrimaryKeySpec = if (withKey) {
    PrimaryKeySpec.builderFor(schema).addColumn("id").build()
  } else {
    PrimaryKeySpec.noPrimaryKey()
  }


  @Before def setupDatabase(): Unit = {
    sql(s"USE ${catalog}")
    sql(s"CREATE DATABASE IF NOT EXISTS $database")
  }


  @After def cleanTable(): Unit = {
    sql(s"USE ${catalog}")
    sql(s"DROP TABLE IF EXISTS ${database}.${table}")
  }

  def loadTable(): ArcticTable = {
    val arcticCatalog = CatalogLoader.load(catalogUrl)
    arcticCatalog.loadTable(TableIdentifier.of(arcticCatalogName, database, table))
  }


  def assertTableSchema(actual: ArcticTable): Unit = {
    Assert.assertEquals(s"the table should withKey: ${withKey}", withKey, actual.isKeyedTable)
    Assert.assertEquals(
      s"the table should withPartition: ${withPartition}",
      withPartition, actual.spec().isPartitioned)

    if (withKey) {
      val actualKeySpec = actual.asKeyedTable().primaryKeySpec()
      Assert.assertEquals(
        "the table keySpec is not expected",
        keySpec.fieldNames(), actualKeySpec.fieldNames())
    }

    if (withPartition) {
      val actualSpec = actual.spec()
      Assert.assertEquals(partitionSpec.fields().size(), actualSpec.fields().size())
      partitionSpec.fields().asScala.zip(actualSpec.fields().asScala)
        .foreach(x => {
          println(x)
          Assert.assertEquals(x._1.name(), x._2.name())
          Assert.assertEquals(x._1.transform(), x._2.transform())
        })
    }

    val actualSchema = actual.schema().asStruct()
    val expectStruct = schema.asStruct()
    assertType(expectStruct, actualSchema)
  }

  def assertType(expect: Type, actual: Type): Unit = {
    Assert.assertEquals(expect.isPrimitiveType, actual.isPrimitiveType)
    if (expect.isPrimitiveType) {
      Assert.assertEquals(expect, actual)
    } else {
      val expectFields = expect.asNestedType().fields().asScala
      val actualFields = actual.asNestedType().fields().asScala

      Assert.assertEquals(expectFields.length, actualFields.length)
      expectFields.zip(actualFields).foreach(x => {
        Assert.assertEquals(x._1.name(), x._2.name())
        Assert.assertEquals(x._1.isOptional, x._2.isOptional)
        Assert.assertEquals(x._1.doc(), x._2.doc())
        assertType(x._1.`type`(), x._2.`type`())
      })

    }
  }

}
