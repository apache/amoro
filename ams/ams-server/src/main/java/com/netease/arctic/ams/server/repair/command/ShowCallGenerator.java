package com.netease.arctic.ams.server.repair.command;

public class ShowCallGenerator {

  private String amsAddress;

  public ShowCallGenerator(String amsAddress) {
    this.amsAddress = amsAddress;
  }

  public ShowCall generate(ShowCall.Namespaces namespaces) {
    return new ShowCall(amsAddress, namespaces);
  }
}
