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

package com.netease.arctic.spark.test.junit4;

import com.netease.arctic.spark.SparkSQLProperties;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.stream.Stream;

@RunWith(Parameterized.class)
public class TestCreateTableSQL extends SparkTableTestBase {

  @Parameterized.Parameters(name = "SQL Test: CreateTable [Run in {0}]")
  public static String[] suiteArgs() {
    return new String[] {
        SESSION_CATALOG, INTERNAL_CATALOG, HIVE_CATALOG
    };
  }

  public TestCreateTableSQL(String catalog) {
    setTestCatalog(catalog);
  }

  @Test
  public void testTimestampZoneHandleInCreateSQL( ) {
    Boolean[] usingTimestampWithoutZoneArgs = new Boolean[]{true, false};

    for (Boolean usingTimestampWithoutZone: usingTimestampWithoutZoneArgs){
      if ()
      testTimestampZoneHandleInCreateSQL(usingTimestampWithoutZone, );
    }


  }

  public void testTimestampZoneHandleInCreateSQL(
      boolean usingTimestampWithoutZone, Types.TimestampType type
  ) {
    sql("SET `" + SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES
        + "`=" + usingTimestampWithoutZone);

    String sqlText = "CREATE TABLE " + database() + "." + table() + "(\n" +
        "id INT, \n" +
        "ts TIMESTAMP \n) using arctic ";

    Schema schema = new Schema(
        Types.NestedField.optional(1, "id", Types.IntegerType.get()),
        Types.NestedField.optional(2, "ts", type)
    );
    sql(sqlText);
    ArcticTable actual = loadTable();

  }
}
