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

package org.apache.amoro.flink.write.hidden.kafka;

import static org.apache.amoro.flink.shuffle.LogRecordV1.arrayFactory;
import static org.apache.amoro.flink.shuffle.LogRecordV1.mapFactory;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.flink.shuffle.LogRecordV1;
import org.apache.amoro.log.FormatVersion;
import org.apache.amoro.log.LogData;
import org.apache.amoro.log.LogDataJsonDeserialization;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.IdGenerator;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.types.Types;

import java.util.ArrayList;

public class TestBaseLog {
  public static final Schema USER_SCHEMA =
      new Schema(
          new ArrayList<Types.NestedField>() {
            {
              add(Types.NestedField.optional(0, "f_boolean", Types.BooleanType.get()));
              add(Types.NestedField.optional(1, "f_int", Types.IntegerType.get()));
              add(Types.NestedField.optional(2, "f_long", Types.LongType.get()));
              add(
                  Types.NestedField.optional(
                      3,
                      "f_struct",
                      Types.StructType.of(
                          Types.NestedField.optional(4, "f_sub_boolean", Types.BooleanType.get()),
                          Types.NestedField.optional(5, "f_sub_int", Types.IntegerType.get()),
                          Types.NestedField.optional(6, "f_sub_long", Types.LongType.get()),
                          Types.NestedField.optional(7, "f_sub_string", Types.StringType.get()),
                          Types.NestedField.optional(8, "f_sub_time", Types.TimeType.get()),
                          Types.NestedField.optional(
                              9, "f_sub_decimal", Types.DecimalType.of(38, 18)),
                          Types.NestedField.optional(10, "f_sub_float", Types.FloatType.get()),
                          Types.NestedField.optional(11, "f_sub_double", Types.DoubleType.get()),
                          Types.NestedField.optional(12, "f_sub_date", Types.DateType.get()),
                          Types.NestedField.optional(
                              13, "f_sub_timestamp_local", Types.TimestampType.withoutZone()),
                          Types.NestedField.optional(
                              14, "f_sub_timestamp_tz", Types.TimestampType.withZone()),
                          Types.NestedField.optional(15, "f_sub_uuid", Types.UUIDType.get()),
                          Types.NestedField.optional(
                              16, "f_sub_fixed", Types.FixedType.ofLength(18)),
                          Types.NestedField.optional(17, "f_sub_binary", Types.BinaryType.get()),
                          Types.NestedField.optional(
                              18,
                              "f_sub_list",
                              Types.ListType.ofOptional(19, Types.LongType.get())),
                          Types.NestedField.optional(
                              20,
                              "f_list2",
                              Types.ListType.ofOptional(21, Types.IntegerType.get())),
                          Types.NestedField.optional(
                              22,
                              "f_list3",
                              Types.ListType.ofOptional(
                                  23,
                                  Types.StructType.of(
                                      Types.NestedField.optional(
                                          24, "f_sub_boolean", Types.BooleanType.get()),
                                      Types.NestedField.optional(
                                          25, "f_sub_int", Types.IntegerType.get()),
                                      Types.NestedField.optional(
                                          26, "f_sub_long", Types.LongType.get())))),
                          Types.NestedField.optional(
                              27,
                              "f_map",
                              Types.MapType.ofOptional(
                                  28, 29, Types.StringType.get(), Types.StringType.get())))));
            }
          });

