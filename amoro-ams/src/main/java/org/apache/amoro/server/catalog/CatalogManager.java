/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.catalog;

import org.apache.amoro.api.CatalogMeta;

import java.util.List;

/** The CatalogManager interface defines the operations that can be performed on catalogs. */
public interface CatalogManager {
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
   * Retrieves the InternalCatalog with the given catalog name.
   *
   * @param catalogName the name of the ServerCatalog to retrieve
   * @return the InternalCatalog object matching the catalogName, or null if no catalog exists
   */
  InternalCatalog getInternalCatalog(String catalogName);

  /**
   * Retrieves all ExternalCatalogs.
   *
   * @return a list of ExternalCatalogs
   */
  List<ExternalCatalog> getExternalCatalogs();

  /**
   * Creates a catalog based on the provided catalog meta information. The catalog name is obtained
   * from the catalog meta.
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
