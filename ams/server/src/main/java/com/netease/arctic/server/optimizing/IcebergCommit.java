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

package com.netease.arctic.server.optimizing;

import com.netease.arctic.ams.api.CommitMetaProducer;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.server.ArcticServiceConstants;
import com.netease.arctic.server.exception.OptimizingCommitException;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.trace.SnapshotSummary;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.exceptions.ValidationException;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class IcebergCommit {
  private static final Logger LOG = LoggerFactory.getLogger(IcebergCommit.class);

  private final long targetSnapshotId;
  private final ArcticTable table;
  private final Collection<TaskRuntime> tasks;

  IcebergCommit(long targetSnapshotId, ArcticTable table, Collection<TaskRuntime> tasks) {
    this.targetSnapshotId = targetSnapshotId;
    this.table = table;
    this.tasks = tasks;
  }

  public void commit() throws OptimizingCommitException {
    LOG.info("{} get tasks to commit {}", table.id(), tasks);

    // collect files
    Set<DataFile> addedDataFiles = Sets.newHashSet();
    Set<DataFile> removedDataFiles = Sets.newHashSet();
    Set<DeleteFile> addedDeleteFiles = Sets.newHashSet();
    Set<DeleteFile> removedDeleteFiles = Sets.newHashSet();
    for (TaskRuntime task : tasks) {
      if (task.getOutput().getDataFiles() != null) {
        addedDataFiles.addAll(Arrays.asList(task.getOutput().getDataFiles()));
      }
      if (task.getOutput().getDeleteFiles() != null) {
        addedDeleteFiles.addAll(Arrays.asList(task.getOutput().getDeleteFiles()));
      }
      if (task.getInput().rewrittenDataFiles() != null) {
        removedDataFiles.addAll(Arrays.asList(task.getInput().rewrittenDataFiles()));
      }
      if (task.getInput().deleteFiles() != null) {
        removedDeleteFiles.addAll(Arrays.stream(task.getInput().deleteFiles())
            .map(IcebergContentFile::asDeleteFile).collect(Collectors.toSet()));
      }
    }

    UnkeyedTable icebergTable = table.asUnkeyedTable();

    replaceFiles(icebergTable, removedDataFiles, addedDataFiles, addedDeleteFiles);

    removeOldDeleteFiles(icebergTable, removedDeleteFiles);
  }

  protected void replaceFiles(
      UnkeyedTable icebergTable,
      Set<DataFile> removedDataFiles,
      Set<DataFile> addedDataFiles,
      Set<DeleteFile> addDeleteFiles) throws OptimizingCommitException {
    try {
      Transaction transaction = icebergTable.newTransaction();
      if (CollectionUtils.isNotEmpty(removedDataFiles) ||
          CollectionUtils.isNotEmpty(addedDataFiles)) {
        RewriteFiles dataFileRewrite = transaction.newRewrite();
        if (targetSnapshotId != ArcticServiceConstants.INVALID_SNAPSHOT_ID) {
          dataFileRewrite.validateFromSnapshot(targetSnapshotId);
          long sequenceNumber = table.asUnkeyedTable().snapshot(targetSnapshotId).sequenceNumber();
          dataFileRewrite.rewriteFiles(removedDataFiles, addedDataFiles, sequenceNumber);
        } else {
          dataFileRewrite.rewriteFiles(removedDataFiles, addedDataFiles);
        }
        dataFileRewrite.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
        dataFileRewrite.commit();
      }
      if (CollectionUtils.isNotEmpty(addDeleteFiles)) {
        RowDelta addDeleteFileRowDelta = transaction.newRowDelta();
        addDeleteFiles.forEach(addDeleteFileRowDelta::addDeletes);
        addDeleteFileRowDelta.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
        addDeleteFileRowDelta.commit();
      }
      transaction.commitTransaction();
    } catch (Exception e) {
      LOG.warn("Optimize commit table {} failed, give up commit.", table.id(), e);
      throw new OptimizingCommitException("unexpected commit error ", e);
    }
  }

  protected void removeOldDeleteFiles(
      UnkeyedTable icebergTable,
      Set<DeleteFile> removedDeleteFiles) {
    if (CollectionUtils.isEmpty(removedDeleteFiles)) {
      return;
    }

    RewriteFiles deleteFileRewrite = icebergTable.newRewrite();
    deleteFileRewrite.rewriteFiles(Collections.emptySet(),
        removedDeleteFiles, Collections.emptySet(), Collections.emptySet());
    deleteFileRewrite.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());

    try {
      deleteFileRewrite.commit();
    } catch (ValidationException e) {
      // Iceberg will drop DeleteFiles that are older than the min Data sequence number. So some DeleteFiles
      // maybe already dropped in the last commit, the exception can be ignored.
      LOG.warn("Iceberg RewriteFiles commit failed, but ignore", e);
    }
  }
}