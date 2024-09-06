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

package org.apache.amoro.trino.mixed;

import static org.apache.amoro.MockAmoroManagementServer.TEST_CATALOG_NAME;
import static org.apache.amoro.MockAmoroManagementServer.TEST_DB_NAME;

import io.trino.testing.AbstractTestQueryFramework;
import io.trino.testng.services.ManageTestResources;
import org.apache.amoro.MockAmoroManagementServer;
import org.apache.amoro.api.CatalogMeta;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.io.reader.GenericKeyedDataReader;
import org.apache.amoro.io.writer.GenericBaseTaskWriter;
import org.apache.amoro.io.writer.GenericChangeTaskWriter;
import org.apache.amoro.io.writer.GenericTaskWriters;
import org.apache.amoro.mixed.CatalogLoader;
import org.apache.amoro.mixed.MixedFormatCatalog;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.types.Types;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class TableTestBaseForTrino extends AbstractTestQueryFramework {

  protected static TemporaryFolder tmp = new TemporaryFolder();
  protected static File warehouse;

  @ManageTestResources.Suppress(because = "no need")
  protected static MockAmoroManagementServer AMS;

  protected static final TableIdentifier TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_table");
  protected static final TableIdentifier PK_TABLE_ID =
      TableIdentifier.of(TEST_CATALOG_NAME, TEST_DB_NAME, "test_pk_table");
  protected static final Schema TABLE_SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "name$name", Types.StringType.get()),
          Types.NestedField.required(3, "op_time", Types.TimestampType.withoutZone()));
  protected static final Record RECORD = GenericRecord.create(TABLE_SCHEMA);
  protected static final Schema POS_DELETE_SCHEMA =
      new Schema(MetadataColumns.DELETE_FILE_PATH, MetadataColumns.DELETE_FILE_POS);
  protected static final PartitionSpec SPEC =
      PartitionSpec.builderFor(TABLE_SCHEMA).day("op_time").build();
  protected static final PrimaryKeySpec PRIMARY_KEY_SPEC =
      PrimaryKeySpec.builderFor(TABLE_SCHEMA).addColumn("id").build();
  protected static final DataFile FILE_A =
      DataFiles.builder(SPEC)
          .withPath("/path/to/data-a.parquet")
          .withFileSizeInBytes(0)
          .withPartitionPath("op_time_day=2022-01-01") // easy way to set partition data for now
          .withRecordCount(2) // needs at least one record or else metrics will filter it out
          .build();
  protected static final DataFile FILE_B =
      DataFiles.builder(SPEC)
          .withPath("/path/to/data-b.parquet")
          .withFileSizeInBytes(0)
          .withPartitionPath("op_time_day=2022-01-02") // easy way to set partition data for now
          .withRecordCount(2) // needs at least one record or else metrics will filter it out
          .build();
  protected static final DataFile FILE_C =
      DataFiles.builder(SPEC)
          .withPath("/path/to/data-b.parquet")
          .withFileSizeInBytes(0)
          .withPartitionPath("op_time_day=2022-01-03") // easy way to set partition data for now
          .withRecordCount(2) // needs at least one record or else metrics will filter it out
          .build();

  protected MixedFormatCatalog testCatalog;
  protected UnkeyedTable testTable;
  protected KeyedTable testKeyedTable;

  protected void setupTables() throws Exception {
    testCatalog = CatalogLoader.load(AMS.getUrl(CatalogTestHelper.TEST_CATALOG_NAME));

    File tableDir = tmp.newFolder();
    testTable =
        testCatalog
            .newTableBuilder(TABLE_ID, TABLE_SCHEMA)
            .withProperty(TableProperties.LOCATION, tableDir.getPath() + "/table")
            .withPartitionSpec(SPEC)
            .create()
            .asUnkeyedTable();

    testKeyedTable =
        testCatalog
            .newTableBuilder(PK_TABLE_ID, TABLE_SCHEMA)
            .withProperty(TableProperties.LOCATION, tableDir.getPath() + "/pk_table")
            .withPartitionSpec(SPEC)
            .withPrimaryKeySpec(PRIMARY_KEY_SPEC)
            .create()
            .asKeyedTable();

    this.before();
  }

  protected void before() {
    // implement for sub case
  }

  protected static String warehousePath() {
    return warehouse.getAbsolutePath();
  }

  protected static void setupCatalog(CatalogTestHelper catalogTestHelper) throws IOException {
    tmp.create();
    warehouse = tmp.newFolder("warehouse");
    AMS = MockAmoroManagementServer.getInstance();
    CatalogMeta catalogMeta = catalogTestHelper.buildCatalogMeta(warehouse.getAbsolutePath());
    AMS.handler().createCatalog(catalogMeta);
  }

  protected void clearTable() {
    testCatalog.dropTable(TABLE_ID, true);
    AMS.handler().getTableCommitMetas().remove(TABLE_ID.buildTableIdentifier());

    testCatalog.dropTable(PK_TABLE_ID, true);
    AMS.handler().getTableCommitMetas().remove(PK_TABLE_ID.buildTableIdentifier());
    AMS = null;
  }

  protected List<DataFile> writeBase(TableIdentifier identifier, List<Record> records) {
    KeyedTable table = testCatalog.loadTable(identifier).asKeyedTable();
    long txId = table.beginTransaction("");
    try (GenericBaseTaskWriter writer =
        GenericTaskWriters.builderFor(table).withTransactionId(txId).buildBaseWriter()) {
      records.forEach(
          d -> {
            try {
              writer.write(d);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
      WriteResult result = writer.complete();
      AppendFiles appendFiles = table.baseTable().newAppend();
      Arrays.stream(result.dataFiles()).forEach(appendFiles::appendFile);
      appendFiles.commit();
      return Arrays.asList(result.dataFiles());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<DataFile> writeChange(
      TableIdentifier identifier, ChangeAction action, List<Record> records) {
    KeyedTable table = testCatalog.loadTable(identifier).asKeyedTable();
    try (GenericChangeTaskWriter writer =
        GenericTaskWriters.builderFor(table).withChangeAction(action).buildChangeWriter()) {
      records.forEach(
          d -> {
            try {
              writer.write(d);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

      WriteResult result = writer.complete();
      AppendFiles appendFiles = table.changeTable().newAppend();
      Arrays.stream(result.dataFiles()).forEach(appendFiles::appendFile);
      appendFiles.commit();
      return Arrays.asList(result.dataFiles());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected static List<Record> readKeyedTable(KeyedTable keyedTable) {
    GenericKeyedDataReader reader =
        new GenericKeyedDataReader(
            keyedTable.io(),
            keyedTable.schema(),
            keyedTable.schema(),
            keyedTable.primaryKeySpec(),
            null,
            true,
            IdentityPartitionConverters::convertConstant);
    List<Record> result = Lists.newArrayList();
    try (CloseableIterable<CombinedScanTask> combinedScanTasks = keyedTable.newScan().planTasks()) {
      combinedScanTasks.forEach(
          combinedTask ->
              combinedTask
                  .tasks()
                  .forEach(
                      scTask -> {
                        try (CloseableIterator<Record> records = reader.readData(scTask)) {
                          while (records.hasNext()) {
                            result.add(records.next());
                          }
                        } catch (IOException e) {
                          throw new RuntimeException(e);
                        }
                      }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  protected static Record newGenericRecord(Schema schema, Object... fields) {
    GenericRecord record = GenericRecord.create(schema);
    for (int i = 0; i < schema.columns().size(); i++) {
      record.set(i, fields[i]);
    }
    return record;
  }

  protected static Record newGenericRecord(Types.StructType type, Object... fields) {
    GenericRecord record = GenericRecord.create(type);
    for (int i = 0; i < type.fields().size(); i++) {
      record.set(i, fields[i]);
    }
    return record;
  }

  public static LocalDateTime quickDate(int day) {
    return LocalDateTime.of(2020, 1, day, 0, 0);
  }

  protected StructLike partitionData(
      Schema tableSchema, PartitionSpec spec, Object... partitionValues) {
    GenericRecord record = GenericRecord.create(tableSchema);
    int index = 0;
    Set<Integer> partitionField = Sets.newHashSet();
    spec.fields().forEach(f -> partitionField.add(f.sourceId()));
    List<Types.NestedField> tableFields = tableSchema.columns();
    for (int i = 0; i < tableFields.size(); i++) {
      // String sourceColumnName = tableSchema.findColumnName(i);
      Types.NestedField sourceColumn = tableFields.get(i);
      if (partitionField.contains(sourceColumn.fieldId())) {
        Object partitionVal = partitionValues[index];
        index++;
        record.set(i, partitionVal);
      } else {
        record.set(i, 0);
      }
    }

    PartitionKey pd = new PartitionKey(spec, tableSchema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(tableSchema.asStruct());
    wrapper = wrapper.wrap(record);
    pd.partition(wrapper);
    return pd;
  }

  protected static List<DataFile> writeBaseNoCommit(
      KeyedTable table, long txId, List<Record> records) {
    try (GenericBaseTaskWriter writer =
        GenericTaskWriters.builderFor(table).withTransactionId(txId).buildBaseWriter()) {
      records.forEach(
          d -> {
            try {
              writer.write(d);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
      WriteResult result = writer.complete();
      return Arrays.asList(result.dataFiles());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
