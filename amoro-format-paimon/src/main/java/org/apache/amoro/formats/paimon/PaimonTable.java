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

package org.apache.amoro.formats.paimon;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.paimon.Snapshot;
import org.apache.paimon.table.DataTable;
import org.apache.paimon.table.Table;

import java.util.Map;
import java.util.function.Supplier;

public class PaimonTable implements AmoroTable<Table> {

  private final TableIdentifier tableIdentifier;

  private final Table table;
  private final Supplier<PaimonTable> refresh;

  public PaimonTable(TableIdentifier tableIdentifier, Table table) {
    this(tableIdentifier, table, null);
  }

  PaimonTable(TableIdentifier tableIdentifier, Table table, Supplier<PaimonTable> refresh) {
    this.tableIdentifier = tableIdentifier;
    this.table = table;
    this.refresh = refresh;
  }

  /** Reload this table through its owning catalog, invalidating any cached metadata. */
  public PaimonTable refresh() {
    if (refresh == null) {
      throw new IllegalStateException("Paimon table has no catalog refresh callback");
    }
    return refresh.get();
  }

  @Override
  public TableIdentifier id() {
    return tableIdentifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }

  @Override
  public Map<String, String> properties() {
    return table.options();
  }

  @Override
  public Table originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    if (!(table instanceof DataTable)) {
      return null;
    }

    Snapshot snapshot = ((DataTable) table).snapshotManager().latestSnapshot();
    return snapshot == null ? null : new PaimonSnapshot(snapshot);
  }
}
