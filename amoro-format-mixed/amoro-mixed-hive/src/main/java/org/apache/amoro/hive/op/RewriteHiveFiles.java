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
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Transaction;

import java.util.List;
import java.util.Set;

public class RewriteHiveFiles extends UpdateHiveFiles<RewriteFiles> implements RewriteFiles {

  public RewriteHiveFiles(
      Transaction transaction,
      boolean insideTransaction,
      UnkeyedHiveTable table,
      HMSClientPool hmsClient,
      HMSClientPool transactionClient) {
    super(
        transaction,
        insideTransaction,
        table,
        transaction.newRewrite(),
        hmsClient,
        transactionClient);
  }

  @Override
  @Deprecated
  public RewriteFiles rewriteFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd) {
    filesToDelete.forEach(this::deleteFile);
    filesToAdd.forEach(this::addFile);
    return this;
  }

  @Override
  @Deprecated
  public RewriteFiles rewriteFiles(
      Set<DataFile> filesToDelete, Set<DataFile> filesToAdd, long sequenceNumber) {
    delegate.dataSequenceNumber(sequenceNumber);
    filesToDelete.forEach(delegate::deleteFile);
    // only add datafile not in hive location
    filesToAdd.stream().filter(dataFile -> !isHiveDataFile(dataFile)).forEach(delegate::addFile);
    markHiveFiles(filesToDelete, filesToAdd);
    return this;
  }

  @Override
  public RewriteFiles rewriteFiles(
      Set<DataFile> dataFilesToReplace,
      Set<DeleteFile> deleteFilesToReplace,
      Set<DataFile> dataFilesToAdd,
      Set<DeleteFile> deleteFilesToAdd) {
    dataFilesToReplace.forEach(this::deleteFile);
    deleteFilesToReplace.forEach(this::deleteFile);
    dataFilesToAdd.forEach(this::addFile);
    deleteFilesToAdd.forEach(this::addFile);
    return this;
  }

  @Override
  public RewriteFiles deleteFile(DataFile dataFile) {
    delegate.deleteFile(dataFile);
    markDeletedHiveFile(dataFile);
    return this;
  }

  @Override
  public RewriteFiles deleteFile(DeleteFile deleteFile) {
    delegate.deleteFile(deleteFile);
    return this;
  }

  @Override
  public RewriteFiles addFile(DataFile dataFile) {
    if (isHiveDataFile(dataFile)) {
      markAddedHiveFile(dataFile);
    } else {
      // Only add data file not in hive location
      delegate.addFile(dataFile);
    }
    return this;
  }

  @Override
  public RewriteFiles addFile(DeleteFile deleteFile) {
    delegate.addFile(deleteFile);
    return this;
  }

  @Override
  public RewriteFiles dataSequenceNumber(long sequenceNumber) {
    delegate.dataSequenceNumber(sequenceNumber);
    return this;
  }

  private void markAddedHiveFile(DataFile dataFile) {
    if (isHiveDataFile(dataFile)) {
      this.addFiles.add(dataFile);
    }
  }

  private void markDeletedHiveFile(DataFile dataFile) {
    if (isHiveDataFile(dataFile)) {
      this.deleteFiles.add(dataFile);
    }
  }

  private void markHiveFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd) {
    // Handle files to add, only handle file in hive location
    filesToAdd.forEach(this::markAddedHiveFile);

    // Handle files to delete, only handle file in hive location
    filesToDelete.forEach(this::markDeletedHiveFile);
  }

  @Override
  public RewriteFiles validateFromSnapshot(long snapshotId) {
    delegate.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  protected void postHiveDataCommitted(List<DataFile> committedDataFile) {
    committedDataFile.forEach(delegate::addFile);
  }

  @Override
  protected RewriteFiles self() {
    return this;
  }
}
