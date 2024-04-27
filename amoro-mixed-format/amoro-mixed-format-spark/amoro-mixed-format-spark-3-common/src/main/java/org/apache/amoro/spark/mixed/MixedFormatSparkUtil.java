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

package org.apache.amoro.spark.mixed;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.utils.CompatibleHivePropertyUtil;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.table.TableProperties;
import org.apache.spark.sql.connector.catalog.Table;

/** Util class for mixed format in spark engines. */
public class MixedFormatSparkUtil {

  /**
   * Check if a Spark Table is a mixed-format table.
   *
   * @param table the spark table loaded from session catalog
   * @return is it a mixed format table.
   */
  public static boolean isMixedFormatTable(Table table) {
    return isMixedHiveTable(table) || isMixedIcebergTable(table);
  }

  /**
   * Check if a Spark Table is a Mixed Iceberg table
   *
   * @param table the spark table loaded from session catalog
   * @return Is it a mixed Iceberg table.
   */
  public static boolean isMixedIcebergTable(Table table) {
    return table.properties() != null
        && table.properties().containsKey(TableProperties.TABLE_FORMAT)
        && TableFormat.MIXED_ICEBERG
            .name()
            .equalsIgnoreCase(table.properties().get(TableProperties.TABLE_FORMAT));
  }

  /**
   * Check if a Spark Table is a Mixed Hive table
   *
   * @param table the spark table loaded from session catalog
   * @return Is it a mixed Hive table.
   */
  public static boolean isMixedHiveTable(Table table) {
    return table.properties() != null
        && CompatibleHivePropertyUtil.propertyAsBoolean(
            table.properties(), HiveTableProperties.MIXED_TABLE_FLAG, false);
  }
}
