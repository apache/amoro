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

package org.apache.amoro.flink.write;

import static org.apache.amoro.BasicTableTestHelper.PRIMARY_KEY_SPEC;

import org.apache.amoro.flink.FlinkTableTestBase;
import org.apache.amoro.flink.read.FlinkSplitPlanner;
import org.apache.amoro.flink.read.hybrid.reader.RowDataReaderFunction;
import org.apache.amoro.flink.read.hybrid.split.MixedFormatSplit;
import org.apache.amoro.flink.read.source.DataIterator;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.iceberg.Schema;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.flink.TestHelpers;
import org.apache.iceberg.flink.source.FlinkInputFormat;
import org.apache.iceberg.flink.source.FlinkSource;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.TypeUtil;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface FlinkTaskWriterBaseTest extends FlinkTableTestBase {
  Logger LOG = LoggerFactory.getLogger(FlinkTaskWriterBaseTest.class);

  default void testWriteAndReadMixedFormatTable(
      MixedTable mixedTable, TableSchema flinkTableSchema, RowData expected) {

    // This is a partial-write schema from Flink engine view.
    RowType rowType = (RowType) flinkTableSchema.toRowDataType().getLogicalType();

    try (TaskWriter<RowData> taskWriter = createTaskWriter(mixedTable, rowType)) {
      Assert.assertNotNull(taskWriter);

      writeAndCommit(expected, taskWriter, mixedTable);

      mixedTable.refresh();

      // This is a partial-read schema from Flink engine view, should reassign schema id to
      // selected-schema
      Schema selectedSchema =
          TypeUtil.reassignIds(FlinkSchemaUtil.convert(flinkTableSchema), mixedTable.schema());

      assertRecords(mixedTable.schema(), selectedSchema, mixedTable, expected, flinkTableSchema);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  default void assertRecords(
      Schema tableSchema,
      Schema selectedSchema,
      MixedTable mixedTable,
      RowData expected,
      TableSchema flinkTableSchema)
      throws IOException {
    List<RowData> records;
    if (mixedTable.isKeyedTable()) {
      records =
          recordsOfKeyedTable(
              mixedTable.asKeyedTable(), tableSchema, selectedSchema, mixedTable.io());
    } else {
      records =
          recordsOfUnkeyedTable(
              getTableLoader(getCatalogName(), getMetastoreUrl(), mixedTable),
              selectedSchema,
              flinkTableSchema);
    }
    Assert.assertEquals(1, records.size());
    Assert.assertEquals(expected, records.get(0));
  }

  /** For asserting unkeyed table records. */
  String getMetastoreUrl();

  /** For asserting unkeyed table records. */
  String getCatalogName();

  default void writeAndCommit(
      RowData expected, TaskWriter<RowData> taskWriter, MixedTable mixedTable) throws IOException {
    writeAndCommit(expected, taskWriter, mixedTable, false);
  }

  default void writeAndCommit(
      RowData expected,
      TaskWriter<RowData> taskWriter,
      MixedTable mixedTable,
      boolean upsertEnabled)
      throws IOException {
    taskWriter.write(expected);
    WriteResult writerResult = taskWriter.complete();
    boolean writeToBase = mixedTable.isUnkeyedTable();
    commit(mixedTable, writerResult, writeToBase);
    Assert.assertEquals(upsertEnabled ? 2 : 1, writerResult.dataFiles().length);
  }

  default boolean upsertEnabled() {
    return false;
  }

  default List<RowData> recordsOfUnkeyedTable(
      TableLoader tableLoader, Schema projectedSchema, TableSchema flinkTableSchema)
      throws IOException {
    FlinkInputFormat inputFormat =
        FlinkSource.forRowData().tableLoader(tableLoader).project(flinkTableSchema).buildFormat();
    return runFormat(inputFormat, FlinkSchemaUtil.convert(projectedSchema));
  }

  default List<RowData> recordsOfKeyedTable(
      KeyedTable table, Schema tableSchema, Schema projectedSchema, AuthenticatedFileIO io) {
    List<MixedFormatSplit> mixedFormatSplits =
        FlinkSplitPlanner.planFullTable(table, new AtomicInteger(0));

    RowDataReaderFunction rowDataReaderFunction =
        new RowDataReaderFunction(
            new Configuration(), tableSchema, projectedSchema, PRIMARY_KEY_SPEC, null, true, io);

    List<RowData> actual = new ArrayList<>();
    mixedFormatSplits.forEach(
        split -> {
          LOG.info("Mixed-format split: {}.", split);
          DataIterator<RowData> dataIterator = rowDataReaderFunction.createDataIterator(split);
          while (dataIterator.hasNext()) {
            RowData rowData = dataIterator.next();
            LOG.info("{}", rowData);
            actual.add(rowData);
          }
        });

    return actual;
  }

  default List<RowData> runFormat(FlinkInputFormat inputFormat, RowType readRowType)
      throws IOException {
    return TestHelpers.readRowData(inputFormat, readRowType);
  }
}
