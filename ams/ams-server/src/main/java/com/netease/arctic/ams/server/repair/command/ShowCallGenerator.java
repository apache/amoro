package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.client.AmsClientPools;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;

public class ShowCallGenerator {

  private ArcticCatalog arcticCatalog;

  public ShowCallGenerator(String amsAddress) {
    this.arcticCatalog = CatalogLoader.load(amsAddress);
  }

  public ShowCall generate(ShowCall.Namespaces namespaces) {
    return new ShowCall(arcticCatalog, namespaces);
  }
}
