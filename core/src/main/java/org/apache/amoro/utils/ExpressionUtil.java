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

package org.apache.amoro.utils;

import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.UnboundTerm;
import org.apache.iceberg.types.Types;

import java.util.Collection;

/** Utility class for working with {@link Expression}. */
public class ExpressionUtil {

  /**
   * Convert partition data to data filter.
   *
   * @param table the {@link MixedTable} table
   * @param specId the partition spec id
   * @param partitions the collection of partition data
   * @return data filter converted from partition data
   */
  public static Expression convertPartitionDataToDataFilter(
      MixedTable table, int specId, Collection<StructLike> partitions) {
    Expression filter = Expressions.alwaysFalse();
    for (StructLike partition : partitions) {
      filter = Expressions.or(filter, convertPartitionDataToDataFilter(table, specId, partition));
    }
    return filter;
  }

  /**
   * Convert partition data to data filter.
   *
   * @param table the {@link MixedTable} table
   * @param specId the partition spec id
   * @param partition the partition data
   * @return data filter converted from partition data
   */
  public static Expression convertPartitionDataToDataFilter(
      MixedTable table, int specId, StructLike partition) {
    PartitionSpec spec = MixedTableUtil.getMixedTablePartitionSpecById(table, specId);
    Schema schema = table.schema();
    Expression filter = Expressions.alwaysTrue();
    for (int i = 0; i < spec.fields().size(); i++) {
      PartitionField partitionField = spec.fields().get(i);
      Types.NestedField sourceField = schema.findField(partitionField.sourceId());
      UnboundTerm transform = Expressions.transform(sourceField.name(), partitionField.transform());

      Class<?> resultType =
          partitionField.transform().getResultType(sourceField.type()).typeId().javaClass();
      Object partitionValue = partition.get(i, resultType);
      if (partitionValue != null) {
        filter = Expressions.and(filter, Expressions.equal(transform, partitionValue));
      } else {
        filter = Expressions.and(filter, Expressions.isNull(transform));
      }
    }
    return filter;
  }
}
