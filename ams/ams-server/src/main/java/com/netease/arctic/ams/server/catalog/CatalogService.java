package com.netease.arctic.ams.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;

import java.util.List;

public interface CatalogService {
  List<CatalogMeta> listCatalogMetas();

  CatalogMeta getCatalogMeta(String catalogName);

  boolean catalogExist(String catalogName);

  void createCatalog(CatalogMeta catalogMeta);

  void dropCatalog(String catalogName);

  void updateCatalog(CatalogMeta catalogMeta);
}
