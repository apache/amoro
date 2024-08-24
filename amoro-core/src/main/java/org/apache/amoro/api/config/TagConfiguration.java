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

package org.apache.amoro.api.config;

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Configuration for auto creating tags. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TagConfiguration {
  // tag.auto-create.enabled
  private boolean autoCreateTag = false;
  // tag.auto-create.tag-format
  private String tagFormat;
  // tag.auto-create.trigger.period
  private Period triggerPeriod;
  // tag.auto-create.trigger.offset.minutes
  private int triggerOffsetMinutes;
  // tag.auto-create.trigger.max-delay.minutes
  private int maxDelayMinutes;
  // tag.auto-create.max-age-ms
  private long tagMaxAgeMs;

  /** The interval for periodically triggering creating tags */
  public enum Period {
    DAILY("daily") {
      @Override
      protected Duration periodDuration() {
        return Duration.ofDays(1);
      }

      @Override
      public LocalDateTime getTagTime(LocalDateTime checkTime, int triggerOffsetMinutes) {
        return checkTime.minusMinutes(triggerOffsetMinutes).truncatedTo(ChronoUnit.DAYS);
      }
    },

    HOURLY("hourly") {
      @Override
      protected Duration periodDuration() {
        return Duration.ofHours(1);
      }

      @Override
      public LocalDateTime getTagTime(LocalDateTime checkTime, int triggerOffsetMinutes) {
        return checkTime.minusMinutes(triggerOffsetMinutes).truncatedTo(ChronoUnit.HOURS);
      }
    };

    private final String propertyName;

    Period(String propertyName) {
      this.propertyName = propertyName;
    }

    public String propertyName() {
      return propertyName;
    }

    protected abstract Duration periodDuration();

    /**
     * Obtain the tag time for creating a tag, which is the ideal time of the last tag before the
     * check time.
     *
     * <p>For example, when creating a daily tag, the check time is 2022-08-08 11:00:00 and the
     * offset is set to be 5 min, the idea tag time is 2022-08-08 00:00:00.
     *
     * <p>For example, when creating a daily tag, the offset is set to be 30 min, if the check time
     * is 2022-08-08 02:00:00, the ideal tag time is 2022-08-08 00:00:00; if the check time is
     * 2022-08-09 00:20:00 (before 00:30 of the next day), the ideal tag time is still 2022-08-08
     * 00:00:00.
     */
    public abstract LocalDateTime getTagTime(LocalDateTime checkTime, int triggerOffsetMinutes);

    public String generateTagName(LocalDateTime tagTime, String tagFormat) {
      return tagTime.minus(periodDuration()).format(DateTimeFormatter.ofPattern(tagFormat));
    }
  }

  public boolean isAutoCreateTag() {
    return autoCreateTag;
  }

  public void setAutoCreateTag(boolean autoCreateTag) {
    this.autoCreateTag = autoCreateTag;
  }

  public String getTagFormat() {
    return tagFormat;
  }

  public void setTagFormat(String tagFormat) {
    this.tagFormat = tagFormat;
  }

  public Period getTriggerPeriod() {
    return triggerPeriod;
  }

  public void setTriggerPeriod(Period triggerPeriod) {
    this.triggerPeriod = triggerPeriod;
  }

  public int getTriggerOffsetMinutes() {
    return triggerOffsetMinutes;
  }

  public void setTriggerOffsetMinutes(int triggerOffsetMinutes) {
    this.triggerOffsetMinutes = triggerOffsetMinutes;
  }

  public int getMaxDelayMinutes() {
    return maxDelayMinutes;
  }

  public void setMaxDelayMinutes(int maxDelayMinutes) {
    this.maxDelayMinutes = maxDelayMinutes;
  }

  public long getTagMaxAgeMs() {
    return tagMaxAgeMs;
  }

  public void setTagMaxAgeMs(long tagMaxAgeMs) {
    this.tagMaxAgeMs = tagMaxAgeMs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TagConfiguration that = (TagConfiguration) o;
    return autoCreateTag == that.autoCreateTag
        && triggerOffsetMinutes == that.triggerOffsetMinutes
        && maxDelayMinutes == that.maxDelayMinutes
        && Objects.equal(tagFormat, that.tagFormat)
        && triggerPeriod == that.triggerPeriod
        && tagMaxAgeMs == that.tagMaxAgeMs;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        autoCreateTag,
        tagFormat,
        triggerPeriod,
        triggerOffsetMinutes,
        maxDelayMinutes,
        tagMaxAgeMs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("autoCreateTag", autoCreateTag)
        .add("tagFormat", tagFormat)
        .add("triggerPeriod", triggerPeriod)
        .add("triggerOffsetMinutes", triggerOffsetMinutes)
        .add("maxDelayMinutes", maxDelayMinutes)
        .add("tagMaxAgeMs", tagMaxAgeMs)
        .toString();
  }
}
