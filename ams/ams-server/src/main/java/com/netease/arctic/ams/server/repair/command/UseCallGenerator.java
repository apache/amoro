package com.netease.arctic.ams.server.repair.command;

public class UseCallGenerator {
  private String amsAddress;

  public UseCallGenerator(String amsAddress) {
    this.amsAddress = amsAddress;
  }

  public UseCall generate(String namespace) {
    return new UseCall(namespace, this.amsAddress);
  }
}
