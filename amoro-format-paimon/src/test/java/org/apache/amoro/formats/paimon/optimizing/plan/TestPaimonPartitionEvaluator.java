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
import static org.junit.jupiter.api.Assertions.assertNull;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@DisplayName("PaimonPartitionEvaluator")
class TestPaimonPartitionEvaluator {

  @Test
  void evaluatesMinorForSmallFilesThatCanPack() {
    PaimonPlanContext context = context(0L, -1, false);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("a", 100, 100), file("b", 100, 100), file("c", 100, 100)));

    assertEquals(OptimizingType.MINOR, evaluation.optimizingType());
    assertEquals(3, evaluation.selectedFiles().size());
    assertTrue(evaluation.selectedFiles().stream().allMatch(PaimonFileCandidate::isSmallFile));
    assertTrue(evaluation.necessary());
  }

  @Test
  void evaluatesMinorForSingleSmallDeletionVectorFile() {
    PaimonPlanContext context = context(0L, -1, false);
    PaimonFileCandidate smallDv =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW,
            file("small-dv", 100, 100),
            context,
            new DeletionFile("dv-index", 0L, 1L, 1L),
            "dv-index");

    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context).evaluate(BinaryRow.EMPTY_ROW, Arrays.asList(smallDv));
    List<AppendCompactTask> tasks = new PaimonAppendTaskPacker(context).pack(evaluation);

    assertEquals(OptimizingType.MINOR, evaluation.optimizingType());
    assertTrue(evaluation.necessary());
    assertEquals(1, tasks.size());
    assertEquals(1, tasks.get(0).compactBefore().size());
  }

  @Test
  void evaluatesMajorForUndersizedAndIncludesSmallFiles() {
    PaimonPlanContext context = context(0L, -1, false);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("small", 100, 100), file("under", 800, 100)));

    assertEquals(OptimizingType.MAJOR, evaluation.optimizingType());
    assertEquals(2, evaluation.selectedFiles().size());
    assertEquals(1, evaluation.smallFileCount());
    assertEquals(1, evaluation.undersizedFileCount());
  }

  @Test
  void evaluatesMajorForHighDeleteRatio() {
    PaimonPlanContext context = context(0L, -1, false);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("deleted", 1200, 100, 30L), file("small", 100, 100)));

    assertEquals(OptimizingType.MAJOR, evaluation.optimizingType());
    assertTrue(
        evaluation.selectedFiles().stream().anyMatch(PaimonFileCandidate::isHighDeleteRatio));
    assertEquals(1, evaluation.highDeleteFileCount());
    assertTrue(evaluation.necessary());
  }

  @Test
  void evaluatesFullForProblemPartitionWhenFullIntervalReached() {
    PaimonPlanContext context = context(10_000L, 1, true);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("small", 100, 100), file("large", 1300, 100)));

    assertEquals(OptimizingType.FULL, evaluation.optimizingType());
    assertEquals(2, evaluation.selectedFiles().size(), "rewrite-all-files=true selects all files");
    assertEquals(2, evaluation.allFiles().size());
  }

  @Test
  void fullRewriteAllFilesFalseSelectsOnlyProblemFiles() {
    PaimonPlanContext context = context(10_000L, 1, false);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("small", 100, 100), file("large", 1300, 100)));

    assertEquals(OptimizingType.FULL, evaluation.optimizingType());
    assertEquals(1, evaluation.selectedFiles().size());
    assertEquals("small", evaluation.selectedFiles().get(0).fileName());
  }

  @Test
  void fullIntervalDoesNotTriggerCleanPartition() {
    PaimonPlanContext context = context(10_000L, 1, true);
    PaimonPartitionEvaluation evaluation =
        new PaimonPartitionEvaluator(context)
            .evaluate(
                BinaryRow.EMPTY_ROW,
                candidates(context, file("large-a", 1200, 100), file("large-b", 1300, 100)));

    assertNull(evaluation.optimizingType());
    assertFalse(evaluation.necessary());
    assertTrue(evaluation.selectedFiles().isEmpty());
  }

  private static PaimonPlanContext context(long planTime, int fullInterval, boolean rewriteAll) {
    OptimizingConfig config =
        new OptimizingConfig()
            .setMinorLeastInterval(1)
            .setFullTriggerInterval(fullInterval)
            .setFullRewriteAllFiles(rewriteAll)
            .setMaxTaskSize(4096L);
    HashMap<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    options.put("compaction.small-file-ratio", "0.7");
    options.put("compaction.min.file-num", "3");
    options.put("compaction.delete-ratio-threshold", "0.2");
    options.put("source.split.open-file-cost", "10 b");
    return PaimonPlanContext.forOptions(
        CoreOptions.fromMap(options), config, 0L, 0L, 0L, 4.0, 4096L, planTime);
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
}
