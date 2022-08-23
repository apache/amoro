package com.netease.arctic.hive.table;

import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


public class HiveTableProperties {

  public static final String ICEBERG_CATALOG = "iceberg.catalog";
  public static final String ICEBERG_CATALOG_LOCATION_BASED_TABLE = "location_based_table";

  private static final String HIVE_PROPERTY_NAME_PREFIX = "arctic.";

  public static final String ARCTIC_TABLE_FLAG = "arctic.enable";

  public static final String ARCTIC_SERVER_NAME = "arctic.server.name";

  public static final String ARCTIC_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";

  public static final String ARCTIC_HIVE_EXTERNAL_TABLE = "arctic.hive.external";

  public static final String ARCTIC_CATALOG_NAME = "arctic.catalog.name";

  private static final Set<String> IGNORE_ARCTIC_PROPERTIES = Collections.unmodifiableSet(
      Arrays.stream(new String[]{
          ARCTIC_TABLE_FLAG,
          ARCTIC_TABLE_PRIMARY_KEYS,
          ARCTIC_SERVER_NAME,
          ARCTIC_HIVE_EXTERNAL_TABLE,
          ARCTIC_CATALOG_NAME
      }).collect(Collectors.toSet()));

  public static String hiveTablePropertyName(String arcticPropertyName) {
    return HIVE_PROPERTY_NAME_PREFIX + arcticPropertyName;
  }

  public static boolean isArcticProperty(String hivePropertyName) {
    return hivePropertyName != null && hivePropertyName.startsWith(HIVE_PROPERTY_NAME_PREFIX);
  }

  public static boolean isIgnoreArcticProperty(String hivePropertyName) {
    return IGNORE_ARCTIC_PROPERTIES.contains(hivePropertyName);
  }

  public static String arcticPropertyName(String hivePropertyName) {
    Preconditions.checkArgument(isArcticProperty(hivePropertyName));
    return hivePropertyName.substring(HIVE_PROPERTY_NAME_PREFIX.length());
  }
}