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

package org.apache.iceberg;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterables;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.iceberg.expressions.Evaluator;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.expressions.ManifestEvaluator;
import org.apache.iceberg.expressions.Projections;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.metrics.ScanMetrics;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.DateTimeUtil;
import org.apache.iceberg.util.ParallelIterable;
import org.apache.iceberg.util.PartitionSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Find Iceberg files like {@link org.apache.iceberg.FindFiles} but with more options. Supports
 * scanning delete files, partition filter and returning SpecId.
 *
 * <p>Some code is copied from {@link org.apache.iceberg.FindFiles}, {@link
 * org.apache.iceberg.ManifestReader}, {@link org.apache.iceberg.ManifestGroup}
 */
public class IcebergFindFiles {
  private static final Types.StructType EMPTY_STRUCT = Types.StructType.of();

  private final Table table;
  private final FileIO io;

  private final TableOperations ops;
  private Predicate<ManifestFile> manifestPredicate;
  private Predicate<ManifestEntry<?>> manifestEntryPredicate;
  private final Map<Integer, PartitionSpec> specsById;
  private Expression dataFilter;
  private Expression fileFilter;
  private Map<Integer, Expression> partitionFilters;
  private PartitionSet partitionSet;
  private boolean ignoreDeleted;
  private boolean ignoreExisting;
  private List<String> columns;
  private boolean caseSensitive;
  private ExecutorService executorService;
  private ScanMetrics scanMetrics;

  private Long snapshotId = null;

  private boolean includeColumnStats = false;

  private ManifestContent manifestContent;

  public IcebergFindFiles(Table table) {
    this.table = table;
    this.io = table.io();
    this.ops = ((HasTableOperations) table).operations();
    this.dataFilter = Expressions.alwaysTrue();
    this.fileFilter = Expressions.alwaysTrue();
    this.specsById = table.specs();
    this.ignoreDeleted = false;
    this.ignoreExisting = false;
    this.columns = ManifestReader.ALL_COLUMNS;
    this.caseSensitive = true;
    this.manifestPredicate = m -> true;
    this.manifestEntryPredicate = e -> true;
    this.scanMetrics = ScanMetrics.noop();
  }

  /**
   * Base results on the given snapshot.
   *
   * @param findSnapshotId a snapshot ID
   * @return this for method chaining
   */
  public IcebergFindFiles inSnapshot(long findSnapshotId) {
    Preconditions.checkArgument(
        this.snapshotId == null,
        "Cannot set snapshot multiple times, already set to id=%s",
        findSnapshotId);
    Preconditions.checkArgument(
        table.snapshot(findSnapshotId) != null, "Cannot find snapshot for id=%s", findSnapshotId);
    this.snapshotId = findSnapshotId;
    return this;
  }

  /**
   * Base results on files in the snapshot that was current as of a timestamp.
   *
   * @param timestampMillis a timestamp in milliseconds
   * @return this for method chaining
   */
  public IcebergFindFiles asOfTime(long timestampMillis) {
    Preconditions.checkArgument(
        this.snapshotId == null,
        "Cannot set snapshot multiple times, already set to id=%s",
        snapshotId);

    Long lastSnapshotId = null;
    for (HistoryEntry logEntry : ops.current().snapshotLog()) {
      if (logEntry.timestampMillis() <= timestampMillis) {
        lastSnapshotId = logEntry.snapshotId();
      } else {
        // the last snapshot ID was the last one older than the timestamp
        break;
      }
    }

    // the snapshot ID could be null if no entries were older than the requested time. in that
    // case, there is no valid snapshot to read.
    Preconditions.checkArgument(
        lastSnapshotId != null,
        "Cannot find a snapshot older than %s",
        DateTimeUtil.formatTimestampMillis(timestampMillis));
    return inSnapshot(lastSnapshotId);
  }

  public IcebergFindFiles filterData(Expression newDataFilter) {
    this.dataFilter = Expressions.and(dataFilter, newDataFilter);
    return this;
  }

  public IcebergFindFiles filterFiles(Expression newFileFilter) {
    this.fileFilter = Expressions.and(fileFilter, newFileFilter);
    return this;
  }

  public IcebergFindFiles includeColumnStats() {
    this.includeColumnStats = true;
    return this;
  }

  public IcebergFindFiles fileContent(ManifestContent manifestContent) {
    this.manifestContent = manifestContent;
    return this;
  }

