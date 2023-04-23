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

import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.spark.test.Asserts;
import com.netease.arctic.spark.test.helper.TestSource;
import com.netease.arctic.spark.test.helper.TestTables;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;


public class TestCreateTableAsSelect extends SparkTableTestBase {

  // 1. ts handle
  // 2. primary key spec/table schema
  // 3. data expect
  // 4. duplicate check

  public static Stream<Arguments> testTimestampZoneHandle() {
    Schema schema = TestTables.MixedIceberg.PK_PT.schema;
    TestSource source = new TestSource(
        TestTables.MixedIceberg.PK_PT.generator.records(10), schema);

    return Stream.of(
        Arguments.of(INTERNAL_CATALOG, "PRIMARY KEY(id, pt)", source, true,
            Types.TimestampType.withoutZone()),
        Arguments.of(INTERNAL_CATALOG, "", source, false,
            Types.TimestampType.withZone()),
        Arguments.of(HIVE_CATALOG, "PRIMARY KEY(id, pt)", source, true,
            Types.TimestampType.withoutZone()),
        Arguments.of(HIVE_CATALOG, "", source, false,
            Types.TimestampType.withoutZone())
    );
  }

  @ParameterizedTest
  @MethodSource
  public void testTimestampZoneHandle(
      String catalog, String primaryKeyDDL, TestSource source,
      boolean timestampWithoutZone,
      Types.TimestampType expectType
  ) {
    test().inSparkCatalog(catalog)
        .registerSourceView(source)
        .execute(context -> {
          sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
              + "`=" + timestampWithoutZone);
          String sqlText = "CREATE TABLE " + context.databaseAndTable + " " + primaryKeyDDL
              + " USING arctic AS SELECT * FROM " + context.sourceDatabaseAndTable;
          sql(sqlText);
          ArcticTable table = context.loadTable();
          Types.NestedField f = table.schema().findField("ts");
          Asserts.assertType(expectType, f.type());
        });
  }


}
