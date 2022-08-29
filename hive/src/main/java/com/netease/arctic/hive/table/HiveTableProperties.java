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

package com.netease.arctic.hive.table;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


public class HiveTableProperties {

  private static final String HIVE_PROPERTY_NAME_PREFIX = "arctic.";

  public static final String ARCTIC_TABLE_FLAG = "arctic.enable";

  public static final String ARCTIC_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";

  public static final String ARCTIC_CATALOG_NAME = "arctic.catalog.name";

  private static final Set<String> IGNORE_ARCTIC_PROPERTIES = Collections.unmodifiableSet(
      Arrays.stream(new String[]{
          ARCTIC_TABLE_FLAG,
          ARCTIC_TABLE_PRIMARY_KEYS,
          ARCTIC_CATALOG_NAME
      }).collect(Collectors.toSet()));

  public static String hiveTablePropertyName(String arcticPropertyName) {
    return HIVE_PROPERTY_NAME_PREFIX + arcticPropertyName;
  }

  public static boolean isArcticProperty(String hivePropertyName) {
    return hivePropertyName != null && hivePropertyName.startsWith(HIVE_PROPERTY_NAME_PREFIX);
  }

  public static boolean isIgnoreArcticProperty(String hivePropertyName) {
    return IGNORE_ARCTIC_PROPERTIES.contains(hivePropertyName);
  }

  public static String arcticPropertyName(String hivePropertyName) {
    Preconditions.checkArgument(isArcticProperty(hivePropertyName));
    return hivePropertyName.substring(HIVE_PROPERTY_NAME_PREFIX.length());
  }
}