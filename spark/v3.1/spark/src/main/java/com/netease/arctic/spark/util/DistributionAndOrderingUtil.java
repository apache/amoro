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

package com.netease.arctic.spark.util;

import com.clearspring.analytics.util.Lists;
import com.netease.arctic.spark.SparkAdapterLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.DistributionHashMode;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableProperties;
import org.apache.curator.shaded.com.google.common.collect.ObjectArrays;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.transforms.SortOrderVisitor;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.spark.sql.connector.expressions.Expression;
import org.apache.spark.sql.connector.expressions.Expressions;
import org.apache.spark.sql.connector.expressions.NamedReference;
import org.apache.spark.sql.connector.expressions.Transform;

import java.util.Arrays;
import java.util.List;

import static org.apache.iceberg.spark.Spark3Util.toTransforms;

public class DistributionAndOrderingUtil {

  private static final ExpressionHelper expressionHelper = SparkAdapterLoader.getOrLoad().expressions();

  private static final NamedReference SPEC_ID = Expressions.column(MetadataColumns.SPEC_ID.name());
  private static final NamedReference PARTITION = Expressions.column(MetadataColumns.PARTITION_COLUMN_NAME);
  private static final NamedReference FILE_PATH = Expressions.column(MetadataColumns.FILE_PATH.name());
  private static final NamedReference ROW_POSITION = Expressions.column(MetadataColumns.ROW_POSITION.name());

  private static final Expression SPEC_ID_ORDER = expressionHelper.sort(SPEC_ID, true);
  private static final Expression PARTITION_ORDER = expressionHelper.sort(PARTITION, true);
  private static final Expression FILE_PATH_ORDER = expressionHelper.sort(FILE_PATH, true);
  private static final Expression ROW_POSITION_ORDER = expressionHelper.sort(ROW_POSITION, true);
  private static final Expression[] METADATA_ORDERS = new Expression[] {
      PARTITION_ORDER, FILE_PATH_ORDER, ROW_POSITION_ORDER
  };


  public static Expression[] buildTableRequiredDistribution(ArcticTable table) {
    DistributionHashMode distributionHashMode = DistributionHashMode.autoSelect(
        table.isKeyedTable(),
        !table.spec().isUnpartitioned());

    List<Expression> distributionExpressions = Lists.newArrayList();

    if (distributionHashMode.isSupportPrimaryKey()) {
      Transform transform = toTransformsFromPrimary(table, table.asKeyedTable().primaryKeySpec());
      distributionExpressions.add(transform);
      if (distributionHashMode.isSupportPartition()) {
        distributionExpressions.addAll(Arrays.asList(toTransforms(table.spec())));
      }
    } else {
      if (distributionHashMode.isSupportPartition()) {
        distributionExpressions.addAll(Arrays.asList(toTransforms(table.spec())));
      }
    }
    return distributionExpressions.toArray(new Expression[0]);
  }

  private static Transform toTransformsFromPrimary(ArcticTable table, PrimaryKeySpec primaryKeySpec) {
    int numBucket = PropertyUtil.propertyAsInt(table.properties(),
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET, TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT);
    return Expressions.bucket(numBucket, primaryKeySpec.fieldNames().get(0));
  }

  public static Expression[] buildTableRequiredSortOrder(ArcticTable table, boolean rowLevelOperation) {
    Schema schema = table.schema();
    PartitionSpec partitionSpec = table.spec();
    PrimaryKeySpec keySpec = PrimaryKeySpec.noPrimaryKey();
    if (table.isKeyedTable()) {
      keySpec = table.asKeyedTable().primaryKeySpec();
    }
    boolean withMetaColumn = table.isUnkeyedTable() && rowLevelOperation;
    return buildSortOrder(schema, partitionSpec, keySpec, withMetaColumn);
  }

  private static Expression[] buildSortOrder(
      Schema schema,
      PartitionSpec partitionSpec,
      PrimaryKeySpec keySpec,
      boolean withMetaColumn) {
    if (partitionSpec.isUnpartitioned() && !keySpec.primaryKeyExisted() && !withMetaColumn) {
      return new Expression[0];
    }

    SortOrder.Builder builder = SortOrder.builderFor(schema);
    if (partitionSpec.isPartitioned()) {
      for (PartitionField field: partitionSpec.fields()) {
        String sourceName = schema.findColumnName(field.sourceId());
        builder.asc(org.apache.iceberg.expressions.Expressions.transform(sourceName, field.transform()));
      }
    }


    if (keySpec.primaryKeyExisted()) {
      for (PrimaryKeySpec.PrimaryKeyField field: keySpec.fields()) {
        builder.asc(org.apache.iceberg.expressions.Expressions.ref(field.fieldName()));
      }
    }

    SortOrder sortOrder = builder.build();
    List<Expression> converted = SortOrderVisitor.visit(sortOrder, new SortOrderToSpark(expressionHelper));
    Expression[] orders = converted.toArray(new Expression[0]);

    if (withMetaColumn) {
      orders = ObjectArrays.concat(orders, METADATA_ORDERS, Expression.class);
    }
    return orders;
  }
}
