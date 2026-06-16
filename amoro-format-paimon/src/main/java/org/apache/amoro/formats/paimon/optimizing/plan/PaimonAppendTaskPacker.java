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
import org.apache.paimon.utils.BinPacking;
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
        addTask(tasks, evaluation, bin);
        bin = new ArrayList<>();
        total = 0L;
      }
    }
    if (!bin.isEmpty()) {
      addTask(tasks, evaluation, bin);
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
        addTask(tasks, evaluation, bin);
        bin = new ArrayList<>();
        total = 0L;
      }
    }
    if (bin.size() >= context.minorMinFileNum()
        || (bin.size() > 1 && context.reachMinorInterval())) {
      addTask(tasks, evaluation, bin);
    }
    return tasks;
  }

  private List<AppendCompactTask> packMajorOrFull(
      PaimonPartitionEvaluation evaluation,
      Map<String, List<PaimonFileCandidate>> dvGroups,
      List<PaimonFileCandidate> regularFiles) {
    List<AtomicUnit> units = majorOrFullUnits(dvGroups, regularFiles);
    long limit = context.effectiveTaskInputLimit();
    List<AppendCompactTask> binPacked =
        packLegalUnitsWithPaimonBinPacking(evaluation, units, limit);
    if (binPacked != null) {
      return binPacked;
    }
    MajorFullLookahead lookahead = new MajorFullLookahead(units, limit);
    List<AppendCompactTask> tasks = new ArrayList<>();
    List<AtomicUnit> bin = new ArrayList<>();
    long total = 0L;
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
            packOversizedRegular(evaluation, bin, total, unit, lookahead, i, limit);
        if (oversizedRegularPack.packed) {
          tasks.addAll(oversizedRegularPack.tasks);
          bin = oversizedRegularPack.remainingBin;
          total = oversizedRegularPack.remainingTotal;
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
          && canStartNewBin(bin, units, i, lookahead, limit)) {
        addTaskFromUnits(tasks, evaluation, bin);
        bin = new ArrayList<>();
        total = 0L;
      }
      bin.add(unit);
      total += unit.totalSize();
    }
    if (legal(bin)) {
      addTaskFromUnits(tasks, evaluation, bin);
    } else if (!bin.isEmpty()) {
      LOG.info(
          "Skip illegal Paimon compact bin for partition {} with {} file(s).",
          evaluation.partition(),
          fileCount(bin));
    }
    return tasks;
  }

  /**
   * Use Paimon's ordered bin-packing only for the semantics-equivalent case where every packable
   * atomic unit is already legal by itself (currently deletion-vector units). Regular single-file
   * units still need Amoro's partner-reservation rules below, so returning {@code null}
   * deliberately falls back to the custom planner.
   */
  private List<AppendCompactTask> packLegalUnitsWithPaimonBinPacking(
      PaimonPartitionEvaluation evaluation, List<AtomicUnit> units, long limit) {
    List<AtomicUnit> packable = new ArrayList<>();
    for (AtomicUnit unit : units) {
      if (!unit.hasDeletionVector()) {
        return null;
      }
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
      packable.add(unit);
    }

    List<AppendCompactTask> tasks = new ArrayList<>();
    for (List<AtomicUnit> bin : BinPacking.packForOrdered(packable, AtomicUnit::totalSize, limit)) {
      addTaskFromUnits(tasks, evaluation, bin);
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
      long binTotal,
      AtomicUnit unit,
      MajorFullLookahead lookahead,
      int index,
      long limit) {
    int futureOversizedRegular = lookahead.oversizedRegularAfter(index);
    int reservedPartners = Math.min(futureOversizedRegular, Math.max(0, bin.size() - 1));
    int prefixSize = bin.size() - reservedPartners - 1;
    List<AppendCompactTask> tasks = new ArrayList<>();
    long workingTotal = binTotal;
    if (prefixSize > 0) {
      List<AtomicUnit> prefix = new ArrayList<>(bin.subList(0, prefixSize));
      if (legal(prefix)) {
        addTaskFromUnits(tasks, evaluation, prefix);
        workingTotal -= totalSizeOfUnits(prefix);
        bin.subList(0, prefixSize).clear();
      }
    }

    int currentTaskSize = bin.size() - reservedPartners;
    if (currentTaskSize <= 0) {
      return PackResult.notPacked();
    }
    List<AtomicUnit> currentTask = new ArrayList<>(bin.subList(0, currentTaskSize));
    currentTask.add(unit);
    if (!legal(currentTask) || !hasNonOversizedUnit(currentTask, limit)) {
      logSkipOversizedRegular(evaluation, unit, limit);
      return PackResult.packed(tasks, bin, workingTotal);
    }
    addTaskFromUnits(tasks, evaluation, currentTask);
    long remainingTotal = workingTotal - totalSizeOfUnits(bin, 0, currentTaskSize);
    bin.subList(0, currentTaskSize).clear();
    return PackResult.packed(tasks, bin, remainingTotal);
  }

  private boolean canStartNewBin(
      List<AtomicUnit> bin,
      List<AtomicUnit> units,
      int index,
      MajorFullLookahead lookahead,
      long limit) {
    AtomicUnit unit = units.get(index);
    if (unit.hasDeletionVector()) {
      return true;
    }
    if (!lookahead.hasPackableAfter(index)) {
      return false;
    }
    if (isRegularPartner(unit, limit)) {
      int availablePartners = 1 + lookahead.regularPartnersAfter(index);
      int futureOversizedRegular = lookahead.oversizedRegularAfter(index);
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

  private long totalSizeOfUnits(List<AtomicUnit> units) {
    return totalSizeOfUnits(units, 0, units.size());
  }

  private long totalSizeOfUnits(List<AtomicUnit> units, int fromInclusive, int toExclusive) {
    long total = 0L;
    for (int i = fromInclusive; i < toExclusive; i++) {
      total += units.get(i).totalSize();
    }
    return total;
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
    if (!PaimonAppendCompactBenefit.shouldKeep(evaluation.optimizingType(), candidates, context)) {
      return null;
    }
    return new AppendCompactTask(
        evaluation.partition(),
        candidates.stream().map(PaimonFileCandidate::file).collect(Collectors.toList()));
  }

  private void addTask(
      List<AppendCompactTask> tasks,
      PaimonPartitionEvaluation evaluation,
      List<PaimonFileCandidate> candidates) {
    AppendCompactTask task = task(evaluation, candidates);
    if (task != null) {
      tasks.add(task);
    }
  }

  private void addTaskFromUnits(
      List<AppendCompactTask> tasks, PaimonPartitionEvaluation evaluation, List<AtomicUnit> units) {
    List<PaimonFileCandidate> candidates =
        units.stream().flatMap(unit -> unit.files().stream()).collect(Collectors.toList());
    addTask(tasks, evaluation, candidates);
  }

  private static class PackResult {
    private final boolean packed;
    private final List<AppendCompactTask> tasks;
    private final List<AtomicUnit> remainingBin;
    private final long remainingTotal;

    private PackResult(
        boolean packed,
        List<AppendCompactTask> tasks,
        List<AtomicUnit> remainingBin,
        long remainingTotal) {
      this.packed = packed;
      this.tasks = tasks;
      this.remainingBin = remainingBin;
      this.remainingTotal = remainingTotal;
    }

    private static PackResult packed(
        List<AppendCompactTask> tasks, List<AtomicUnit> remainingBin, long remainingTotal) {
      return new PackResult(true, tasks, remainingBin, remainingTotal);
    }

    private static PackResult notPacked() {
      return new PackResult(false, new ArrayList<>(), new ArrayList<>(), 0L);
    }
  }

  private class MajorFullLookahead {
    private final int[] oversizedRegularFrom;
    private final int[] regularPartnersFrom;
    private final boolean[] packableFrom;

    private MajorFullLookahead(List<AtomicUnit> units, long limit) {
      int size = units.size();
      this.oversizedRegularFrom = new int[size + 1];
      this.regularPartnersFrom = new int[size + 1];
      this.packableFrom = new boolean[size + 1];
      for (int i = size - 1; i >= 0; i--) {
        AtomicUnit unit = units.get(i);
        oversizedRegularFrom[i] =
            oversizedRegularFrom[i + 1] + (isOversizedRegular(unit, limit) ? 1 : 0);
        regularPartnersFrom[i] =
            regularPartnersFrom[i + 1] + (isRegularPartner(unit, limit) ? 1 : 0);
        packableFrom[i] = packableFrom[i + 1] || !shouldSkipOversizedUnit(unit, limit);
      }
    }

    private int oversizedRegularAfter(int index) {
      return oversizedRegularFrom[index + 1];
    }

    private int regularPartnersAfter(int index) {
      return regularPartnersFrom[index + 1];
    }

    private boolean hasPackableAfter(int index) {
      return packableFrom[index + 1];
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
