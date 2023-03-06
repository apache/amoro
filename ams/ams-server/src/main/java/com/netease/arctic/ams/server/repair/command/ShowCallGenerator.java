package com.netease.arctic.ams.server.repair.command;

import com.netease.arctic.catalog.CatalogManager;

public class ShowCallGenerator {

  private CatalogManager catalogManager;

  public ShowCallGenerator(String amsAddress) {
    this.catalogManager = new CatalogManager(amsAddress);
  }

  public ShowCall generate(ShowCall.Namespaces namespaces) {
    return new ShowCall(catalogManager, namespaces);
  }
}
