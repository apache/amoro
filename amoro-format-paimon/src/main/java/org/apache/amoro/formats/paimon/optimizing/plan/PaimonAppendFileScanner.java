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

package org.apache.amoro.formats.paimon.optimizing.plan;

import org.apache.paimon.Snapshot;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.deletionvectors.append.AppendDeleteFileMaintainer;
import org.apache.paimon.deletionvectors.append.BaseAppendDeleteFileMaintainer;
import org.apache.paimon.index.IndexFileHandler;
import org.apache.paimon.manifest.FileKind;
import org.apache.paimon.manifest.ManifestEntry;
import org.apache.paimon.operation.FileStoreScan;
import org.apache.paimon.partition.PartitionPredicate;
import org.apache.paimon.predicate.Predicate;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.BucketMode;
import org.apache.paimon.table.source.DeletionFile;
import org.apache.paimon.table.source.ScanMode;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class PaimonAppendFileScanner {

  private final AppendOnlyFileStoreTable table;
  private final PaimonPlanContext context;
  @Nullable private final Predicate partitionFilter;
  private final IndexFileHandler indexFileHandler;

  PaimonAppendFileScanner(
      AppendOnlyFileStoreTable table,
      PaimonPlanContext context,
      @Nullable Predicate partitionFilter) {
    this.table = table;
    this.context = context;
    this.partitionFilter = partitionFilter;
    this.indexFileHandler = table.store().newIndexFileHandler();
  }

  ScanResult scan() {
    if (table.bucketMode() != BucketMode.BUCKET_UNAWARE) {
      return ScanResult.empty();
    }
    Snapshot snapshot = table.snapshotManager().latestSnapshot();
    if (snapshot == null) {
      return ScanResult.empty();
    }

    Map<BinaryRow, AppendDeleteFileMaintainer> dvMaintainers = new ConcurrentHashMap<>();
    Map<BinaryRow, List<PaimonFileCandidate>> candidates =
        scanCandidateFiles(snapshot, dvMaintainers);
    if (candidates.isEmpty()) {
      return new ScanResult(snapshot.id(), candidates);
    }
    if (context.reachFullInterval() && context.fullRewriteAllFiles()) {
      return new ScanResult(
          snapshot.id(), scanAllFilesInPartitions(snapshot, candidates, dvMaintainers));
    }
    return new ScanResult(snapshot.id(), candidates);
  }

  private Map<BinaryRow, List<PaimonFileCandidate>> scanCandidateFiles(
      Snapshot snapshot, Map<BinaryRow, AppendDeleteFileMaintainer> dvMaintainers) {
    Map<BinaryRow, List<PaimonFileCandidate>> candidates = new LinkedHashMap<>();
    Iterator<ManifestEntry> iterator = addFileIterator(snapshot, null);
    int compactCandidateFiles = 0;
    while (iterator.hasNext() && compactCandidateFiles < context.fileNumLimit()) {
      ManifestEntry entry = iterator.next();
      if (entry.kind() != FileKind.ADD) {
        continue;
      }
      PaimonFileCandidate candidate = candidate(entry, snapshot, dvMaintainers);
      if (!candidate.isProblemFile()) {
        continue;
      }
      compactCandidateFiles++;
      candidates
          .computeIfAbsent(candidate.partition(), ignored -> new ArrayList<>())
          .add(candidate);
    }
    return candidates;
  }

  private Map<BinaryRow, List<PaimonFileCandidate>> scanAllFilesInPartitions(
      Snapshot snapshot,
      Map<BinaryRow, List<PaimonFileCandidate>> candidateFiles,
      Map<BinaryRow, AppendDeleteFileMaintainer> dvMaintainers) {
    Map<BinaryRow, List<PaimonFileCandidate>> allFiles = new LinkedHashMap<>();
    Iterator<ManifestEntry> iterator =
        addFileIterator(snapshot, new ArrayList<>(candidateFiles.keySet()));
    while (iterator.hasNext()) {
      ManifestEntry entry = iterator.next();
      if (entry.kind() != FileKind.ADD) {
        continue;
      }
      PaimonFileCandidate candidate = candidate(entry, snapshot, dvMaintainers);
      allFiles.computeIfAbsent(candidate.partition(), ignored -> new ArrayList<>()).add(candidate);
    }
    return allFiles;
  }

  private Iterator<ManifestEntry> addFileIterator(
      Snapshot snapshot, @Nullable List<BinaryRow> partitions) {
    FileStoreScan scan = table.store().newScan().withSnapshot(snapshot).withKind(ScanMode.ALL);
    if (partitions != null) {
      scan.withPartitionFilter(partitions);
    } else if (partitionFilter != null) {
      PartitionPredicate predicate =
          PartitionPredicate.fromPredicate(table.schema().logicalPartitionType(), partitionFilter);
      scan.withPartitionFilter(predicate);
    }
    if (context.coreOptions().manifestDeleteFileDropStats()) {
      scan.dropStats();
    }
    return scan.readFileIterator();
  }

  private PaimonFileCandidate candidate(
      ManifestEntry entry,
      Snapshot snapshot,
      Map<BinaryRow, AppendDeleteFileMaintainer> dvMaintainers) {
    BinaryRow partition = entry.partition().copy();
    DeletionFile deletionFile =
        deletionFile(partition, entry.file().fileName(), snapshot, dvMaintainers);
    String dvGroupKey = deletionFile == null ? null : deletionFile.path();
    return PaimonFileCandidate.from(partition, entry.file(), context, deletionFile, dvGroupKey);
  }

  @Nullable
  private DeletionFile deletionFile(
      BinaryRow partition,
      String fileName,
      Snapshot snapshot,
      Map<BinaryRow, AppendDeleteFileMaintainer> dvMaintainers) {
    if (!context.coreOptions().deletionVectorsEnabled()) {
      return null;
    }
    return dvMaintainers
        .computeIfAbsent(
            partition,
            p -> BaseAppendDeleteFileMaintainer.forUnawareAppend(indexFileHandler, snapshot, p))
        .getDeletionFile(fileName);
  }

  static final class ScanResult {
    private final long snapshotId;
    private final Map<BinaryRow, List<PaimonFileCandidate>> files;

    private ScanResult(long snapshotId, Map<BinaryRow, List<PaimonFileCandidate>> files) {
      this.snapshotId = snapshotId;
      this.files = files;
    }

    private static ScanResult empty() {
      return new ScanResult(-1L, new LinkedHashMap<>());
    }

    long snapshotId() {
      return snapshotId;
    }

    Map<BinaryRow, List<PaimonFileCandidate>> files() {
      return files;
    }
  }
}
