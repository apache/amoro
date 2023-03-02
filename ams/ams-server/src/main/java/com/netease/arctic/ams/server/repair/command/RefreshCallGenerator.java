package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.api.ArcticTableMetastore;
import com.netease.arctic.ams.api.client.AmsClientPools;

public class RefreshCallGenerator {

  private ArcticTableMetastore.Iface client;

  public RefreshCallGenerator(String amsAddress) {
    this.client = AmsClientPools.getClientPool(amsAddress).iface();
  }

  public RefreshCall generate(String tablePath) {
    return new RefreshCall(client, tablePath);
  }
}
