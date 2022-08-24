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

import com.netease.arctic.table.UnkeyedTable;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.SupportsDynamicOverwrite;
import org.apache.spark.sql.connector.write.SupportsOverwrite;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.sources.Filter;

// TODO: AdaptHiveRead/Writer
public class UnkeyedSparkWriteBuilder implements WriteBuilder, SupportsDynamicOverwrite, SupportsOverwrite {

  public UnkeyedSparkWriteBuilder(UnkeyedTable table, LogicalWriteInfo info) {

  }

  @Override
  public WriteBuilder overwriteDynamicPartitions() {
    return null;
  }

  @Override
  public WriteBuilder overwrite(Filter[] filters) {
    return null;
  }

  @Override
  public BatchWrite buildForBatch() {
    return null;
  }
}
