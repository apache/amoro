package com.netease.arctic.server.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExpiringDataConfig {
  // table-expire.data.enabled
  private boolean enabled;
  // table-expire.data.level
  private String level;
  // table-expire.data.field
  private String field;
  // table-expire.data.datetime-string-format
  private String dateStringFormat;
  // table-expire.data.datetime-number-format
  private String dateNumberFormat;
  // table-expire.data.retention-time
  private String retentionTime;

  public ExpiringDataConfig() {
  }

  public boolean isEnabled() {
    return enabled;
  }

  public ExpiringDataConfig setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getLevel() {
    return level;
  }

  public ExpiringDataConfig setLevel(String level) {
    this.level = level;
    return this;
  }

  public String getField() {
    return field;
  }

  public ExpiringDataConfig setField(String field) {
    this.field = field;
    return this;
  }

  public String getDateStringFormat() {
    return dateStringFormat;
  }

  public ExpiringDataConfig setDateStringFormat(String dateStringFormat) {
    this.dateStringFormat = dateStringFormat;
    return this;
  }

  public String getDateNumberFormat() {
    return dateNumberFormat;
  }

  public ExpiringDataConfig setDateNumberFormat(String dateNumberFormat) {
    this.dateNumberFormat = dateNumberFormat;
    return this;
  }

  public String getRetentionTime() {
    return retentionTime;
  }

  public ExpiringDataConfig setRetentionTime(String retentionTime) {
    this.retentionTime = retentionTime;
    return this;
  }

  public static ExpiringDataConfig parse(Map<String, String> properties) {
    return new ExpiringDataConfig()
        .setEnabled(CompatiblePropertyUtil.propertyAsBoolean(
            properties,
            TableProperties.ENABLE_DATA_EXPIRATION,
            TableProperties.ENABLE_DATA_EXPIRATION_DEFAULT))
        .setLevel(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_LEVEL,
            TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT))
        .setField(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_FIELD,
            null))
        .setDateStringFormat(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_STRING_FORMAT,
            TableProperties.DATA_EXPIRATION_DATE_STRING_FORMAT_DEFAULT))
        .setDateNumberFormat(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT))
        .setRetentionTime(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_RETENTION_TIME,
            null
        ));
  }
}
