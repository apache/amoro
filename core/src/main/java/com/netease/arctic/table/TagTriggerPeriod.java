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

  public abstract long getTagTriggerTime(LocalDateTime checkTime, int triggerOffsetMinutes);
}
