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

package org.apache.amoro.server.table;

import org.apache.amoro.config.ConfigHelpers;
import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.config.TagConfiguration;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.annotations.VisibleForTesting;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.PropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Util class to help create table configuration for mixed-format and iceberg table. */
public class TableConfigurations {

  private static final Logger LOG = LoggerFactory.getLogger(TableConfigurations.class);

  /**
   * Parse table configuration from table properties
   *
   * @param properties table properties
   * @return table configuration
   */
  public static TableConfiguration parseTableConfig(Map<String, String> properties) {
    boolean gcEnabled = CompatiblePropertyUtil.propertyAsBoolean(properties, "gc.enabled", true);
    return new TableConfiguration()
        .setExpireSnapshotEnabled(
            gcEnabled
                && CompatiblePropertyUtil.propertyAsBoolean(
                    properties,
                    TableProperties.ENABLE_TABLE_EXPIRE,
                    TableProperties.ENABLE_TABLE_EXPIRE_DEFAULT))
        .setSnapshotTTLMinutes(
            ConfigHelpers.TimeUtils.parseDuration(
                        CompatiblePropertyUtil.propertyAsString(
                            properties,
                            TableProperties.SNAPSHOT_KEEP_DURATION,
                            TableProperties.SNAPSHOT_KEEP_DURATION_DEFAULT),
                        ChronoUnit.MINUTES)
                    .getSeconds()
                / 60)
        .setSnapshotMinCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SNAPSHOT_MIN_COUNT,
                TableProperties.SNAPSHOT_MIN_COUNT_DEFAULT))
        .setChangeDataTTLMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.CHANGE_DATA_TTL,
                TableProperties.CHANGE_DATA_TTL_DEFAULT))
        .setCleanOrphanEnabled(
            gcEnabled
                && CompatiblePropertyUtil.propertyAsBoolean(
                    properties,
                    TableProperties.ENABLE_ORPHAN_CLEAN,
                    TableProperties.ENABLE_ORPHAN_CLEAN_DEFAULT))
        .setOrphanExistingMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT))
        .setDeleteDanglingDeleteFilesEnabled(
            gcEnabled
                && CompatiblePropertyUtil.propertyAsBoolean(
                    properties,
                    TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN,
                    TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN_DEFAULT))
        .setOptimizingConfig(parseOptimizingConfig(properties))
        .setExpiringDataConfig(parseDataExpirationConfig(properties))
        .setTagConfiguration(parseTagConfiguration(properties));
  }

  /**
   * Parse table data expiration config from table properties
   *
   * @param properties table properties
   * @return table expiration config
   */
  @VisibleForTesting
  public static DataExpirationConfig parseDataExpirationConfig(Map<String, String> properties) {
    boolean gcEnabled =
        CompatiblePropertyUtil.propertyAsBoolean(
            properties, org.apache.iceberg.TableProperties.GC_ENABLED, true);
    DataExpirationConfig config =
        new DataExpirationConfig()
            .setEnabled(
                gcEnabled
                    && CompatiblePropertyUtil.propertyAsBoolean(
                        properties,
                        TableProperties.ENABLE_DATA_EXPIRATION,
                        TableProperties.ENABLE_DATA_EXPIRATION_DEFAULT))
            .setExpirationLevel(
                DataExpirationConfig.ExpireLevel.fromString(
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
                parseDataExpirationBaseOnRule(
                    CompatiblePropertyUtil.propertyAsString(
                        properties,
                        TableProperties.DATA_EXPIRATION_BASE_ON_RULE,
                        TableProperties.DATA_EXPIRATION_BASE_ON_RULE_DEFAULT)));
    String retention =
        CompatiblePropertyUtil.propertyAsString(
            properties, TableProperties.DATA_EXPIRATION_RETENTION_TIME, null);
    if (StringUtils.isNotBlank(retention)) {
      config.setRetentionTime(ConfigHelpers.TimeUtils.parseDuration(retention).toMillis());
    }

    return config;
  }

  private static DataExpirationConfig.BaseOnRule parseDataExpirationBaseOnRule(String since) {
    Preconditions.checkArgument(
        null != since, TableProperties.DATA_EXPIRATION_BASE_ON_RULE + " is invalid: null");
    try {
      return DataExpirationConfig.BaseOnRule.valueOf(since.toUpperCase(Locale.ENGLISH));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          String.format("Unable to expire data since: %s", since), e);
    }
  }

  /**
   * Check if the given field is valid for data expiration.
   *
   * @param config data expiration config
   * @param field table nested field
   * @param name table name
   * @return true if field is valid
   */
  public static boolean isValidDataExpirationField(
      DataExpirationConfig config, Types.NestedField field, String name) {
    return config.isEnabled()
        && config.getRetentionTime() > 0
        && validateExpirationField(field, name, config.getExpirationField());
  }

  public static final Set<Type.TypeID> DATA_EXPIRATION_FIELD_TYPES =
      Sets.newHashSet(Type.TypeID.TIMESTAMP, Type.TypeID.STRING, Type.TypeID.LONG);

  private static boolean validateExpirationField(
      Types.NestedField field, String name, String expirationField) {
    if (StringUtils.isBlank(expirationField) || null == field) {
      LOG.warn(
          String.format(
              "Field(%s) used to determine data expiration is illegal for table(%s)",
              expirationField, name));
      return false;
    }
    Type.TypeID typeID = field.type().typeId();
    if (!DATA_EXPIRATION_FIELD_TYPES.contains(typeID)) {
      LOG.warn(
          String.format(
              "Table(%s) field(%s) type(%s) is not supported for data expiration, please use the "
                  + "following types: %s",
              name,
              expirationField,
              typeID.name(),
              StringUtils.join(DATA_EXPIRATION_FIELD_TYPES, ", ")));
      return false;
    }

    return true;
  }

  /**
   * Parse optimizing config from table properties
   *
   * @param properties table properties
   * @return table optimizing config
   */
  @VisibleForTesting
  public static OptimizingConfig parseOptimizingConfig(Map<String, String> properties) {
    return new OptimizingConfig()
        .setEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_SELF_OPTIMIZING,
                TableProperties.ENABLE_SELF_OPTIMIZING_DEFAULT))
        .setMaxExecuteRetryCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER,
                TableProperties.SELF_OPTIMIZING_EXECUTE_RETRY_NUMBER_DEFAULT))
        .setOptimizerGroup(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.SELF_OPTIMIZING_GROUP,
                TableProperties.SELF_OPTIMIZING_GROUP_DEFAULT))
        .setFragmentRatio(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
                TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT))
        .setMinTargetSizeRatio(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO,
                TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT))
        .setMaxFileCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT,
                TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT))
        .setOpenFileCost(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SPLIT_OPEN_FILE_COST,
                TableProperties.SPLIT_OPEN_FILE_COST_DEFAULT))
        .setTargetSize(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
                TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT))
        .setMaxTaskSize(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_MAX_TASK_SIZE,
                TableProperties.SELF_OPTIMIZING_MAX_TASK_SIZE_DEFAULT))
        .setTargetQuota(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_QUOTA,
                TableProperties.SELF_OPTIMIZING_QUOTA_DEFAULT))
        .setMinorLeastFileCount(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT_DEFAULT))
        .setMinorLeastInterval(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL,
                TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_INTERVAL_DEFAULT))
        .setMajorDuplicateRatio(
            CompatiblePropertyUtil.propertyAsDouble(
                properties,
                TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO,
                TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO_DEFAULT))
        .setFullTriggerInterval(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL,
                TableProperties.SELF_OPTIMIZING_FULL_TRIGGER_INTERVAL_DEFAULT))
        .setFullRewriteAllFiles(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES,
                TableProperties.SELF_OPTIMIZING_FULL_REWRITE_ALL_FILES_DEFAULT))
        .setBaseHashBucket(
            CompatiblePropertyUtil.propertyAsInt(
                properties,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
                TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT))
        .setBaseRefreshInterval(
            PropertyUtil.propertyAsLong(
                properties,
                TableProperties.BASE_REFRESH_INTERVAL,
                TableProperties.BASE_REFRESH_INTERVAL_DEFAULT))
        .setHiveRefreshInterval(
            PropertyUtil.propertyAsLong(
                properties,
                HiveTableProperties.REFRESH_HIVE_INTERVAL,
                HiveTableProperties.REFRESH_HIVE_INTERVAL_DEFAULT))
        .setMinPlanInterval(
            PropertyUtil.propertyAsLong(
                properties,
                TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL,
                TableProperties.SELF_OPTIMIZING_MIN_PLAN_INTERVAL_DEFAULT));
  }

  /**
   * Parse tag configuration from table properties
   *
   * @param tableProperties table properties
   * @return table tag configuration
   */
  @VisibleForTesting
  public static TagConfiguration parseTagConfiguration(Map<String, String> tableProperties) {
    TagConfiguration tagConfig = new TagConfiguration();
    tagConfig.setAutoCreateTag(
        CompatiblePropertyUtil.propertyAsBoolean(
            tableProperties,
            TableProperties.ENABLE_AUTO_CREATE_TAG,
            TableProperties.ENABLE_AUTO_CREATE_TAG_DEFAULT));
    tagConfig.setTriggerPeriod(
        TagConfiguration.Period.valueOf(
            CompatiblePropertyUtil.propertyAsString(
                    tableProperties,
                    TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD,
                    TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD_DEFAULT)
                .toUpperCase(Locale.ROOT)));

    String defaultFormat;
    switch (tagConfig.getTriggerPeriod()) {
      case DAILY:
        defaultFormat = TableProperties.AUTO_CREATE_TAG_FORMAT_DAILY_DEFAULT;
        break;
      case HOURLY:
        defaultFormat = TableProperties.AUTO_CREATE_TAG_FORMAT_HOURLY_DEFAULT;
        break;
      default:
        throw new IllegalArgumentException(
            "Unsupported trigger period: " + tagConfig.getTriggerPeriod());
    }
    tagConfig.setTagFormat(
        CompatiblePropertyUtil.propertyAsString(
            tableProperties, TableProperties.AUTO_CREATE_TAG_FORMAT, defaultFormat));
    tagConfig.setTriggerOffsetMinutes(
        CompatiblePropertyUtil.propertyAsInt(
            tableProperties,
            TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES,
            TableProperties.AUTO_CREATE_TAG_TRIGGER_OFFSET_MINUTES_DEFAULT));
    tagConfig.setMaxDelayMinutes(
        CompatiblePropertyUtil.propertyAsInt(
            tableProperties,
            TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES,
            TableProperties.AUTO_CREATE_TAG_MAX_DELAY_MINUTES_DEFAULT));
    tagConfig.setTagMaxAgeMs(
        CompatiblePropertyUtil.propertyAsLong(
            tableProperties,
            TableProperties.AUTO_CREATE_TAG_MAX_AGE_MS,
            TableProperties.AUTO_CREATE_TAG_MAX_AGE_MS_DEFAULT));
    return tagConfig;
  }
}
