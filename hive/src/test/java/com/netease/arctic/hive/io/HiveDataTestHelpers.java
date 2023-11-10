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

package com.netease.arctic.hive.io;

import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.hive.HiveTableProperties;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.io.DataTestHelpers;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.TableFileUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.PropertyUtil;
import org.junit.Assert;

public class HiveDataTestHelpers {

  public static WriterHelper writerOf(ArcticTable table) {
    return new WriterHelper(table);
  }

  public static class WriterHelper {
    ArcticTable table;

    public WriterHelper(ArcticTable table) {
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
        return DataTestHelpers.writeRecords(writer, records);
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
        return DataTestHelpers.writeRecords(writer, records);
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
    return Lists.newArrayList(tableStore.currentSnapshot().addedFiles());
  }

  /**
   * Assert the consistent-write process, with this parameter enabled, the written file is a hidden
   * file.
   */
  public static void assertWriteConsistentFilesName(SupportHive table, List<DataFile> files) {
    boolean consistentWriteEnabled = PropertyUtil.propertyAsBoolean(
        table.properties(),
        HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED,
        HiveTableProperties.HIVE_CONSISTENT_WRITE_ENABLED_DEFAULT);
    String hiveLocation = table.hiveLocation();
    for (DataFile f : files) {
      String filename = TableFileUtils.getFileName(f.path().toString());
      if (isHiveFile(hiveLocation, f)) {
        Assert.assertEquals(consistentWriteEnabled, filename.startsWith("."));
      } else {
        Assert.assertFalse(filename.startsWith("."));
      }
    }
  }

  /** Assert the consistent-write commit, all file will not be hidden file after commit. */
  public static void assertWriteConsistentFilesCommit(ArcticTable table) {
    table.refresh();
    UnkeyedTable unkeyedTable = baseStore(table);
    unkeyedTable
        .newScan()
        .planFiles()
        .forEach(
            t -> {
              String filename = TableFileUtils.getFileName(t.file().path().toString());
              Assert.assertFalse(filename.startsWith("."));
            });
  }

  public static boolean isHiveFile(String hiveLocation, DataFile file) {
    String location = file.path().toString();
    return location.toLowerCase().startsWith(hiveLocation.toLowerCase());
  }

  /** Return the base store of the arctic table. */
  private static UnkeyedTable baseStore(ArcticTable arcticTable) {
    if (arcticTable.isKeyedTable()) {
      return arcticTable.asKeyedTable().baseTable();
    } else {
      return arcticTable.asUnkeyedTable();
    }
  }
}
