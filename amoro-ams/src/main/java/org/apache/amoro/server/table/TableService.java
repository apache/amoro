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

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.server.catalog.CatalogService;
import org.apache.amoro.server.persistence.TableRuntimeMeta;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

public interface TableService extends CatalogService, TableManager {

  void initialize();

  /**
   * create table metadata
   *
   * @param catalogName internal catalog to create the table
   * @param tableMeta table metadata info
   */
  void createTable(String catalogName, TableMetadata tableMeta);

  /**
   * delete the table metadata
   *
   * @param tableIdentifier table id
   * @param deleteData if delete the external table
   */
  void dropTableMetadata(TableIdentifier tableIdentifier, boolean deleteData);

  /**
   * Load all managed tables. Managed tables means the tables which are managed by AMS, AMS will
   * watch their change and make them health.
   *
   * @return {@link ServerTableIdentifier} list
   */
  List<ServerTableIdentifier> listManagedTables();

  /**
   * Get the ServerTableIdentifier instance of the specified table identifier
   *
   * @return the {@link ServerTableIdentifier} instance
   */
  ServerTableIdentifier getServerTableIdentifier(TableIdentifier id);

  /**
   * blocker operations
   *
   * @return the created blocker
   */
  Blocker block(
      TableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties);

  /** release the blocker */
  void releaseBlocker(TableIdentifier tableIdentifier, String blockerId);

  /**
   * renew the blocker
   *
   * @return expiration time
   */
  long renewBlocker(TableIdentifier tableIdentifier, String blockerId);

  /**
   * get blockers of table
   *
   * @return block list
   */
  List<Blocker> getBlockers(TableIdentifier tableIdentifier);

  /**
   * Get the table info from database for given parameters.
   *
   * @param optimizerGroup The optimizer group of the table associated to. will be if we want the
   *     info for all groups.
   * @param fuzzyDbName the fuzzy db name used to filter the result, will be null if no filter set.
   * @param fuzzyTableName the fuzzy table name used to filter the result, will be null if no filter
   *     set.
   * @param limit How many entries we want to retrieve.
   * @param offset The entries we'll skip when retrieving the entries.
   */
  List<TableRuntimeMeta> getTableRuntimes(
      String optimizerGroup,
      @Nullable String fuzzyDbName,
      @Nullable String fuzzyTableName,
      int limit,
      int offset);
}
