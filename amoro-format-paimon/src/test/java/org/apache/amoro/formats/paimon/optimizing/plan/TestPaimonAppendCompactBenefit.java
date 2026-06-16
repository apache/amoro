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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.stats.SimpleStats;
import org.apache.paimon.table.source.DeletionFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@DisplayName("PaimonAppendCompactBenefit")
class TestPaimonAppendCompactBenefit {

  @Test
  void skipsPureMajorWhenEstimatedOutputFileCountDoesNotDrop() {
    PaimonPlanContext context = context("1000 b");
    List<PaimonFileCandidate> files =
        Arrays.asList(
            candidate(context, file("near-target", 990L, 100L, null), null),
            candidate(context, file("tail", 300L, 100L, null), null));

    assertFalse(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, files, context));
  }

  @Test
  void keepsPureMajorWhenEstimatedOutputFileCountDrops() {
    PaimonPlanContext context = context("1000 b");
    List<PaimonFileCandidate> files =
        Arrays.asList(
            candidate(context, file("left", 400L, 100L, null), null),
            candidate(context, file("right", 450L, 100L, null), null));

    assertTrue(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, files, context));
  }

  @Test
  void keepsHighDeleteMajorEvenWithoutFileCountReduction() {
    PaimonPlanContext context = context("1000 b");
    List<PaimonFileCandidate> files =
        Arrays.asList(
            candidate(context, file("deleted", 1200L, 100L, 30L), null),
            candidate(context, file("partner", 100L, 100L, null), null));

    assertTrue(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, files, context));
  }

  @Test
  void keepsDeletionVectorMajorEvenForSingleFile() {
    PaimonPlanContext context = context("1000 b");
    PaimonFileCandidate file =
        candidate(context, file("dv", 990L, 100L, null), new DeletionFile("dv-index", 0L, 1L, 1L));

    assertTrue(
        PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, Arrays.asList(file), context));
  }

  @Test
  void doesNotFilterMinorOrFull() {
    PaimonPlanContext context = context("1000 b");
    List<PaimonFileCandidate> files =
        Arrays.asList(
            candidate(context, file("near-target", 990L, 100L, null), null),
            candidate(context, file("tail", 300L, 100L, null), null));

    assertTrue(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MINOR, files, context));
    assertTrue(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.FULL, files, context));
  }

  @Test
  void skipsEmptyMajorCandidates() {
    PaimonPlanContext context = context("1000 b");

    assertFalse(
        PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, Arrays.asList(), context));
  }

  @Test
  void keepsMajorWhenTargetSizeIsInvalid() {
    PaimonPlanContext context = context("0 b");
    List<PaimonFileCandidate> files =
        Arrays.asList(
            candidate(context, file("left", 400L, 100L, null), null),
            candidate(context, file("right", 450L, 100L, null), null));

    assertTrue(PaimonAppendCompactBenefit.shouldKeep(OptimizingType.MAJOR, files, context));
  }

  private static PaimonPlanContext context(String targetFileSize) {
    HashMap<String, String> options = new HashMap<>();
    options.put("target-file-size", targetFileSize);
    options.put("compaction.small-file-ratio", "0.7");
    options.put("compaction.min.file-num", "3");
    options.put("compaction.delete-ratio-threshold", "0.2");
    options.put("source.split.open-file-cost", "10 b");
    return PaimonPlanContext.forOptions(
        CoreOptions.fromMap(options),
        new OptimizingConfig().setMaxTaskSize(4096L),
        0L,
        0L,
        0L,
        4.0,
        4096L,
        10_000L);
  }

  private static PaimonFileCandidate candidate(
      PaimonPlanContext context, DataFileMeta file, DeletionFile deletionFile) {
    return PaimonFileCandidate.from(
        BinaryRow.EMPTY_ROW,
        file,
        context,
        deletionFile,
        deletionFile == null ? null : deletionFile.path());
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
