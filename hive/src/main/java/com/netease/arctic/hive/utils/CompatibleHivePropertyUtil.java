package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

public class CompatibleHivePropertyUtil {

  private CompatibleHivePropertyUtil() {
  }

  public static boolean propertyAsBoolean(Map<String, String> properties,
                                          String property, boolean defaultValue) {
    return PropertyUtil.propertyAsBoolean(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static double propertyAsDouble(Map<String, String> properties,
                                        String property, double defaultValue) {
    return PropertyUtil.propertyAsDouble(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static int propertyAsInt(Map<String, String> properties,
                                  String property, int defaultValue) {
    return PropertyUtil.propertyAsInt(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static long propertyAsLong(Map<String, String> properties,
                                    String property, long defaultValue) {
    return PropertyUtil.propertyAsLong(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  public static String propertyAsString(Map<String, String> properties,
                                        String property, String defaultValue) {
    return PropertyUtil.propertyAsString(properties, getCompatibleProperty(properties, property), defaultValue);
  }

  private static String getCompatibleProperty(Map<String, String> properties, String property) {
    String legacyProperty = getLegacyProperty(property);
    if (legacyProperty != null && properties.containsKey(legacyProperty) && !properties.containsKey(property)) {
      return legacyProperty;
    } else {
      return property;
    }
  }

  private static String getLegacyProperty(String property) {
    if (property == null) {
      return null;
    }
    switch (property) {
      case HiveTableProperties.ARCTIC_TABLE_FLAG:
        return HiveTableProperties.ARCTIC_TABLE_FLAG_LEGACY;
      default:
        return null;
    }
  }
}
