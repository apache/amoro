package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.AmsClient;
import com.netease.arctic.PooledAmsClient;

public class RefreshCallGenerator {
  private AmsClient client;

  public RefreshCallGenerator(String amsAddress) {
    this.client = new PooledAmsClient(amsAddress);
  }

  public RefreshCall generate(String tablePath) {
    return new RefreshCall(client, tablePath);
  }
}
