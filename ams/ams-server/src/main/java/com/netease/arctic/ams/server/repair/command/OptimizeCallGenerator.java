package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.ams.api.client.OptimizeManagerClient;

public class OptimizeCallGenerator {
  private OptimizeManagerClient client;

  public OptimizeCallGenerator(String amsAddress) {
    client = new OptimizeManagerClient(amsAddress);
  }

  public OptimizeCall generate(OptimizeCall.Action action, String tablePath) {
    return new OptimizeCall(client, action, tablePath);
  }
}
