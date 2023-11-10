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

package com.netease.arctic.spark.procedures;

import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.spark.actions.BaseRewriteAction;
import com.netease.arctic.spark.table.ArcticSparkTable;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.blocker.Blocker;
import com.netease.arctic.table.blocker.TableBlockerManager;
import org.apache.iceberg.actions.RewriteDataFiles;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.Spark3Util;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.Expression;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.apache.spark.sql.connector.iceberg.catalog.ProcedureParameter;
import org.apache.spark.sql.execution.datasources.SparkExpressionConverter;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.unsafe.types.UTF8String;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RewriteDataFilesProcedure extends BaseProcedure {

  static final DataType STRING_MAP =
      DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType);

  private static final ProcedureParameter[] PARAMETERS =
      new ProcedureParameter[] {
          ProcedureParameter.required("table", DataTypes.StringType),
          ProcedureParameter.optional("where", DataTypes.StringType),
          ProcedureParameter.optional("options", STRING_MAP)
      };

  private static final StructType OUTPUT_TYPE =
      new StructType(
          new StructField[] {
              new StructField(
                  "rewritten_data_files_count", DataTypes.IntegerType, false, Metadata.empty()),
              new StructField(
                  "rewritten_delete_files_count", DataTypes.IntegerType, false, Metadata.empty()),
              new StructField(
                  "rewritten_partition_count", DataTypes.IntegerType, false, Metadata.empty()),
              new StructField(
                  "rewritten_base_store_partition_count", DataTypes.IntegerType, false, Metadata.empty()),
              new StructField(
                  "rewritten_partitions", DataTypes.StringType, false, Metadata.empty()),
              new StructField(
                  "added_data_files_count", DataTypes.IntegerType, false, Metadata.empty()),
          });

  protected TableBlockerManager tableBlockerManager;
  protected Blocker block;

  public static SupportProcedures.ProcedureBuilder<RewriteDataFilesProcedure> builder() {
    return new SupportProcedures.ProcedureBuilder<RewriteDataFilesProcedure>() {
      TableCatalog tableCatalog;

      @Override
      public SupportProcedures.ProcedureBuilder<RewriteDataFilesProcedure> withTableCatalog(TableCatalog tableCatalog) {
        this.tableCatalog = tableCatalog;
        return this;
      }

      @Override
      public RewriteDataFilesProcedure build() {
        return new RewriteDataFilesProcedure(tableCatalog);
      }
    };
  }

  public RewriteDataFilesProcedure(TableCatalog tableCatalog) {
    super(tableCatalog);
  }

  @Override
  public ProcedureParameter[] parameters() {
    return PARAMETERS;
  }

  @Override
  public StructType outputType() {
    return OUTPUT_TYPE;
  }

  @Override
  public InternalRow[] call(InternalRow args) {
    ArcticSparkTable sparkTable = loadSparkTable(toIdentifier(args.getString(0), PARAMETERS[0].name()));
    ArcticTable arcticTable = sparkTable.table();
    ArcticCatalog arcticCatalog = sparkTable.arcticCatalog();
    String where = args.isNullAt(1) ? null : args.getString(1);
    getBlocker(arcticCatalog, arcticTable);
    try {
      checkBlocker(tableBlockerManager);
      BaseRewriteAction action = actions().rewriteDataFiles(arcticTable);
      action = checkAndApplyFilter(action, where, arcticTable.id().toString());
      return toOutputRows(action.execute());
    } finally {
      tableBlockerManager.release(block);
    }
  }

  private InternalRow[] toOutputRows(BaseRewriteAction.RewriteResult result) {
    InternalRow row = newInternalRow(result.rewriteFileCount(),
        result.rewriteDeleteFileCount(),
        result.rewritePartitionCount(),
        result.rewriteBaseStorePartitionCount(),
        UTF8String.fromString(result.rewritePartitions()),
        result.addFileCount());
    return new InternalRow[] {row};
  }

  private InternalRow newInternalRow(Object... values) {
    return new GenericInternalRow(values);
  }

  public void checkBlocker(TableBlockerManager tableBlockerManager) {
    List<String> blockerIds = tableBlockerManager.getBlockers()
        .stream().map(Blocker::blockerId).collect(Collectors.toList());
    if (!blockerIds.contains(block.blockerId())) {
      throw new IllegalStateException("block is not in blockerManager");
    }
  }

  public void getBlocker(ArcticCatalog catalog, ArcticTable table) {
    this.tableBlockerManager = catalog.getTableBlockerManager(table.id());
    ArrayList<BlockableOperation> operations = Lists.newArrayList();
    operations.add(BlockableOperation.BATCH_WRITE);
    operations.add(BlockableOperation.OPTIMIZE);
    try {
      this.block = tableBlockerManager.block(operations);
    } catch (OperationConflictException e) {
      throw new IllegalStateException("failed to block table " + table.id() + " with " + operations, e);
    }
  }

  private BaseRewriteAction checkAndApplyFilter(
      BaseRewriteAction action, String where, String tableName) {
    if (where != null) {
      try {
        Expression expression =
            SparkExpressionConverter.collectResolvedSparkExpression(spark, tableName, where);
        return action.filter(SparkExpressionConverter.convertToIcebergExpression(expression));
      } catch (AnalysisException e) {
        throw new IllegalArgumentException("Cannot parse predicates in where option: " + where, e);
      }
    }
    return action;
  }

  @Override
  public String description() {
    return "RewriteDataFilesProcedure";
  }
}
