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

package org.apache.amoro.op;

import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ExpireSnapshots;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.ReplaceSortOrder;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RewriteManifests;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateLocation;
import org.apache.iceberg.UpdatePartitionSpec;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreateTableTransaction implements Transaction {
  private final List<DataFile> appendDataFiles = Lists.newArrayList();
  private final Supplier<MixedTable> tableCreator;
  private final Runnable rollback;
  private final Transaction transaction;

  public CreateTableTransaction(
      Transaction transaction, Supplier<MixedTable> tableSupplier, Runnable rollback) {
    this.transaction = transaction;
    this.tableCreator = tableSupplier;
    this.rollback = rollback;
  }

  @Override
  public Table table() {
    return transaction.table();
  }

  @Override
  public UpdateSchema updateSchema() {
    throw new UnsupportedOperationException("create table transaction unsupported updateSchema");
  }

  @Override
  public UpdatePartitionSpec updateSpec() {
    throw new UnsupportedOperationException("create table transaction unsupported updateSpec");
  }

  @Override
  public UpdateProperties updateProperties() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported updateProperties");
  }

  @Override
  public ReplaceSortOrder replaceSortOrder() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported updateProperties");
  }

  @Override
  public UpdateLocation updateLocation() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported updateProperties");
  }

  @Override
  public AppendFiles newAppend() {
    return new AppendFiles() {
      private final List<DataFile> dataFiles = Lists.newArrayList();

      @Override
      public AppendFiles appendFile(DataFile dataFile) {
        dataFiles.add(dataFile);
        return this;
      }

      @Override
      public AppendFiles appendManifest(ManifestFile file) {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported ManifestFile");
      }

      @Override
      public AppendFiles set(String property, String value) {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported set");
      }

      @Override
      public AppendFiles deleteWith(Consumer<String> deleteFunc) {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported deleteWith");
      }

      @Override
      public AppendFiles stageOnly() {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported stageOnly");
      }

      @Override
      public AppendFiles scanManifestsWith(ExecutorService executorService) {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported scanManifestsWith");
      }

      @Override
      public Snapshot apply() {
        throw new UnsupportedOperationException(
            "create table transaction AppendFiles unsupported apply");
      }

      @Override
      public void commit() {
        appendDataFiles.addAll(this.dataFiles);
      }
    };
  }

  @Override
  public RewriteFiles newRewrite() {
    throw new UnsupportedOperationException("create table transaction unsupported newRewrite");
  }

  @Override
  public RewriteManifests rewriteManifests() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported rewriteManifests");
  }

  @Override
  public OverwriteFiles newOverwrite() {
    throw new UnsupportedOperationException("create table transaction unsupported newOverwrite");
  }

  @Override
  public RowDelta newRowDelta() {
    throw new UnsupportedOperationException("create table transaction unsupported newRowDelta");
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported newReplacePartitions");
  }

  @Override
  public DeleteFiles newDelete() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported newReplacePartitions");
  }

  @Override
  public ExpireSnapshots expireSnapshots() {
    throw new UnsupportedOperationException(
        "create table transaction unsupported newReplacePartitions");
  }

  @Override
  public void commitTransaction() {
    MixedTable table = tableCreator.get();
    try {
      Transaction tx;
      if (table.isUnkeyedTable()) {
        tx = table.asUnkeyedTable().newTransaction();
      } else {
        tx = table.asKeyedTable().baseTable().newTransaction();
      }

      AppendFiles appendFiles = tx.newAppend();
      appendDataFiles.forEach(appendFiles::appendFile);
      appendFiles.commit();
      tx.commitTransaction();
    } catch (Throwable t) {
      rollback.run();
      throw t;
    }
  }
}
