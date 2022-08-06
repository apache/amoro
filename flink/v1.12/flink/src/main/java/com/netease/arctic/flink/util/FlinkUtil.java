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

package com.netease.arctic.flink.util;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.config.TableConfigOptions;

import java.time.ZoneId;
import java.util.TimeZone;

import static java.time.ZoneId.SHORT_IDS;

public class FlinkUtil {

  public static TimeZone getLocalTimeZone(Configuration configuration) {
    String zone = configuration.getString(TableConfigOptions.LOCAL_TIME_ZONE);
    validateTimeZone(zone);
    ZoneId zoneId = TableConfigOptions.LOCAL_TIME_ZONE.defaultValue().equals(zone)
        ? ZoneId.systemDefault()
        : ZoneId.of(zone);
    return TimeZone.getTimeZone(zoneId);
  }

  /**
   * Validates user configured time zone.
   */
  public static void validateTimeZone(String zone) {
    final String zoneId = zone.toUpperCase();
    if (zoneId.startsWith("UTC+")
        || zoneId.startsWith("UTC-")
        || SHORT_IDS.containsKey(zoneId)) {
      throw new IllegalArgumentException(
          String.format(
              "The supported Zone ID is either a full name such as 'America/Los_Angeles',"
                  + " or a custom timezone id such as 'GMT-08:00', but configured Zone ID is '%s'.",
              zone));
    }
  }

}
