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

package org.apache.amoro.server.optimizing.maintainer;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.formats.iceberg.maintainer.MixedTableMaintainer;
import org.apache.amoro.maintainer.TableMaintainer;
import org.apache.amoro.server.table.CompatibleTableRuntime;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.Table;

/** Factory for creating {@link TableMaintainer}. */
@Deprecated
public class TableMaintainers {

  /**
   * Create a {@link TableMaintainer} for the given table.
   *
   * @deprecated since 0.9.0, will be removed in 0.10.0. Use {@link
   *     TableMaintainerFactory#create(AmoroTable, TableRuntime)} instead.
   */
  public static TableMaintainer create(AmoroTable<?> amoroTable, TableRuntime tableRuntime) {
    return TableMaintainerFactory.create(amoroTable, tableRuntime);
  }

  /**
   * Create a {@link TableMaintainer} for the given table with CompatibleTableRuntime.
   *
   * @deprecated since 0.9.0, will be removed in 0.10.0. Use {@link
   *     TableMaintainerFactory#createIcebergMaintainer(Table, CompatibleTableRuntime)} instead.
   */
  public static TableMaintainer create(
      AmoroTable<?> amoroTable, CompatibleTableRuntime tableRuntime) {
    TableFormat format = amoroTable.format();
    if (format.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG)) {
      MixedTable mixedTable = (MixedTable) amoroTable.originalTable();
      return new MixedTableMaintainer(
          mixedTable, new DefaultTableMaintainerContext(tableRuntime, mixedTable));
    } else if (TableFormat.ICEBERG.equals(format)) {
      return TableMaintainerFactory.createIcebergMaintainer(
          (Table) amoroTable.originalTable(), tableRuntime);
    } else {
      throw new RuntimeException("Unsupported table type" + amoroTable.originalTable().getClass());
    }
  }
}
