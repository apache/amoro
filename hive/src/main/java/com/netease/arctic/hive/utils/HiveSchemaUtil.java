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

package com.netease.arctic.hive.utils;

import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.TypeUtil;
import org.apache.iceberg.types.Types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils to handle Hive table schema.
 */
public class HiveSchemaUtil {

  /**
   * Converts the Iceberg schema to a Hive schema.
   * Filter partition fields from iceberg schema fields.
   *
   * @param schema The original Iceberg schema to convert
   * @param spec The original Iceberg partition spec to convert
   * @return The Hive table schema
   */
  public static Schema hiveTableSchema(Schema schema, PartitionSpec spec) {
    if (spec.isUnpartitioned()) {
      return schema;
    } else {
      return TypeUtil.selectNot(schema, spec.identitySourceIds());
    }
  }

  /**
   * Converts the Iceberg schema to a Hive schema (list of FieldSchema objects).
   * Filter partition fields from iceberg schema fields.
   *
   * @param schema The original Iceberg schema to convert
   * @param spec The original Iceberg partition spec to convert
   * @return The Hive column list generated from the Iceberg schema
   */
  public static List<FieldSchema> hiveTableFields(Schema schema, PartitionSpec spec) {
    return org.apache.iceberg.hive.HiveSchemaUtil.convert(hiveTableSchema(schema, spec));
  }

  /**
   * Converts the Iceberg partition spec to a Hive partition fields list.
   *
   * @param schema The original Iceberg schema to convert
   * @param spec The original Iceberg partition spec to convert
   * @return The Hive column list generated from the Iceberg schema
   */
  public static List<FieldSchema> hivePartitionFields(Schema schema, PartitionSpec spec) {
    return org.apache.iceberg.hive.HiveSchemaUtil.convert(TypeUtil.select(schema, spec.identitySourceIds()));
  }

  /**
   * Converts the Hive schema to a Iceberg schema with pk.
   *
   * @param hiveSchema The original Hive schema to convert
   * @param primaryKeys The primary keys need to add to Iceberg schema
   * @return An Iceberg schema
   */
  public static Schema convertHiveSchemaToIcebergSchema(List<FieldSchema> hiveSchema, List<String> primaryKeys) {
    Set<String> pkSet = new HashSet<>(primaryKeys);
    Schema schema = org.apache.iceberg.hive.HiveSchemaUtil.convert(hiveSchema);
    List<Types.NestedField> columns = schema.columns();
    List<Types.NestedField> columnsWithPk = new ArrayList<>();
    columns.forEach(nf -> {
      if (pkSet.contains(nf.name())) {
        columnsWithPk.add(nf.asRequired());
      } else {
        columnsWithPk.add(nf);
      }
    });
    return new Schema(columnsWithPk);
  }
}
