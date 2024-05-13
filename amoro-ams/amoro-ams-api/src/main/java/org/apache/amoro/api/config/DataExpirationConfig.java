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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.TimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Objects;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Data expiration configuration. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataExpirationConfig {
  // data-expire.enabled
  private boolean enabled;
  // data-expire.field
  private String expirationField;
  // data-expire.level
  @JsonProperty(defaultValue = TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT)
  private ExpireLevel expirationLevel;
  // data-expire.retention-time
  private long retentionTime;
  // data-expire.datetime-string-pattern
  @JsonProperty(defaultValue = TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT)
  private String dateTimePattern;
  // data-expire.datetime-number-format
  @JsonProperty(defaultValue = TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT)
  private String numberDateFormat;
  // data-expire.base-on-rule
  @JsonProperty(defaultValue = TableProperties.DATA_EXPIRATION_BASE_ON_RULE_DEFAULT)
  private BaseOnRule baseOnRule;

  // Retention time must be positive
  public static final long INVALID_RETENTION_TIME = 0L;

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
    CURRENT_TIME;

    public static BaseOnRule fromString(String since) {
      Preconditions.checkArgument(
          null != since, TableProperties.DATA_EXPIRATION_BASE_ON_RULE + " is invalid: null");
      try {
        return BaseOnRule.valueOf(since.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Unable to expire data since: %s", since), e);
      }
    }
  }

  public static final Set<Type.TypeID> FIELD_TYPES =
      Sets.newHashSet(Type.TypeID.TIMESTAMP, Type.TypeID.STRING, Type.TypeID.LONG);

  private static final Logger LOG = LoggerFactory.getLogger(DataExpirationConfig.class);

  public DataExpirationConfig() {}

  public DataExpirationConfig(
      boolean enabled,
      String expirationField,
      ExpireLevel expirationLevel,
      long retentionTime,
      String dateTimePattern,
      String numberDateFormat,
      BaseOnRule baseOnRule) {
    this.enabled = enabled;
    this.expirationField = expirationField;
    this.expirationLevel = expirationLevel;
    this.retentionTime = retentionTime;
    this.dateTimePattern = dateTimePattern;
    this.numberDateFormat = numberDateFormat;
    this.baseOnRule = baseOnRule;
  }

  public DataExpirationConfig(MixedTable table) {
    Map<String, String> properties = table.properties();
    expirationField =
        CompatiblePropertyUtil.propertyAsString(
            properties, TableProperties.DATA_EXPIRATION_FIELD, null);
    Types.NestedField field = table.schema().findField(expirationField);
    Preconditions.checkArgument(
        StringUtils.isNoneBlank(expirationField) && null != field,
        String.format(
            "Field(%s) used to determine data expiration is illegal for table(%s)",
            expirationField, table.name()));
    Type.TypeID typeID = field.type().typeId();
    Preconditions.checkArgument(
        FIELD_TYPES.contains(typeID),
        String.format(
            "The type(%s) of filed(%s) is incompatible for table(%s)",
            typeID.name(), expirationField, table.name()));

    expirationLevel =
        ExpireLevel.fromString(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_LEVEL,
                TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT));
    retentionTime =
        parseRetentionToMillis(
            CompatiblePropertyUtil.propertyAsString(
                properties, TableProperties.DATA_EXPIRATION_RETENTION_TIME, null));
    dateTimePattern =
        CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN,
            TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT);
    numberDateFormat =
        CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT);
    baseOnRule =
        BaseOnRule.fromString(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_BASE_ON_RULE,
                TableProperties.DATA_EXPIRATION_BASE_ON_RULE_DEFAULT));
  }

  public static DataExpirationConfig parse(Map<String, String> properties) {
    boolean gcEnabled =
        CompatiblePropertyUtil.propertyAsBoolean(
            properties, org.apache.iceberg.TableProperties.GC_ENABLED, true);

    return new DataExpirationConfig()
        .setEnabled(
            gcEnabled
                && CompatiblePropertyUtil.propertyAsBoolean(
                    properties,
                    TableProperties.ENABLE_DATA_EXPIRATION,
                    TableProperties.ENABLE_DATA_EXPIRATION_DEFAULT))
        .setExpirationLevel(
            ExpireLevel.fromString(
                CompatiblePropertyUtil.propertyAsString(
                    properties,
                    TableProperties.DATA_EXPIRATION_LEVEL,
                    TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT)))
        .setExpirationField(
            CompatiblePropertyUtil.propertyAsString(
                properties, TableProperties.DATA_EXPIRATION_FIELD, null))
        .setDateTimePattern(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN,
                TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT))
        .setNumberDateFormat(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT,
                TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT))
        .setBaseOnRule(
            BaseOnRule.fromString(
                CompatiblePropertyUtil.propertyAsString(
                    properties,
                    TableProperties.DATA_EXPIRATION_BASE_ON_RULE,
                    TableProperties.DATA_EXPIRATION_BASE_ON_RULE_DEFAULT)))
        .setRetentionTime(
            parseRetentionToMillis(
                CompatiblePropertyUtil.propertyAsString(
                    properties, TableProperties.DATA_EXPIRATION_RETENTION_TIME, null)));
  }

  private static long parseRetentionToMillis(String retention) {
    try {
      return TimeUtil.estimatedMills(retention);
    } catch (Exception e) {
      return INVALID_RETENTION_TIME;
    }
  }

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

  public boolean isValid(Types.NestedField field, String name) {
    return isEnabled()
        && getRetentionTime() > INVALID_RETENTION_TIME
        && validateExpirationField(field, name, getExpirationField());
  }

  private boolean validateExpirationField(
      Types.NestedField field, String name, String expirationField) {
    if (StringUtils.isBlank(expirationField) || null == field) {
      LOG.warn(
          String.format(
              "Field(%s) used to determine data expiration is illegal for table(%s)",
              expirationField, name));
      return false;
    }
    Type.TypeID typeID = field.type().typeId();
    if (!DataExpirationConfig.FIELD_TYPES.contains(typeID)) {
      LOG.warn(
          String.format(
              "Table(%s) field(%s) type(%s) is not supported for data expiration, please use the "
                  + "following types: %s",
              name,
              expirationField,
              typeID.name(),
              StringUtils.join(DataExpirationConfig.FIELD_TYPES, ", ")));
      return false;
    }

    return true;
  }
}
