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

package org.apache.amoro.formats.paimon.optimizing.commit;

import org.apache.amoro.exception.OptimizingCommitException;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.CommitMessageSerializer;
import org.apache.paimon.table.sink.StreamTableCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * AMS-side committer for Paimon BUCKET_UNAWARE compaction.
 *
 * <p>Deserialises every {@link CommitMessage} carried by {@link PaimonCompactionTask#getOutput()},
 * then performs a single atomic commit via Paimon's {@link
 * AppendOnlyFileStoreTable#newCommit(String)} and {@link StreamTableCommit#filterAndCommit} so
 * replaying the same {@code (commitUser, commitIdentifier)} is filtered before touching files.
 *
 * <p>The caller is expected to pass the persisted plan commit identifier from {@link
 * org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput#getCommitIdentifier()}.
 *
 * <p>Behaviour:
 *
 * <ul>
 *   <li>Empty task collection → no-op, no snapshot created.
 *   <li>Every success task must carry a {@link PaimonCompactionOutput} with non-null commit message
 *       bytes; missing bytes indicate corrupted task state and fail the commit.
 *   <li>Any runtime exception from Paimon's commit path (conflict, IO, schema drift, …) is wrapped
 *       in {@link OptimizingCommitException} so the AMS optimizer queue marks this process as
 *       failed and re-plans on the next tick.
 * </ul>
 */
public class PaimonTableCommit implements TableOptimizingCommitter {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonTableCommit.class);

  private final PaimonTable paimonTable;
  private final AppendOnlyFileStoreTable table;
  private final Collection<PaimonCompactionTask> successTasks;
  private final String commitUser;
  /**
   * Paimon commit identifier; must be monotonic per {@code commitUser} so that {@code
   * FileStoreCommitImpl.filterCommitted} can dedupe replayed commits. The caller (see {@code
   * PaimonProcessFactory.createCommitter}) validates that all success tasks carry the same
   * persisted value.
   */
  private final long commitIdentifier;

  public PaimonTableCommit(
      AppendOnlyFileStoreTable table,
      Collection<PaimonCompactionTask> successTasks,
      String commitUser,
      long commitIdentifier) {
    this(null, table, successTasks, commitUser, commitIdentifier);
  }

  public PaimonTableCommit(
      PaimonTable paimonTable,
      AppendOnlyFileStoreTable table,
      Collection<PaimonCompactionTask> successTasks,
      String commitUser,
      long commitIdentifier) {
    this.paimonTable = paimonTable;
    this.table = table;
    this.successTasks = successTasks;
    this.commitUser = commitUser;
    this.commitIdentifier = commitIdentifier;
  }

  @Override
  public void commit() throws OptimizingCommitException {
    if (successTasks == null || successTasks.isEmpty()) {
      LOG.info(
          "PaimonTableCommit: no success tasks for table={} commitUser={} — skip commit.",
          table.name(),
          commitUser);
      return;
    }

    List<CommitMessage> messages = new ArrayList<>(successTasks.size());
    CommitMessageSerializer serializer = new CommitMessageSerializer();
    for (PaimonCompactionTask task : successTasks) {
      PaimonCompactionOutput output = task.getOutput();
      if (output == null || output.getCommitMessageBytes() == null) {
        throw new OptimizingCommitException(
            "Paimon success task for partition "
                + task.getPartition()
                + " has no Paimon CommitMessage",
            /* causedByVersionMismatch */ false);
      }
      try {
        messages.add(
            serializer.deserialize(
                output.getCommitMessageVersion(), output.getCommitMessageBytes()));
      } catch (Exception e) {
        throw new OptimizingCommitException(
            "Failed to deserialize Paimon CommitMessage for partition " + task.getPartition(), e);
      }
    }
    if (messages.isEmpty()) {
      LOG.info(
          "PaimonTableCommit: empty CommitMessage list for table={} — skip commit.", table.name());
      return;
    }

    if (commitUser == null || commitUser.isEmpty()) {
      throw new OptimizingCommitException(
          "Paimon commit user must not be empty for table=" + table.name(),
          /* causedByVersionMismatch */ false);
    }
    if (commitIdentifier <= 0L) {
      throw new OptimizingCommitException(
          "Paimon commit identifier must be > 0, got "
              + commitIdentifier
              + " for table="
              + table.name(),
          /* causedByVersionMismatch */ false);
    }
    try {
      if (paimonTable == null) {
        commitMessages(messages);
      } else {
        paimonTable.doAs(
            () -> {
              commitMessages(messages);
              return null;
            });
      }
    } catch (RuntimeException e) {
      if (e.getCause() instanceof OptimizingCommitException) {
        throw (OptimizingCommitException) e.getCause();
      }
      throw e;
    }
  }

  private void commitMessages(List<CommitMessage> messages) throws OptimizingCommitException {
    try (StreamTableCommit commit = table.newCommit(commitUser)) {
      int committed = commit.filterAndCommit(Collections.singletonMap(commitIdentifier, messages));
      LOG.info(
          "PaimonTableCommit: committed {} identifier(s), {} messages for table={} "
              + "commitUser={} identifier={}",
          committed,
          messages.size(),
          table.name(),
          commitUser,
          commitIdentifier);
    } catch (RuntimeException e) {
      throw new OptimizingCommitException(
          "Paimon commit failed for table=" + table.name() + " identifier=" + commitIdentifier, e);
    } catch (Exception e) {
      throw new OptimizingCommitException(
          "Unexpected error closing Paimon commit for table=" + table.name(), e);
    }
  }
}
