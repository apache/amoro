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

package org.apache.amoro.spark.test.utils;

import org.apache.amoro.TableFormat;
import org.apache.iceberg.types.Types;

public class TestTables {
  static final Types.NestedField ID = Types.NestedField.optional(1, "id", Types.IntegerType.get());
  static final Types.NestedField DATA =
      Types.NestedField.optional(2, "data", Types.StringType.get(), "test comment");
  static final Types.NestedField D = Types.NestedField.optional(3, "d", Types.DoubleType.get());
  static final Types.NestedField TS_LONG =
      Types.NestedField.optional(4, "ts_long", Types.LongType.get());
  static final Types.NestedField TS =
      Types.NestedField.optional(8, "ts", Types.TimestampType.withoutZone());
  static final Types.NestedField PT = Types.NestedField.optional(20, "pt", Types.StringType.get());
  static Types.NestedField[] fields = new Types.NestedField[] {ID, DATA, D, TS_LONG, TS, PT};

  public static class MixedHive {
    public static final TestTable PK_PT =
        TestTable.format(TableFormat.MIXED_HIVE, fields).pk(ID.name()).pt(PT.name()).build();
    public static final TestTable PK_NO_PT =
        TestTable.format(TableFormat.MIXED_HIVE, fields).pk(ID.name()).build();

    public static final TestTable NO_PK_PT =
        TestTable.format(TableFormat.MIXED_HIVE, fields).pt(PT.name()).build();

    public static final TestTable NO_PK_NO_PT =
        TestTable.format(TableFormat.MIXED_HIVE, fields).build();
  }

  public static class MixedIceberg {
    public static final TestTable PK_PT =
        TestTable.format(TableFormat.MIXED_ICEBERG, fields).pk(ID.name()).pt(PT.name()).build();
    public static final TestTable PK_NO_PT =
        TestTable.format(TableFormat.MIXED_ICEBERG, fields).pk(ID.name()).build();

    public static final TestTable NO_PK_PT =
        TestTable.format(TableFormat.MIXED_ICEBERG, fields).pt(PT.name()).build();

    public static final TestTable NO_PK_NO_PT =
        TestTable.format(TableFormat.MIXED_ICEBERG, fields).build();
  }
}
