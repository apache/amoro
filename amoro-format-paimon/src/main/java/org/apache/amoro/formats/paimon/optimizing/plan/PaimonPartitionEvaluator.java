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
import org.apache.paimon.data.BinaryRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

final class PaimonPartitionEvaluator {

  private final PaimonPlanContext context;

  PaimonPartitionEvaluator(PaimonPlanContext context) {
    this.context = context;
  }

  PaimonPartitionEvaluation evaluate(BinaryRow partition, List<PaimonFileCandidate> files) {
    List<PaimonFileCandidate> smallFiles =
        files.stream().filter(PaimonFileCandidate::isSmallFile).collect(Collectors.toList());
    List<PaimonFileCandidate> undersizedFiles =
        files.stream().filter(PaimonFileCandidate::isUndersizedFile).collect(Collectors.toList());
    List<PaimonFileCandidate> highDeleteFiles =
        files.stream().filter(PaimonFileCandidate::isHighDeleteRatio).collect(Collectors.toList());

    boolean hasProblem =
        !smallFiles.isEmpty() || !undersizedFiles.isEmpty() || !highDeleteFiles.isEmpty();
    if (context.reachFullInterval() && hasProblem) {
      List<PaimonFileCandidate> selected =
          context.fullRewriteAllFiles() ? new ArrayList<>(files) : problemFiles(files);
      return result(
          partition,
          OptimizingType.FULL,
          selected,
          files,
          smallFiles,
          undersizedFiles,
          highDeleteFiles);
    }

    if (!undersizedFiles.isEmpty() || !highDeleteFiles.isEmpty()) {
      List<PaimonFileCandidate> selected = problemFiles(files);
      return result(
          partition,
          OptimizingType.MAJOR,
          selected,
          files,
          smallFiles,
          undersizedFiles,
          highDeleteFiles);
    }

    if (isMinorNecessary(smallFiles)) {
      return result(
          partition,
          OptimizingType.MINOR,
          smallFiles,
          files,
          smallFiles,
          undersizedFiles,
          highDeleteFiles);
    }

    return result(
        partition, null, new ArrayList<>(), files, smallFiles, undersizedFiles, highDeleteFiles);
  }

  private boolean isMinorNecessary(List<PaimonFileCandidate> smallFiles) {
    if (smallFiles.stream().anyMatch(PaimonFileCandidate::hasDeletionVector)) {
      return true;
    }
    if (smallFiles.size() >= context.minorMinFileNum()) {
      return true;
    }
    if (smallFiles.size() > 1 && context.reachMinorInterval()) {
      return true;
    }
    long binSize = 0L;
    int count = 0;
    List<PaimonFileCandidate> sorted =
        smallFiles.stream()
            .sorted(Comparator.comparingLong(PaimonFileCandidate::fileSize))
            .collect(Collectors.toList());
    for (PaimonFileCandidate file : sorted) {
      binSize += file.fileSize() + context.openFileCost();
      count++;
      if (count > 1 && binSize >= context.targetSize() * 2) {
        return true;
      }
    }
    return false;
  }

  private List<PaimonFileCandidate> problemFiles(List<PaimonFileCandidate> files) {
    return files.stream().filter(PaimonFileCandidate::isProblemFile).collect(Collectors.toList());
  }

  private PaimonPartitionEvaluation result(
      BinaryRow partition,
      OptimizingType type,
      List<PaimonFileCandidate> selected,
      List<PaimonFileCandidate> all,
      List<PaimonFileCandidate> small,
      List<PaimonFileCandidate> undersized,
      List<PaimonFileCandidate> highDelete) {
    return new PaimonPartitionEvaluation(
        partition, type, selected, all, small.size(), undersized.size(), highDelete.size());
  }
}
