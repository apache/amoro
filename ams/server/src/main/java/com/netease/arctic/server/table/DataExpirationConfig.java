package com.netease.arctic.server.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.netease.arctic.server.utils.ConfigurationUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

  @VisibleForTesting
  public enum ExpireLevel {
    PARTITION,
    FILE;

    public static ExpireLevel fromString(String level) {
      Preconditions.checkArgument(null != level, "Invalid level type: null");
      try {
        return ExpireLevel.valueOf(level.toUpperCase(Locale.ENGLISH));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            String.format("Invalid level type: %s", level), e);
      }
    }
  }

  private static final Set<Type.TypeID> FIELD_TYPES = Sets.newHashSet(
      Type.TypeID.TIMESTAMP,
      Type.TypeID.STRING,
      Type.TypeID.LONG
  );

  public DataExpirationConfig() {
  }

  public DataExpirationConfig(
      boolean enabled,
      String expirationField,
      ExpireLevel expirationLevel,
      long retentionTime,
      String dateTimePattern,
      String numberDateFormat) {
    this.enabled = enabled;
    this.expirationField = expirationField;
    this.expirationLevel = expirationLevel;
    this.retentionTime = retentionTime;
    this.dateTimePattern = dateTimePattern;
    this.numberDateFormat = numberDateFormat;
  }

  public DataExpirationConfig(ArcticTable table) {
    Map<String, String> properties = table.properties();
    expirationField = CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_FIELD,
        null);
    Types.NestedField field = table.schema().findField(expirationField);
    Preconditions.checkArgument(
        StringUtils.isNoneBlank(expirationField) && null != field,
        String.format("Field(%s) used to determine data expiration is illegal for table(%s)",
            expirationField,
            table.name()));
    Type.TypeID typeID = field.type().typeId();
    Preconditions.checkArgument(FIELD_TYPES.contains(typeID),
        String.format("The type(%s) of filed(%s) is incompatible for table(%s)",
            typeID.name(),
            expirationField,
            table.name()));

    expirationLevel = ExpireLevel.fromString(CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_LEVEL,
        TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT));

    String retention = CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_RETENTION_TIME,
        null);
    if (StringUtils.isNotBlank(retention)) {
      retentionTime = ConfigurationUtil.TimeUtils.parseDuration(retention).toMillis();
    }

    dateTimePattern = CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN,
        TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT);
    numberDateFormat = CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT,
        TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT);
  }

  public static DataExpirationConfig parse(Map<String, String> properties) {
    DataExpirationConfig config = new DataExpirationConfig()
        .setEnabled(CompatiblePropertyUtil.propertyAsBoolean(
        properties,
        TableProperties.ENABLE_DATA_EXPIRATION,
        TableProperties.ENABLE_DATA_EXPIRATION_DEFAULT))
        .setExpirationLevel(ExpireLevel.fromString(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_LEVEL,
            TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT)))
        .setExpirationField(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_FIELD,
            null))
        .setDateTimePattern(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN,
            TableProperties.DATA_EXPIRATION_DATE_STRING_PATTERN_DEFAULT))
        .setNumberDateFormat(CompatiblePropertyUtil.propertyAsString(
            properties,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT,
            TableProperties.DATA_EXPIRATION_DATE_NUMBER_FORMAT_DEFAULT));
    String retention = CompatiblePropertyUtil.propertyAsString(
        properties,
        TableProperties.DATA_EXPIRATION_RETENTION_TIME,
        null
    );
    if (StringUtils.isNotBlank(retention)) {
      config.setRetentionTime(ConfigurationUtil.TimeUtils.parseDuration(retention).toMillis());
    }

    return config;
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
}
