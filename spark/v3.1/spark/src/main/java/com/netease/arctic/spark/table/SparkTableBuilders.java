package com.netease.arctic.spark.table;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.spark.ArcticTableStoreType;
import com.netease.arctic.table.BasicUnkeyedTable;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.spark.source.SparkTable;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import static com.netease.arctic.spark.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES;
import static com.netease.arctic.spark.SparkSQLProperties.USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT;
import static org.apache.iceberg.spark.SparkSQLProperties.HANDLE_TIMESTAMP_WITHOUT_TIMEZONE;

public class SparkTableBuilders {

  public static SparkTableBuilderFactory icebergTableBuilderFactory() {
    return new IcebergTableBuilderFactory();
  }

  public static SparkTableBuilderFactory mixedIcebergTableBuilderFactory() {
    return new MixedIcebergTableBuilderFactory();
  }

  public static SparkTableBuilderFactory mixedHiveTableBuilderFactory() {
    return new MixedHiveTableBuilderFactory();
  }

  static class IcebergTableBuilderFactory implements SparkTableBuilderFactory {

    @Override
    public SparkTableBuilder create(ArcticCatalog catalog, CaseInsensitiveStringMap options) {
      return (table, internalTable) -> {
        Preconditions.checkArgument(internalTable == null,
            "Iceberg tables do not support visit internal table");
        return new SparkTable(table.asUnkeyedTable(), true);
      };
    }
  }

  static class MixedIcebergTableBuilderFactory implements SparkTableBuilderFactory {

    @Override
    public SparkTableBuilder create(ArcticCatalog catalog, CaseInsensitiveStringMap options) {
      return (table, internalTable) -> {
        boolean useTimestampWithoutZoneInNewTables;
        SparkSession sparkSession = SparkSession.active();
        useTimestampWithoutZoneInNewTables = Boolean.parseBoolean(
            sparkSession.conf().get(USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES,
                USE_TIMESTAMP_WITHOUT_TIME_ZONE_IN_NEW_TABLES_DEFAULT));
        if (useTimestampWithoutZoneInNewTables) {
          sparkSession.conf().set(HANDLE_TIMESTAMP_WITHOUT_TIMEZONE, true);
        }

        if (table.isUnkeyedTable()) {
          return new ArcticIcebergSparkTable(table.asUnkeyedTable(), true);
        }
        if (ArcticTableStoreType.CHANGE.name().equalsIgnoreCase(internalTable)) {
          return new ArcticSparkChangeTable((BasicUnkeyedTable) table.asKeyedTable().changeTable(),
              true);
        }
        return new ArcticSparkTable(table, catalog);
      };
    }
  }

  static class MixedHiveTableBuilderFactory implements SparkTableBuilderFactory {

    @Override
    public SparkTableBuilder create(ArcticCatalog catalog, CaseInsensitiveStringMap options) {
      return (table, internalTable) -> {
        SparkSession sparkSession = SparkSession.active();
        sparkSession.conf().set(HANDLE_TIMESTAMP_WITHOUT_TIMEZONE, true);

        if (table.isKeyedTable() && ArcticTableStoreType.CHANGE.name().equalsIgnoreCase(internalTable)) {
          return new ArcticSparkChangeTable((BasicUnkeyedTable) table.asKeyedTable().changeTable(),
              true);
        }
        return new ArcticSparkTable(table, catalog);
      };
    }
  }
}