  public IcebergFindFiles inPartitions(PartitionSpec spec, StructLike... partitions) {
    return inPartitions(spec, Arrays.asList(partitions));
  }

  public IcebergFindFiles inPartitions(PartitionSpec spec, List<StructLike> partitions) {
    if (partitionSet == null) {
      this.partitionSet = PartitionSet.create(specsById);
      this.partitionFilters = new HashMap<>();
    }
    for (StructLike partition : partitions) {
      partitionSet.add(spec.specId(), partition);
    }

    Expression partitionSetFilter = Expressions.alwaysFalse();
    for (StructLike partitionData : partitions) {
      Expression partFilter = Expressions.alwaysTrue();
      for (int i = 0; i < spec.fields().size(); i += 1) {
        PartitionField field = spec.fields().get(i);
        Object partitionValue = partitionData.get(i, Object.class);
        if (Objects.isNull(partitionValue)) {
          partFilter = Expressions.and(partFilter, Expressions.isNull(field.name()));
        } else {
          partFilter = Expressions.and(partFilter, Expressions.equal(field.name(), partitionValue));
        }
      }
      partitionSetFilter = Expressions.or(partitionSetFilter, partFilter);
    }

    partitionFilters.put(spec.specId(), partitionSetFilter);
    return this;
  }

  public IcebergFindFiles filterManifests(Predicate<ManifestFile> newManifestPredicate) {
    this.manifestPredicate = manifestPredicate.and(newManifestPredicate);
    return this;
  }

  public IcebergFindFiles filterManifestEntries(
      Predicate<ManifestEntry<?>> newManifestEntryPredicate) {
    this.manifestEntryPredicate = manifestEntryPredicate.and(newManifestEntryPredicate);
    return this;
  }

  public IcebergFindFiles scanMetrics(ScanMetrics metrics) {
    this.scanMetrics = metrics;
    return this;
  }

  public IcebergFindFiles ignoreDeleted() {
    this.ignoreDeleted = true;
    return this;
  }

  public IcebergFindFiles ignoreExisting() {
    this.ignoreExisting = true;
    return this;
  }

  public IcebergFindFiles select(List<String> newColumns) {
    this.columns = Lists.newArrayList(newColumns);
    return this;
  }

  public IcebergFindFiles caseSensitive(boolean newCaseSensitive) {
    this.caseSensitive = newCaseSensitive;
    return this;
  }

  public IcebergFindFiles planWith(ExecutorService newExecutorService) {
    this.executorService = newExecutorService;
    return this;
  }

  public CloseableIterable<IcebergManifestEntry> entries() {
    BiFunction<
            ManifestFile,
            CloseableIterable<ManifestEntry<?>>,
            CloseableIterable<IcebergManifestEntry>>
        entryFn =
            (manifest, entries) ->
                CloseableIterable.transform(
                    entries,
                    e ->
                        new IcebergManifestEntry(
                            e.file().copy(includeColumnStats),
                            IcebergManifestEntry.Status.parse(e.status().id()),
                            e.snapshotId()));
    if (executorService != null) {
      return new ParallelIterable<>(entries(entryFn), executorService);
    } else {
      return CloseableIterable.concat(entries(entryFn));
    }
  }

