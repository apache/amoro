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

package com.netease.arctic.ams.server.optimizing;

import com.netease.arctic.ams.api.CommitMetaProducer;
import com.netease.arctic.ams.server.ArcticServiceConstants;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.trace.SnapshotSummary;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.exceptions.ValidationException;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

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

  public void commit() {
    try {
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
          removedDeleteFiles.addAll(Arrays.asList(task.getInput().deleteFiles()));
        }
      }

      UnkeyedTable icebergTable = table.asUnkeyedTable();
      Transaction transaction = icebergTable.newTransaction();

      replaceDataFiles(transaction, removedDataFiles, addedDataFiles);
      replaceDeleteFiles(transaction, removedDeleteFiles, addedDeleteFiles);

      transaction.commitTransaction();
    } catch (ValidationException e) {
      String missFileMessage = "Missing required files to delete";
      String foundNewDeleteMessage = "found new delete for replaced data file";
      String foundNewPosDeleteMessage = "found new position delete for replaced data file";
      if (e.getMessage().contains(missFileMessage) ||
          e.getMessage().contains(foundNewDeleteMessage) ||
          e.getMessage().contains(foundNewPosDeleteMessage)) {
        LOG.warn("Optimize commit table {} failed, give up commit.", table.id(), e);
      } else {
        LOG.error("unexpected commit error " + table.id(), e);
        throw new RuntimeException("unexpected commit error ", e);
      }
    } catch (Throwable t) {
      LOG.error("unexpected commit error " + table.id(), t);
      throw new RuntimeException("unexpected commit error ", t);
    }
  }

  protected void replaceDataFiles(
      Transaction transaction,
      Set<DataFile> removedDataFiles,
      Set<DataFile> addedDataFiles) {
    if (CollectionUtils.isNotEmpty(removedDataFiles) || CollectionUtils.isNotEmpty(addedDataFiles)) {
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
  }

  protected void replaceDeleteFiles(
      Transaction transaction,
      Set<DeleteFile> removedDeleteFiles,
      Set<DeleteFile> addedDeleteFiles) {
    if (CollectionUtils.isNotEmpty(removedDeleteFiles) || CollectionUtils.isNotEmpty(addedDeleteFiles)) {
      RewriteFiles deleteFileRewrite = transaction.newRewrite();

      deleteFileRewrite.rewriteFiles(Collections.emptySet(),
          removedDeleteFiles, Collections.emptySet(), addedDeleteFiles);

      deleteFileRewrite.set(SnapshotSummary.SNAPSHOT_PRODUCER, CommitMetaProducer.OPTIMIZE.name());
      deleteFileRewrite.commit();
    }
  }
}