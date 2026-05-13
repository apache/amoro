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
import org.apache.paimon.append.AppendCompactTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class PaimonAppendTaskPacker {

  private static final Logger LOG = LoggerFactory.getLogger(PaimonAppendTaskPacker.class);

  private final PaimonPlanContext context;

  PaimonAppendTaskPacker(PaimonPlanContext context) {
    this.context = context;
  }

  List<AppendCompactTask> pack(PaimonPartitionEvaluation evaluation) {
    if (!evaluation.necessary()) {
      return new ArrayList<>();
    }
    Map<String, List<PaimonFileCandidate>> dvGroups = new LinkedHashMap<>();
    List<PaimonFileCandidate> regularFiles = new ArrayList<>();
    splitDeletionVectorGroups(evaluation.selectedFiles(), dvGroups, regularFiles);

    List<AppendCompactTask> tasks = new ArrayList<>();
    if (evaluation.optimizingType() == OptimizingType.MINOR) {
      tasks.addAll(packDeletionVectorGroupsForMinor(evaluation, dvGroups));
      tasks.addAll(packMinor(evaluation, regularFiles));
    } else {
      tasks.addAll(packMajorOrFull(evaluation, dvGroups, regularFiles));
    }
    return tasks;
  }

  private void splitDeletionVectorGroups(
      List<PaimonFileCandidate> files,
      Map<String, List<PaimonFileCandidate>> dvGroups,
      List<PaimonFileCandidate> regularFiles) {
    for (PaimonFileCandidate file : files) {
      if (file.dvGroupKey() == null) {
        regularFiles.add(file);
      } else {
        dvGroups.computeIfAbsent(file.dvGroupKey(), ignored -> new ArrayList<>()).add(file);
      }
    }
  }

  private List<AppendCompactTask> packDeletionVectorGroupsForMinor(
      PaimonPartitionEvaluation evaluation, Map<String, List<PaimonFileCandidate>> groupedFiles) {
    List<List<PaimonFileCandidate>> groups = new ArrayList<>(groupedFiles.values());
    groups.sort(Comparator.comparingLong(this::totalFileSize));
    List<AppendCompactTask> tasks = new ArrayList<>();
    List<PaimonFileCandidate> bin = new ArrayList<>();
    long total = 0L;
    for (List<PaimonFileCandidate> group : groups) {
      for (PaimonFileCandidate file : sorted(group)) {
        bin.add(file);
        total += file.fileSize() + context.openFileCost();
      }
      if (bin.size() > 1 && total >= context.targetSize() * 2) {
        tasks.add(task(evaluation, bin));
        bin = new ArrayList<>();
        total = 0L;
      }
    }
    if (!bin.isEmpty()) {
      tasks.add(task(evaluation, bin));
    }
    return tasks;
  }

  private List<AppendCompactTask> packMinor(
      PaimonPartitionEvaluation evaluation, List<PaimonFileCandidate> input) {
    List<PaimonFileCandidate> files = sorted(input);
    List<AppendCompactTask> tasks = new ArrayList<>();
    List<PaimonFileCandidate> bin = new ArrayList<>();
    long total = 0L;
    for (PaimonFileCandidate file : files) {
      bin.add(file);
      total += file.fileSize() + context.openFileCost();
      if (bin.size() > 1 && total >= context.targetSize() * 2) {
        tasks.add(task(evaluation, bin));
        bin = new ArrayList<>();
        total = 0L;
      }
    }
    if (bin.size() >= context.minorMinFileNum()
        || (bin.size() > 1 && context.reachMinorInterval())) {
      tasks.add(task(evaluation, bin));
    }
    return tasks;
  }

  private List<AppendCompactTask> packMajorOrFull(
      PaimonPartitionEvaluation evaluation,
      Map<String, List<PaimonFileCandidate>> dvGroups,
      List<PaimonFileCandidate> regularFiles) {
    List<AtomicUnit> units = majorOrFullUnits(dvGroups, regularFiles);
    List<AppendCompactTask> tasks = new ArrayList<>();
    List<AtomicUnit> bin = new ArrayList<>();
    long total = 0L;
    long limit = context.effectiveTaskInputLimit();
    for (int i = 0; i < units.size(); i++) {
      AtomicUnit unit = units.get(i);
      if (shouldSkipOversizedUnit(unit, limit)) {
        LOG.warn(
            "Skip oversized Paimon compact atomic unit for partition {} with {} file(s), "
                + "size={} bytes, limit={} bytes.",
            evaluation.partition(),
            unit.files().size(),
            unit.totalSize(),
            limit);
        continue;
      }
      if (isOversizedRegular(unit, limit) && !bin.isEmpty()) {
        PackResult oversizedRegularPack =
            packOversizedRegular(evaluation, bin, unit, units, i, limit);
        if (oversizedRegularPack.packed) {
          tasks.addAll(oversizedRegularPack.tasks);
          bin = oversizedRegularPack.remainingBin;
          total = totalSizeOfUnits(bin);
          continue;
        }
      }
      if (isOversizedRegular(unit, limit)) {
        logSkipOversizedRegular(evaluation, unit, limit);
        continue;
      }
      if (!bin.isEmpty()
          && total + unit.totalSize() > limit
          && legal(bin)
          && canStartNewBin(bin, units, i, limit)) {
        tasks.add(taskFromUnits(evaluation, bin));
        bin = new ArrayList<>();
        total = 0L;
      }
      bin.add(unit);
      total += unit.totalSize();
    }
    if (legal(bin)) {
      tasks.add(taskFromUnits(evaluation, bin));
    } else if (!bin.isEmpty()) {
      LOG.info(
          "Skip illegal Paimon compact bin for partition {} with {} file(s).",
          evaluation.partition(),
          fileCount(bin));
    }
    return tasks;
  }

  private boolean shouldSkipOversizedUnit(AtomicUnit unit, long limit) {
    return unit.hasDeletionVector() && unit.totalSize() > limit;
  }

  private boolean isOversizedRegular(AtomicUnit unit, long limit) {
    return !unit.hasDeletionVector() && unit.totalSize() > limit;
  }

  private boolean isRegularPartner(AtomicUnit unit, long limit) {
    return !unit.hasDeletionVector() && unit.totalSize() <= limit;
  }

  private PackResult packOversizedRegular(
      PaimonPartitionEvaluation evaluation,
      List<AtomicUnit> bin,
      AtomicUnit unit,
      List<AtomicUnit> units,
      int index,
      long limit) {
    int futureOversizedRegular = countOversizedRegular(units, index + 1, limit);
    int reservedPartners = Math.min(futureOversizedRegular, Math.max(0, bin.size() - 1));
    int prefixSize = bin.size() - reservedPartners - 1;
    List<AppendCompactTask> tasks = new ArrayList<>();
    List<AtomicUnit> workingBin = new ArrayList<>(bin);
    if (prefixSize > 0) {
      List<AtomicUnit> prefix = new ArrayList<>(workingBin.subList(0, prefixSize));
      if (legal(prefix)) {
        tasks.add(taskFromUnits(evaluation, prefix));
        workingBin = new ArrayList<>(workingBin.subList(prefixSize, workingBin.size()));
      }
    }

    int currentTaskSize = workingBin.size() - reservedPartners;
    if (currentTaskSize <= 0) {
      return PackResult.notPacked();
    }
    List<AtomicUnit> currentTask = new ArrayList<>(workingBin.subList(0, currentTaskSize));
    currentTask.add(unit);
    if (!legal(currentTask) || !hasNonOversizedUnit(currentTask, limit)) {
      logSkipOversizedRegular(evaluation, unit, limit);
      return PackResult.packed(tasks, workingBin);
    }
    tasks.add(taskFromUnits(evaluation, currentTask));
    return PackResult.packed(
        tasks, new ArrayList<>(workingBin.subList(currentTaskSize, workingBin.size())));
  }

  private boolean canStartNewBin(
      List<AtomicUnit> bin, List<AtomicUnit> units, int index, long limit) {
    AtomicUnit unit = units.get(index);
    if (unit.hasDeletionVector()) {
      return true;
    }
    if (!hasPackableFutureUnit(units, index, limit)) {
      return false;
    }
    if (isRegularPartner(unit, limit)) {
      int availablePartners = 1 + countRegularPartners(units, index + 1, limit);
      int futureOversizedRegular = countOversizedRegular(units, index + 1, limit);
      if (availablePartners < futureOversizedRegular && !bin.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private boolean hasNonOversizedUnit(List<AtomicUnit> units, long limit) {
    return units.stream().anyMatch(unit -> !isOversizedRegular(unit, limit));
  }

  private void logSkipOversizedRegular(
      PaimonPartitionEvaluation evaluation, AtomicUnit unit, long limit) {
    LOG.warn(
        "Skip oversized Paimon regular compact unit for partition {} with {} file(s), "
            + "size={} bytes, limit={} bytes, because no non-oversized partner is available.",
        evaluation.partition(),
        unit.files().size(),
        unit.totalSize(),
        limit);
  }

  private int countOversizedRegular(List<AtomicUnit> units, int start, long limit) {
    int count = 0;
    for (int i = start; i < units.size(); i++) {
      if (isOversizedRegular(units.get(i), limit)) {
        count++;
      }
    }
    return count;
  }

  private int countRegularPartners(List<AtomicUnit> units, int start, long limit) {
    int count = 0;
    for (int i = start; i < units.size(); i++) {
      if (isRegularPartner(units.get(i), limit)) {
        count++;
      }
    }
    return count;
  }

  private long totalSizeOfUnits(List<AtomicUnit> units) {
    return units.stream().mapToLong(AtomicUnit::totalSize).sum();
  }

  private boolean hasPackableFutureUnit(List<AtomicUnit> units, int index, long limit) {
    for (int i = index + 1; i < units.size(); i++) {
      if (!shouldSkipOversizedUnit(units.get(i), limit)) {
        return true;
      }
    }
    return false;
  }

  private List<AtomicUnit> majorOrFullUnits(
      Map<String, List<PaimonFileCandidate>> dvGroups, List<PaimonFileCandidate> regularFiles) {
    List<AtomicUnit> units = new ArrayList<>();
    for (List<PaimonFileCandidate> dvGroup : dvGroups.values()) {
      units.add(new AtomicUnit(sorted(dvGroup)));
    }
    for (PaimonFileCandidate regularFile : regularFiles) {
      List<PaimonFileCandidate> singleFile = new ArrayList<>();
      singleFile.add(regularFile);
      units.add(new AtomicUnit(singleFile));
    }
    units.sort(Comparator.comparingLong(AtomicUnit::totalSize));
    return units;
  }

  private boolean legal(List<AtomicUnit> bin) {
    return fileCount(bin) > 1 || bin.stream().anyMatch(AtomicUnit::hasDeletionVector);
  }

  private int fileCount(List<AtomicUnit> units) {
    return units.stream().mapToInt(unit -> unit.files().size()).sum();
  }

  private long totalFileSize(List<PaimonFileCandidate> files) {
    return files.stream().mapToLong(PaimonFileCandidate::fileSize).sum();
  }

  private List<PaimonFileCandidate> sorted(List<PaimonFileCandidate> files) {
    return files.stream()
        .sorted(Comparator.comparingLong(PaimonFileCandidate::fileSize))
        .collect(Collectors.toList());
  }

  private AppendCompactTask task(
      PaimonPartitionEvaluation evaluation, List<PaimonFileCandidate> candidates) {
    return new AppendCompactTask(
        evaluation.partition(),
        candidates.stream().map(PaimonFileCandidate::file).collect(Collectors.toList()));
  }

  private AppendCompactTask taskFromUnits(
      PaimonPartitionEvaluation evaluation, List<AtomicUnit> units) {
    List<PaimonFileCandidate> candidates =
        units.stream().flatMap(unit -> unit.files().stream()).collect(Collectors.toList());
    return task(evaluation, candidates);
  }

  private static class PackResult {
    private final boolean packed;
    private final List<AppendCompactTask> tasks;
    private final List<AtomicUnit> remainingBin;

    private PackResult(
        boolean packed, List<AppendCompactTask> tasks, List<AtomicUnit> remainingBin) {
      this.packed = packed;
      this.tasks = tasks;
      this.remainingBin = remainingBin;
    }

    private static PackResult packed(List<AppendCompactTask> tasks, List<AtomicUnit> remainingBin) {
      return new PackResult(true, tasks, remainingBin);
    }

    private static PackResult notPacked() {
      return new PackResult(false, new ArrayList<>(), new ArrayList<>());
    }
  }

  private static class AtomicUnit {
    private final List<PaimonFileCandidate> files;
    private final long totalSize;
    private final boolean hasDeletionVector;

    private AtomicUnit(List<PaimonFileCandidate> files) {
      this.files = new ArrayList<>(files);
      this.totalSize = files.stream().mapToLong(PaimonFileCandidate::fileSize).sum();
      this.hasDeletionVector = files.stream().anyMatch(PaimonFileCandidate::hasDeletionVector);
    }

    private List<PaimonFileCandidate> files() {
      return files;
    }

    private long totalSize() {
      return totalSize;
    }

    private boolean hasDeletionVector() {
      return hasDeletionVector;
    }
  }
}
