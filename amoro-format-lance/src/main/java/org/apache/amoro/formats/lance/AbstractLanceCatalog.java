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

package org.apache.amoro.formats.lance;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.FormatCatalog;
import org.apache.amoro.NoSuchDatabaseException;
import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.table.TableIdentifier;
import org.lance.Dataset;
import org.lance.namespace.LanceNamespace;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.TableExistsRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Base implementation of {@link FormatCatalog} for Lance. */
public abstract class AbstractLanceCatalog implements FormatCatalog {

  protected final String catalogName;
  protected final LanceNamespace namespace;

  protected AbstractLanceCatalog(String catalogName, LanceNamespace namespace) {
    this.catalogName = catalogName;
    this.namespace = namespace;
  }

  /** Composes the namespace table id used to reference a single table. */
  protected abstract List<String> tableId(String database, String table);

  /** Composes the namespace id used to list tables of a database. */
  protected abstract List<String> tableIdForListTables(String database);

  @Override
  public boolean tableExists(String database, String table) {
    if (!databaseExists(database)) {
      return false;
    }

    try {
      namespace.tableExists(new TableExistsRequest().id(tableId(database, table)));
      return true;
    } catch (TableNotFoundException e) {
      return false;
    }
  }

  @Override
  public AmoroTable<?> loadTable(String database, String tableName) {
    if (!tableExists(database, tableName)) {
      throw new NoSuchTableException("Table: " + database + "." + tableName + " does not exist");
    }

    TableIdentifier identifier = TableIdentifier.of(catalogName, database, tableName);
    Dataset dataset =
        Dataset.open().namespaceClient(namespace).tableId(tableId(database, tableName)).build();
    return new LanceTable(identifier, dataset, Collections.emptyMap());
  }

  @Override
  public boolean dropTable(String database, String table, boolean purge) {
    validateDatabase(database);

    try {
      namespace.dropTable(new DropTableRequest().id(tableId(database, table)));
      return true;
    } catch (TableNotFoundException e) {
      return false;
    }
  }

  @Override
  public List<String> listTables(String database) {
    if (!databaseExists(database)) {
      return Collections.emptyList();
    }
    ListTablesRequest request = new ListTablesRequest().id(tableIdForListTables(database));
    ListTablesResponse response = namespace.listTables(request);
    if (response == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(response.getTables());
  }

  protected void validateDatabase(String database) {
    if (!databaseExists(database)) {
      throw new NoSuchDatabaseException("Database: " + database + " does not exist");
    }
  }
}
