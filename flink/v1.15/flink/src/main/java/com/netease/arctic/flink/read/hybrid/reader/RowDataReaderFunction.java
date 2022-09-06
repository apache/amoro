/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.netease.arctic.flink.read.hybrid.reader;

import com.netease.arctic.flink.read.hybrid.split.ArcticSplit;
import com.netease.arctic.flink.read.source.ChangeLogDataIterator;
import com.netease.arctic.flink.read.source.DataIterator;
import com.netease.arctic.flink.read.source.FileScanTaskReader;
import com.netease.arctic.flink.read.source.FlinkArcticDataReader;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.RowDataUtil;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.Collections;

import static com.netease.arctic.flink.shuffle.RowKindUtil.convertToFlinkRowKind;
import static com.netease.arctic.utils.SchemaUtil.changeWriteSchema;

/**
 * This Function accept a {@link ArcticSplit} and produces an {@link DataIterator} of {@link RowData}.
 */
public class RowDataReaderFunction extends DataIteratorReaderFunction<RowData> {
  private static final long serialVersionUID = 1446614576495721883L;
  private final Schema tableSchema;
  private final Schema readSchema;
  private final String nameMapping;
  private final boolean caseSensitive;
  private final ArcticFileIO io;
  private final PrimaryKeySpec primaryKeySpec;
  private final int fileOffsetIndex;

  public RowDataReaderFunction(
      ReadableConfig config, Schema tableSchema, Schema projectedSchema, PrimaryKeySpec primaryKeySpec,
      String nameMapping, boolean caseSensitive, ArcticFileIO io) {
    super(new ArrayPoolDataIteratorBatcher<>(config, new RowDataRecordFactory(
        FlinkSchemaUtil.convert(readSchema(tableSchema, projectedSchema)))));
    this.tableSchema = tableSchema;
    this.readSchema = readSchema(tableSchema, projectedSchema);
    this.primaryKeySpec = primaryKeySpec;
    this.nameMapping = nameMapping;
    this.caseSensitive = caseSensitive;
    this.io = io;
    // Add file offset column after readSchema. Refer to this#wrapArcticFileOffsetColumnMeta
    this.fileOffsetIndex = readSchema.columns().size();
  }

  @Override
  public DataIterator<RowData> createDataIterator(ArcticSplit split) {
    if (split.isSnapshotSplit()) {

      FileScanTaskReader<RowData> rowDataReader =
          new FlinkArcticDataReader(
              io, tableSchema, readSchema, primaryKeySpec, nameMapping, caseSensitive, RowDataUtil::convertConstant,
              Collections.singleton(split.dataTreeNode()), false);
      return new DataIterator<>(
          rowDataReader,
          split.asSnapshotSplit().insertTasks(),
          rowData -> Long.MIN_VALUE);
    } else if (split.isChangelogSplit()) {
      FileScanTaskReader<RowData> rowDataReader =
          new FlinkArcticDataReader(
              io, wrapArcticFileOffsetColumnMeta(tableSchema), wrapArcticFileOffsetColumnMeta(readSchema),
              primaryKeySpec, nameMapping, caseSensitive, RowDataUtil::convertConstant,
              Collections.singleton(split.dataTreeNode()), false);
      return new ChangeLogDataIterator<>(
          rowDataReader,
          split.asChangelogSplit().insertTasks(),
          split.asChangelogSplit().deleteTasks(),
          this::arcticFileOffset,
          this::removeArcticMetaColumn,
          this::transformRowKind);
    } else {
      throw new IllegalArgumentException(
          String.format("As of now this split %s is not supported.", split.getClass().getSimpleName()));
    }
  }

  private Schema wrapArcticFileOffsetColumnMeta(Schema schema) {
    return changeWriteSchema(schema);
  }

  long arcticFileOffset(RowData rowData) {
    return rowData.getLong(fileOffsetIndex);
  }

  RowData removeArcticMetaColumn(RowData rowData) {
    GenericRowData newRowData = new GenericRowData(rowData.getRowKind(), rowData.getArity() - 1);
    if (rowData instanceof GenericRowData) {
      GenericRowData before = (GenericRowData) rowData;
      for (int i = 0; i < newRowData.getArity(); i++) {
        newRowData.setField(i, before.getField(i));
      }
      return newRowData;
    }
    throw new UnsupportedOperationException(
        String.format(
            "Can't remove arctic meta column from this RowData %s",
            rowData.getClass().getSimpleName()));
  }

  RowData transformRowKind(ChangeLogDataIterator.ChangeActionTrans<RowData> trans) {
    RowData rowData = trans.row();
    rowData.setRowKind(convertToFlinkRowKind(trans.changeAction()));
    return rowData;
  }

  private static Schema readSchema(Schema tableSchema, Schema projectedSchema) {
    Preconditions.checkNotNull(tableSchema, "Table schema can't be null");
    return projectedSchema == null ? tableSchema : projectedSchema;
  }
}
