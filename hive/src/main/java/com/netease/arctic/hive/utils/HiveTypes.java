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

package com.netease.arctic.hive.utils;

import org.apache.commons.lang.StringUtils;

import static org.apache.hadoop.hive.serde.serdeConstants.BIGINT_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.BINARY_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.BOOLEAN_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.DATE_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.DECIMAL_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.DOUBLE_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.FLOAT_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.INT_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.STRING_TYPE_NAME;
import static org.apache.hadoop.hive.serde.serdeConstants.TIMESTAMP_TYPE_NAME;

public class HiveTypes {
  public static String typeIcebergToHive(String icebergType) {
    if (StringUtils.isBlank(icebergType)) {
      return icebergType;
    }

    icebergType = icebergType.replaceAll("\\s+", "").toLowerCase();

    // Special case
    if (icebergType.startsWith("decimal(")) {
      return icebergType;
    } else if (icebergType.startsWith("time(") || icebergType.equals("time")) {
      return STRING_TYPE_NAME;
    }

    switch (icebergType) {
      case "string":
        return STRING_TYPE_NAME;
      case "boolean":
        return BOOLEAN_TYPE_NAME;
      case "int":
        return INT_TYPE_NAME;
      case "long":
        return BIGINT_TYPE_NAME;
      case "float":
        return FLOAT_TYPE_NAME;
      case "double":
        return DOUBLE_TYPE_NAME;
      case "decimal":
        return DECIMAL_TYPE_NAME;
      case "binary":
        return BINARY_TYPE_NAME;
      case "timestamptz":
      case "timestamp":
        return TIMESTAMP_TYPE_NAME;
      case "date":
        return DATE_TYPE_NAME;
      default:
        throw new IllegalArgumentException("Unsupported iceberg type: " + icebergType);
    }
  }
}
