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
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.expressions.Expression;

import java.util.function.Supplier;

public class MixedDeleteFiles extends MixedUpdate<DeleteFiles> implements DeleteFiles {

  private final DeleteFiles deleteFiles;

  public static Builder buildFor(MixedTable table) {
    return new Builder(table);
  }

  protected MixedDeleteFiles(MixedTable table, DeleteFiles deleteFiles) {
    super(table, deleteFiles);
    this.deleteFiles = deleteFiles;
  }

  @Override
  public DeleteFiles deleteFile(CharSequence path) {
    throw new UnsupportedOperationException("this method is not supported");
  }

  @Override
  public DeleteFiles deleteFile(DataFile file) {
    deleteFiles.deleteFile(file);
    deleteIcebergDataFile(file);
    return this;
  }

  @Override
  public DeleteFiles deleteFromRowFilter(Expression expr) {
    deleteFiles.deleteFromRowFilter(expr);
    return this;
  }

  @Override
  public DeleteFiles caseSensitive(boolean caseSensitive) {
    deleteFiles.caseSensitive(caseSensitive);
    return this;
  }

  @Override
  protected DeleteFiles self() {
    return this;
  }

  public static class Builder extends MixedUpdate.Builder<MixedDeleteFiles, DeleteFiles> {

    protected Builder(MixedTable table) {
      super(table);
    }

    @Override
    protected MixedDeleteFiles updateWithWatermark(
        Transaction transaction, boolean autoCommitTransaction) {
      return new MixedDeleteFiles(table, transaction.newDelete());
    }

    @Override
    protected MixedDeleteFiles updateWithoutWatermark(Supplier<DeleteFiles> delegateSupplier) {
      return new MixedDeleteFiles(table, delegateSupplier.get());
    }

    @Override
    protected Supplier<DeleteFiles> transactionDelegateSupplier(Transaction transaction) {
      return transaction::newDelete;
    }

    @Override
    protected Supplier<DeleteFiles> tableStoreDelegateSupplier(Table tableStore) {
      return tableStore::newDelete;
    }
  }
}
