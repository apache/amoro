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

package org.apache.iceberg.parquet;

import static org.apache.iceberg.types.Types.NestedField.optional;
import static org.apache.iceberg.types.Types.NestedField.required;

import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.apache.parquet.schema.MessageType;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link AdaptHiveParquetSchemaUtil}, specifically verifying that {@code pruneColumns}
 * uses {@link TypeWithSchemaVisitor} (Iceberg 1.7+) to correctly prune columns using the expected
 * schema. Prior to this migration the method used {@link ParquetTypeVisitor} which did not pass the
 * expected schema, causing incorrect results for schemas whose column order differed from the file
 * schema.
 */
public class TestAdaptHiveParquetSchemaUtil {

  @Test
  public void testPruneColumnsSelectsSubset() {
    // Full file schema with 3 columns
    Schema fullSchema =
        new Schema(
            required(1, "id", Types.IntegerType.get()),
            optional(2, "name", Types.StringType.get()),
            optional(3, "ts", Types.TimestampType.withoutZone()));

    MessageType fileSchema = AdaptHiveParquetSchemaUtil.convert(fullSchema, "test");

    // Expected schema requests only 2 of the 3 columns
    Schema expectedSchema =
        new Schema(
            required(1, "id", Types.IntegerType.get()),
            optional(3, "ts", Types.TimestampType.withoutZone()));

    MessageType pruned = AdaptHiveParquetSchemaUtil.pruneColumns(fileSchema, expectedSchema);

    Assert.assertEquals(
        "Pruned schema should contain only the requested columns", 2, pruned.getFieldCount());
    Assert.assertTrue("Pruned schema should contain 'id'", pruned.containsField("id"));
    Assert.assertTrue("Pruned schema should contain 'ts'", pruned.containsField("ts"));
    Assert.assertFalse("Pruned schema should not contain 'name'", pruned.containsField("name"));
  }

  @Test
  public void testPruneColumnsWithNestedStruct() {
    // File schema with a nested struct
    Schema fullSchema =
        new Schema(
            required(1, "id", Types.IntegerType.get()),
            optional(
                2,
                "data",
                Types.StructType.of(
                    required(3, "x", Types.IntegerType.get()),
                    optional(4, "y", Types.StringType.get()))),
            optional(5, "extra", Types.StringType.get()));

    MessageType fileSchema = AdaptHiveParquetSchemaUtil.convert(fullSchema, "test");

    // Request only id and the nested struct — the TypeWithSchemaVisitor approach
    // correctly preserves nested fields using the expectedSchema tree.
    Schema expectedSchema =
        new Schema(
            required(1, "id", Types.IntegerType.get()),
            optional(
                2,
                "data",
                Types.StructType.of(
                    required(3, "x", Types.IntegerType.get()),
                    optional(4, "y", Types.StringType.get()))));

    MessageType pruned = AdaptHiveParquetSchemaUtil.pruneColumns(fileSchema, expectedSchema);

    Assert.assertEquals(2, pruned.getFieldCount());
    Assert.assertTrue(pruned.containsField("id"));
    Assert.assertTrue(pruned.containsField("data"));
    Assert.assertFalse(pruned.containsField("extra"));
  }

  @Test
  public void testPruneColumnsSingleColumn() {
    Schema fullSchema =
        new Schema(
            required(1, "id", Types.IntegerType.get()),
            optional(2, "name", Types.StringType.get()),
            optional(3, "value", Types.DoubleType.get()));

    MessageType fileSchema = AdaptHiveParquetSchemaUtil.convert(fullSchema, "test");

    Schema expectedSchema = new Schema(optional(2, "name", Types.StringType.get()));

    MessageType pruned = AdaptHiveParquetSchemaUtil.pruneColumns(fileSchema, expectedSchema);

    Assert.assertEquals(1, pruned.getFieldCount());
    Assert.assertTrue(pruned.containsField("name"));
  }
}
