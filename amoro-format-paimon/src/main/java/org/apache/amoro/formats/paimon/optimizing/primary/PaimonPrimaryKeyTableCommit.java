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

package org.apache.amoro.formats.paimon.optimizing.primary;

import org.apache.amoro.exception.OptimizingCommitException;
import org.apache.amoro.formats.paimon.PaimonTable;
import org.apache.amoro.optimizing.TableOptimizingCommitter;
import org.apache.paimon.table.FileStoreTable;
import org.apache.paimon.table.sink.BatchTableCommit;
import org.apache.paimon.table.sink.CommitMessage;
import org.apache.paimon.table.sink.CommitMessageSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaimonPrimaryKeyTableCommit implements TableOptimizingCommitter {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonPrimaryKeyTableCommit.class);

  private final PaimonTable paimonTable;
  private final FileStoreTable table;
  private final Collection<PaimonPrimaryKeyCompactionTask> successTasks;

  public PaimonPrimaryKeyTableCommit(
      FileStoreTable table, Collection<PaimonPrimaryKeyCompactionTask> successTasks) {
    this(null, table, successTasks);
  }

  public PaimonPrimaryKeyTableCommit(
      PaimonTable paimonTable,
      FileStoreTable table,
      Collection<PaimonPrimaryKeyCompactionTask> successTasks) {
    this.paimonTable = paimonTable;
    this.table = table;
    this.successTasks = successTasks;
  }

  @Override
  public void commit() throws OptimizingCommitException {
    if (successTasks == null || successTasks.isEmpty()) {
      LOG.info("PaimonPrimaryKeyTableCommit: no success tasks for table={} - skip commit.", name());
      return;
    }

    List<CommitMessage> messages = collectCommitMessages();
    if (messages.isEmpty()) {
      throw new OptimizingCommitException(
          "Paimon primary-key commit has empty CommitMessage list for table=" + name(), false);
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
      throw new OptimizingCommitException(
          "Paimon primary-key commit failed for table=" + name(), e);
    }
  }

  private List<CommitMessage> collectCommitMessages() throws OptimizingCommitException {
    List<CommitMessage> messages = new ArrayList<>();
    for (PaimonPrimaryKeyCompactionTask task : successTasks) {
      PaimonPrimaryKeyCompactionOutput output = task == null ? null : task.getOutput();
      if (output == null) {
        throw new OptimizingCommitException(
            "Paimon primary-key success task for partition "
                + partition(task)
                + " has no Paimon CommitMessage list",
            false);
      }
      List<byte[]> bytesList = output.getCommitMessageBytesList();
      if (bytesList.isEmpty()) {
        throw new OptimizingCommitException(
            "Paimon primary-key success task for partition "
                + partition(task)
                + " has empty CommitMessage list",
            false);
      }
      try {
        messages.addAll(CommitMessageSerializer.deserializeAll(bytesList));
      } catch (Exception e) {
        throw new OptimizingCommitException(
            "Failed to deserialize Paimon primary-key CommitMessage list for partition "
                + partition(task),
            e);
      }
    }
    return messages;
  }

  private void commitMessages(List<CommitMessage> messages) throws OptimizingCommitException {
    try (BatchTableCommit commit = table.newBatchWriteBuilder().newCommit()) {
      commit.commit(messages);
      LOG.info(
          "PaimonPrimaryKeyTableCommit: committed {} messages for table={}",
          messages.size(),
          name());
    } catch (Exception e) {
      throw new OptimizingCommitException(
          "Paimon primary-key commit failed for table=" + name(), e);
    }
  }

  private String name() {
    return table == null ? "<unknown>" : table.name();
  }

  private static String partition(PaimonPrimaryKeyCompactionTask task) {
    return task == null ? "<null-task>" : task.getPartition();
  }
}
