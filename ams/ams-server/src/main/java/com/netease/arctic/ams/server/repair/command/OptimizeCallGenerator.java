package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.api.OptimizeManager;
import com.netease.arctic.ams.api.client.OptimizeManagerClientPools;
import com.netease.arctic.ams.server.repair.DamageType;

public class OptimizeCallGenerator {

  private OptimizeManager.Iface client;

  public OptimizeCallGenerator(String amsAddress) {
    client = OptimizeManagerClientPools.getClient(amsAddress);
  }

  public OptimizeCall generate(OptimizeCall.Action action, String tablePath) {
    return new OptimizeCall(client, action, tablePath);
  }
}
