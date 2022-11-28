package com.netease.arctic.hive.utils;

import com.netease.arctic.hive.catalog.ArcticHiveCatalog;
import com.netease.arctic.catalog.ArcticCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogUtil {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogUtil.class);

  /**
   * check arctic catalog is hive catalog
   * @param arcticCatalog target arctic catalog
   * @return Whether hive catalog. true is hive catalog, false isn't hive catalog.
   */
  public static boolean isHiveCatalog(ArcticCatalog arcticCatalog) {
    return arcticCatalog instanceof ArcticHiveCatalog;
  }
}