  public static final Schema USER_SCHEMA_WITH_ALL_DATA_TYPE =
      new Schema(
          new ArrayList<Types.NestedField>() {
            {
              add(Types.NestedField.optional(0, "f_boolean", Types.BooleanType.get()));
              add(Types.NestedField.optional(1, "f_int", Types.IntegerType.get()));
              add(Types.NestedField.optional(2, "f_date", Types.DateType.get()));
              add(Types.NestedField.optional(3, "f_long", Types.LongType.get()));
              add(Types.NestedField.optional(4, "f_time", Types.TimeType.get()));
              add(Types.NestedField.optional(5, "f_float", Types.FloatType.get()));
              add(Types.NestedField.optional(6, "f_double", Types.DoubleType.get()));
              add(
                  Types.NestedField.optional(
                      7, "f_timestamp_local", Types.TimestampType.withoutZone()));
              add(Types.NestedField.optional(8, "f_timestamp_tz", Types.TimestampType.withZone()));
              add(Types.NestedField.optional(9, "f_string", Types.StringType.get()));
              add(Types.NestedField.optional(10, "f_uuid", Types.UUIDType.get()));
              add(Types.NestedField.optional(11, "f_fixed", Types.FixedType.ofLength(18)));
              add(Types.NestedField.optional(12, "f_binary", Types.BinaryType.get()));
              add(Types.NestedField.optional(13, "f_decimal", Types.DecimalType.of(38, 18)));
              add(
                  Types.NestedField.optional(
                      14, "f_list", Types.ListType.ofOptional(15, Types.LongType.get())));
              add(
                  Types.NestedField.optional(
                      16,
                      "f_map",
                      Types.MapType.ofOptional(
                          17, 18, Types.StringType.get(), Types.StringType.get())));
              add(
                  Types.NestedField.optional(
                      19,
                      "f_struct",
                      Types.StructType.of(
                          Types.NestedField.optional(20, "f_sub_boolean", Types.BooleanType.get()),
                          Types.NestedField.optional(21, "f_sub_int", Types.IntegerType.get()),
                          Types.NestedField.optional(22, "f_sub_long", Types.LongType.get()),
                          Types.NestedField.optional(23, "f_sub_string", Types.StringType.get()),
                          Types.NestedField.optional(24, "f_sub_time", Types.TimeType.get()),
                          Types.NestedField.optional(
                              25, "f_sub_decimal", Types.DecimalType.of(36, 18)),
                          Types.NestedField.optional(26, "f_sub_float", Types.FloatType.get()),
                          Types.NestedField.optional(27, "f_sub_double", Types.DoubleType.get()),
                          Types.NestedField.optional(28, "f_sub_date", Types.DateType.get()),
                          Types.NestedField.optional(
                              29, "f_sub_timestamp_local", Types.TimestampType.withoutZone()),
                          Types.NestedField.optional(
                              30, "f_sub_timestamp_tz", Types.TimestampType.withZone()),
                          Types.NestedField.optional(31, "f_sub_uuid", Types.UUIDType.get()),
                          Types.NestedField.optional(
                              32, "f_sub_fixed", Types.FixedType.ofLength(18)),
                          Types.NestedField.optional(33, "f_sub_binary", Types.BinaryType.get()),
                          Types.NestedField.optional(
                              34,
                              "f_sub_list",
                              Types.ListType.ofOptional(35, Types.LongType.get())),
                          Types.NestedField.optional(
                              36,
                              "f_list2",
                              Types.ListType.ofOptional(37, Types.IntegerType.get())),
                          Types.NestedField.optional(
                              38,
                              "f_list3",
                              Types.ListType.ofOptional(
                                  39,
                                  Types.StructType.of(
                                      Types.NestedField.optional(
                                          40, "f_sub_boolean", Types.BooleanType.get()),
                                      Types.NestedField.optional(
                                          41, "f_sub_int", Types.IntegerType.get()),
                                      Types.NestedField.optional(
                                          42, "f_sub_long", Types.LongType.get())))),
                          Types.NestedField.optional(
                              43,
                              "f_map",
                              Types.MapType.ofOptional(
                                  44, 45, Types.StringType.get(), Types.StringType.get())))));
            }
          });

  private final PrimaryKeySpec primaryKeySpec =
      PrimaryKeySpec.builderFor(USER_SCHEMA).addColumn(1).build();

  public final RowType flinkUserSchema = FlinkSchemaUtil.convert(USER_SCHEMA);

  public final LogData<RowData> FLIP_LOG =
      new LogRecordV1(
          FormatVersion.FORMAT_VERSION_V1,
          IdGenerator.generateUpstreamId(),
          1L,
          true,
          ChangeAction.INSERT,
          new GenericRowData(0));

  public static LogDataJsonDeserialization<RowData> createLogDataDeserialization() {
    return new LogDataJsonDeserialization<>(
        USER_SCHEMA, LogRecordV1.factory, arrayFactory, mapFactory);
  }
}
