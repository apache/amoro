/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.optimizing;

import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestBase;
import com.netease.arctic.common.IcebergWrite;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.writer.RecordWithAction;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.map.StructLikeCollections;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IcebergGenerics;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.Iterables;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IcebergRewriteExecutorDataTypeTest extends CatalogTestBase {

  public IcebergRewriteExecutorDataTypeTest() {
    super(new BasicCatalogTestHelper(TableFormat.ICEBERG));
  }

  private static Map<String, String> buildTableProperties() {
    Map<String, String> tableProperties = Maps.newHashMapWithExpectedSize(3);
    tableProperties.put(TableProperties.FORMAT_VERSION, "2");
    tableProperties.put(TableProperties.DEFAULT_FILE_FORMAT, FileFormat.PARQUET.name());
    tableProperties.put(TableProperties.DELETE_DEFAULT_FILE_FORMAT, FileFormat.PARQUET.name());
    return tableProperties;
  }

  @Test
  public void validDateType() throws IOException {
    Schema schema = new Schema(
        com.google.common.collect.Lists.newArrayList(
            Types.NestedField.of(1, true, "pk1", Types.DateType.get()),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(4, true, "v1", Types.StringType.get())
        ));
    Schema primary = schema.select("pk1", "pk2");

    Record record = GenericRecord.create(schema);
    record = record.copy("pk1", LocalDate.now(), "pk2", "pk2", "v1", "v1");

    valid(schema, primary, record);
  }

  @Test
  public void validTimeType() throws IOException {
    Schema schema = new Schema(
        com.google.common.collect.Lists.newArrayList(
            Types.NestedField.of(1, true, "pk1", Types.TimeType.get()),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(4, true, "v1", Types.StringType.get())
        ));
    Schema primary = schema.select("pk1", "pk2");

    Record record = GenericRecord.create(schema);
    record = record.copy("pk1", LocalTime.now(), "pk2", "pk2", "v1", "v1");

    valid(schema, primary, record);
  }

  @Test
  public void validTimestampWithZoneType() throws IOException {
    Schema schema = new Schema(
        com.google.common.collect.Lists.newArrayList(
            Types.NestedField.of(1, true, "pk1", Types.TimestampType.withZone()),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(4, true, "v1", Types.StringType.get())
        ));
    Schema primary = schema.select("pk1", "pk2");

    Record record = GenericRecord.create(schema);
    record = record.copy("pk1", OffsetDateTime.now(), "pk2", "pk2", "v1", "v1");

    valid(schema, primary, record);
  }

  @Test
  public void validDecimalType() throws IOException {
    Schema schema = new Schema(
        com.google.common.collect.Lists.newArrayList(
            Types.NestedField.of(1, true, "pk1", Types.DecimalType.of(5, 2)),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(4, true, "v1", Types.StringType.get())
        ));
    Schema primary = schema.select("pk1", "pk2");

    Record record = GenericRecord.create(schema);
    record = record.copy("pk1", new BigDecimal("234.12"), "pk2", "pk2", "v1", "v1");

    valid(schema, primary, record);
  }

  @Test
  public void validTimestampWithoutZoneType() throws IOException {
    Schema schema = new Schema(
        com.google.common.collect.Lists.newArrayList(
            Types.NestedField.of(1, true, "pk1", Types.TimestampType.withoutZone()),
            Types.NestedField.of(2, false, "pk2", Types.StringType.get()),
            Types.NestedField.of(4, true, "v1", Types.StringType.get())
        ));
    Schema primary = schema.select("pk1", "pk2");

    Record record = GenericRecord.create(schema);
    record = record.copy("pk1", LocalDateTime.now(), "pk2", "pk2", "v1", "v1");

    valid(schema, primary, record);
  }

  private void valid(Schema schema, Schema primary, Record record) throws IOException {
    UnkeyedTable table = createTable(UUID.randomUUID().toString(),
        schema, PartitionSpec.unpartitioned(), buildTableProperties()).asUnkeyedTable();

    List<RecordWithAction> list = new ArrayList<>();
    list.add(new RecordWithAction(record, ChangeAction.DELETE));
    list.add(new RecordWithAction(record, ChangeAction.INSERT));
    write(primary, table, list);
    write(primary, table, list);

    long snapshotId = table.currentSnapshot().snapshotId();

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

    RewriteFilesInput input = new RewriteFilesInput(
        dataFiles,
        new DataFile[] {},
        new DeleteFile[] {},
        deleteFiles,
        table);

    IcebergRewriteExecutor executor = new IcebergRewriteExecutor(
        input,
        table,
        StructLikeCollections.DEFAULT
    );

    RewriteFilesOutput output = executor.execute();

    RewriteFiles rewriteFiles = table.newRewrite();
    rewriteFiles.rewriteFiles(
        Sets.newHashSet(dataFiles),
        Sets.newHashSet(deleteFiles),
        Sets.newHashSet(output.getDataFiles()),
        Sets.newHashSet(output.getDeleteFiles())
    ).validateFromSnapshot(snapshotId).commit();

    CloseableIterable<Record> records = IcebergGenerics.read(table).build();
    for (Record r : records) {
      System.out.println(r);
    }
    Assert.assertEquals(Iterables.size(records), 1);
  }

  private static void write(Schema primary, UnkeyedTable table, List<RecordWithAction> list)
      throws IOException {
    IcebergWrite write = new IcebergWrite(primary, table, 1024);
    WriteResult result = write.write(list);

    RowDelta rowDelta = table.newRowDelta();
    Arrays.stream(result.dataFiles()).forEach(rowDelta::addRows);
    Arrays.stream(result.deleteFiles()).forEach(rowDelta::addDeletes);
    rowDelta.commit();
  }

  private ArcticTable createTable(
      String tableName,
      Schema schema,
      PartitionSpec partitionSpec,
      Map<String, String> properties) {
    getIcebergCatalog().createTable(
        org.apache.iceberg.catalog.TableIdentifier.of(TableTestHelper.TEST_DB_NAME, tableName),
        schema,
        partitionSpec,
        properties);
    return getCatalog().loadTable(TableIdentifier.of(
        TableTestHelper.TEST_CATALOG_NAME,
        TableTestHelper.TEST_DB_NAME,
        tableName));
  }
}
