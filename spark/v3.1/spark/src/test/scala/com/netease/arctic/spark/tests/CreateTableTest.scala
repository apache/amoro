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
import org.apache.iceberg.Schema
import org.apache.iceberg.relocated.com.google.common.collect.Lists
import org.apache.iceberg.types.Types
import org.apache.iceberg.types.Types.NestedField
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import scala.language.postfixOps


object CreateTableTest {

  @Parameters(name = "catalog={0}, withKey={1}, withPartition={2}")
  def parameters(): java.util.List[Array[Any]] = {
    Lists.newArrayList(
      Array("session", true, true),
      Array("session", true, false),
      Array("session", false, true),
      Array("session", false, false),

      Array("arctic", true, true),
      Array("arctic", true, false),
      Array("arctic", false, true),
      Array("arctic", false, false),

      Array("hive", true, true),
      Array("hive", true, false),
      Array("hive", false, true),
      Array("hive", false, false),
    )
  }
}

@RunWith(classOf[Parameterized])
case class CreateTableTest(
  catalogType: String,
  withKey: Boolean,
  withPartition: Boolean
) extends SparkCatalogTestSupport
  with TableTestSupport {

  private val (partitionFieldSQL, partitionedBy) = tableFormat match {
    case TableFormat.MIXED_ICEBERG => ("pt timestamp", "PARTITIONED BY (days(pt))")
    case TableFormat.MIXED_HIVE => ("pt string", "PARTITIONED BY (pt)")
  }


  override lazy val schema: Schema = new Schema(
    NestedField.of(nextId(), !withKey, "id", Types.IntegerType.get()),
    NestedField.required(nextId(), "data", Types.StringType.get()),
    NestedField.optional(
      nextId(), "point", Types.StructType.of(
        NestedField.required(nextId(), "x", Types.DoubleType.get()),
        NestedField.required(nextId(), "y", Types.DoubleType.get())
      )),
    NestedField.optional(
      nextId(), "maps", Types.MapType.ofOptional(
        nextId(), nextId(),
        Types.StringType.get(), Types.StringType.get()
      )),
    NestedField.optional(
      nextId(), "arrays", Types.ListType.ofOptional(
        nextId(), Types.StringType.get()
      )),
    timestampNestedField("ts", optional = true),
    partitionField
  )


  @Test
  def test(): Unit = {
    val pt = if (withPartition) partitionedBy else ""
    val primaryKey = if (withKey) {
      ", \n PRIMARY KEY(id) ".stripMargin
    } else ""

    val sqlText =
      s"""
         |CREATE TABLE ${database}.${table} (
         |  id int ,
         |  data string NOT NULL,
         |  point struct<x: double NOT NULL, y: double NOT NULL>,
         |  maps map<string, string>,
         |  arrays array<string>,
         |  ts timestamp,
         |  ${partitionFieldSQL} ${primaryKey}
         |) USING arctic
         | ${pt}
         | TBLPROPERTIES (
         |  'props.test1' = 'val1',
         |  'props.test2' = 'val2'
         | )
         |""".stripMargin
    sql(sqlText)

    val arcticTable = loadTable()
    assertTableSchema(arcticTable)
  }


}
