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

package com.netease.arctic.spark.mixed;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.utils.CompatibleHivePropertyUtil;
import com.netease.arctic.table.TableProperties;
import org.apache.spark.sql.connector.catalog.Table;

public class MixedFormatSparkUtil {

  /**
   * Check a Spark Table is a mixed-format table.
   *
   * @param table the spark table loaded from session catalog
   * @return is it a mixed format table.
   */
  public static boolean isMixedFormatTable(Table table) {
    boolean isMixedHive = isMixedHiveTable(table);
    boolean isMixedIceberg = isMixedIcebergTable(table);
    return isMixedHive || isMixedIceberg;
  }

  /**
   * Check a Spark Table is a Mixed Iceberg table
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
   * Check a Spark Table is a Mixed Hive table
   *
   * @param table the spark table loaded from session catalog
   * @return Is it a mixed Hive table.
   */
  public static boolean isMixedHiveTable(Table table) {
    return table.properties() != null
        && CompatibleHivePropertyUtil.propertyAsBoolean(
            table.properties(), HiveTableProperties.ARCTIC_TABLE_FLAG, false);
  }
}
