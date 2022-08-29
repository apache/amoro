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

package com.netease.arctic.flink;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.utils.TypeConversions;

import java.util.List;

/**
 * An util that converts flink table schema.
 */
public class FlinkSchemaUtil {
  /**
   * Convert a {@link RowType} to a {@link TableSchema}.
   *
   * @param rowType     a RowType
   * @param primaryKeys
   * @return Flink TableSchema
   */
  public static TableSchema toSchema(RowType rowType, List<String> primaryKeys) {
    TableSchema.Builder builder = TableSchema.builder();
    for (RowType.RowField field : rowType.getFields()) {
      builder.field(field.getName(), TypeConversions.fromLogicalToDataType(field.getType()));
    }
    if (CollectionUtils.isNotEmpty(primaryKeys)) {
      builder.primaryKey(primaryKeys.toArray(new String[0]));
    }
    return builder.build();
  }

}
