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

package org.apache.amoro.server.optimizing.plan;

import org.apache.amoro.config.OptimizingConfig;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.server.optimizing.OptimizingType;
import org.apache.amoro.server.table.TableRuntime;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractPartitionPlan implements PartitionEvaluator {

  protected final Pair<Integer, StructLike> partition;
  protected final OptimizingConfig config;
  protected final TableRuntime tableRuntime;
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
   * Reserved Delete files are Delete files which are related to Data files not optimized in this
   * plan.
   */
  protected final Set<String> reservedDeleteFiles = Sets.newHashSet();

  public AbstractPartitionPlan(
      TableRuntime tableRuntime,
      MixedTable table,
      Pair<Integer, StructLike> partition,
      long planTime) {
    this.partition = partition;
    this.tableObject = table;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.planTime = planTime;
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
    return new CommonPartitionEvaluator(tableRuntime, partition, planTime);
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
        rewriteDataFiles.put(dataFile, deletes);
      } else if (evaluator().isUndersizedSegmentFile(dataFile)) {
        undersizedSegmentFiles.put(dataFile, deletes);
      } else if (evaluator().segmentShouldRewritePos(dataFile, deletes)) {
        rewritePosDataFiles.put(dataFile, deletes);
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

  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
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

  protected abstract OptimizingInputProperties buildTaskProperties();

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
  public int getHealthScore() {
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
        rewritePosDataFiles.put(rewriteDataFile, deletes);
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

    public TaskDescriptor buildTask(OptimizingInputProperties properties) {
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
      return new TaskDescriptor(
          tableRuntime.getTableIdentifier().getId(),
          partitionPath,
          input,
          properties.getProperties());
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
      undersizedSegmentFiles.forEach(
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
      rewriteDataFiles.forEach(
          (dataFile, deleteFiles) -> fileTasks.add(new FileTask(dataFile, deleteFiles, true)));
      rewritePosDataFiles.forEach(
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
