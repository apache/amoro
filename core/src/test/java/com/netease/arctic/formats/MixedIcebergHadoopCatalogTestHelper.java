package com.netease.arctic.formats;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.formats.mixed.MixedIcebergCatalogFactory;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

import java.util.HashMap;
import java.util.Map;

public class MixedIcebergHadoopCatalogTestHelper
    extends AbstractFormatCatalogTestHelper<ArcticCatalog> {

  public static final Schema schema =
      new Schema(
          Lists.newArrayList(
              Types.NestedField.required(1, DEFAULT_SCHEMA_ID_NAME, Types.LongType.get()),
              Types.NestedField.required(2, DEFAULT_SCHEMA_NAME_NAME, Types.StringType.get()),
              Types.NestedField.optional(3, DEFAULT_SCHEMA_AGE_NAME, Types.IntegerType.get())),
          Sets.newHashSet(1));

  public static final PartitionSpec spec =
      PartitionSpec.builderFor(schema).identity(DEFAULT_SCHEMA_AGE_NAME).build();

  public MixedIcebergHadoopCatalogTestHelper(
      String catalogName, Map<String, String> catalogProperties) {
    super(catalogName, catalogProperties);
  }

  @Override
  protected TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public AmoroCatalog amoroCatalog() {
    MixedIcebergCatalogFactory mixedIcebergCatalogFactory = new MixedIcebergCatalogFactory();
    TableMetaStore metaStore = CatalogUtil.buildMetaStore(getCatalogMeta());
    Map<String, String> properties =
        mixedIcebergCatalogFactory.convertCatalogProperties(
            catalogName, getMetastoreType(), catalogProperties);
    return mixedIcebergCatalogFactory.create(
        catalogName, getMetastoreType(), properties, metaStore);
  }

  @Override
  public ArcticCatalog originalCatalog() {
    CatalogMeta meta = getCatalogMeta();
    TableMetaStore metaStore = CatalogUtil.buildMetaStore(meta);
    return CatalogLoader.createCatalog(
        catalogName(), meta.getCatalogType(), meta.getCatalogProperties(), metaStore);
  }

  @Override
  public void setTableProperties(String db, String tableName, String key, String value) {
    originalCatalog()
        .loadTable(TableIdentifier.of(catalogName(), db, tableName))
        .updateProperties()
        .set(key, value)
        .commit();
  }

  @Override
  public void removeTableProperties(String db, String tableName, String key) {
    originalCatalog()
        .loadTable(TableIdentifier.of(catalogName(), db, tableName))
        .updateProperties()
        .remove(key)
        .commit();
  }

  @Override
  public void clean() {
    ArcticCatalog catalog = originalCatalog();
    catalog
        .listDatabases()
        .forEach(
            db -> {
              catalog.listTables(db).forEach(id -> catalog.dropTable(id, true));
              try {
                catalog.dropDatabase(db);
              } catch (Exception e) {
                // pass
              }
            });
  }

  @Override
  public void createTable(String db, String tableName) throws Exception {
    ArcticCatalog catalog = originalCatalog();
    catalog
        .newTableBuilder(TableIdentifier.of(catalogName(), db, tableName), schema)
        .withPartitionSpec(spec)
        .create();
  }

  public static MixedIcebergHadoopCatalogTestHelper defaultHelper() {
    return new MixedIcebergHadoopCatalogTestHelper("test_mixed_iceberg_catalog", new HashMap<>());
  }
}
