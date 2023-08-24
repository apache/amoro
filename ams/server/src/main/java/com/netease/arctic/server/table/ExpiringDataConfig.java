package com.netease.arctic.server.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
  // table-expire.data.date-formatter
  private String dateFormatter;
  // table-expire.data.retention-time
  private long retentionTime;

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

  public String getDateFormatter() {
    return dateFormatter;
  }

  public ExpiringDataConfig setDateFormatter(String dateFormatter) {
    this.dateFormatter = dateFormatter;
    return this;
  }

  public long getRetentionTime() {
    return retentionTime;
  }

  public ExpiringDataConfig setRetentionTime(long retentionTime) {
    this.retentionTime = retentionTime;
    return this;
  }

  public static ExpiringDataConfig parse(Map<String, String> properties) {
    return new ExpiringDataConfig().setEnabled(CompatiblePropertyUtil.propertyAsBoolean(
        properties,
        "",
        false));
  }
}
