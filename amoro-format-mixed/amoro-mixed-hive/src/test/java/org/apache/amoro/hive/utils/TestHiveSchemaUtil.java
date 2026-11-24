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

package org.apache.amoro.hive.utils;

import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

public class TestHiveSchemaUtil {

  @Test
  public void testChangeFieldNameToLowercase() {

    Schema schema =
        new Schema(
            Types.NestedField.optional(1, "Col1", Types.IntegerType.get()),
            Types.NestedField.optional(2, "COL2", Types.LongType.get()),
            Types.NestedField.optional(
                3, "COL3", Types.ListType.ofOptional(4, Types.StringType.get())),
            Types.NestedField.optional(
                5,
                "COL4",
                Types.MapType.ofOptional(6, 7, Types.StringType.get(), Types.StringType.get())),
            Types.NestedField.optional(
                8,
                "COL5",
                Types.StructType.of(
                    Types.NestedField.optional(9, "COL6", Types.StringType.get()),
                    Types.NestedField.optional(10, "COL7", Types.TimestampType.withoutZone()))));

    Schema changedSchema =
        new Schema(
            Types.NestedField.optional(1, "col1", Types.IntegerType.get()),
            Types.NestedField.optional(2, "col2", Types.LongType.get()),
            Types.NestedField.optional(
                3, "col3", Types.ListType.ofOptional(4, Types.StringType.get())),
            Types.NestedField.optional(
                5,
                "col4",
                Types.MapType.ofOptional(6, 7, Types.StringType.get(), Types.StringType.get())),
            Types.NestedField.optional(
                8,
                "col5",
                Types.StructType.of(
                    Types.NestedField.optional(9, "col6", Types.StringType.get()),
                    Types.NestedField.optional(10, "col7", Types.TimestampType.withoutZone()))));

    Assert.assertEquals(
        changedSchema.asStruct(), HiveSchemaUtil.changeFieldNameToLowercase(schema).asStruct());
  }

  /** The same field name is allowed in sibling scopes, e.g. in two independent nested structs. */
  @Test
  public void testChangeFieldNameToLowercaseWithRepeatedNameInIndependentStructs() {

    Schema schema =
        new Schema(
            Types.NestedField.optional(
                1,
                "Col1",
                Types.StructType.of(Types.NestedField.required(2, "X", Types.IntegerType.get()))),
            Types.NestedField.optional(
                3,
                "Col2",
                Types.StructType.of(Types.NestedField.optional(4, "X", Types.StringType.get()))));

    Schema changedSchema =
        new Schema(
            Types.NestedField.optional(
                1,
                "col1",
                Types.StructType.of(Types.NestedField.required(2, "x", Types.IntegerType.get()))),
            Types.NestedField.optional(
                3,
                "col2",
                Types.StructType.of(Types.NestedField.optional(4, "x", Types.StringType.get()))));

    Assert.assertEquals(
        changedSchema.asStruct(), HiveSchemaUtil.changeFieldNameToLowercase(schema).asStruct());
  }

  /** A top level field and a field nested in a struct are in different scopes. */
  @Test
  public void testChangeFieldNameToLowercaseWithNameRepeatedInTopLevelAndNestedScope() {

    Schema schema =
        new Schema(
            Types.NestedField.optional(1, "X", Types.IntegerType.get(), "top level x"),
            Types.NestedField.optional(
                2,
                "Col2",
                Types.StructType.of(Types.NestedField.required(3, "X", Types.StringType.get()))));

    Schema changedSchema =
        new Schema(
            Types.NestedField.optional(1, "x", Types.IntegerType.get(), "top level x"),
            Types.NestedField.optional(
                2,
                "col2",
                Types.StructType.of(Types.NestedField.required(3, "x", Types.StringType.get()))));

    Assert.assertEquals(
        changedSchema.asStruct(), HiveSchemaUtil.changeFieldNameToLowercase(schema).asStruct());
  }

  /** Fields of the same struct that fold to the same name must still be rejected. */
  @Test
  public void testChangeFieldNameToLowercaseRejectsSiblingCollision() {

    Schema schema =
        new Schema(
            Types.NestedField.optional(1, "Col", Types.IntegerType.get()),
            Types.NestedField.optional(2, "COL", Types.LongType.get()));

    Assert.assertThrows(
        IllegalArgumentException.class, () -> HiveSchemaUtil.changeFieldNameToLowercase(schema));
  }

  /** Fields nested in lists and maps are renamed as well, keeping ids and requiredness. */
  @Test
  public void testChangeFieldNameToLowercaseInsideListAndMap() {

    Schema schema =
        new Schema(
            Types.NestedField.optional(
                1,
                "Col1",
                Types.ListType.ofRequired(
                    2,
                    Types.StructType.of(
                        Types.NestedField.optional(3, "ELEM", Types.StringType.get())))),
            Types.NestedField.optional(
                4,
                "Col2",
                Types.MapType.ofRequired(
                    5,
                    6,
                    Types.StringType.get(),
                    Types.StructType.of(
                        Types.NestedField.required(7, "VALUE", Types.IntegerType.get())))),
            Types.NestedField.optional(
                8,
                "Col3",
                Types.MapType.ofOptional(
                    9,
                    10,
                    Types.StringType.get(),
                    Types.StructType.of(
                        Types.NestedField.optional(11, "VALUE", Types.IntegerType.get())))));

    Schema changedSchema =
        new Schema(
            Types.NestedField.optional(
                1,
                "col1",
                Types.ListType.ofRequired(
                    2,
                    Types.StructType.of(
                        Types.NestedField.optional(3, "elem", Types.StringType.get())))),
            Types.NestedField.optional(
                4,
                "col2",
                Types.MapType.ofRequired(
                    5,
                    6,
                    Types.StringType.get(),
                    Types.StructType.of(
                        Types.NestedField.required(7, "value", Types.IntegerType.get())))),
            Types.NestedField.optional(
                8,
                "col3",
                Types.MapType.ofOptional(
                    9,
                    10,
                    Types.StringType.get(),
                    Types.StructType.of(
                        Types.NestedField.optional(11, "value", Types.IntegerType.get())))));

    Schema changed = HiveSchemaUtil.changeFieldNameToLowercase(schema);
    Assert.assertEquals(changedSchema.asStruct(), changed.asStruct());
    // Map value requiredness is part of the map type, do not silently relax it.
    Assert.assertFalse(changed.findType("col2").asMapType().isValueOptional());
    Assert.assertTrue(changed.findType("col3").asMapType().isValueOptional());
  }
}