  private <T> Iterable<CloseableIterable<T>> entries(
      BiFunction<ManifestFile, CloseableIterable<ManifestEntry<?>>, CloseableIterable<T>> entryFn) {

    Snapshot snapshot =
        snapshotId != null ? ops.current().snapshot(snapshotId) : ops.current().currentSnapshot();

    // snapshot could be null when the table just gets created
    if (snapshot == null) {
      return CloseableIterable.empty();
    }

    List<ManifestFile> manifests;
    if (manifestContent == null) {
      manifests = snapshot.allManifests(io);
    } else if (manifestContent == ManifestContent.DATA) {
      manifests = snapshot.dataManifests(io);
    } else {
      manifests = snapshot.deleteManifests(io);
    }

    LoadingCache<Integer, ManifestEvaluator> evalCache =
        specsById == null
            ? null
            : Caffeine.newBuilder()
                .build(
                    specId -> {
                      PartitionSpec spec = specsById.get(specId);
                      Expression partitionFilter = partitionFilter(specId);
                      return ManifestEvaluator.forPartitionFilter(
                          Expressions.and(
                              partitionFilter,
                              Projections.inclusive(spec, caseSensitive).project(dataFilter)),
                          spec,
                          caseSensitive);
                    });

    Evaluator evaluator;
    if (fileFilter != null && fileFilter != Expressions.alwaysTrue()) {
      evaluator = new Evaluator(DataFile.getType(EMPTY_STRUCT), fileFilter, caseSensitive);
    } else {
      evaluator = null;
    }

    CloseableIterable<ManifestFile> closeableDataManifests =
        CloseableIterable.withNoopClose(manifests);
    CloseableIterable<ManifestFile> matchingManifests =
        evalCache == null
            ? closeableDataManifests
            : CloseableIterable.filter(
                scanMetrics.skippedDataManifests(),
                closeableDataManifests,
                manifest -> evalCache.get(manifest.partitionSpecId()).eval(manifest));

    if (ignoreDeleted) {
      // only scan manifests that have entries other than deletes
      // remove any manifests that don't have any existing or added files. if either the added or
      // existing files count is missing, the manifest must be scanned.
      matchingManifests =
          CloseableIterable.filter(
              scanMetrics.skippedDataManifests(),
              matchingManifests,
              manifest -> manifest.hasAddedFiles() || manifest.hasExistingFiles());
    }

    if (ignoreExisting) {
      // only scan manifests that have entries other than existing
      // remove any manifests that don't have any deleted or added files. if either the added or
      // deleted files count is missing, the manifest must be scanned.
      matchingManifests =
          CloseableIterable.filter(
              scanMetrics.skippedDataManifests(),
              matchingManifests,
              manifest -> manifest.hasAddedFiles() || manifest.hasDeletedFiles());
    }

    matchingManifests =
        CloseableIterable.filter(
            scanMetrics.skippedDataManifests(), matchingManifests, manifestPredicate);
    matchingManifests =
        CloseableIterable.count(scanMetrics.scannedDataManifests(), matchingManifests);

    return Iterables.transform(
        matchingManifests,
        manifest ->
            new CloseableIterable<T>() {
              private CloseableIterable<T> iterable;

              @Override
              public CloseableIterator<T> iterator() {
                ManifestReader<?> reader =
                    ManifestFiles.open(manifest, io, specsById)
                        .filterRows(dataFilter)
                        .filterPartitions(partitionSet)
                        .caseSensitive(caseSensitive)
                        .select(columns)
                        .scanMetrics(scanMetrics);

                CloseableIterable<? extends ManifestEntry<?>> tmpEntries;
                if (ignoreDeleted) {
                  tmpEntries = reader.liveEntries();
                } else {
                  tmpEntries = reader.entries();
                }

                CloseableIterable<ManifestEntry<?>> entries =
                    CloseableIterable.transform(tmpEntries, ManifestEntry.class::cast);

                if (ignoreExisting) {
                  entries =
                      CloseableIterable.filter(
                          scanMetrics.skippedDataFiles(),
                          entries,
                          entry -> entry.status() != ManifestEntry.Status.EXISTING);
                }

                if (evaluator != null) {
                  entries =
                      CloseableIterable.filter(
                          scanMetrics.skippedDataFiles(),
                          entries,
                          entry -> evaluator.eval((GenericDataFile) entry.file()));
                }

                iterable = entryFn.apply(manifest, entries);

                return iterable.iterator();
              }

              @Override
              public void close() throws IOException {
                if (iterable != null) {
                  iterable.close();
                }
              }
            });
  }

  private Expression partitionFilter(Integer specId) {
    if (partitionFilters == null || partitionFilters.isEmpty()) {
      return Expressions.alwaysTrue();
    }

    Expression filter = partitionFilters.get(specId);
    return filter == null ? Expressions.alwaysFalse() : filter;
  }

  public static class IcebergManifestEntry {

    private final ContentFile<?> file;

    private final Status status;

    private final long SnapshotId;

    public IcebergManifestEntry(ContentFile<?> file, Status status, long snapshotId) {
      this.file = file;
      this.status = status;
      SnapshotId = snapshotId;
    }

    public ContentFile<?> getFile() {
      return file;
    }

    public Status getStatus() {
      return status;
    }

    public long getSnapshotId() {
      return SnapshotId;
    }

    enum Status {
      EXISTING(0),
      ADDED(1),
      DELETED(2);

      private final int id;

      Status(int id) {
        this.id = id;
      }

      public int id() {
        return id;
      }

      public static Status parse(int id) {
        switch (id) {
          case 0:
            return EXISTING;
          case 1:
            return ADDED;
          case 2:
            return DELETED;
          default:
            throw new IllegalArgumentException("Unknown status id: " + id);
        }
      }
    }
  }
}
