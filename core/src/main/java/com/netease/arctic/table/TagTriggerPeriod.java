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

package com.netease.arctic.table;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/** The interval for periodically triggering creating tags */
public enum TagTriggerPeriod {
  DAILY("daily") {
    @Override
    public long getTagTriggerTime(LocalDateTime checkTime, int triggerOffsetMinutes) {
      LocalTime offsetTime = LocalTime.ofSecondOfDay(triggerOffsetMinutes * 60L);
      LocalDateTime triggerTime = LocalDateTime.of(checkTime.toLocalDate(), offsetTime);
      return triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
  };

  private final String propertyName;

  TagTriggerPeriod(String propertyName) {
    this.propertyName = propertyName;
  }

  public String propertyName() {
    return propertyName;
  }

  /**
   * Obtain the trigger time for creating a tag, which is the idea time of the last tag before the
   * check time.
   *
   * <p>For example, when creating a daily tag, the check time is 2022-08-08 11:00:00 and the offset
   * is set to be 5 min, the idea trigger time is 2022-08-08 00:05:00.
   */
  public abstract long getTagTriggerTime(LocalDateTime checkTime, int triggerOffsetMinutes);
}
