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

package org.apache.amoro.config;

import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Objects;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Locale;

/** Data expiration configuration. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataExpirationConfig {
  // data-expire.enabled
  private boolean enabled;
  // data-expire.field
  private String expirationField;
  // data-expire.level
  private ExpireLevel expirationLevel;
  // data-expire.retention-time
  private long retentionTime;
  // data-expire.datetime-string-pattern
  private String dateTimePattern;
  // data-expire.datetime-number-format
  private String numberDateFormat;
  // data-expire.base-on-rule
  private BaseOnRule baseOnRule;

  @VisibleForTesting
  public enum ExpireLevel {
    PARTITION,
    FILE;

    public static ExpireLevel fromString(String level) {
      Preconditions.checkArgument(null != level, "Invalid level type: null");
      try {
        return ExpireLevel.valueOf(level.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(String.format("Invalid level type: %s", level), e);
      }
    }
  }

  @VisibleForTesting
  public enum BaseOnRule {
    LAST_COMMIT_TIME,
    CURRENT_TIME
  }

  public DataExpirationConfig() {}

  public boolean isEnabled() {
    return enabled;
  }

  public DataExpirationConfig setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getExpirationField() {
    return expirationField;
  }

  public DataExpirationConfig setExpirationField(String expirationField) {
    this.expirationField = expirationField;
    return this;
  }

  public ExpireLevel getExpirationLevel() {
    return expirationLevel;
  }

  public DataExpirationConfig setExpirationLevel(ExpireLevel expirationLevel) {
    this.expirationLevel = expirationLevel;
    return this;
  }

  public long getRetentionTime() {
    return retentionTime;
  }

  public DataExpirationConfig setRetentionTime(long retentionTime) {
    this.retentionTime = retentionTime;
    return this;
  }

  public String getDateTimePattern() {
    return dateTimePattern;
  }

  public DataExpirationConfig setDateTimePattern(String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
    return this;
  }

  public String getNumberDateFormat() {
    return numberDateFormat;
  }

  public DataExpirationConfig setNumberDateFormat(String numberDateFormat) {
    this.numberDateFormat = numberDateFormat;
    return this;
  }

  public BaseOnRule getBaseOnRule() {
    return baseOnRule;
  }

  public DataExpirationConfig setBaseOnRule(BaseOnRule baseOnRule) {
    this.baseOnRule = baseOnRule;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DataExpirationConfig)) {
      return false;
    }
    DataExpirationConfig config = (DataExpirationConfig) o;
    return enabled == config.enabled
        && retentionTime == config.retentionTime
        && Objects.equal(expirationField, config.expirationField)
        && expirationLevel == config.expirationLevel
        && Objects.equal(dateTimePattern, config.dateTimePattern)
        && Objects.equal(numberDateFormat, config.numberDateFormat)
        && baseOnRule == config.baseOnRule;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        enabled,
        expirationField,
        expirationLevel,
        retentionTime,
        dateTimePattern,
        numberDateFormat,
        baseOnRule);
  }
}
