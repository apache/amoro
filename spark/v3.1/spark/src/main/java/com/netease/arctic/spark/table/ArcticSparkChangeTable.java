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

package com.netease.arctic.spark.table;

import org.apache.iceberg.Table;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableSet;
import org.apache.iceberg.spark.source.SparkTable;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.types.StructType;

import java.util.Set;

public class ArcticSparkChangeTable extends SparkTable {
  private static final Set<TableCapability> CAPABILITIES = ImmutableSet.of(
      TableCapability.BATCH_READ
      );

  public ArcticSparkChangeTable(Table icebergTable) {
    super(icebergTable, true);
  }

  public ArcticSparkChangeTable(Table icebergTable, StructType requestedSchema, boolean refreshEagerly) {
    super(icebergTable, requestedSchema, refreshEagerly);
  }

  public Set<TableCapability> capabilities() {
    return CAPABILITIES;
  }
}
