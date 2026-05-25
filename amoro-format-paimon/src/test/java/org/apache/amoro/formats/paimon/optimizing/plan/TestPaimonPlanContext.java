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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.amoro.OptimizerProperties;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.stats.SimpleStats;
import org.apache.paimon.table.source.DeletionFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

@DisplayName("PaimonPlanContext and PaimonFileCandidate")
class TestPaimonPlanContext {

  @Test
  void mapsPaimonOptionsAsPrimaryFileSemantics() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    options.put("compaction.small-file-ratio", "0.6");
    options.put("compaction.min.file-num", "3");
    options.put("compaction.file-num-limit", "9");
    options.put("compaction.delete-ratio-threshold", "0.25");
    options.put("source.split.open-file-cost", "10 b");

    OptimizingConfig config = new OptimizingConfig().setMaxTaskSize(2048L);
    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options), config, 1_000L, 2_000L, 3_000L, 4.0, 4096L, 10_000L);

    assertEquals(1000L, context.targetSize());
    assertEquals(600L, context.smallFileBoundary());
    assertEquals(3, context.minorMinFileNum());
    assertEquals(9, context.fileNumLimit());
    assertEquals(0.25D, context.deleteRatioThreshold(), 0.0001D);
    assertEquals(10L, context.openFileCost());
    assertEquals(2048L, context.effectiveTaskInputLimit());
  }

  @Test
  void fallsBackToCurrentPaimonDefaultsWhenNewOptionsAreAbsent() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");

    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options), new OptimizingConfig(), 0L, 0L, 0L, 1.0, 0L, 10_000L);

    assertEquals(700L, context.smallFileBoundary());
    assertEquals(100_000, context.fileNumLimit());
  }

  @Test
  void usesOptimizerDefaultWhenBothTaskLimitsAreAbsent() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");

    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options),
            new OptimizingConfig().setMaxTaskSize(0L),
            0L,
            0L,
            0L,
            1.0,
            0L,
            10_000L);

    assertEquals(
        OptimizerProperties.MAX_INPUT_FILE_SIZE_PER_THREAD_DEFAULT,
        context.effectiveTaskInputLimit());
  }

  @Test
  void classifiesSmallUndersizedAndHighDeleteFallback() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    options.put("compaction.small-file-ratio", "0.7");
    options.put("compaction.delete-ratio-threshold", "0.2");
    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options),
            new OptimizingConfig().setMaxTaskSize(1024L),
            0L,
            0L,
            0L,
            1.0,
            1024L,
            10_000L);

    PaimonFileCandidate small =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW, file("small", 699L, 100L, null), context, null, null);
    PaimonFileCandidate undersized =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW, file("under", 700L, 100L, null), context, null, null);
    PaimonFileCandidate deleted =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW, file("deleted", 1200L, 100L, 30L), context, null, null);

    assertTrue(small.isSmallFile());
    assertFalse(small.isUndersizedFile());
    assertFalse(undersized.isSmallFile());
    assertTrue(undersized.isUndersizedFile());
    assertTrue(deleted.isHighDeleteRatio());
  }

  @Test
  void treatsUnknownDeletionVectorCardinalityAsHighDeleteRatio() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    options.put("compaction.small-file-ratio", "0.7");
    options.put("compaction.delete-ratio-threshold", "0.2");
    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options),
            new OptimizingConfig().setMaxTaskSize(1024L),
            0L,
            0L,
            0L,
            1.0,
            1024L,
            10_000L);

    PaimonFileCandidate deleted =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW,
            file("deleted", 1200L, 100L, null),
            context,
            new DeletionFile("dv-index", 0L, 1L, null),
            "dv-index");

    assertTrue(deleted.hasDeletionVector());
    assertTrue(deleted.isHighDeleteRatio());
  }

  @Test
  void derivesDeletionVectorGroupKeyFromDeletionFileWhenAbsent() {
    Map<String, String> options = new HashMap<>();
    options.put("target-file-size", "1000 b");
    PaimonPlanContext context =
        PaimonPlanContext.forOptions(
            CoreOptions.fromMap(options), new OptimizingConfig(), 0L, 0L, 0L, 1.0, 1024L, 10_000L);

    PaimonFileCandidate deleted =
        PaimonFileCandidate.from(
            BinaryRow.EMPTY_ROW,
            file("deleted", 1200L, 100L, null),
            context,
            new DeletionFile("dv-index", 0L, 1L, 1L),
            null);

    assertEquals("dv-index", deleted.dvGroupKey());
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
