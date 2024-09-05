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

package org.apache.amoro.hive.io;

import org.apache.amoro.data.ChangeAction;
import org.apache.amoro.hive.io.reader.AdaptHiveGenericKeyedDataReader;
import org.apache.amoro.hive.io.reader.AdaptHiveGenericUnkeyedDataReader;
import org.apache.amoro.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import org.apache.amoro.hive.table.HiveLocationKind;
import org.apache.amoro.hive.table.SupportHive;
import org.apache.amoro.io.MixedDataTestHelpers;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BaseLocationKind;
import org.apache.amoro.table.ChangeLocationKind;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.LocationKind;
import org.apache.amoro.table.MetadataColumns;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.TableFileUtil;
import org.apache.amoro.utils.map.StructLikeCollections;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.Assert;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class HiveDataTestHelpers {

  public static WriterHelper writerOf(MixedTable table) {
    return new WriterHelper(table);
  }

  public static class WriterHelper {
    MixedTable table;

    public WriterHelper(MixedTable table) {
      this.table = table;
    }

    boolean orderedWrite = false;
    String customHiveLocation = null;

    Long txId = null;

    Boolean consistentWriteEnabled = null;

    public WriterHelper customHiveLocation(String customHiveLocation) {
      this.customHiveLocation = customHiveLocation;
      return this;
    }

    public WriterHelper transactionId(Long txId) {
      this.txId = txId;
      return this;
    }

    public WriterHelper consistentWriteEnabled(boolean consistentWriteEnabled) {
      this.consistentWriteEnabled = consistentWriteEnabled;
      return this;
    }

    public WriterHelper orderedWrite(boolean orderedWrite) {
      this.orderedWrite = orderedWrite;
      return this;
    }

    public List<DataFile> writeChange(List<Record> records, ChangeAction action) {
      AdaptHiveGenericTaskWriterBuilder builder =
          AdaptHiveGenericTaskWriterBuilder.builderFor(table)
              .withChangeAction(action)
              .withTransactionId(txId);
      if (orderedWrite) {
        builder.withOrdered();
      }
      try (TaskWriter<Record> writer = builder.buildWriter(ChangeLocationKind.INSTANT)) {
        return MixedDataTestHelpers.writeRecords(writer, records);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    public List<DataFile> writeBase(List<Record> records) {
      return writeRecords(BaseLocationKind.INSTANT, records);
    }

    public List<DataFile> writeHive(List<Record> records) {
      return writeRecords(HiveLocationKind.INSTANT, records);
    }

    private List<DataFile> writeRecords(LocationKind writeLocationKind, List<Record> records) {
      try (TaskWriter<Record> writer = newBaseWriter(writeLocationKind)) {
        return MixedDataTestHelpers.writeRecords(writer, records);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    private TaskWriter<Record> newBaseWriter(LocationKind writeLocationKind) {
      AdaptHiveGenericTaskWriterBuilder builder =
          AdaptHiveGenericTaskWriterBuilder.builderFor(table);
      if (table.isKeyedTable()) {
        builder.withTransactionId(txId);
      }
      if (orderedWrite) {
        builder.withOrdered();
      }
      if (customHiveLocation != null) {
        builder.withCustomHiveSubdirectory(customHiveLocation);
      }
      if (this.consistentWriteEnabled != null) {
        builder.hiveConsistentWrite(this.consistentWriteEnabled);
      }
      return builder.buildWriter(writeLocationKind);
    }
  }

  public static List<DataFile> lastedAddedFiles(Table tableStore) {
    tableStore.refresh();
    return Lists.newArrayList(tableStore.currentSnapshot().addedDataFiles(tableStore.io()));
  }

  /**
   * Assert the consistent-write process, with this parameter enabled, the written file is a hidden
   * file.
   */
  public static void assertWriteConsistentFilesName(SupportHive table, List<DataFile> files) {
    boolean consistentWriteEnabled =
        PropertyUtil.propertyAsBoolean(
            table.properties(),
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED,
            HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT);
    String hiveLocation = table.hiveLocation();
    for (DataFile f : files) {
      String filename = TableFileUtil.getFileName(f.path().toString());
      if (isHiveFile(hiveLocation, f)) {
        Assert.assertEquals(consistentWriteEnabled, filename.startsWith("."));
      } else {
        Assert.assertFalse(filename.startsWith("."));
      }
    }
  }

  /** Assert the consistent-write commit, all file will not be hidden file after commit. */
  public static void assertWriteConsistentFilesCommit(MixedTable table) {
    table.refresh();
    UnkeyedTable unkeyedTable = MixedTableUtil.baseStore(table);
    unkeyedTable
        .newScan()
        .planFiles()
        .forEach(
            t -> {
              String filename = TableFileUtil.getFileName(t.file().path().toString());
              Assert.assertFalse(filename.startsWith("."));
            });
  }

  public static boolean isHiveFile(String hiveLocation, DataFile file) {
    String location = file.path().toString();
    return location.toLowerCase().startsWith(hiveLocation.toLowerCase());
  }

  public static List<Record> readKeyedTable(
      KeyedTable keyedTable,
      Expression expression,
      Schema projectSchema,
      boolean useDiskMap,
      boolean readDeletedData) {
    AdaptHiveGenericKeyedDataReader reader;
    if (projectSchema == null) {
      projectSchema = keyedTable.schema();
    }
    if (useDiskMap) {
      reader =
          new AdaptHiveGenericKeyedDataReader(
              keyedTable.io(),
              keyedTable.schema(),
              projectSchema,
              keyedTable.primaryKeySpec(),
              null,
              true,
              IdentityPartitionConverters::convertConstant,
              null,
              false,
              new StructLikeCollections(true, 0L));
    } else {
      reader =
          new AdaptHiveGenericKeyedDataReader(
              keyedTable.io(),
              keyedTable.schema(),
              projectSchema,
              keyedTable.primaryKeySpec(),
              null,
              true,
              IdentityPartitionConverters::convertConstant);
    }

    return MixedDataTestHelpers.readKeyedTable(
        keyedTable, reader, expression, projectSchema, readDeletedData);
  }

  public static List<Record> readChangeStore(
      KeyedTable keyedTable, Expression expression, Schema projectSchema, boolean useDiskMap) {
    if (projectSchema == null) {
      projectSchema = keyedTable.schema();
    }
    Schema expectTableSchema =
        MetadataColumns.appendChangeStoreMetadataColumns(keyedTable.schema());
    Schema expectProjectSchema = MetadataColumns.appendChangeStoreMetadataColumns(projectSchema);

    AdaptHiveGenericUnkeyedDataReader reader;
    if (useDiskMap) {
      reader =
          new AdaptHiveGenericUnkeyedDataReader(
              keyedTable.asKeyedTable().io(),
              expectTableSchema,
              expectProjectSchema,
              null,
              false,
              IdentityPartitionConverters::convertConstant,
              false,
              new StructLikeCollections(true, 0L));
    } else {
      reader =
          new AdaptHiveGenericUnkeyedDataReader(
              keyedTable.asKeyedTable().io(),
              expectTableSchema,
              expectProjectSchema,
              null,
              false,
              IdentityPartitionConverters::convertConstant,
              false);
    }

    return MixedDataTestHelpers.readChangeStore(keyedTable, reader, expression);
  }

  public static List<Record> readBaseStore(
      MixedTable table, Expression expression, Schema projectSchema, boolean useDiskMap) {
    if (projectSchema == null) {
      projectSchema = table.schema();
    }

    AdaptHiveGenericUnkeyedDataReader reader;
    if (useDiskMap) {
      reader =
          new AdaptHiveGenericUnkeyedDataReader(
              table.io(),
              table.schema(),
              projectSchema,
              null,
              false,
              IdentityPartitionConverters::convertConstant,
              false,
              new StructLikeCollections(true, 0L));
    } else {
      reader =
          new AdaptHiveGenericUnkeyedDataReader(
              table.io(),
              table.schema(),
              projectSchema,
              null,
              false,
              IdentityPartitionConverters::convertConstant,
              false);
    }

    return MixedDataTestHelpers.readBaseStore(table, reader, expression);
  }
}
