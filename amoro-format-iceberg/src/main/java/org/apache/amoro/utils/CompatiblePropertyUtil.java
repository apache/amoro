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

package org.apache.amoro.utils;

import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

/** PropertyUtil compatible with legacy properties */
public class CompatiblePropertyUtil {

  private CompatiblePropertyUtil() {}

  public static boolean propertyAsBoolean(
      Map<String, String> properties, String property, boolean defaultValue) {
    return PropertyUtil.propertyAsBoolean(properties, property, defaultValue);
  }

  public static double propertyAsDouble(
      Map<String, String> properties, String property, double defaultValue) {
    return PropertyUtil.propertyAsDouble(properties, property, defaultValue);
  }

  public static int propertyAsInt(
      Map<String, String> properties, String property, int defaultValue) {
    return PropertyUtil.propertyAsInt(properties, property, defaultValue);
  }

  public static long propertyAsLong(
      Map<String, String> properties, String property, long defaultValue) {
    return PropertyUtil.propertyAsLong(properties, property, defaultValue);
  }

  public static String propertyAsString(
      Map<String, String> properties, String property, String defaultValue) {
    return PropertyUtil.propertyAsString(properties, property, defaultValue);
  }

  public static MemorySize propertyAsMemorySize(
      Map<String, String> properties, String property, MemorySize defaultValue) {
    String value = properties.get(property);
    if (value != null) {
      return MemorySize.parse(value);
    }
    return defaultValue;
  }
}
