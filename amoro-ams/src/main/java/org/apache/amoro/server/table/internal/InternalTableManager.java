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

package org.apache.amoro.server.table.internal;

import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.server.table.TableMetadata;

public interface InternalTableManager {

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
}
