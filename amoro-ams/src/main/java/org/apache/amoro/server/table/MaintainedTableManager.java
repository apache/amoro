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
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;

import java.util.List;

public interface MaintainedTableManager {

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

  /** Get the table runtime meta. */
  TableRuntimeMeta getTableRuntimeMata(ServerTableIdentifier id);

  /**
   * Get the table info from database for given parameters.
   *
   * @param optimizerGroup The optimizer group of the table associated to. will be if we want the
   *     info for all groups.
   * @param fuzzyDbName the fuzzy db name used to filter the result, will be null if no filter set.
   * @param fuzzyTableName the fuzzy table name used to filter the result, will be null if no filter
   *     set.
   * @param statusCodeFilters the status code used to filter the result, wil be null if no filter
   *     set.
   * @param limit How many entries we want to retrieve.
   * @param offset The entries we'll skip when retrieving the entries.
   * @return A pair with the first entry is the actual list under the filters with the offset and
   *     limit, and second value will be the number of total entries under the filters.
   */
  Pair<List<TableOptimizingInfo>, Integer> queryTableOptimizingInfo(
      String optimizerGroup,
      @Nullable String fuzzyDbName,
      @Nullable String fuzzyTableName,
      @Nullable List<Integer> statusCodeFilters,
      int limit,
      int offset);
}
