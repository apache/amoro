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

package org.apache.iceberg.parquet;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.iceberg.Schema;
import org.apache.iceberg.mapping.MappingUtil;
import org.apache.iceberg.mapping.NameMapping;
import org.apache.iceberg.types.TypeUtil;
import org.apache.parquet.schema.MessageType;
import org.junit.Assert;
import org.junit.Test;

import static org.apache.iceberg.types.Types.NestedField.optional;
import static org.apache.iceberg.types.Types.NestedField.required;

public class TestAdaptHiveReadConf {

  private static final org.apache.iceberg.types.Types.StructType SUPPORTED_PRIMITIVES =
      org.apache.iceberg.types.Types.StructType.of(
          required(100, "id", org.apache.iceberg.types.Types.LongType.get()),
          optional(101, "data", org.apache.iceberg.types.Types.StringType.get()),
          required(102, "b", org.apache.iceberg.types.Types.BooleanType.get()),
          optional(103, "i", org.apache.iceberg.types.Types.IntegerType.get()),
          required(104, "l", org.apache.iceberg.types.Types.LongType.get()),
          optional(105, "f", org.apache.iceberg.types.Types.FloatType.get()),
          required(106, "d", org.apache.iceberg.types.Types.DoubleType.get()),
          optional(107, "date", org.apache.iceberg.types.Types.DateType.get()),
          required(108, "ts", org.apache.iceberg.types.Types.TimestampType.withZone()),
          required(110, "s", org.apache.iceberg.types.Types.StringType.get()),
          required(112, "fixed", org.apache.iceberg.types.Types.FixedType.ofLength(7)),
          optional(113, "bytes", org.apache.iceberg.types.Types.BinaryType.get()),
          required(114, "dec_9_0", org.apache.iceberg.types.Types.DecimalType.of(9, 0)),
          required(115, "dec_11_2", org.apache.iceberg.types.Types.DecimalType.of(11, 2)),
          required(116, "dec_38_10", org.apache.iceberg.types.Types.DecimalType.of(38, 10)) // spark's maximum precision
      );

