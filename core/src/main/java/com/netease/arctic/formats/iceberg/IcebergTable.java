/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.formats.iceberg;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableSnapshot;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.ArcticFileIOs;
import com.netease.arctic.table.BasicUnkeyedTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.CatalogUtil;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;

import java.util.Map;

public class IcebergTable implements AmoroTable<UnkeyedTable> {

  /**
   * build an amoro table implement which table format is iceberg.
   *
   * @param identifier table identifier.
   * @param icebergTable iceberg table
   * @param metaStore auth context
   * @param catalogProperties catalog properties.
   * @return amoro table.
   */
  public static IcebergTable newIcebergTable(
      TableIdentifier identifier,
      Table icebergTable,
      TableMetaStore metaStore,
      Map<String, String> catalogProperties) {
    ArcticFileIO io = ArcticFileIOs.buildAdaptIcebergFileIO(metaStore, icebergTable.io());

    UnkeyedTable wrapped =
        new BasicUnkeyedTable(
            identifier,
            CatalogUtil.useArcticTableOperations(
                icebergTable, icebergTable.location(), io, metaStore.getConfiguration()),
            io,
            catalogProperties) {
          @Override
          public TableFormat format() {
            return TableFormat.ICEBERG;
          }
        };
    return new IcebergTable(identifier, wrapped);
  }

  private final TableIdentifier identifier;
  private final UnkeyedTable table;

  protected IcebergTable(TableIdentifier identifier, UnkeyedTable table) {
    this.table = table;
    this.identifier = identifier;
  }

  @Override
  public TableIdentifier id() {
    return this.identifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.ICEBERG;
  }

  @Override
  public Map<String, String> properties() {
    return table.properties();
  }

  @Override
  public UnkeyedTable originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Snapshot snapshot = table.currentSnapshot();
    return new IcebergSnapshot(snapshot);
  }
}
