package com.netease.arctic.ams.server.service;

import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import com.netease.arctic.ams.server.AmsTestBase;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.catalog.CatalogLoader;
import org.apache.iceberg.CatalogProperties;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.netease.arctic.ams.api.properties.CatalogMetaProperties.CATALOG_TYPE_HADOOP;
import static com.netease.arctic.ams.server.AmsTestBase.AMS_TEST_ICEBERG_CATALOG_NAME;
import static com.netease.arctic.ams.server.AmsTestBase.icebergCatalog;
import static com.netease.arctic.ams.server.AmsTestBase.tempFolder;

public class TestRefreshCatalog {

  @Test
  public void refreshCatalog() throws IOException, TException {
    Map<String, String> catalogProperties = new HashMap<>();
    catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, "newLocation");

    CatalogMeta icebergMeta = AmsTestBase.amsHandler.getCatalog(AMS_TEST_ICEBERG_CATALOG_NAME);
    CatalogMeta catalogMeta = new CatalogMeta(AMS_TEST_ICEBERG_CATALOG_NAME, CATALOG_TYPE_HADOOP,
        icebergMeta.storageConfigs, icebergMeta.authConfigs, catalogProperties);
    icebergCatalog = CatalogLoader.load(AmsTestBase.amsHandler, AMS_TEST_ICEBERG_CATALOG_NAME);
    CatalogMetadataService catalogMetadataService = ServiceContainer.getCatalogMetadataService();
    catalogMetadataService.updateCatalog(catalogMeta);
    icebergCatalog.refresh();
    Assert.assertEquals("newLocation",
        AmsTestBase.amsHandler.getCatalog(AMS_TEST_ICEBERG_CATALOG_NAME).
        getCatalogProperties().get(CatalogProperties.WAREHOUSE_LOCATION));
  }
}
