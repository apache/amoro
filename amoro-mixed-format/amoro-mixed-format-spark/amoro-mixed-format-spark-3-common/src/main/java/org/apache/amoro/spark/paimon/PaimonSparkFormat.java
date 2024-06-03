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

package org.apache.amoro.spark.paimon;

import org.apache.amoro.TableFormat;
import org.apache.amoro.spark.SparkTableFormat;
import org.apache.spark.sql.connector.catalog.Table;

/** The spark table format implements of paimon */
public class PaimonSparkFormat implements SparkTableFormat {
  static final String KEY_STORAGE_HANDLER = "storage_handler";
  static final String PAIMON_STORAGE_HANDLER = "org.apache.paimon.hive.PaimonStorageHandler";

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }

  @Override
  public boolean isFormatOf(Table table) {
    return table.properties().containsKey(KEY_STORAGE_HANDLER)
        && PAIMON_STORAGE_HANDLER.equalsIgnoreCase(table.properties().get(KEY_STORAGE_HANDLER));
  }
}