  @Test
  public void testAssignIdsByNameMapping() {
    org.apache.iceberg.types.Types.StructType hiveStructType = org.apache.iceberg.types.Types.StructType.of(
        required(0, "id".toUpperCase(), org.apache.iceberg.types.Types.LongType.get()),
        optional(1, "list_of_maps".toUpperCase(),
            org.apache.iceberg.types.Types.ListType.ofOptional(
                2,
                org.apache.iceberg.types.Types.MapType.ofOptional(3, 4,
                    org.apache.iceberg.types.Types.StringType.get(),
                    SUPPORTED_PRIMITIVES))),
        optional(5, "map_of_lists".toUpperCase(),
            org.apache.iceberg.types.Types.MapType.ofOptional(6, 7,
                org.apache.iceberg.types.Types.StringType.get(),
                org.apache.iceberg.types.Types.ListType.ofOptional(8, SUPPORTED_PRIMITIVES))),
        required(9, "list_of_lists".toUpperCase(),
            org.apache.iceberg.types.Types.ListType.ofOptional(
                10,
                org.apache.iceberg.types.Types.ListType.ofOptional(11, SUPPORTED_PRIMITIVES))),
        required(12, "map_of_maps".toUpperCase(),
            org.apache.iceberg.types.Types.MapType.ofOptional(13, 14,
                org.apache.iceberg.types.Types.StringType.get(),
                org.apache.iceberg.types.Types.MapType.ofOptional(15, 16,
                    org.apache.iceberg.types.Types.StringType.get(),
                    SUPPORTED_PRIMITIVES))),
        required(
            17,
            "list_of_struct_of_nested_types".toUpperCase(),
            org.apache.iceberg.types.Types.ListType.ofOptional(19, org.apache.iceberg.types.Types.StructType.of(
                org.apache.iceberg.types.Types.NestedField.required(
                    20,
                    "m1".toUpperCase(),
                    org.apache.iceberg.types.Types.MapType.ofOptional(21, 22,
                        org.apache.iceberg.types.Types.StringType.get(),
                        SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.optional(
                    23,
                    "l1".toUpperCase(),
                    org.apache.iceberg.types.Types.ListType.ofRequired(24, SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.required(
                    25,
                    "l2".toUpperCase(),
                    org.apache.iceberg.types.Types.ListType.ofRequired(26, SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.optional(
                    27,
                    "m2".toUpperCase(),
                    org.apache.iceberg.types.Types.MapType.ofOptional(28, 29,
                        org.apache.iceberg.types.Types.StringType.get(),
                        SUPPORTED_PRIMITIVES))
            )))
    );

    org.apache.iceberg.types.Types.StructType structType = org.apache.iceberg.types.Types.StructType.of(
        required(0, "id", org.apache.iceberg.types.Types.LongType.get()),
        optional(1, "list_of_maps",
            org.apache.iceberg.types.Types.ListType.ofOptional(
                2,
                org.apache.iceberg.types.Types.MapType.ofOptional(3, 4,
                    org.apache.iceberg.types.Types.StringType.get(),
                    SUPPORTED_PRIMITIVES))),
        optional(5, "map_of_lists",
            org.apache.iceberg.types.Types.MapType.ofOptional(6, 7,
                org.apache.iceberg.types.Types.StringType.get(),
                org.apache.iceberg.types.Types.ListType.ofOptional(8, SUPPORTED_PRIMITIVES))),
        required(9, "list_of_lists",
            org.apache.iceberg.types.Types.ListType.ofOptional(
                10,
                org.apache.iceberg.types.Types.ListType.ofOptional(11, SUPPORTED_PRIMITIVES))),
        required(12, "map_of_maps",
            org.apache.iceberg.types.Types.MapType.ofOptional(13, 14,
                org.apache.iceberg.types.Types.StringType.get(),
                org.apache.iceberg.types.Types.MapType.ofOptional(15, 16,
                    org.apache.iceberg.types.Types.StringType.get(),
                    SUPPORTED_PRIMITIVES))),
        required(
            17,
            "list_of_struct_of_nested_types",
            org.apache.iceberg.types.Types.ListType.ofOptional(19, org.apache.iceberg.types.Types.StructType.of(
                org.apache.iceberg.types.Types.NestedField.required(
                    20,
                    "m1",
                    org.apache.iceberg.types.Types.MapType.ofOptional(21, 22,
                        org.apache.iceberg.types.Types.StringType.get(),
                        SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.optional(
                    23,
                    "l1",
                    org.apache.iceberg.types.Types.ListType.ofRequired(24, SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.required(
                    25,
                    "l2",
                    org.apache.iceberg.types.Types.ListType.ofRequired(26, SUPPORTED_PRIMITIVES)),
                org.apache.iceberg.types.Types.NestedField.optional(
                    27,
                    "m2",
                    org.apache.iceberg.types.Types.MapType.ofOptional(28, 29,
                        org.apache.iceberg.types.Types.StringType.get(),
                        SUPPORTED_PRIMITIVES))
            )))
    );

    Schema schema = new Schema(TypeUtil.assignFreshIds(structType, new AtomicInteger(0)::incrementAndGet)
        .asStructType().fields());
    Schema hiveSchema = new Schema(TypeUtil.assignFreshIds(hiveStructType, new AtomicInteger(0)::incrementAndGet)
        .asStructType().fields());
    NameMapping nameMapping = MappingUtil.create(schema);
    MessageType messageTypeWithIds = ParquetSchemaUtil.convert(hiveSchema, "parquet_type");
    MessageType messageTypeWithIdsFromNameMapping = (MessageType) ParquetTypeVisitor.visit(
        RemoveIds.removeIds(messageTypeWithIds),
        new AdaptHiveApplyNameMapping(nameMapping));

    Assert.assertEquals(messageTypeWithIds, messageTypeWithIdsFromNameMapping);
  }
}
