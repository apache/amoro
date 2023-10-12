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

package com.netease.arctic.op;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.WatermarkGenerator;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.PendingUpdate;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotUpdate;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract implementation of {@link PendingUpdate}, adding arctic logics like tracing and watermark generating for
 * iceberg operations.
 *
 * @param <T> Java class of changes from this update; returned by {@link #apply} for validation.
 */
public abstract class ArcticUpdate<T> implements SnapshotUpdate<T> {

  private static final Logger LOG = LoggerFactory.getLogger(ArcticUpdate.class);

  protected final SnapshotUpdate<T> delegate;
  private final ArcticTable arcticTable;
  protected final Transaction transaction;
  protected final boolean autoCommitTransaction;
  protected final WatermarkGenerator watermarkGenerator;

  public ArcticUpdate(ArcticTable arcticTable, SnapshotUpdate<T> delegate) {
    this.arcticTable = arcticTable;
    this.transaction = null;
    this.autoCommitTransaction = false;
    this.watermarkGenerator = null;
    this.delegate = delegate;
  }

  public ArcticUpdate(
      ArcticTable arcticTable, SnapshotUpdate<T> delegate, Transaction transaction,
      boolean autoCommitTransaction) {
    this.arcticTable = arcticTable;
    this.transaction = transaction;
    this.autoCommitTransaction = autoCommitTransaction;
    WatermarkGenerator watermarkGenerator = null;
    try {
      watermarkGenerator = WatermarkGenerator.forTable(arcticTable);
    } catch (Exception e) {
      LOG.warn("Failed to initialize watermark generator", e);
    }
    this.watermarkGenerator = watermarkGenerator;
    this.delegate = delegate;
  }

  protected void addIcebergDataFile(DataFile file) {
    if (watermarkGenerator != null) {
      watermarkGenerator.addFile(file);
    }
  }

  protected void deleteIcebergDataFile(DataFile file) {
    if (watermarkGenerator != null) {
      watermarkGenerator.addFile(file);
    }
  }

  protected void addIcebergDeleteFile(DeleteFile file) {
    if (watermarkGenerator != null) {
      watermarkGenerator.addFile(file);
    }
  }

  protected void deleteIcebergDeleteFile(DeleteFile file) {
    if (watermarkGenerator != null) {
      watermarkGenerator.addFile(file);
    }
  }

  @Override
  public T set(String property, String value) {
    this.delegate.set(property, value);
    return this.self();
  }

  @Override
  public T deleteWith(Consumer<String> deleteFunc) {
    this.delegate.deleteWith(deleteFunc);
    return this.self();
  }

  @Override
  public T stageOnly() {
    this.delegate.stageOnly();
    return this.self();
  }

  @Override
  public T scanManifestsWith(ExecutorService executorService) {
    this.delegate.scanManifestsWith(executorService);
    return this.self();
  }

  @Override
  public Snapshot apply() {
    return this.delegate.apply();
  }

  @Override
  public Object updateEvent() {
    return this.delegate.updateEvent();
  }

  protected abstract T self();

  @Override
  public void commit() {
    this.delegate.commit();
    if (transaction != null && watermarkGenerator != null) {
      long currentWatermark = TablePropertyUtil.getTableWatermark(arcticTable.properties());
      long newWatermark = watermarkGenerator.watermark();
      if (newWatermark > currentWatermark) {
        transaction.updateProperties().set(TableProperties.WATERMARK_TABLE, String.valueOf(newWatermark)).commit();
      }
    }
    if (transaction != null && autoCommitTransaction) {
      transaction.commitTransaction();
    }
  }

  @Override
  public T toBranch(String branch) {
    this.delegate.toBranch(branch);
    return this.self();
  }

  public abstract static class Builder<T extends I, I> {

    protected final ArcticTable table;
    protected Table tableStore;
    protected boolean onChangeStore = false;
    protected Transaction insideTransaction;
    protected boolean generateWatermark = false;

    protected Builder(ArcticTable table) {
      this.table = table;
    }

    public Builder<T, I> onChange() {
      this.onChangeStore = true;
      return this;
    }

    public Builder<T, I> onTableStore(Table tableStore) {
      this.tableStore = tableStore;
      return this;
    }

    public Builder<T, I> inTransaction(Transaction transaction) {
      this.insideTransaction = transaction;
      return this;
    }

    public Builder<T, I> generateWatermark() {
      this.generateWatermark = true;
      return this;
    }

    protected Table getTableStore() {
      if (tableStore == null) {
        if (table.isKeyedTable()) {
          if (onChangeStore) {
            tableStore = table.asKeyedTable().changeTable();
          } else {
            tableStore = table.asKeyedTable().baseTable();
          }
        } else {
          tableStore = table.asUnkeyedTable();
        }
      }
      return tableStore;
    }

    public T build() {
      Table tableStore = getTableStore();
      if (generateWatermark) {
        if (insideTransaction != null) {
          return updateWithWatermark(insideTransaction, false);
        } else {
          Transaction transaction = tableStore.newTransaction();
          return updateWithWatermark(transaction, true);
        }
      } else {
        if (insideTransaction != null) {
          return updateWithoutWatermark(transactionDelegateSupplier(insideTransaction));
        } else {
          return updateWithoutWatermark(tableStoreDelegateSupplier(tableStore));
        }
      }
    }

    protected abstract T updateWithWatermark(Transaction transaction,
        boolean autoCommitTransaction);

    protected abstract T updateWithoutWatermark(Supplier<I> delegateSupplier);

    protected abstract Supplier<I> transactionDelegateSupplier(Transaction transaction);

    protected abstract Supplier<I> tableStoreDelegateSupplier(Table tableStore);
  }
}
