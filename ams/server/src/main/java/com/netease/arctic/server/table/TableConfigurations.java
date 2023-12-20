package com.netease.arctic.server.table;

import com.netease.arctic.ams.api.config.DataExpirationConfig;
import com.netease.arctic.ams.api.config.OptimizingConfig;
import com.netease.arctic.ams.api.config.TableConfiguration;
import com.netease.arctic.ams.api.config.TagConfiguration;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.server.utils.ConfigurationUtil;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Locale;
import java.util.Map;

public class TableConfigurations {


  private static TagConfiguration parseTagConfiguration(Map<String, String> tableProperties) {
    TagConfiguration tagConfig = new TagConfiguration();
    tagConfig.setAutoCreateTag(
        CompatiblePropertyUtil.propertyAsBoolean(
            tableProperties,
            TableProperties.ENABLE_AUTO_CREATE_TAG,
            TableProperties.ENABLE_AUTO_CREATE_TAG_DEFAULT));
    tagConfig.setTagFormat(
        CompatiblePropertyUtil.propertyAsString(
            tableProperties,
            TableProperties.AUTO_CREATE_TAG_DAILY_FORMAT,
            TableProperties.AUTO_CREATE_TAG_DAILY_FORMAT_DEFAULT));
    tagConfig.setTriggerPeriod(
        TagConfiguration.Period.valueOf(
            CompatiblePropertyUtil.propertyAsString(
                    tableProperties,
                    TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD,
                    TableProperties.AUTO_CREATE_TAG_TRIGGER_PERIOD_DEFAULT)
                .toUpperCase(Locale.ROOT)));
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
    return tagConfig;
  }

  /*public DataExpirationConfig parseDataExpirationConfig(ArcticTable table) {
    Map<String, String> properties = table.properties();
    expirationField =
        CompatiblePropertyUtil.propertyAsString(
            properties, TableProperties.DATA_EXPIRATION_FIELD, null);
    Types.NestedField field = table.schema().findField(expirationField);
    Preconditions.checkArgument(
        org.apache.commons.lang3.StringUtils.isNoneBlank(expirationField) && null != field,
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
        DataExpirationConfig.ExpireLevel.fromString(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_LEVEL,
                TableProperties.DATA_EXPIRATION_LEVEL_DEFAULT));

    String retention =
        CompatiblePropertyUtil.propertyAsString(
            properties, TableProperties.DATA_EXPIRATION_RETENTION_TIME, null);
    if (org.apache.commons.lang3.StringUtils.isNotBlank(retention)) {
      retentionTime = ConfigurationUtil.TimeUtils.parseDuration(retention).toMillis();
    }

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
    since =
        DataExpirationConfig.Since.fromString(
            CompatiblePropertyUtil.propertyAsString(
                properties,
                TableProperties.DATA_EXPIRATION_SINCE,
                TableProperties.DATA_EXPIRATION_SINCE_DEFAULT));
  }*/

  private static DataExpirationConfig parseDataExpirationConfig(Map<String, String> properties) {
    DataExpirationConfig config =
        new DataExpirationConfig()
            .setEnabled(
                CompatiblePropertyUtil.propertyAsBoolean(
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
            .setSince(
                DataExpirationConfig.Since.fromString(
                    CompatiblePropertyUtil.propertyAsString(
                        properties,
                        TableProperties.DATA_EXPIRATION_SINCE,
                        TableProperties.DATA_EXPIRATION_SINCE_DEFAULT)));
    String retention =
        CompatiblePropertyUtil.propertyAsString(
            properties, TableProperties.DATA_EXPIRATION_RETENTION_TIME, null);
    if (StringUtils.isNotBlank(retention)) {
      config.setRetentionTime(ConfigurationUtil.TimeUtils.parseDuration(retention).toMillis());
    }

    return config;
  }

  private static OptimizingConfig parseOptimizingConfig(Map<String, String> properties) {
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

  public static TableConfiguration parseConfig(Map<String, String> properties) {
    return new TableConfiguration()
        .setExpireSnapshotEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_TABLE_EXPIRE,
                TableProperties.ENABLE_TABLE_EXPIRE_DEFAULT))
        .setSnapshotTTLMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.BASE_SNAPSHOT_KEEP_MINUTES,
                TableProperties.BASE_SNAPSHOT_KEEP_MINUTES_DEFAULT))
        .setChangeDataTTLMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.CHANGE_DATA_TTL,
                TableProperties.CHANGE_DATA_TTL_DEFAULT))
        .setCleanOrphanEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_ORPHAN_CLEAN,
                TableProperties.ENABLE_ORPHAN_CLEAN_DEFAULT))
        .setOrphanExistingMinutes(
            CompatiblePropertyUtil.propertyAsLong(
                properties,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME,
                TableProperties.MIN_ORPHAN_FILE_EXISTING_TIME_DEFAULT))
        .setDeleteDanglingDeleteFilesEnabled(
            CompatiblePropertyUtil.propertyAsBoolean(
                properties,
                TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN,
                TableProperties.ENABLE_DANGLING_DELETE_FILES_CLEAN_DEFAULT))
        .setOptimizingConfig(parseOptimizingConfig(properties))
        .setExpiringDataConfig(parseDataExpirationConfig(properties))
        .setTagConfiguration(parseTagConfiguration(properties));
  }
}
