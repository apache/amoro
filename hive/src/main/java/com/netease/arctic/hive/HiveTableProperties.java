package com.netease.arctic.hive;


public class HiveTableProperties {

  public static final String ARCTIC_TABLE_FLAG = "arctic.enable";

  public static final String ARCTIC_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";

  public static final String PARTITION_PROPERTIES_KEY_HIVE_LOCATION = "hive-location";

  public static final String PARTITION_PROPERTIES_KEY_TRANSIENT_TIME = "transient-time";

  public static final String BASE_HIVE_LOCATION_ROOT = "base.hive.location-root";

  public static final String AUTO_SYNC_HIVE_SCHEMA_CHANGE = "base.hive.auto-sync-schema-change";
  public static final boolean AUTO_SYNC_HIVE_SCHEMA_CHANGE_DEFAULT = true;

  public static final String AUTO_SYNC_HIVE_DATA_WRITE = "base.hive.auto-sync-data-write";
  public static final boolean AUTO_SYNC_HIVE_DATA_WRITE_DEFAULT = false;
}

