package com.netease.arctic.flink.util;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.config.TableConfigOptions;

import java.time.ZoneId;
import java.util.TimeZone;

import static java.time.ZoneId.SHORT_IDS;

public class FlinkUtil {

  public static TimeZone getLocalTimeZone(Configuration configuration) {
    String zone = configuration.getString(TableConfigOptions.LOCAL_TIME_ZONE);
    validateTimeZone(zone);
    ZoneId zoneId = TableConfigOptions.LOCAL_TIME_ZONE.defaultValue().equals(zone)
        ? ZoneId.systemDefault()
        : ZoneId.of(zone);
    return TimeZone.getTimeZone(zoneId);
  }

  /**
   * Validates user configured time zone.
   */
  public static void validateTimeZone(String zone) {
    final String zoneId = zone.toUpperCase();
    if (zoneId.startsWith("UTC+")
        || zoneId.startsWith("UTC-")
        || SHORT_IDS.containsKey(zoneId)) {
      throw new IllegalArgumentException(
          String.format(
              "The supported Zone ID is either a full name such as 'America/Los_Angeles',"
                  + " or a custom timezone id such as 'GMT-08:00', but configured Zone ID is '%s'.",
              zone));
    }
  }

}
