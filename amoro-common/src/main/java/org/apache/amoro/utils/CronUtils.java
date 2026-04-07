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
 *
 * Modified by Datazip Inc. in 2026
 */

package org.apache.amoro.utils;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Lightweight utility for evaluating whether a cron expression has fired in a given time window.
 *
 * <p>Supports standard 5-field Unix cron format: {@code minute hour day-of-month month day-of-week}
 */
public final class CronUtils {

  private static final CronParser UNIX_CRON_PARSER =
      new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

  private CronUtils() {}

  public static boolean hasFiredInWindow(String cronExpr, long currentTimeMs, long windowSec) {
    if (cronExpr == null || cronExpr.trim().isEmpty()) {
      return false;
    }
    try {
      Cron cron = UNIX_CRON_PARSER.parse(cronExpr);
      ExecutionTime cronTime = ExecutionTime.forCron(cron);
      ZonedDateTime current =
          ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMs), ZoneId.systemDefault());

      Optional<ZonedDateTime> cronStartTimeOptional = cronTime.lastExecution(current);
      if (cronStartTimeOptional.isPresent()) {
        ZonedDateTime cronStartTime = cronStartTimeOptional.get();
        ZonedDateTime cronEndTime = cronStartTime.plusSeconds(windowSec);
        return (cronStartTime.isBefore(current) || cronStartTime.isEqual(current))
            && cronEndTime.isAfter(current);
      }
      return false;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns true if the cron expression fired at least once in the last 59 seconds relative to the
   * provided current time.
   */
  public static boolean hasFiredInLastMinute(String cronExpr) {
    return hasFiredInWindow(cronExpr, System.currentTimeMillis(), 60);
  }
}
