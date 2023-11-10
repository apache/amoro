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

package com.netease.arctic.spark.actions;

import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.spark.SparkWriteOptions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

class MixFormatDataRewriter {

  private final SparkSession spark;
  private final ArcticTable table;

  MixFormatDataRewriter(SparkSession spark, ArcticTable table) {
    this.spark = spark;
    this.table = table;
  }

  protected void doRewrite(String groupId) {
    Dataset<Row> scanDF =
        spark
            .read()
            .format("arctic")
            .option(SparkWriteOptions.REWRITTEN_FILE_SCAN_TASK_SET_ID, groupId)
            .load(table.id().getDatabase() + "." + table.id().getTableName());

    scanDF
        .write()
        .format("arctic")
        .option(SparkWriteOptions.REWRITTEN_FILE_SCAN_TASK_SET_ID, groupId)
        .mode("overwrite")
        .save(table.id().getDatabase() + "." + table.id().getTableName());
  }
}
