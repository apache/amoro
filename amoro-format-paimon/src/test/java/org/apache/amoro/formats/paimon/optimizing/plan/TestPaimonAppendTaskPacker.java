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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.append.AppendCompactTask;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.stats.SimpleStats;
import org.apache.paimon.table.source.DeletionFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@DisplayName("PaimonAppendTaskPacker")
class TestPaimonAppendTaskPacker {

  @Test
  void minorPackingMatchesFileBinMinFileNum() {
    PaimonPlanContext context = context(4096L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MINOR,
            candidates(context, file("a", 100, 100), file("b", 100, 100), file("c", 100, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(3, tasks.get(0).compactBefore().size());
  }

  @Test
  void minorPackingMatchesFileBinEnoughContent() {
    PaimonPlanContext context = context(4096L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MINOR, candidates(context, file("a", 995, 100), file("b", 995, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void minorPackingUsesIntervalFallbackForTrailingRegularFiles() {
    PaimonPlanContext context = context(4096L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MINOR, candidates(context, file("a", 100, 100), file("b", 100, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void majorPackingSplitsByInputLimit() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MAJOR,
            candidates(
                context,
                file("a", 400, 100),
                file("b", 400, 100),
                file("c", 400, 100),
                file("d", 400, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(2, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
    assertEquals(2, tasks.get(1).compactBefore().size());
  }

  @Test
  void majorPackingEmitsOverLimitMinimumRegularBin() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MAJOR, candidates(context, file("a", 500, 100), file("b", 500, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void majorPackingSkipsPureUndersizedNoBenefitBin() {
    PaimonPlanContext context = context(4096L);
    // Keep this regression at DataFileMeta level because exact Parquet output sizes vary.
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MAJOR,
            candidates(context, file("near-target", 990, 100), file("tail", 300, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertTrue(tasks.isEmpty());
  }

  @Test
  void majorPackingKeepsPureSizeBasedReducingBin() {
    PaimonPlanContext context = context(4096L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MAJOR,
            candidates(context, file("left", 400, 100), file("right", 450, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void majorPackingKeepsHighDeleteNoBenefitBin() {
    PaimonPlanContext context = context(4096L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.MAJOR,
            candidates(context, file("deleted", 1200, 100, 30L), file("tail", 300, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void fullPackingKeepsEarlierLegalBinWhenLastRegularNeedsPartner() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.FULL,
            candidates(
                context,
                file("a", 100, 100),
                file("b", 100, 100),
                file("c", 100, 100),
                file("large", 10_000, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(2, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
    assertEquals(2, tasks.get(1).compactBefore().size());
  }

  @Test
  void fullPackingPairsMultipleOverLimitRegularFilesWithReservedPartners() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.FULL,
            candidates(
                context,
                file("a", 100, 100),
                file("b", 100, 100),
                file("c", 100, 100),
                file("d", 100, 100),
                file("large-1", 10_000, 100),
                file("large-2", 10_000, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(3, tasks.size());
    for (AppendCompactTask task : tasks) {
      assertEquals(2, task.compactBefore().size());
      assertTrue(task.compactBefore().stream().filter(file -> file.fileSize() > 900L).count() <= 1);
    }
    assertEquals(
        new HashSet<>(Arrays.asList("a", "b", "c", "d", "large-1", "large-2")), fileNames(tasks));
  }

  @Test
  void fullPackingSkipsUnpairedOverLimitRegularFiles() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.FULL,
            candidates(context, file("large-1", 10_000, 100), file("large-2", 10_000, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertTrue(tasks.isEmpty());
  }

  @Test
  void fullPackingDoesNotPairOverLimitRegularFilesTogetherWhenPartnersAreInsufficient() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(
            OptimizingType.FULL,
            candidates(
                context,
                file("small", 100, 100),
                file("large-1", 10_000, 100),
                file("large-2", 10_000, 100),
                file("large-3", 10_000, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
    assertEquals(
        1, tasks.get(0).compactBefore().stream().filter(file -> file.fileSize() > 900L).count());
    assertEquals(new HashSet<>(Arrays.asList("small", "large-1")), fileNames(tasks));
  }

  @Test
  void nonDvSingleFileIsSkipped() {
    PaimonPlanContext context = context(900L);
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, candidates(context, file("a", 800, 100)));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertTrue(tasks.isEmpty());
  }

  @Test
  void dvSingleFileIsLegal() {
    PaimonPlanContext context = context(900L);
    PaimonFileCandidate dv =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv", 800, 100))
            .withDvGroupKey("dv-index")
            .create();
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, Collections.singletonList(dv));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(1, tasks.get(0).compactBefore().size());
  }

  @Test
  void dvGroupIsNotSplitByInputLimit() {
    PaimonPlanContext context = context(900L);
    PaimonFileCandidate left =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-left", 400, 100))
            .withDvGroupKey("shared-dv-index")
            .create();
    PaimonFileCandidate right =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-right", 400, 100))
            .withDvGroupKey("shared-dv-index")
            .create();
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, Arrays.asList(left, right));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
    assertEquals(new HashSet<>(Arrays.asList("dv-left", "dv-right")), fileNames(tasks));
  }

  @Test
  void smallDvGroupsCanShareOneTask() {
    PaimonPlanContext context = context(900L);
    PaimonFileCandidate first =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-first", 100, 100))
            .withDvGroupKey("dv-index-1")
            .create();
    PaimonFileCandidate second =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-second", 100, 100))
            .withDvGroupKey("dv-index-2")
            .create();
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, Arrays.asList(first, second));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void majorPackingSkipsOversizedDvGroup() {
    PaimonPlanContext context = context(900L);
    PaimonFileCandidate left =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-left", 600, 100))
            .withDvGroupKey("oversized-dv-index")
            .create();
    PaimonFileCandidate right =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv-right", 600, 100))
            .withDvGroupKey("oversized-dv-index")
            .create();
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, Arrays.asList(left, right));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertTrue(tasks.isEmpty());
  }

  @Test
  void majorPackingMergesDvAtomicUnitWithRegularFile() {
    PaimonPlanContext context = context(900L);
    PaimonFileCandidate dv =
        new PaimonFileCandidateForTestFactory()
            .withContext(context)
            .withFile(file("dv", 300, 100))
            .withDvGroupKey("dv-index")
            .create();
    PaimonFileCandidate regular =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW, file("regular", 300, 100), context, null, null);
    PaimonPartitionEvaluation evaluation =
        evaluation(OptimizingType.MAJOR, Arrays.asList(dv, regular));

    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(1, tasks.size());
    assertEquals(2, tasks.get(0).compactBefore().size());
  }

  @Test
  void fullPackingKeepsLargePartnerReservationLinearForManyUnits() {
    PaimonPlanContext context = context(900L);
    List<PaimonFileCandidate> files = new ArrayList<>();
    for (int i = 0; i < 30_000; i++) {
      files.addAll(candidates(context, file("small-" + i, 100, 100)));
    }
    for (int i = 0; i < 30_000; i++) {
      files.addAll(candidates(context, file("large-" + i, 10_000, 100)));
    }
    PaimonPartitionEvaluation evaluation = evaluation(OptimizingType.FULL, files);

    List<AppendCompactTask> tasks =
        assertTimeoutPreemptively(
            Duration.ofSeconds(5), () -> new PaimonAppendTaskPacker(context).pack(evaluation));

    assertFalse(tasks.isEmpty());
    for (AppendCompactTask task : tasks) {
      assertTrue(task.compactBefore().size() > 1);
      assertTrue(task.compactBefore().stream().filter(file -> file.fileSize() > 900L).count() <= 1);
    }
  }

  private static PaimonPlanContext context(long taskLimit) {
    OptimizingConfig config =
        new OptimizingConfig()
            .setMinorLeastInterval(1)
            .setFullTriggerInterval(-1)
            .setFullRewriteAllFiles(false)
            .setMaxTaskSize(taskLimit);
    HashMap<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    options.put("compaction.small-file-ratio", "0.7");
    options.put("compaction.min.file-num", "3");
    options.put("source.split.open-file-cost", "10 b");
    return PaimonPlanContext.forOptions(
        CoreOptions.fromMap(options), config, 0L, 0L, 0L, 4.0, taskLimit, 10_000L);
  }

  private static PaimonPartitionEvaluation evaluation(
      OptimizingType type, List<PaimonFileCandidate> files) {
    return new PaimonPartitionEvaluation(
        BinaryRow.EMPTY_ROW, type, files, files, files.size(), 0, 0);
  }

  private static List<PaimonFileCandidate> candidates(
      PaimonPlanContext context, DataFileMeta... files) {
    return Arrays.stream(files)
        .map(file -> PaimonFileCandidate.from(BinaryRow.EMPTY_ROW, file, context, null, null))
        .collect(Collectors.toList());
  }

  private static DataFileMeta file(String name, long size, long rows) {
    return file(name, size, rows, null);
  }

  private static DataFileMeta file(String name, long size, long rows, Long deleteRows) {
    return DataFileMeta.create(
        name,
        size,
        rows,
        DataFileMeta.EMPTY_MIN_KEY,
        DataFileMeta.EMPTY_MAX_KEY,
        SimpleStats.EMPTY_STATS,
        SimpleStats.EMPTY_STATS,
        0L,
        0L,
        0L,
        DataFileMeta.DUMMY_LEVEL,
        deleteRows,
        null,
        null,
        null,
        null,
        null);
  }

  private static Set<String> fileNames(List<AppendCompactTask> tasks) {
    return tasks.stream()
        .flatMap(task -> task.compactBefore().stream())
        .map(DataFileMeta::fileName)
        .collect(Collectors.toSet());
  }

  private static class PaimonFileCandidateForTestFactory {
    private PaimonPlanContext context;
    private DataFileMeta file;
    private String dvGroupKey;

    PaimonFileCandidateForTestFactory withContext(PaimonPlanContext context) {
      this.context = context;
      return this;
    }

    PaimonFileCandidateForTestFactory withFile(DataFileMeta file) {
      this.file = file;
      return this;
    }

    PaimonFileCandidateForTestFactory withDvGroupKey(String dvGroupKey) {
      this.dvGroupKey = dvGroupKey;
      return this;
    }

    PaimonFileCandidate create() {
      return PaimonFileCandidate.from(
          BinaryRow.EMPTY_ROW, file, context, new DeletionFile(dvGroupKey, 0L, 1L, 1L), dvGroupKey);
    }
  }
}
