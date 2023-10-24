package com.netease.arctic.server.catalog;

import com.netease.arctic.ams.api.CatalogMeta;

import java.util.List;

/**
 * The CatalogService interface defines the operations that can be performed on catalogs.
 */
public interface CatalogService {
  /**
   * Returns a list of CatalogMeta objects.
   *
   * @return a List of CatalogMeta objects representing the catalog metas available.
   */
  List<CatalogMeta> listCatalogMetas();

  /**
   * Gets the catalog metadata for the given catalog name.
   *
   * @return the catalog meta information
   */
  CatalogMeta getCatalogMeta(String catalogName);

  /**
   * Checks if a catalog exists.
   *
   * @param catalogName the name of the catalog to check for existence
   * @return true if the catalog exists, false otherwise
   */
  boolean catalogExist(String catalogName);

  /**
   * Retrieves the ServerCatalog with the given catalog name.
   *
   * @param catalogName the name of the ServerCatalog to retrieve
   * @return the ServerCatalog object matching the catalogName, or null if no catalog exists
   */
  ServerCatalog getServerCatalog(String catalogName);

  /**
   * Creates a catalog based on the provided catalog meta information.
   * The catalog name is obtained from the catalog meta.
   *
   * @param catalogMeta the catalog meta information used to create the catalog
   */
  void createCatalog(CatalogMeta catalogMeta);

  /**
   * Drops a catalog with the given name.
   *
   * @param catalogName the name of the catalog to be dropped
   */
  void dropCatalog(String catalogName);

  /**
   * Updates the catalog with the provided catalog meta information.
   *
   * @param catalogMeta The CatalogMeta object representing the updated catalog information.
   */
  void updateCatalog(CatalogMeta catalogMeta);
}
