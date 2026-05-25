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

package org.apache.amoro.formats.paimon.optimizing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPaimonPendingInput {

  @Test
  public void constructorAndGetters() {
    PaimonPendingInput input =
        new PaimonPendingInput(47, 2147483648L, 5600000L, 23, 536870912L, 3, 0, 0L, 51);

    assertEquals(47, input.getDataFileCount());
    assertEquals(2147483648L, input.getDataFileSize());
    assertEquals(5600000L, input.getDataRecordCount());
    assertEquals(23, input.getSmallFileCount());
    assertEquals(536870912L, input.getSmallFileSize());
    assertEquals(3, input.getPartitionCount());
    assertEquals(0, input.getFileWithDeleteCount());
    assertEquals(0L, input.getDeleteRecordCount());
    assertEquals(51, input.getHealthScore());
  }

  @Test
  public void defaultConstructorZeros() {
    PaimonPendingInput input = new PaimonPendingInput();
    assertEquals(0, input.getDataFileCount());
    assertEquals(0L, input.getDataFileSize());
    assertEquals(0, input.getHealthScore());
  }

  @Test
  public void setters() {
    PaimonPendingInput input = new PaimonPendingInput();
    input.setDataFileCount(10);
    input.setDataFileSize(1024L);
    input.setHealthScore(85);
    assertEquals(10, input.getDataFileCount());
    assertEquals(1024L, input.getDataFileSize());
    assertEquals(85, input.getHealthScore());
  }

  @Test
  public void healthScore_emptyTable_is100() {
    PaimonPendingInput input = computeHealthScoreInput(0, 0L, 0, 0L, 0, 0, new HashMap<>());
    assertEquals(100, input.getHealthScore());
  }

  @Test
  public void healthScore_noSmallFiles_is100() {
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(10, 0));
    PaimonPendingInput input =
        computeHealthScoreInput(10, 1_000L, 0, 0L, 1_000_000L, 0, partitions);
    assertEquals(100, input.getHealthScore());
  }

  @Test
  public void healthScore_halfSmallFiles_usesCountAndSizeScores() {
    // smallFileCountScore = 100 * (1 - 10 / 20) = 50
    // smallFileSizeScore = 100 * (1 - 250 / 1000) = 75
    // smallFileScore = 50 * 0.5 + 75 * 0.5 = 62.5
    // healthScore = round(62.5 * 0.6 + 100 * 0.2 + 100 * 0.2) = 78
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(20, 0));
    PaimonPendingInput input =
        computeHealthScoreInput(20, 1_000L, 10, 250L, 1_000_000L, 0, partitions);
    assertEquals(78, input.getHealthScore());
  }

  @Test
  public void healthScore_allSmallFiles_scores40WhenSmallFilesHoldAllBytes() {
    // smallFileCountScore = 0
    // smallFileSizeScore = 0
    // healthScore = 0 * 0.6 + 100 * 0.2 + 100 * 0.2 = 40
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(20, 0));
    PaimonPendingInput input =
        computeHealthScoreInput(20, 1_000L, 20, 1_000L, 1_000_000L, 0, partitions);
    assertEquals(40, input.getHealthScore());
  }

  @Test
  public void healthScore_allSmallFilesScoresHigherWhenSmallFilesHoldFewBytes() {
    // smallFileCountScore = 0
    // smallFileSizeScore = 100 * (1 - 100 / 1000) = 90
    // smallFileScore = 45
    // healthScore = round(45 * 0.6 + 100 * 0.2 + 100 * 0.2) = 67
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(20, 0));
    PaimonPendingInput input =
        computeHealthScoreInput(20, 1_000L, 20, 100L, 1_000_000L, 0, partitions);
    assertEquals(67, input.getHealthScore());
  }

  @Test
  public void healthScore_deleteScoreIsClampedWhenDeletesExceedRecords() {
    // deleteScore is clamped to 0, so the final score remains bounded.
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(10, 0));
    PaimonPendingInput input = computeHealthScoreInput(10, 1_000L, 0, 0L, 100L, 200L, partitions);
    assertEquals(80, input.getHealthScore());
  }

  @Test
  public void healthScore_smallFileScoreIsClampedWhenCountsAndSizesExceedTotals() {
    Map<Integer, List<Integer>> partitions = new HashMap<>();
    partitions.put(0, Collections.nCopies(10, 0));
    PaimonPendingInput input =
        computeHealthScoreInput(10, 1_000L, 20, 2_000L, 100L, 0L, partitions);
    assertEquals(40, input.getHealthScore());
  }

  private PaimonPendingInput computeHealthScoreInput(
      int dataFileCount,
      long dataFileSize,
      int smallFileCount,
      long smallFileSize,
      long dataRecordCount,
      long deleteRecordCount,
      Map<?, ? extends List<?>> partitionFiles) {
    int healthScore =
        PaimonPendingInput.computeHealthScore(
            dataFileCount,
            dataFileSize,
            smallFileCount,
            smallFileSize,
            dataRecordCount,
            deleteRecordCount,
            partitionFiles);
    return new PaimonPendingInput(
        dataFileCount,
        dataFileSize,
        dataRecordCount,
        smallFileCount,
        smallFileSize,
        partitionFiles.size(),
        0,
        deleteRecordCount,
        healthScore);
  }
}
