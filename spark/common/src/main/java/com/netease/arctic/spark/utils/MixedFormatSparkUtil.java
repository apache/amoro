package com.netease.arctic.spark.utils;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.utils.CompatibleHivePropertyUtil;
import com.netease.arctic.table.TableProperties;
import org.apache.spark.sql.connector.catalog.Table;

public class MixedFormatSparkUtil {

  /**
   * check a Spark is a mixed-format table.
   * @param table the spark table loaded from hive catalog
   * @return is it a mixed format table.
   */
  public static boolean isMixedFormatTable(Table table) {
    boolean isMixedHive = table.properties() != null &&
        CompatibleHivePropertyUtil.propertyAsBoolean(
            table.properties(), HiveTableProperties.ARCTIC_TABLE_FLAG, false);

    boolean isMixedIceberg = table.properties() != null &&
        table.properties().containsKey(TableProperties.TABLE_FORMAT) &&
        TableFormat.MIXED_ICEBERG.name().equalsIgnoreCase(table.properties().get(TableProperties.TABLE_FORMAT));
    return isMixedHive || isMixedIceberg;
  }
}
