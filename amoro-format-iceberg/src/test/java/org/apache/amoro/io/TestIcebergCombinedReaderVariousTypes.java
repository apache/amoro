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

package org.apache.amoro.io;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.TableTestBase;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.reader.CombinedDeleteFilter;
import org.apache.amoro.io.reader.GenericCombinedIcebergDataReader;
import org.apache.amoro.io.writer.RecordWithAction;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.RandomGenericData;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestIcebergCombinedReaderVariousTypes extends TableTestBase {

  public TestIcebergCombinedReaderVariousTypes(Schema schema) {
    super(
        new BasicCatalogTestHelper(TableFormat.ICEBERG),
        new BasicTableTestHelper(
            schema,
            PrimaryKeySpec.noPrimaryKey(),
            PartitionSpec.unpartitioned(),
            buildTableProperties()));
  }

  @Parameterized.Parameters(name = "schema = {0}")
  public static Object[] parameters() {
    Schema dateSchema = getSchema(Types.DateType.get());

    Schema timeSchema = getSchema(Types.TimeType.get());

    Schema timestampWithoutZoneSchema = getSchema(Types.TimestampType.withoutZone());

    Schema timestampWithZoneSchema = getSchema(Types.TimestampType.withZone());

    Schema decimalSchema = getSchema(Types.DecimalType.of(5, 2));

    return new Object[] {
      dateSchema, timeSchema, timestampWithoutZoneSchema, timestampWithZoneSchema, decimalSchema
    };
  }

  @NotNull
  private static Schema getSchema(Type type) {
    return new Schema(
        Lists.newArrayList(
            Types.NestedField.of(1, false, "pk1", type),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(3, true, "v1", Types.StringType.get())),
        Sets.newHashSet(1, 2));
  }

  private static Map<String, String> buildTableProperties() {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.DEFAULT_FILE_FORMAT, FileFormat.PARQUET.name());
    tableProperties.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, FileFormat.PARQUET.name());
    return tableProperties;
  }

  @Test
  public void valid() throws IOException {
    UnkeyedTable table = getMixedTable().asUnkeyedTable();
    Record record = RandomGenericData.generate(table.schema(), 1, 1).get(0);

    List<RecordWithAction> list = new ArrayList<>();
    list.add(new RecordWithAction(record, ChangeAction.DELETE));
    list.add(new RecordWithAction(record, ChangeAction.INSERT));
    write(table, list);
    write(table, list);

    List<DataFile> dataFileList = new ArrayList<>();
    List<DeleteFile> deleteFileList = new ArrayList<>();
    try (CloseableIterable<FileScanTask> tasks = table.newScan().planFiles()) {
      for (FileScanTask task : tasks) {
        dataFileList.add(task.file());
        deleteFileList.addAll(task.deletes());
      }
    }

    DataFile[] dataFiles = dataFileList.toArray(new DataFile[0]);
    DeleteFile[] deleteFiles = deleteFileList.toArray(new DeleteFile[0]);

    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, new DataFile[] {}, new DeleteFile[] {}, deleteFiles, table);

    CloseableIterable<Record> readData =
        new GenericCombinedIcebergDataReader(
                table.io(),
                table.schema(),
                table.spec(),
                table.encryption(),
                null,
                false,
                IdentityPartitionConverters::convertConstant,
                false,
                null,
                input)
            .readData();

    Assert.assertEquals(Iterables.size(readData), 1);
  }

  @Test
  public void readDataEnableFilterEqDelete() throws IOException {
    UnkeyedTable table = getMixedTable().asUnkeyedTable();
    List<Record> records = RandomGenericData.generate(table.schema(), 50, 1);
    List<Record> deleteRecords = RandomGenericData.generate(table.schema(), 200, 1);

    List<RecordWithAction> list = new ArrayList<>();
    records.forEach(r -> list.add(new RecordWithAction(r, ChangeAction.INSERT)));
    write(table, list);

    List<RecordWithAction> deletes = new ArrayList<>();
    deleteRecords.forEach(r -> deletes.add(new RecordWithAction(r, ChangeAction.DELETE)));
    write(table, deletes);

    List<DataFile> dataFileList = new ArrayList<>();
    List<DeleteFile> deleteFileList = new ArrayList<>();
    try (CloseableIterable<FileScanTask> tasks = table.newScan().planFiles()) {
      for (FileScanTask task : tasks) {
        dataFileList.add(task.file());
        deleteFileList.addAll(task.deletes());
      }
    }

    DataFile[] dataFiles = dataFileList.toArray(new DataFile[0]);
    DeleteFile[] deleteFiles = deleteFileList.toArray(new DeleteFile[0]);

    Assert.assertNotEquals(dataFiles.length, 0);
    Assert.assertNotEquals(deleteFiles.length, 0);

    RewriteFilesInput input =
        new RewriteFilesInput(
            dataFiles, new DataFile[] {}, new DeleteFile[] {}, deleteFiles, table);

    CombinedDeleteFilter.FILTER_EQ_DELETE_TRIGGER_RECORD_COUNT = 100L;
    GenericCombinedIcebergDataReader reader =
        new GenericCombinedIcebergDataReader(
            table.io(),
            table.schema(),
            table.spec(),
            table.encryption(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false,
            null,
            input);
    Assert.assertTrue(reader.getDeleteFilter().isFilterEqDelete());

    CloseableIterable<Record> readData = reader.readData();
    Assert.assertEquals(Iterables.size(readData), 0);
  }

  private static void write(UnkeyedTable table, List<RecordWithAction> list) throws IOException {
    WriteResult result = IcebergDataTestHelpers.delta(table, list);

    RowDelta rowDelta = table.newRowDelta();
    Arrays.stream(result.dataFiles()).forEach(rowDelta::addRows);
    Arrays.stream(result.deleteFiles()).forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }
}
