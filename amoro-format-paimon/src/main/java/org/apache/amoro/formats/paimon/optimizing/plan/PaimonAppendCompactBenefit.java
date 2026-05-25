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

import org.apache.amoro.optimizing.OptimizingType;

import java.util.List;

final class PaimonAppendCompactBenefit {

  private PaimonAppendCompactBenefit() {}

  static boolean shouldKeep(
      OptimizingType optimizingType,
      List<PaimonFileCandidate> candidates,
      PaimonPlanContext context) {
    if (optimizingType != OptimizingType.MAJOR) {
      return true;
    }
    if (candidates == null || candidates.isEmpty()) {
      return false;
    }
    if (candidates.stream()
        .anyMatch(file -> file.hasDeletionVector() || file.isHighDeleteRatio())) {
      return true;
    }

    int inputFileCount = candidates.size();
    if (inputFileCount <= 1) {
      return false;
    }

    long totalSize = 0L;
    for (PaimonFileCandidate candidate : candidates) {
      long fileSize = candidate.fileSize();
      if (fileSize < 0 || Long.MAX_VALUE - totalSize < fileSize) {
        return true;
      }
      totalSize += fileSize;
    }

    long targetSize = context.targetSize();
    if (targetSize <= 0L) {
      return true;
    }

    long estimatedOutputFiles = totalSize / targetSize + (totalSize % targetSize == 0L ? 0L : 1L);
    return Math.max(1L, estimatedOutputFiles) < inputFileCount;
  }
}
