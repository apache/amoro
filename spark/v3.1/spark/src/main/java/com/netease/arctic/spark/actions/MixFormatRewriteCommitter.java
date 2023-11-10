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

package com.netease.arctic.spark.actions;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.data.file.FileNameGenerator;
import com.netease.arctic.spark.actions.optimizing.TableFileScanHelper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.exceptions.ValidationException;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableSet;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MixFormatRewriteCommitter implements RewriteCommitter {
  private static final Logger LOG = LoggerFactory.getLogger(MixFormatRewriteCommitter.class);
  protected Set<String> needRewritePartition;
  protected Set<StructLike> allPartition = Sets.newHashSet();
  protected CloseableIterable<TableFileScanHelper.FileScanResult> rewriteFiles;
  long snapshotId;
  protected long maxTransactionId;
  protected final Set<DataFile> addDatafiles = Sets.newHashSet();
  protected final Set<DataFile> removeDataFile = Sets.newHashSet();
  protected final Set<DeleteFile> removeDeleteFile = Sets.newHashSet();
  protected final ArcticTable table;

  public MixFormatRewriteCommitter(
      ArcticTable table,
      Set<String> needRewritePartition,
      CloseableIterable<TableFileScanHelper.FileScanResult> rewriteFiles, long snapshotId) {
    this.table = table;
    this.needRewritePartition = needRewritePartition;
    this.rewriteFiles = rewriteFiles;
    this.snapshotId = snapshotId;
  }

  @Override
  public RewriteCommitter addFiles(Set<DataFile> addDatafiles) {
    this.addDatafiles.addAll(addDatafiles);
    this.maxTransactionId = Math.max(this.maxTransactionId, addDatafiles.stream()
        .mapToLong(dataFile -> FileNameGenerator.parseTransactionId(dataFile.path().toString()))
        .max()
        .orElse(0L));
    return this;
  }

  @Override
  public void apply() {
    addDatafiles.forEach(e -> allPartition.add(e.partition()));
    rewriteFiles.forEach(fileScanResult -> {
      PrimaryKeyedFile file = (PrimaryKeyedFile) fileScanResult.file();
      if (file.type() == DataFileType.BASE_FILE) {
        removeDataFile.add(fileScanResult.file());
      }
      fileScanResult.deleteFiles().forEach(deleteFile -> {
        if (deleteFile.content() == FileContent.POSITION_DELETES) {
          removeDeleteFile.add((DeleteFile) deleteFile);
        }
      });
    });
  }

  @Override
  public void commitOrClean() {
    try {
      apply();
      commit(table, addDatafiles);
    } catch (Exception e) {
      LOG.info("clean rewrite files");
      addDatafiles.forEach(file -> {
        try {
          table.io().deleteFile(file.path().toString());
        } catch (Exception ex) {
          LOG.error("delete file {} failed", file.path().toString(), ex);
        }
      });
      throw new RuntimeException("commit failed, clean rewrite files", e);
    }
  }

  @Override
  public BaseRewriteAction.RewriteResult result() {
    return BaseRewriteAction.RewriteResult.of(
        removeDataFile.size() + removeDeleteFile.size(),
        removeDeleteFile.size(),
        needRewritePartition.size(), 0, String.join(",", needRewritePartition),
        addDatafiles.size());
  }

  private void commit(ArcticTable arcticTable, Set<DataFile> addDatafiles) {
    UnkeyedTable table =
        arcticTable.isKeyedTable() ? arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
    Transaction tx = table.newTransaction();
    OverwriteFiles overwriteFiles = tx.newOverwrite().validateFromSnapshot(snapshotId);
    removeDataFile.forEach(file -> {
      if (((PrimaryKeyedFile) file).type() == DataFileType.BASE_FILE) {
        overwriteFiles.deleteFile(file);
      }
    });
    addDatafiles.forEach(overwriteFiles::addFile);
    overwriteFiles.commit();
    allPartition.forEach(partition -> table.updatePartitionProperties(tx).set(
        partition,
        TableProperties.PARTITION_OPTIMIZED_SEQUENCE,
        String.valueOf(maxTransactionId)).commit());
    tx.commitTransaction();
    try {
      if (!removeDeleteFile.isEmpty()) {
        table.newRewrite().validateFromSnapshot(snapshotId)
            .rewriteFiles(ImmutableSet.of(), removeDeleteFile, ImmutableSet.of(), ImmutableSet.of())
            .commit();
      }
    } catch (ValidationException e) {
      LOG.warn("Iceberg RewriteFiles commit failed, but ignore", e);
    }
  }
}
