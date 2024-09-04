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

package org.apache.amoro.flink;

import org.apache.amoro.flink.table.MixedFormatTableLoader;
import org.apache.amoro.flink.write.MixedFormatRowDataTaskWriterFactory;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;

import java.util.Arrays;

/**
 * This class contains flink table rowType schema and others, and will replace {@link FlinkTestBase}
 * base class in the future.
 */
public interface FlinkTableTestBase {
  default TaskWriter<RowData> createTaskWriter(MixedTable mixedTable, RowType rowType) {
    return mixedTable.isKeyedTable()
        ? createKeyedTaskWriter((KeyedTable) mixedTable, rowType)
        : createUnkeyedTaskWriter((UnkeyedTable) mixedTable, rowType);
  }

  default TaskWriter<RowData> createBaseTaskWriter(MixedTable mixedTable, RowType rowType) {
    return mixedTable.isKeyedTable()
        ? createKeyedTaskWriter((KeyedTable) mixedTable, rowType, true, 3)
        : createUnkeyedTaskWriter((UnkeyedTable) mixedTable, rowType);
  }

  default TaskWriter<RowData> createKeyedTaskWriter(KeyedTable keyedTable, RowType rowType) {
    return createKeyedTaskWriter(keyedTable, rowType, false, 3);
  }

  default TaskWriter<RowData> createKeyedTaskWriter(
      KeyedTable keyedTable, RowType rowType, boolean overwrite, long mask) {
    return createTaskWriter(keyedTable, rowType, overwrite, mask);
  }

  default TaskWriter<RowData> createUnkeyedTaskWriter(UnkeyedTable unkeyedTable, RowType rowType) {
    return createTaskWriter(unkeyedTable, rowType, false, 3);
  }

  default TaskWriter<RowData> createTaskWriter(
      MixedTable mixedTable, RowType rowType, boolean overwrite, long mask) {
    MixedFormatRowDataTaskWriterFactory taskWriterFactory =
        new MixedFormatRowDataTaskWriterFactory(mixedTable, rowType, overwrite);
    taskWriterFactory.setMask(mask);
    taskWriterFactory.initialize(0, 0);
    return taskWriterFactory.create();
  }

  default void commit(MixedTable mixedTable, WriteResult result, boolean base) {
    if (mixedTable.isKeyedTable()) {
      KeyedTable keyedTable = mixedTable.asKeyedTable();
      if (base) {
        AppendFiles baseAppend = keyedTable.baseTable().newAppend();
        Arrays.stream(result.dataFiles()).forEach(baseAppend::appendFile);
        baseAppend.commit();
      } else {
        AppendFiles changeAppend = keyedTable.changeTable().newAppend();
        Arrays.stream(result.dataFiles()).forEach(changeAppend::appendFile);
        changeAppend.commit();
      }
    } else {
      if (!base) {
        throw new IllegalArgumentException(
            String.format(
                "mixed-format table %s is a unkeyed table, can't commit to change table",
                mixedTable.name()));
      }
      UnkeyedTable unkeyedTable = mixedTable.asUnkeyedTable();
      AppendFiles baseAppend = unkeyedTable.newAppend();
      Arrays.stream(result.dataFiles()).forEach(baseAppend::appendFile);
      baseAppend.commit();
    }
  }

  default MixedFormatTableLoader getTableLoader(
      String catalogName, String metastoreUrl, MixedTable mixedTable) {
    TableIdentifier identifier =
        TableIdentifier.of(
            catalogName, mixedTable.id().getDatabase(), mixedTable.id().getTableName());
    InternalCatalogBuilder internalCatalogBuilder =
        InternalCatalogBuilder.builder().metastoreUrl(metastoreUrl);
    return MixedFormatTableLoader.of(identifier, internalCatalogBuilder, mixedTable.properties());
  }
}
