package com.netease.arctic.formats;

import com.netease.arctic.AmoroCatalog;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.catalog.CatalogTestHelpers;
import org.apache.hadoop.conf.Configuration;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFormatCatalogTestHelper<C> implements AmoroCatalogTestHelper<C> {

  protected final String catalogName;

  protected final Map<String, String> catalogProperties;

  protected AbstractFormatCatalogTestHelper(
      String catalogName, Map<String, String> catalogProperties) {
    this.catalogName = catalogName;
    this.catalogProperties = catalogProperties == null ? new HashMap<>() : catalogProperties;
  }

  @Override
  public void initWarehouse(String warehouseLocation) {
    catalogProperties.put(catalogWarehouseKey(), warehouseLocation);
  }

  protected String catalogWarehouseKey() {
    return "warehouse";
  }

  protected abstract TableFormat format();

  protected String getMetastoreType() {
    return CatalogMetaProperties.CATALOG_TYPE_HADOOP;
  }

  @Override
  public void initHiveConf(Configuration hiveConf) {}

  @Override
  public CatalogMeta getCatalogMeta() {
    return CatalogTestHelpers.buildCatalogMeta(
        catalogName, getMetastoreType(), catalogProperties, format());
  }

  @Override
  public abstract AmoroCatalog amoroCatalog();

  @Override
  public abstract C originalCatalog();

  @Override
  public String catalogName() {
    return catalogName;
  }
}
