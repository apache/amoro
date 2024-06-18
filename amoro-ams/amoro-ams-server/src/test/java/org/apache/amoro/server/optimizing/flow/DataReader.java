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

package org.apache.amoro.server.optimizing.flow;

import org.apache.amoro.hive.io.reader.AdaptHiveGenericKeyedDataReader;
import org.apache.amoro.hive.io.reader.AdaptHiveGenericUnkeyedDataReader;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DataReader {
  private final MixedTable table;

  public DataReader(MixedTable table) {
    this.table = table;
  }

  public List<Record> allData() throws Exception {
    if (table.isKeyedTable()) {
      return readKeyed(table.asKeyedTable());
    } else {
      return readIceberg(table.asUnkeyedTable());
    }
  }

  private List<Record> readKeyed(KeyedTable table)
      throws IOException, ExecutionException, InterruptedException {
    CloseableIterable<CombinedScanTask> combinedScanTasks = table.newScan().planTasks();
    AdaptHiveGenericKeyedDataReader dataReader =
        new AdaptHiveGenericKeyedDataReader(
            table.io(),
            table.schema(),
            table.schema(),
            table.primaryKeySpec(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            null,
            false);
    List<CompletableFuture<List<Record>>> completableFutures = new ArrayList<>();
    for (CombinedScanTask combinedScanTask : combinedScanTasks) {
      for (KeyedTableScanTask scanTask : combinedScanTask.tasks()) {
        completableFutures.add(
            CompletableFuture.supplyAsync(
                () -> {
                  return table
                      .io()
                      .doAs(
                          () -> {
                            CloseableIterator<Record> closeableIterator =
                                dataReader.readData(scanTask);
                            List<Record> list = new ArrayList<>();
                            Iterators.addAll(list, closeableIterator);
                            try {
                              closeableIterator.close();
                            } catch (IOException e) {
                              throw new RuntimeException(e);
                            }
                            return list;
                          });
                }));
      }
    }

    List<Record> list = new ArrayList<>();
    for (CompletableFuture<List<Record>> completableFuture : completableFutures) {
      list.addAll(completableFuture.get());
    }
    return list;
  }

  private List<Record> readIceberg(UnkeyedTable table)
      throws ExecutionException, InterruptedException {
    AdaptHiveGenericUnkeyedDataReader dataReader =
        new AdaptHiveGenericUnkeyedDataReader(
            table.io(),
            table.schema(),
            table.schema(),
            null,
            false,
            IdentityPartitionConverters::convertConstant,
            false);
    CloseableIterable<FileScanTask> fileScanTasks = table.newScan().planFiles();

    List<CompletableFuture<List<Record>>> completableFutures = new ArrayList<>();
    for (FileScanTask fileScanTask : fileScanTasks) {
      completableFutures.add(
          CompletableFuture.supplyAsync(
              () -> {
                CloseableIterable<Record> closeableIterable = dataReader.readData(fileScanTask);
                List<Record> list = new ArrayList<>();
                Iterables.addAll(list, closeableIterable);
                try {
                  closeableIterable.close();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
                return list;
              }));
    }

    List<Record> list = new ArrayList<>();
    for (CompletableFuture<List<Record>> completableFuture : completableFutures) {
      List<Record> records = completableFuture.get();
      list.addAll(records);
    }
    return list;
  }
}
