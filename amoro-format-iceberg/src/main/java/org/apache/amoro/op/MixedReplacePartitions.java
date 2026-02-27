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
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;

import java.util.function.Supplier;

public class MixedReplacePartitions extends MixedUpdate<ReplacePartitions>
    implements ReplacePartitions {

  private final ReplacePartitions replacePartitions;

  public static MixedReplacePartitions.Builder buildFor(MixedTable table) {
    return new MixedReplacePartitions.Builder(table);
  }

  private MixedReplacePartitions(MixedTable mixedTable, ReplacePartitions replacePartitions) {
    super(mixedTable, replacePartitions);
    this.replacePartitions = replacePartitions;
  }

  private MixedReplacePartitions(
      MixedTable mixedTable,
      ReplacePartitions replacePartitions,
      Transaction transaction,
      boolean autoCommitTransaction) {
    super(mixedTable, replacePartitions, transaction, autoCommitTransaction);
    this.replacePartitions = replacePartitions;
  }

  @Override
  public ReplacePartitions addFile(DataFile file) {
    replacePartitions.addFile(file);
    addIcebergDataFile(file);
    return this;
  }

  @Override
  public ReplacePartitions validateAppendOnly() {
    replacePartitions.validateAppendOnly();
    return this;
  }

  @Override
  public ReplacePartitions validateFromSnapshot(long snapshotId) {
    replacePartitions.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  public ReplacePartitions validateNoConflictingDeletes() {
    replacePartitions.validateNoConflictingDeletes();
    return this;
  }

  @Override
  public ReplacePartitions validateNoConflictingData() {
    replacePartitions.validateNoConflictingData();
    return this;
  }

  @Override
  protected ReplacePartitions self() {
    return this;
  }

  public static class Builder
      extends MixedUpdate.Builder<MixedReplacePartitions, ReplacePartitions> {

    private Builder(MixedTable table) {
      super(table);
      generateWatermark();
    }

    @Override
    protected MixedReplacePartitions updateWithWatermark(
        Transaction transaction, boolean autoCommitTransaction) {
      return new MixedReplacePartitions(
          table, transaction.newReplacePartitions(), transaction, autoCommitTransaction);
    }

    @Override
    protected MixedReplacePartitions updateWithoutWatermark(
        Supplier<ReplacePartitions> delegateSupplier) {
      return new MixedReplacePartitions(table, delegateSupplier.get());
    }

    @Override
    protected Supplier<ReplacePartitions> transactionDelegateSupplier(Transaction transaction) {
      return transaction::newReplacePartitions;
    }

    @Override
    protected Supplier<ReplacePartitions> tableStoreDelegateSupplier(Table tableStore) {
      return tableStore::newReplacePartitions;
    }
  }
}
