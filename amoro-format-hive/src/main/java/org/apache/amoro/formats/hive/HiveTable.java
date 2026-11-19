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

package org.apache.amoro.formats.hive;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.api.Table;

import java.util.Collections;
import java.util.Map;

/** A native Hive table backed by its Hive Metastore metadata. */
public class HiveTable implements AmoroTable<Table> {

  private final TableIdentifier tableIdentifier;
  private final Table table;

  public HiveTable(TableIdentifier tableIdentifier, Table table) {
    this.tableIdentifier = tableIdentifier;
    this.table = table;
  }

  @Override
  public TableIdentifier id() {
    return tableIdentifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.HIVE;
  }

  @Override
  public Map<String, String> properties() {
    Map<String, String> parameters = table.getParameters();
    return parameters == null ? Collections.emptyMap() : Collections.unmodifiableMap(parameters);
  }

  @Override
  public Table originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    throw new IllegalStateException("The method is not implemented.");
  }
}
