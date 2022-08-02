package com.netease.arctic.spark.util;

import org.apache.commons.lang.StringUtils;
import org.apache.spark.sql.RuntimeConfig;

public class ArcticSparkUtil {
  public static final String CATALOG_URL = "arctic.catalog.url";
  public static final String SQL_DELEGATE_HIVE_TABLE = "arctic.sql.delegate-hive-table";

  public static String catalogUrl(RuntimeConfig conf) {
    String catalogUrl = conf.get(CATALOG_URL, "");
    if (StringUtils.isBlank(catalogUrl)) {
      throw new IllegalArgumentException("arctic.catalog.url is blank");
    }
    return catalogUrl;
  }

  public static boolean delegateHiveTable(RuntimeConfig config) {
    String val = config.get(SQL_DELEGATE_HIVE_TABLE, "true");
    return Boolean.parseBoolean(val);
  }
}
