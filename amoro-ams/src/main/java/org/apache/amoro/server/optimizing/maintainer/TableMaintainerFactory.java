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
import org.apache.amoro.formats.iceberg.maintainer.IcebergTableMaintainer;
import org.apache.amoro.formats.iceberg.maintainer.MixedTableMaintainer;
import org.apache.amoro.maintainer.TableMaintainer;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.Table;

/** Factory for creating {@link TableMaintainer} instances. */
public class TableMaintainerFactory {

  /**
   * Create an Iceberg table maintainer with AMS context.
   *
   * @param table the Iceberg table
   * @param tableRuntime the table runtime
   * @return IcebergTableMaintainer instance
   */
  public static IcebergTableMaintainer createIcebergMaintainer(
      Table table, TableRuntime tableRuntime) {
    return new IcebergTableMaintainer(
        table,
        tableRuntime.getTableIdentifier().getIdentifier(),
        new DefaultTableMaintainerContext(tableRuntime));
  }

  /**
   * Create a {@link TableMaintainer} for the given table.
   *
   * @param amoroTable the Amoro table
   * @param tableRuntime the table runtime
   * @return TableMaintainer instance
   */
  public static TableMaintainer create(AmoroTable<?> amoroTable, TableRuntime tableRuntime) {
    TableFormat format = amoroTable.format();

    if (format.in(TableFormat.MIXED_HIVE, TableFormat.MIXED_ICEBERG)) {
      MixedTable mixedTable = (MixedTable) amoroTable.originalTable();
      return new MixedTableMaintainer(
          mixedTable, new DefaultTableMaintainerContext(tableRuntime, mixedTable));
    } else if (TableFormat.ICEBERG.equals(format)) {
      return new IcebergTableMaintainer(
          (Table) amoroTable.originalTable(),
          amoroTable.id(),
          new DefaultTableMaintainerContext(tableRuntime));
    } else {
      throw new RuntimeException("Unsupported table type" + amoroTable.originalTable().getClass());
    }
  }
}
