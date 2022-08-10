package com.netease.arctic.spark.source;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.spark.util.ArcticSparkUtil;
import com.netease.arctic.table.PrimaryKeySpec;
import com.netease.arctic.table.TableBuilder;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.spark.SparkSchemaUtil;
import com.netease.arctic.table.ArcticTable;
import org.apache.spark.sql.RuntimeConfig;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.catalyst.catalog.CatalogTable;
import org.apache.spark.sql.internal.StaticSQLConf$;
import org.apache.spark.sql.sources.DataSourceRegister;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.DataSourceV2;
import org.apache.spark.sql.sources.v2.ReadSupport;
import org.apache.spark.sql.sources.v2.WriteSupport;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.sources.v2.writer.DataSourceWriter;
import org.apache.spark.sql.types.StructType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArcticSource implements DataSourceRegister, DataSourceV2,
    ReadSupport, WriteSupport, TableSupport {
  @Override
  public String shortName() {
    return "arctic";
  }

  @Override
  public DataSourceReader createReader(DataSourceOptions options) {
    return null;
  }

  @Override
  public Optional<DataSourceWriter> createWriter(String jobId, StructType schema,
                                                 SaveMode mode, DataSourceOptions options) {
    return Optional.empty();
  }

  @Override
  public DataSourceTable createTable(
      TableIdentifier identifier, StructType schema, List<String> partitions, Map<String, String> properties) {
    SparkSession spark = SparkSession.getActiveSession().get();
    ArcticCatalog catalog = catalog(spark.conf());
    Schema arcticSchema = SparkSchemaUtil.convert(schema, false);
    PartitionSpec spec = toPartitionSpec(partitions, arcticSchema);
    TableBuilder tableBuilder = catalog.newTableBuilder(com.netease.arctic.table.TableIdentifier.
        of(catalog.name(), identifier.database().get(), identifier.table()), arcticSchema);
    if (properties.containsKey("primary.keys")) {
      PrimaryKeySpec primaryKeySpec = PrimaryKeySpec.builderFor(arcticSchema)
          .addDescription(properties.get("primary.keys"))
          .build();
      tableBuilder.withPartitionSpec(spec)
          .withPrimaryKeySpec(primaryKeySpec)
          .withProperties(properties)
          .create();
    } else {
      tableBuilder.withPartitionSpec(spec)
          .withProperties(properties)
          .create();
    }
    return null;
  }

  private static PartitionSpec toPartitionSpec(List<String> partitionKeys, Schema icebergSchema) {
    PartitionSpec.Builder builder = PartitionSpec.builderFor(icebergSchema);
    partitionKeys.forEach(builder::identity);
    return builder.build();
  }

  @Override
  public DataSourceTable loadTable(TableIdentifier identifier) {
    SparkSession spark = SparkSession.getActiveSession().get();
    ArcticCatalog catalog = catalog(spark.conf());
    com.netease.arctic.table.TableIdentifier tableId = com.netease.arctic.table.TableIdentifier.of(
        catalog.name(), identifier.database().get(), identifier.table());
    ArcticTable arcticTable = catalog.loadTable(tableId);
    return ArcticSparkTable.ofArcticTable(arcticTable);
  }

  @Override
  public boolean dropTable(TableIdentifier identifier, boolean purge) {
    SparkSession spark = SparkSession.getActiveSession().get();
    ArcticHiveCatalog catalog = (ArcticHiveCatalog) catalog(spark.conf());
    return catalog.dropTable(com.netease.arctic.table.TableIdentifier.
        of(catalog.name(), identifier.database().get(), identifier.table()), purge);
  }

  /**
   * delegate table to arctic if all condition matched
   * 1. spark catalog-impl is hive
   * 2. spark conf 'arctic.sql.delegate-hive-table = true'
   * 3. table.provider in (hive, parquet)
   * 4. arctic catalog have table with same identifier with given table
   */
  public boolean isDelegateTable(CatalogTable tableDesc) {
    SparkSession spark = SparkSession.getActiveSession().get();
    String sparkCatalogImpl = spark.conf().get(StaticSQLConf$.MODULE$.CATALOG_IMPLEMENTATION());
    if (!"hive".equalsIgnoreCase(sparkCatalogImpl)) {
      return false;
    }
    boolean delegateHiveTable = ArcticSparkUtil.delegateHiveTable(spark.conf());
    if (!delegateHiveTable) {
      return false;
    }
    if (!tableDesc.provider().isDefined()) {
      return false;
    }
    String provider = tableDesc.provider().get();
    if (!"hive".equalsIgnoreCase(provider) &&
        !"parquet".equalsIgnoreCase(provider) &&
        !"arctic".equalsIgnoreCase(provider)) {
      return false;
    }
    TableIdentifier identifier = tableDesc.identifier();
    ArcticCatalog catalog = catalog(spark.conf());
    com.netease.arctic.table.TableIdentifier tableId = com.netease.arctic.table.TableIdentifier.of(
        catalog.name(), identifier.database().get(), identifier.table());
    return catalog.tableExists(tableId);
  }

  private ArcticCatalog catalog(RuntimeConfig conf) {
    String url = ArcticSparkUtil.catalogUrl(conf);
    return CatalogLoader.load(url);
  }
}
