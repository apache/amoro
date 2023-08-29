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

package com.netease.arctic.trace;

import com.netease.arctic.op.ArcticUpdate;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import java.util.Set;
import java.util.function.Supplier;

public class ArcticRewriteFiles extends ArcticUpdate<RewriteFiles> implements RewriteFiles {
  private final RewriteFiles rewriteFiles;

  public static Builder buildFor(ArcticTable table) {
    return new Builder(table);
  }

  protected ArcticRewriteFiles(ArcticTable table, RewriteFiles rewriteFiles) {
    super(table, rewriteFiles);
    this.rewriteFiles = rewriteFiles;
  }

  @Override
  public RewriteFiles rewriteFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd) {
    rewriteFiles.rewriteFiles(filesToDelete, filesToAdd);
    filesToAdd.forEach(this::addIcebergDataFile);
    filesToDelete.forEach(this::deleteIcebergDataFile);
    return this;
  }

  @Override
  public RewriteFiles rewriteFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd, long sequenceNumber) {
    rewriteFiles.rewriteFiles(filesToDelete, filesToAdd, sequenceNumber);
    filesToAdd.forEach(this::addIcebergDataFile);
    filesToDelete.forEach(this::deleteIcebergDataFile);
    return this;
  }

  @Override
  public RewriteFiles rewriteFiles(
      Set<DataFile> dataFilesToReplace, Set<DeleteFile> deleteFilesToReplace,
      Set<DataFile> dataFilesToAdd, Set<DeleteFile> deleteFilesToAdd) {
    rewriteFiles.rewriteFiles(dataFilesToReplace, deleteFilesToReplace, dataFilesToAdd, deleteFilesToAdd);
    dataFilesToAdd.forEach(this::addIcebergDataFile);
    dataFilesToReplace.forEach(this::deleteIcebergDataFile);
    deleteFilesToAdd.forEach(this::addIcebergDeleteFile);
    deleteFilesToReplace.forEach(this::deleteIcebergDeleteFile);
    return this;
  }

  @Override
  public RewriteFiles validateFromSnapshot(long snapshotId) {
    rewriteFiles.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  protected RewriteFiles self() {
    return this;
  }

  public static class Builder extends ArcticUpdate.Builder<ArcticRewriteFiles, RewriteFiles> {

    private Builder(ArcticTable table) {
      super(table);
    }

    @Override
    protected ArcticRewriteFiles updateWithWatermark(
        Transaction transaction,
        boolean autoCommitTransaction) {
      return new ArcticRewriteFiles(table, transaction.newRewrite());
    }

    @Override
    protected ArcticRewriteFiles updateWithoutWatermark(Supplier<RewriteFiles> delegateSupplier) {
      return new ArcticRewriteFiles(table, delegateSupplier.get());
    }

    @Override
    protected Supplier<RewriteFiles> transactionDelegateSupplier(Transaction transaction) {
      return transaction::newRewrite;
    }

    @Override
    protected Supplier<RewriteFiles> tableStoreDelegateSupplier(Table tableStore) {
      return tableStore::newRewrite;
    }
  }
}
