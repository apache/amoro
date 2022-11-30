package com.netease.arctic.catalog;

import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.CatalogMeta;
import com.netease.arctic.ams.api.properties.CatalogMetaProperties;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;

public class TestRefreshCatalog extends TableTestBase {

  @Test
  public void refreshCatalog() throws TException {
    CatalogMeta catalog = AMS.handler().getCatalog(TEST_CATALOG_NAME);
    AMS.handler().updateMeta(catalog, CatalogMetaProperties.KEY_WAREHOUSE_DIR, "/test");
    testCatalog = CatalogLoader.load(AMS.getUrl());
    testCatalog.refresh();
    Assert.assertEquals("/test",
        AMS.handler().getCatalog(TEST_CATALOG_NAME).
        getCatalogProperties().get(CatalogMetaProperties.KEY_WAREHOUSE_DIR));
  }
}
