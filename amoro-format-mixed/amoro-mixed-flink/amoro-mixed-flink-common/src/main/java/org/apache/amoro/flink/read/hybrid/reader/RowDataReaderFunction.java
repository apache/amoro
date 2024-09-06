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

package org.apache.amoro.flink.read.hybrid.reader;

import static org.apache.amoro.flink.shuffle.RowKindUtil.convertToFlinkRowKind;
import static org.apache.amoro.utils.SchemaUtil.changeWriteSchema;
import static org.apache.amoro.utils.SchemaUtil.fillUpIdentifierFields;

import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.source.ChangeLogDataIterator;
import org.apache.amoro.flink.read.source.DataIterator;
import org.apache.amoro.flink.read.source.FileScanTaskReader;
import org.apache.amoro.flink.read.source.FlinkKeyedMORDataReader;
import org.apache.amoro.flink.read.source.FlinkUnkyedDataReader;
import org.apache.amoro.flink.read.source.MergeOnReadDataIterator;
import org.apache.amoro.flink.util.MixedFormatUtils;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.utils.NodeFilter;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.data.RowDataUtil;

import java.util.Collections;

/**
 * This Function accept a {@link MixedFormatSplit} and produces an {@link DataIterator} of {@link
 * RowData}.
 */
public class RowDataReaderFunction extends DataIteratorReaderFunction<RowData> {
  private static final long serialVersionUID = 1446614576495721883L;
  private final Schema tableSchema;
  private final Schema readSchema;
  private final String nameMapping;
  private final boolean caseSensitive;
  private final AuthenticatedFileIO io;
  private final PrimaryKeySpec primaryKeySpec;
  /** The accurate selected columns size if the mixed-format source projected */
  private final int columnSize;
  /**
   * The index of the mixed-format file offset field in the read schema Refer to {@link
   * this#wrapFileOffsetColumnMeta}
   */
  private final int fileOffsetIndex;

  private final boolean reuse;

  public RowDataReaderFunction(
      ReadableConfig config,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      AuthenticatedFileIO io) {
    this(
        config,
        tableSchema,
        projectedSchema,
        primaryKeySpec,
        nameMapping,
        caseSensitive,
        io,
        false);
  }

  public RowDataReaderFunction(
      ReadableConfig config,
      Schema tableSchema,
      Schema projectedSchema,
      PrimaryKeySpec primaryKeySpec,
      String nameMapping,
      boolean caseSensitive,
      AuthenticatedFileIO io,
      boolean reuse) {
    super(
        new ArrayPoolDataIteratorBatcher<>(
            config,
            new RowDataRecordFactory(
                FlinkSchemaUtil.convert(readSchema(tableSchema, projectedSchema)))));
    this.tableSchema = tableSchema;
    this.readSchema = fillUpReadSchema(tableSchema, projectedSchema, primaryKeySpec);
    this.primaryKeySpec = primaryKeySpec;
    this.nameMapping = nameMapping;
    this.caseSensitive = caseSensitive;
    this.io = io;
    // Add file offset column after readSchema.
    this.fileOffsetIndex = readSchema.columns().size();
    this.columnSize =
        projectedSchema == null ? readSchema.columns().size() : projectedSchema.columns().size();
    this.reuse = reuse;
  }

  @Override
  public DataIterator<RowData> createDataIterator(MixedFormatSplit split) {
    if (split.isMergeOnReadSplit()) {
      FlinkKeyedMORDataReader morDataReader =
          new FlinkKeyedMORDataReader(
              io,
              tableSchema,
              readSchema,
              primaryKeySpec,
              nameMapping,
              caseSensitive,
              RowDataUtil::convertConstant,
              reuse);
      return new MergeOnReadDataIterator(
          morDataReader, split.asMergeOnReadSplit().keyedTableScanTask(), io);
    } else if (split.isSnapshotSplit()) {
      FileScanTaskReader<RowData> rowDataReader =
          new FlinkUnkyedDataReader(
              io,
              tableSchema,
              readSchema,
              primaryKeySpec,
              nameMapping,
              caseSensitive,
              RowDataUtil::convertConstant,
              Collections.singleton(split.dataTreeNode()),
              reuse);
      return new DataIterator<>(
          rowDataReader,
          split.asSnapshotSplit().insertTasks(),
          rowData -> Long.MIN_VALUE,
          this::removeMixedFormatMetaColumn);
    } else if (split.isChangelogSplit()) {
      FileScanTaskReader<RowData> rowDataReader =
          new FlinkUnkyedDataReader(
              io,
              wrapFileOffsetColumnMeta(tableSchema),
              wrapFileOffsetColumnMeta(readSchema),
              primaryKeySpec,
              nameMapping,
              caseSensitive,
              RowDataUtil::convertConstant,
              Collections.singleton(split.dataTreeNode()),
              reuse);
      return new ChangeLogDataIterator<>(
          rowDataReader,
          split.asChangelogSplit().insertTasks(),
          split.asChangelogSplit().deleteTasks(),
          this::mixedFormatFileOffset,
          this::removeMixedFormatMetaColumn,
          this::transformRowKind);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "As of now this split %s is not supported.", split.getClass().getSimpleName()));
    }
  }

  private Schema wrapFileOffsetColumnMeta(Schema schema) {
    return changeWriteSchema(schema);
  }

  long mixedFormatFileOffset(RowData rowData) {
    return rowData.getLong(fileOffsetIndex);
  }

  /**
   * @param rowData It may have more columns than readSchema. Refer to {@link
   *     FlinkUnkyedDataReader}'s annotation.
   */
  RowData removeMixedFormatMetaColumn(RowData rowData) {
    return MixedFormatUtils.removeMixedFormatMetaColumn(rowData, columnSize);
  }

  RowData transformRowKind(ChangeLogDataIterator.ChangeActionTrans<RowData> trans) {
    RowData rowData = trans.row();
    rowData.setRowKind(convertToFlinkRowKind(trans.changeAction()));
    return rowData;
  }

  /**
   * If the projected schema is not null, this method will check and fill up the identifierFields of
   * the tableSchema and the projected schema.
   *
   * <p>projectedSchema may not include the primary keys, but the {@link NodeFilter} must filter the
   * record with the value of the primary keys. So the mixed-format reader function schema must
   * include the primary keys.
   *
   * @param tableSchema table schema
   * @param projectedSchema projected schema
   * @return a new Schema on which includes the identifier fields.
   */
  private static Schema fillUpReadSchema(
      Schema tableSchema, Schema projectedSchema, PrimaryKeySpec primaryKeySpec) {
    Preconditions.checkNotNull(tableSchema, "Table schema can't be null");
    return projectedSchema == null
        ? tableSchema
        : fillUpIdentifierFields(tableSchema, projectedSchema, primaryKeySpec);
  }

  private static Schema readSchema(Schema tableSchema, Schema projectedSchema) {
    Preconditions.checkNotNull(tableSchema, "Table schema can't be null");
    return projectedSchema == null ? tableSchema : projectedSchema;
  }
}
