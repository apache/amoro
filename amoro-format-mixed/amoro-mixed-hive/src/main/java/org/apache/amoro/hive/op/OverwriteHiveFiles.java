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

package org.apache.amoro.hive.op;

import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.table.UnkeyedHiveTable;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;

import java.util.List;

public class OverwriteHiveFiles extends UpdateHiveFiles<OverwriteFiles> implements OverwriteFiles {

  public OverwriteHiveFiles(
      Transaction transaction,
      boolean insideTransaction,
      UnkeyedHiveTable table,
      HMSClientPool hmsClient,
      HMSClientPool transactionClient) {
    super(
        transaction,
        insideTransaction,
        table,
        transaction.newOverwrite(),
        hmsClient,
        transactionClient);
  }

  @Override
  protected OverwriteFiles self() {
    return this;
  }

  @Override
  public OverwriteFiles overwriteByRowFilter(Expression expr) {
    Preconditions.checkArgument(
        !table.spec().isUnpartitioned() || expr == Expressions.alwaysTrue(),
        "Unpartitioned hive table support alwaysTrue expression only");
    delegate.overwriteByRowFilter(expr);
    this.expr = expr;
    return this;
  }

  @Override
  public OverwriteFiles addFile(DataFile file) {
    if (!isHiveDataFile(file)) {
      delegate.addFile(file);
    } else {
      // handle file in hive location when commit
      this.addFiles.add(file);
    }
    return this;
  }

  @Override
  public OverwriteFiles deleteFile(DataFile file) {
    delegate.deleteFile(file);
    if (isHiveDataFile(file)) {
      // only handle file in hive location
      this.deleteFiles.add(file);
    }
    return this;
  }

  @Override
  public OverwriteFiles validateAddedFilesMatchOverwriteFilter() {
    delegate.validateAddedFilesMatchOverwriteFilter();
    return this;
  }

  @Override
  public OverwriteFiles validateFromSnapshot(long snapshotId) {
    delegate.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  public OverwriteFiles caseSensitive(boolean caseSensitive) {
    delegate.caseSensitive(caseSensitive);
    return this;
  }

  @Override
  public OverwriteFiles conflictDetectionFilter(Expression conflictDetectionFilter) {
    delegate.conflictDetectionFilter(conflictDetectionFilter);
    return this;
  }

  @Override
  public OverwriteFiles validateNoConflictingData() {
    delegate.validateNoConflictingData();
    return this;
  }

  @Override
  public OverwriteFiles validateNoConflictingDeletes() {
    delegate.validateNoConflictingDeletes();
    return this;
  }

  @Override
  protected void postHiveDataCommitted(List<DataFile> committedDataFile) {
    committedDataFile.forEach(delegate::addFile);
  }
}
