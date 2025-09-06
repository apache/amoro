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

package org.apache.amoro.server.table;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.Configurations;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.catalog.CatalogManager;
import org.apache.amoro.server.catalog.ExternalCatalog;
import org.apache.amoro.server.catalog.InternalCatalog;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;

public class MasterSlaveTableService extends PersistentBase implements TableService {
  private final Configurations serverConfiguration;
  private final CatalogManager catalogManager;

  public MasterSlaveTableService(Configurations configuration, CatalogManager catalogManager) {
    this.serverConfiguration = configuration;
    this.catalogManager = catalogManager;
  }

  @Override
  public void initialize() {}

  @Override
  public void dispose() {}

  @Override
  public void onTableCreated(InternalCatalog catalog, ServerTableIdentifier identifier) {}

  @Override
  public void onTableDropped(InternalCatalog catalog, ServerTableIdentifier identifier) {}

  @Override
  public TableRuntime getRuntime(Long tableId) {
    return null;
  }

  @Override
  public AmoroTable<?> loadTable(ServerTableIdentifier identifier) {
    return null;
  }

  @Override
  public void exploreTableRuntimes() {}

  @Override
  public void exploreExternalCatalog(ExternalCatalog externalCatalog) {}

  @Override
  public void setRuntime(DefaultTableRuntime tableRuntime) {}

  @Override
  public void disposeTable(ServerTableIdentifier tableIdentifier) {}

  @Override
  public void addHandlerChain(RuntimeHandlerChain handler) {}

  @Override
  public void handleTableChanged(
      DefaultTableRuntime tableRuntime, OptimizingStatus originalStatus) {}

  @Override
  public void handleTableChanged(
      DefaultTableRuntime tableRuntime, TableConfiguration originalConfig) {}
}
