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

package com.netease.arctic.spark.writer;

import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.spark.SparkFilters;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.SupportsDynamicOverwrite;
import org.apache.spark.sql.connector.write.SupportsOverwrite;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import java.util.Locale;

public class SparkWriteBuilder implements WriteBuilder, SupportsDynamicOverwrite, SupportsOverwrite {
  private final KeyedTable table;
  private final CaseInsensitiveStringMap options;
  private final String overwriteMode;
  private boolean overwriteDynamic = false;
  private boolean overwriteByFilter = false;
  private Expression overwriteExpr = null;

  private StructType dsSchema = null;

  public SparkWriteBuilder(KeyedTable table, LogicalWriteInfo info) {
    this.table = table;
    this.options = info.options();
    this.overwriteMode = options.containsKey("overwrite-mode") ?
        options.get("overwrite-mode").toLowerCase(Locale.ROOT) : null;
    this.overwriteDynamic = "dynamic".equals(overwriteMode);
    this.dsSchema = info.schema();
  }

  @Override
  public WriteBuilder overwriteDynamicPartitions() {
    Preconditions.checkState(!overwriteByFilter, "Cannot overwrite dynamically and by filter: %s", overwriteExpr);
    this.overwriteDynamic = true;
    return this;
  }

  @Override
  public WriteBuilder overwrite(Filter[] filters) {
    this.overwriteExpr = SparkFilters.convert(filters);
    if (overwriteExpr == Expressions.alwaysTrue() && "dynamic".equals(overwriteMode)) {
      // use the write option to override truncating the table. use dynamic overwrite instead.
      this.overwriteDynamic = true;
    } else {
      Preconditions.checkState(!overwriteDynamic, "Cannot overwrite dynamically and by filter: %s", overwriteExpr);
      this.overwriteByFilter = true;
    }
    return this;
  }

  @Override
  public BatchWrite buildForBatch() {
    SparkWrite write = new SparkWrite(table, dsSchema);
    if (overwriteByFilter) {
      return write.asOverwriteByFilter(overwriteExpr);
    } else if (overwriteDynamic) {
      return write.asDynamicOverwrite();
    } else {
      throw new UnsupportedOperationException("support insert overwrite only");
    }
  }
}
