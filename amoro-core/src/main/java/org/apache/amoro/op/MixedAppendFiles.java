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

import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;

import java.util.function.Supplier;

/**
 * Implementation of {@link AppendFiles} for mixed-format table, adding tracing and watermark
 * generating logics.
 */
public class MixedAppendFiles extends MixedUpdate<AppendFiles> implements AppendFiles {

  private final AppendFiles appendFiles;

  public static Builder buildFor(MixedTable table, boolean fastAppend) {
    return new Builder(table, fastAppend);
  }

  private MixedAppendFiles(MixedTable mixedTable, AppendFiles appendFiles) {
    super(mixedTable, appendFiles);
    this.appendFiles = appendFiles;
  }

  private MixedAppendFiles(
      MixedTable mixedTable,
      AppendFiles appendFiles,
      Transaction transaction,
      boolean autoCommitTransaction) {
    super(mixedTable, appendFiles, transaction, autoCommitTransaction);
    this.appendFiles = appendFiles;
  }

  @Override
  public AppendFiles appendFile(DataFile file) {
    appendFiles.appendFile(file);
    addIcebergDataFile(file);
    return this;
  }

  @Override
  public AppendFiles appendManifest(ManifestFile file) {
    // TODO read added files from manifest file
    appendFiles.appendManifest(file);
    return this;
  }

  @Override
  protected AppendFiles self() {
    return this;
  }

  public static class Builder extends MixedUpdate.Builder<MixedAppendFiles, AppendFiles> {

    private final boolean fastAppend;

    private Builder(MixedTable table, boolean fastAppend) {
      super(table);
      generateWatermark();
      this.fastAppend = fastAppend;
    }

    @Override
    protected MixedAppendFiles updateWithWatermark(
        Transaction transaction, boolean autoCommitTransaction) {
      return new MixedAppendFiles(
          table, newAppendFiles(transaction), transaction, autoCommitTransaction);
    }

    @Override
    protected Supplier<AppendFiles> transactionDelegateSupplier(Transaction transaction) {
      return () -> newAppendFiles(transaction);
    }

    @Override
    protected Supplier<AppendFiles> tableStoreDelegateSupplier(Table tableStore) {
      return () -> newAppendFiles(tableStore);
    }

    @Override
    protected MixedAppendFiles updateWithoutWatermark(Supplier<AppendFiles> delegateSupplier) {
      return new MixedAppendFiles(table, delegateSupplier.get());
    }

    private AppendFiles newAppendFiles(Transaction transaction) {
      if (fastAppend) {
        return transaction.newFastAppend();
      } else {
        return transaction.newAppend();
      }
    }

    private AppendFiles newAppendFiles(Table tableStore) {
      if (fastAppend) {
        return tableStore.newFastAppend();
      } else {
        return tableStore.newAppend();
      }
    }
  }
}
