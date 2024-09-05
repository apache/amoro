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

public class SparkSQLProperties {

  public static final String USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES =
      "spark.sql.mixed-format.use-timestamp-without-timezone-in-new-tables";

  public static final String USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT = "false";

  public static final String REFRESH_CATALOG_BEFORE_USAGE =
      "spark.sql.mixed-format.refresh-catalog-before-usage";

  public static final String REFRESH_CATALOG_BEFORE_USAGE_DEFAULT = "false";

  public static final String CHECK_SOURCE_DUPLICATES_ENABLE =
      "spark.sql.mixed-format.check-source-data-uniqueness.enabled";

  public static final String CHECK_SOURCE_DUPLICATES_ENABLE_DEFAULT = "false";

  public static final String OPTIMIZE_WRITE_ENABLED =
      "spark.sql.mixed-format.optimize-write-enabled";
  public static final String OPTIMIZE_WRITE_ENABLED_DEFAULT = "true";
}
