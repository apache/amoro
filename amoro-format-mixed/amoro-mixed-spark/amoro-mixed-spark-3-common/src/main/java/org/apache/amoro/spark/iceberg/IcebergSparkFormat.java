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

package org.apache.amoro.spark.iceberg;

import org.apache.amoro.TableFormat;
import org.apache.amoro.spark.SparkTableFormat;
import org.apache.amoro.spark.mixed.MixedFormatSparkUtil;
import org.apache.iceberg.BaseMetastoreTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.spark.sql.connector.catalog.Table;

import java.util.regex.Pattern;

/** Iceberg format implementation of spark table format */
public class IcebergSparkFormat implements SparkTableFormat {
  private static final Pattern AT_TIMESTAMP = Pattern.compile("at_timestamp_(\\d+)");
  private static final Pattern SNAPSHOT_ID = Pattern.compile("snapshot_id_(\\d+)");
  private static final Pattern BRANCH = Pattern.compile("branch_(.*)");
  private static final Pattern TAG = Pattern.compile("tag_(.*)");

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public boolean isSubTableName(String tableName) {
    return MetadataTableType.from(tableName) != null
        || AT_TIMESTAMP.matcher(tableName).matches()
        || SNAPSHOT_ID.matcher(tableName).matches()
        || TAG.matcher(tableName).matches()
        || BRANCH.matcher(tableName).matches();
  }

  @Override
  public boolean isFormatOf(Table table) {
    String icebergTableType = table.properties().get(BaseMetastoreTableOperations.TABLE_TYPE_PROP);
    return BaseMetastoreTableOperations.ICEBERG_TABLE_TYPE_VALUE.equalsIgnoreCase(icebergTableType)
        && !MixedFormatSparkUtil.isMixedFormatTable(table);
  }
}
