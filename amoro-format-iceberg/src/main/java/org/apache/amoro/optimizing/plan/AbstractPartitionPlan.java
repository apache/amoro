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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.HealthScoreInfo;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.shade.guava32.com.google.common.collect.BiMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.HashBiMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.BinPacking;
import org.apache.iceberg.util.Pair;
import org.roaringbitmap.IntConsumer;
import org.roaringbitmap.RoaringBitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractPartitionPlan implements PartitionEvaluator {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractPartitionPlan.class);

  protected final Pair<Integer, StructLike> partition;
  protected final OptimizingConfig config;
  protected final ServerTableIdentifier identifier;
  protected final long lastMinorOptimizingTime;
  protected final long lastFullOptimizingTime;
  protected final long lastMajorOptimizingTime;
  private CommonPartitionEvaluator evaluator;
  private TaskSplitter taskSplitter;
  protected MixedTable tableObject;
  private Long fromSequence = null;
  private Long toSequence = null;
  protected final long planTime;

  protected final Map<DataFile, List<ContentFile<?>>> rewriteDataFiles = Maps.newHashMap();

  /**
   * Segment file size in the range (fragmentSize, minTargetSize].
   *
   * <p>For example, self-optimizing.target-size is 128m, undersized segment file is (16m, 96m].
   */
  protected final Map<DataFile, List<ContentFile<?>>> undersizedSegmentFiles = Maps.newHashMap();

  protected final Map<DataFile, List<ContentFile<?>>> rewritePosDataFiles = Maps.newHashMap();

  /**
   * BiMap mapping each delete file to a unique integer index. Used only when {@link
   * OptimizingConfig#isUseRoaringBitmapInPlanning()} is enabled, so that delete-file references can
   * be stored as compact {@link RoaringBitmap} indices instead of duplicated lists.
   */
  protected final BiMap<ContentFile<?>, Integer> deleteFileIndex = HashBiMap.create();

  /**
   * Bitmap-based counterparts of {@link #rewriteDataFiles}, {@link #undersizedSegmentFiles} and
   * {@link #rewritePosDataFiles}. When RoaringBitmap planning is enabled, delete files are stored
   * as {@link RoaringBitmap} indices here to reduce memory usage for tables with many delete files.
   */
  protected final Map<DataFile, RoaringBitmap> rewriteDataFilesBitmap = Maps.newHashMap();

  protected final Map<DataFile, RoaringBitmap> undersizedSegmentFilesBitmap = Maps.newHashMap();

  protected final Map<DataFile, RoaringBitmap> rewritePosDataFilesBitmap = Maps.newHashMap();

  /**
   * Reserved Delete files are Delete files which are related to Data files not optimized in this
   * plan.
   */
  protected final Set<String> reservedDeleteFiles = Sets.newHashSet();

  public AbstractPartitionPlan(
      ServerTableIdentifier identifier,
      MixedTable table,
      OptimizingConfig config,
      Pair<Integer, StructLike> partition,
      long planTime,
      long lastMinorOptimizingTime,
      long lastFullOptimizingTime,
      long lastMajorOptimizingTime) {
    this.identifier = identifier;
    this.partition = partition;
    this.tableObject = table;
    this.config = config;
    this.planTime = planTime;
    this.lastMinorOptimizingTime = lastMinorOptimizingTime;
    this.lastFullOptimizingTime = lastFullOptimizingTime;
    this.lastMajorOptimizingTime = lastMajorOptimizingTime;
    if (LOG.isDebugEnabled() && config.isUseRoaringBitmapInPlanning()) {
      LOG.debug(
          "RoaringBitmap is enabled in optimizing planning for table {} to reduce memory usage",
          identifier);
    }
  }

  @Override
  public Pair<Integer, StructLike> getPartition() {
    return partition;
  }

  protected CommonPartitionEvaluator evaluator() {
    if (evaluator == null) {
      evaluator = buildEvaluator();
    }
    return evaluator;
  }

  protected CommonPartitionEvaluator buildEvaluator() {
    return new CommonPartitionEvaluator(
        identifier,
        config,
        partition,
        planTime,
        lastMinorOptimizingTime,
        lastFullOptimizingTime,
        lastMajorOptimizingTime);
  }

  @Override
  public boolean isNecessary() {
    return evaluator().isNecessary();
  }

  @Override
  public OptimizingType getOptimizingType() {
    return evaluator().getOptimizingType();
  }

  @Override
  public long getCost() {
    return evaluator().getCost();
  }

  @Override
  public boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    boolean added = evaluator().addFile(dataFile, deletes);
    if (added) {
      if (evaluator().fileShouldRewrite(dataFile, deletes)) {
        putRewriteDataFile(dataFile, deletes);
      } else if (evaluator().isUndersizedSegmentFile(dataFile)) {
        putUndersizedSegmentFile(dataFile, deletes);
      } else if (evaluator().segmentShouldRewritePos(dataFile, deletes)) {
        putRewritePosDataFile(dataFile, deletes);
      } else {
        added = false;
      }
    }
    if (!added) {
      reservedDeleteFiles(deletes);
    }
    return added;
  }

  /** If the Data file is not added, it's Delete files should not be removed from iceberg */
  protected void reservedDeleteFiles(List<ContentFile<?>> deletes) {
    deletes.stream().map(delete -> delete.path().toString()).forEach(reservedDeleteFiles::add);
  }

  /**
   * Registers a data file for rewriting with its delete files. The deletes are stored either as
   * the original list or as {@link RoaringBitmap} indices, depending on {@link
   * OptimizingConfig#isUseRoaringBitmapInPlanning()}.
   */
  protected void putRewriteDataFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewriteDataFilesBitmap.put(dataFile, getIndexFromDeletes(deletes));
    } else {
      rewriteDataFiles.put(dataFile, deletes);
    }
  }

  /**
   * Registers an undersized segment file with its delete files, with the same dual-track storage
   * rule as {@link #putRewriteDataFile(DataFile, List)}.
   */
  protected void putUndersizedSegmentFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    if (config.isUseRoaringBitmapInPlanning()) {
      undersizedSegmentFilesBitmap.put(dataFile, getIndexFromDeletes(deletes));
    } else {
      undersizedSegmentFiles.put(dataFile, deletes);
    }
  }

  /**
   * Registers a data file for position-delete rewriting with its delete files, with the same
   * dual-track storage rule as {@link #putRewriteDataFile(DataFile, List)}.
   */
  protected void putRewritePosDataFile(DataFile dataFile, List<ContentFile<?>> deletes) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewritePosDataFilesBitmap.put(dataFile, getIndexFromDeletes(deletes));
    } else {
      rewritePosDataFiles.put(dataFile, deletes);
    }
  }

  /**
   * Removes rewrite data files matching the predicate from the plan, regardless of the
   * underlying storage. Subclasses must use this instead of touching the rewrite maps directly,
   * so that the RoaringBitmap-based counterparts stay consistent.
   */
  protected void removeRewriteDataFilesIf(Predicate<DataFile> predicate) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewriteDataFilesBitmap.keySet().removeIf(predicate);
    } else {
      rewriteDataFiles.keySet().removeIf(predicate);
    }
  }

  /**
   * Removes rewrite-position data files matching the predicate from the plan, regardless of the
   * underlying storage, with the same dual-track rule as {@link #removeRewriteDataFilesIf}.
   */
  protected void removeRewritePosDataFilesIf(Predicate<DataFile> predicate) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewritePosDataFilesBitmap.keySet().removeIf(predicate);
    } else {
      rewritePosDataFiles.keySet().removeIf(predicate);
    }
  }

  /**
   * Iterates over undersized segment files with their delete files. When RoaringBitmap planning
   * is enabled, the bitmap indices are converted back to delete files for the consumer.
   */
  protected void forEachUndersizedSegmentFile(BiConsumer<DataFile, List<ContentFile<?>>> action) {
    if (config.isUseRoaringBitmapInPlanning()) {
      undersizedSegmentFilesBitmap.forEach(
          (dataFile, deleteBitmap) -> action.accept(dataFile, getDeletesFromIndex(deleteBitmap)));
    } else {
      undersizedSegmentFiles.forEach(action);
    }
  }

  /**
   * Iterates over rewrite data files with their delete files, with the same dual-track rule as
   * {@link #forEachUndersizedSegmentFile(BiConsumer)}.
   */
  protected void forEachRewriteDataFile(BiConsumer<DataFile, List<ContentFile<?>>> action) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewriteDataFilesBitmap.forEach(
          (dataFile, deleteBitmap) -> action.accept(dataFile, getDeletesFromIndex(deleteBitmap)));
    } else {
      rewriteDataFiles.forEach(action);
    }
  }

  /**
   * Iterates over rewrite-position data files with their delete files, with the same dual-track
   * rule as {@link #forEachUndersizedSegmentFile(BiConsumer)}.
   */
  protected void forEachRewritePosDataFile(BiConsumer<DataFile, List<ContentFile<?>>> action) {
    if (config.isUseRoaringBitmapInPlanning()) {
      rewritePosDataFilesBitmap.forEach(
          (dataFile, deleteBitmap) -> action.accept(dataFile, getDeletesFromIndex(deleteBitmap)));
    } else {
      rewritePosDataFiles.forEach(action);
    }
  }

  /**
   * Convert a list of delete files into a {@link RoaringBitmap} whose bits are the integer indices
   * assigned by {@link #deleteFileIndex}. Each unique delete file is assigned a sequential index on
   * first encounter, so that the same delete file referenced by multiple data files shares a single
   * integer instead of duplicating the object reference.
   *
   * @param deletes the delete files to index
   * @return a {@link RoaringBitmap} containing the indices of the given delete files
   */
  public RoaringBitmap getIndexFromDeletes(List<ContentFile<?>> deletes) {
    boolean debugEnabled = LOG.isDebugEnabled();
    long startTime = debugEnabled ? System.currentTimeMillis() : 0L;
    RoaringBitmap deleteFileBitmap = new RoaringBitmap();
    for (ContentFile<?> delete : deletes) {
      int inx = deleteFileIndex.computeIfAbsent(delete, k -> deleteFileIndex.size());
      deleteFileBitmap.add(inx);
    }
    if (debugEnabled) {
      LOG.debug(
          "Cost {} ms to convert {} delete files to RoaringBitmap",
          System.currentTimeMillis() - startTime,
          deletes.size());
    }
    return deleteFileBitmap;
  }

  /**
   * Convert a {@link RoaringBitmap} of delete-file indices back to the original list of {@link
   * ContentFile} instances by looking up the inverse of {@link #deleteFileIndex}.
   *
   * @param bitmap the bitmap containing delete-file indices
   * @return the list of delete files corresponding to the indices in the bitmap
   */
  public List<ContentFile<?>> getDeletesFromIndex(RoaringBitmap bitmap) {
    boolean debugEnabled = LOG.isDebugEnabled();
    long startTime = debugEnabled ? System.currentTimeMillis() : 0L;
    List<ContentFile<?>> result = Lists.newArrayListWithCapacity(bitmap.getCardinality());
    BiMap<Integer, ContentFile<?>> inverseMap = deleteFileIndex.inverse();
    bitmap.forEach(
        (IntConsumer)
            index -> {
              ContentFile<?> file = inverseMap.get(index);
              if (file != null) {
                result.add(file);
              }
            });
    if (debugEnabled) {
      LOG.debug(
          "Cost {} ms to convert a RoaringBitmap of {} delete-file indices back to delete files",
          System.currentTimeMillis() - startTime,
          bitmap.getCardinality());
    }
    return result;
  }

  public List<RewriteStageTask> splitTasks(int targetTaskCount) {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    beforeSplit();
    return filterSplitTasks(taskSplitter.splitTasks(targetTaskCount)).stream()
        .map(task -> task.buildTask(buildTaskProperties()))
        .collect(Collectors.toList());
  }

  protected void beforeSplit() {}

  protected List<SplitTask> filterSplitTasks(List<SplitTask> splitTasks) {
    return splitTasks;
  }

  protected abstract TaskSplitter buildTaskSplitter();

  protected abstract Map<String, String> buildTaskProperties();

  protected void markSequence(long sequence) {
    if (fromSequence == null || fromSequence > sequence) {
      fromSequence = sequence;
    }
    if (toSequence == null || toSequence < sequence) {
      toSequence = sequence;
    }
  }

  public Long getFromSequence() {
    return fromSequence;
  }

  public Long getToSequence() {
    return toSequence;
  }

  protected interface TaskSplitter {
    List<SplitTask> splitTasks(int targetTaskCount);
  }

  @Override
  public HealthScoreInfo getHealthScore() {
    return evaluator.getHealthScore();
  }

  @Override
  public int getFragmentFileCount() {
    return evaluator().getFragmentFileCount();
  }

  @Override
  public long getFragmentFileSize() {
    return evaluator().getFragmentFileSize();
  }

  @Override
  public long getFragmentFileRecords() {
    return evaluator().getFragmentFileRecords();
  }

  @Override
  public int getSegmentFileCount() {
    return evaluator().getSegmentFileCount();
  }

  @Override
  public long getSegmentFileSize() {
    return evaluator().getSegmentFileSize();
  }

  @Override
  public long getSegmentFileRecords() {
    return evaluator().getSegmentFileRecords();
  }

  @Override
  public int getEqualityDeleteFileCount() {
    return evaluator().getEqualityDeleteFileCount();
  }

  @Override
  public long getEqualityDeleteFileSize() {
    return evaluator().getEqualityDeleteFileSize();
  }

  @Override
  public long getEqualityDeleteFileRecords() {
    return evaluator().getEqualityDeleteFileRecords();
  }

  @Override
  public int getPosDeleteFileCount() {
    return evaluator().getPosDeleteFileCount();
  }

  @Override
  public long getPosDeleteFileSize() {
    return evaluator().getPosDeleteFileSize();
  }

  @Override
  public long getPosDeleteFileRecords() {
    return evaluator().getPosDeleteFileRecords();
  }

  @Override
  public Weight getWeight() {
    return evaluator().getWeight();
  }

  /**
   * When splitTask has only one undersized segment file, it needs to be triggered again to
   * determine whether to rewrite pos. If needed, add it to rewritePosDataFiles and bin-packing
   * together, else reserved delete files.
   */
  protected void disposeUndersizedSegmentFile(SplitTask splitTask) {
    Optional<DataFile> dataFile = splitTask.getRewriteDataFiles().stream().findFirst();
    if (dataFile.isPresent()) {
      DataFile rewriteDataFile = dataFile.get();
      List<ContentFile<?>> deletes = new ArrayList<>(splitTask.getDeleteFiles());
      if (evaluator().segmentShouldRewritePos(rewriteDataFile, deletes)) {
        putRewritePosDataFile(rewriteDataFile, deletes);
      } else {
        reservedDeleteFiles(deletes);
      }
    }
  }

  protected class SplitTask {
    private final Set<DataFile> rewriteDataFiles = Sets.newHashSet();
    private final Set<DataFile> rewritePosDataFiles = Sets.newHashSet();
    private final Set<ContentFile<?>> deleteFiles = Sets.newHashSet();

    public SplitTask(
        Set<DataFile> rewriteDataFiles,
        Set<DataFile> rewritePosDataFiles,
        Set<ContentFile<?>> deleteFiles) {
      this.rewriteDataFiles.addAll(rewriteDataFiles);
      this.rewritePosDataFiles.addAll(rewritePosDataFiles);
      this.deleteFiles.addAll(deleteFiles);
    }

    public Set<DataFile> getRewriteDataFiles() {
      return rewriteDataFiles;
    }

    public Set<ContentFile<?>> getDeleteFiles() {
      return deleteFiles;
    }

    public Set<DataFile> getRewritePosDataFiles() {
      return rewritePosDataFiles;
    }

    public RewriteStageTask buildTask(Map<String, String> properties) {
      Set<ContentFile<?>> readOnlyDeleteFiles = Sets.newHashSet();
      Set<ContentFile<?>> rewriteDeleteFiles = Sets.newHashSet();
      for (ContentFile<?> deleteFile : deleteFiles) {
        if (reservedDeleteFiles.contains(deleteFile.path().toString())) {
          readOnlyDeleteFiles.add(deleteFile);
        } else {
          rewriteDeleteFiles.add(deleteFile);
        }
      }
      RewriteFilesInput input =
          new RewriteFilesInput(
              rewriteDataFiles.toArray(new DataFile[0]),
              rewritePosDataFiles.toArray(new DataFile[0]),
              readOnlyDeleteFiles.toArray(new ContentFile[0]),
              rewriteDeleteFiles.toArray(new ContentFile[0]),
              tableObject);
      PartitionSpec spec =
          MixedTableUtil.getMixedTablePartitionSpecById(tableObject, partition.first());
      String partitionPath = spec.partitionToPath(partition.second());
      return new RewriteStageTask(identifier.getId(), partitionPath, input, properties);
    }
  }

  /** util class for bin-pack */
  protected static class FileTask {
    private final DataFile file;
    private final List<ContentFile<?>> deleteFiles;
    private final boolean isRewriteDataFile;

    public FileTask(DataFile file, List<ContentFile<?>> deleteFiles, boolean isRewriteDataFile) {
      this.file = file;
      this.deleteFiles = deleteFiles;
      this.isRewriteDataFile = isRewriteDataFile;
    }

    public DataFile getFile() {
      return file;
    }

    public List<ContentFile<?>> getDeleteFiles() {
      return deleteFiles;
    }

    public boolean isRewriteDataFile() {
      return isRewriteDataFile;
    }

    public boolean isRewritePosDataFile() {
      return !isRewriteDataFile;
    }
  }

  protected class BinPackingTaskSplitter implements TaskSplitter {

    @Override
    public List<SplitTask> splitTasks(int targetTaskCount) {
      List<SplitTask> results = Lists.newArrayList();
      List<FileTask> fileTasks = Lists.newArrayList();
      // bin-packing for undersized segment files
      forEachUndersizedSegmentFile(
          (dataFile, deleteFiles) -> fileTasks.add(new FileTask(dataFile, deleteFiles, true)));
      for (SplitTask splitTask : genSplitTasks(fileTasks)) {
        if (splitTask.getRewriteDataFiles().size() > 1) {
          results.add(splitTask);
          continue;
        }
        disposeUndersizedSegmentFile(splitTask);
      }

      // bin-packing for fragment file and rewrite pos data file
      fileTasks.clear();
      forEachRewriteDataFile(
          (dataFile, deleteFiles) -> fileTasks.add(new FileTask(dataFile, deleteFiles, true)));
      forEachRewritePosDataFile(
          (dataFile, deleteFiles) -> fileTasks.add(new FileTask(dataFile, deleteFiles, false)));
      results.addAll(genSplitTasks(fileTasks));
      return results;
    }

    private Collection<? extends SplitTask> genSplitTasks(List<FileTask> allDataFiles) {
      List<List<FileTask>> packed =
          new BinPacking.ListPacker<FileTask>(
                  Math.max(config.getTargetSize(), config.getMaxTaskSize()),
                  Integer.MAX_VALUE,
                  false)
              .pack(allDataFiles, f -> f.getFile().fileSizeInBytes());

      List<SplitTask> results = Lists.newArrayListWithCapacity(packed.size());
      for (List<FileTask> fileTasks : packed) {
        Set<DataFile> rewriteDataFiles = Sets.newHashSet();
        Set<DataFile> rewritePosDataFiles = Sets.newHashSet();
        Set<ContentFile<?>> deleteFiles = Sets.newHashSet();

        fileTasks.stream()
            .filter(FileTask::isRewriteDataFile)
            .forEach(
                f -> {
                  rewriteDataFiles.add(f.getFile());
                  deleteFiles.addAll(f.getDeleteFiles());
                });
        fileTasks.stream()
            .filter(FileTask::isRewritePosDataFile)
            .forEach(
                f -> {
                  rewritePosDataFiles.add(f.getFile());
                  deleteFiles.addAll(f.getDeleteFiles());
                });
        results.add(new SplitTask(rewriteDataFiles, rewritePosDataFiles, deleteFiles));
      }
      return results;
    }
  }
}
