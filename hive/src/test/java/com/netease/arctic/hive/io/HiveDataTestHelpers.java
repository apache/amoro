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
import com.netease.arctic.hive.io.reader.AdaptHiveGenericKeyedDataReader;
import com.netease.arctic.hive.io.reader.AdaptHiveGenericUnkeyedDataReader;
import com.netease.arctic.hive.io.writer.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HiveCommitUtil;
import com.netease.arctic.io.MixedDataTestHelpers;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.MetadataColumns;
import com.netease.arctic.utils.TablePropertyUtil;
import com.netease.arctic.utils.map.StructLikeCollections;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

public class HiveDataTestHelpers {

  public static WriterHelper writeOf(ArcticTable table) {
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

    Boolean usingHiveCommitProtocol = null;

    public WriterHelper customHiveLocation(String customHiveLocation) {
      this.customHiveLocation = customHiveLocation;
      return this;
    }

    public WriterHelper transactionId(Long txId) {
      this.txId = txId;
      return this;
    }

    public WriterHelper usingHiveCommitProtocol(boolean usingHiveCommitProtocol) {
      this.usingHiveCommitProtocol = usingHiveCommitProtocol;
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
      if (this.usingHiveCommitProtocol != null) {
        builder.hiveConsistentWrite(this.usingHiveCommitProtocol);
      }
      return builder.buildWriter(writeLocationKind);
    }
  }

  public static List<DataFile> applyConsistentWriteFiles(ArcticTable table, List<DataFile> files) {
    if (!TablePropertyUtil.hiveConsistentWriteEnabled(table.properties())) {
      return files;
    }
    String hiveLocation = ((SupportHive) table).hiveLocation();
    List<DataFile> nonHiveFiles = Lists.newArrayList();
    List<DataFile> hiveFiles = Lists.newArrayList();
    for (DataFile f : files) {
      String location = f.path().toString();
      if (location.toLowerCase().startsWith(hiveLocation.toLowerCase())) {
        hiveFiles.add(f);
      } else {
        nonHiveFiles.add(f);
      }
    }
    hiveFiles = HiveCommitUtil.applyConsistentWriteFile(hiveFiles, table.spec(), (l, c) -> {});
    nonHiveFiles.addAll(hiveFiles);
    return nonHiveFiles;
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
      ArcticTable table, Expression expression, Schema projectSchema, boolean useDiskMap) {
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
