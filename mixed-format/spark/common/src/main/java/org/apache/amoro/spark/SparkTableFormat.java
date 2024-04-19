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

package org.apache.amoro.spark;

import org.apache.amoro.TableFormat;
import org.apache.spark.sql.connector.catalog.Table;

/** SPI interface for spark unified catalog to adapt different table formats */
public interface SparkTableFormat {

  /** Table format */
  TableFormat format();

  /** Check if the give table name is a valid inspecting table name. */
  default boolean isSubTableName(String tableName) {
    return false;
  }

  /** Check the table loaded by spark session catalog is match the table format. */
  boolean isFormatOf(Table table);
}
