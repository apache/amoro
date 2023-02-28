package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.client.AmsClientPools;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;

public class RefreshCallGenerator {

  private AmsClient client;

  public RefreshCallGenerator(String amsAddress) {
    this.client = AmsClientPools.getClientPool(amsAddress).iface();
  }

  public RefreshCall generate(String tablePath) {
    return new RefreshCall(client, tablePath);
  }
}
