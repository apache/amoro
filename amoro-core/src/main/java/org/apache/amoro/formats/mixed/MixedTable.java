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

package org.apache.amoro.formats.mixed;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.Snapshot;

import java.util.Map;

public class MixedTable implements AmoroTable<org.apache.amoro.table.MixedTable> {

  private final org.apache.amoro.table.MixedTable mixedTable;
  private final TableFormat format;

  public MixedTable(org.apache.amoro.table.MixedTable mixedTable, TableFormat format) {
    this.mixedTable = mixedTable;
    this.format = format;
  }

  @Override
  public TableIdentifier id() {
    return mixedTable.id();
  }

  @Override
  public TableFormat format() {
    return format;
  }

  @Override
  public Map<String, String> properties() {
    return mixedTable.properties();
  }

  @Override
  public org.apache.amoro.table.MixedTable originalTable() {
    return mixedTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Snapshot changeSnapshot;
    Snapshot baseSnapshot;
    if (mixedTable.isKeyedTable()) {
      changeSnapshot = mixedTable.asKeyedTable().changeTable().currentSnapshot();
      baseSnapshot = mixedTable.asKeyedTable().baseTable().currentSnapshot();
    } else {
      changeSnapshot = null;
      baseSnapshot = mixedTable.asUnkeyedTable().currentSnapshot();
    }

    if (changeSnapshot == null && baseSnapshot == null) {
      return null;
    }

    return new MixedSnapshot(changeSnapshot, baseSnapshot);
  }
}
