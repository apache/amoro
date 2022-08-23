package com.netease.arctic.ams.server.model;

public enum UpgradeStatus {
  FAILED("Failed"),
  UPGRADING("Upgrading"),
  SUCCESS("Success"),
  NONE("None");

  private String name;

  UpgradeStatus(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
