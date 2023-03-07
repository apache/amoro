package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.catalog.CatalogManager;

public class UseCallGenerator {
  private CatalogManager catalogManager;

  public UseCallGenerator(String amsAddress) {
    this.catalogManager = new CatalogManager(amsAddress);
  }

  public UseCall generate(String namespace) {
    return new UseCall(namespace, this.catalogManager);
  }
}
