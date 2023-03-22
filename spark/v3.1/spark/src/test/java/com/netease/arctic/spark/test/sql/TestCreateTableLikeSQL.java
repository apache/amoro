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

package com.netease.arctic.spark.test.sql;

import com.netease.arctic.spark.test.SessionCatalog;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@SessionCatalog(usingArcticSessionCatalog = true)
public class TestCreateTableLikeSQL extends SparkTableTestBase {
  //1. 源端表是 Arctic 表 (主键，分区)
  //2. 源端表非 Arctic 表 (分区)

  //3. using arctic 测试 (Session catalog, 非Session catalog)

  static final Types.NestedField id = Types.NestedField.optional(1, "id", Types.IntegerType.get());
  static final Types.NestedField data = Types.NestedField.optional(2, "data", Types.StringType.get());
  static final Types.NestedField tsWoZ = Types.NestedField.optional(3, "ts", Types.TimestampType.withoutZone());
  static final Types.NestedField tsWz = Types.NestedField.optional(3, "ts", Types.TimestampType.withZone());

  static final String structDDL = "id INT, " +
      "data STRING, " +
      "ts TIMESTAMP NOT NULL";

  static final String structDDLWithPk = structDDL + ",\n" +
      "PRIMARY KEY(id) ";

  public static Stream<Arguments> testCreateTableLikeTimestampZone() {
    return Stream.of(
        Arguments.of(SESSION_CATALOG, structDDL, true, tsWoZ.type()),
        Arguments.of(SESSION_CATALOG, structDDL, false, tsWoZ.type()),
        Arguments.of(HIVE_CATALOG, structDDL, true, tsWoZ.type()),
        Arguments.of(HIVE_CATALOG, structDDLWithPk, false, tsWoZ.type()),
        Arguments.of(INTERNAL_CATALOG, structDDL, true, tsWoZ.type()),
        Arguments.of(INTERNAL_CATALOG, structDDLWithPk, false, tsWz.type())
    );
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE handle timestamp type in new table.")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLikeTimestampZone(
      String catalog, String sourceTableDDL, boolean newTableTimestampWithoutZone,
      Type expectTimestampType
  ) {
    
  }

  @DisplayName("TestSQL: CREATE TABLE LIKE")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableLike(
      String catalog, String sourceTableDDL,
      Schema expectSchema, PartitionSpec expectSpec, PrimaryKeySpec expectKeySpec
  ) {

  }

  @DisplayName("TestSQL: CREATE TABLE LIKE without USING ARCTIC")
  @ParameterizedTest
  @MethodSource
  public void testCreateTableWithoutProvider(
      String catalog, String sourceTableDDL, String provider,
      boolean expectCreate
  ) {

  }
}
