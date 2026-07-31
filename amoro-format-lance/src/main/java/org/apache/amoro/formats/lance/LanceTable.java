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
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.lance.Dataset;

import java.util.Collections;
import java.util.Map;

/** AmoroTable wrapper for a Lance {@link Dataset}. */
public class LanceTable implements AmoroTable<Dataset> {

  private final TableIdentifier identifier;
  private final Dataset dataset;
  private final Map<String, String> tableProperties;

  public LanceTable(
      TableIdentifier identifier, Dataset dataset, Map<String, String> tableProperties) {
    this.identifier = identifier;
    this.dataset = dataset;
    this.tableProperties =
        tableProperties == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(tableProperties);
  }

  @Override
  public TableIdentifier id() {
    return identifier;
  }

  @Override
  public TableFormat format() {
    return LanceCatalogFactory.LANCE;
  }

  @Override
  public Map<String, String> properties() {
    return tableProperties;
  }

  @Override
  public Dataset originalTable() {
    return dataset;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    throw new IllegalStateException("The method is not implemented.");
  }
}
