package com.netease.arctic.spark.source;

import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.spark.util.ArcticSparkUtil;
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
  public Optional<DataSourceWriter> createWriter(
      String jobId, StructType schema, SaveMode mode, DataSourceOptions options) {
    return Optional.empty();
  }

  @Override
  public DataSourceTable createTable(
      TableIdentifier identifier, StructType schema) {
    return null;
  }

  @Override
  public DataSourceTable loadTable(TableIdentifier identifier) {
    return null;
  }

  @Override
  public boolean dropTable(TableIdentifier identifier) {
    return false;
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
        catalog.name(), identifier.database().get(), identifier.table()
    );
    return catalog.tableExists(tableId);
  }

  private ArcticCatalog catalog(RuntimeConfig conf) {
    String url = ArcticSparkUtil.catalogUrl(conf);
    return CatalogLoader.load(url);
  }
}
