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

package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractPartitionPlan extends PartitionEvaluator {
  public static final int INVALID_SEQUENCE = -1;

  protected final OptimizingConfig config;
  protected final TableRuntime tableRuntime;
  protected final long maxFragmentSize;
  protected final BasicPartitionEvaluator evaluator;
  private TaskSplitter taskSplitter;

  protected ArcticTable tableObject;
  private long fromSequence = INVALID_SEQUENCE;
  private long toSequence = INVALID_SEQUENCE;
  protected final long planTime;

  protected final Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
  protected final Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();
  protected final Map<String, Set<IcebergDataFile>> equalityDeleteFileMap = Maps.newHashMap();
  protected final Map<String, Set<IcebergDataFile>> posDeleteFileMap = Maps.newHashMap();
  
  private List<SplitTask> splitTasks;

  public AbstractPartitionPlan(TableRuntime tableRuntime,
                               ArcticTable table, String partition, long planTime, BasicPartitionEvaluator evaluator) {
    super(partition);
    this.tableObject = table;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.maxFragmentSize = config.maxFragmentSize();
    this.planTime = planTime;
    this.evaluator = evaluator;
  }

  @Override
  public boolean isNecessary() {
    return evaluator.isNecessary();
  }

  @Override
  public OptimizingType getOptimizingType() {
    return evaluator.getOptimizingType();
  }

  @Override
  public long getCost() {
    return evaluator.getCost();
  }

  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    evaluator.addFile(dataFile, deletes);
    if (isFragmentFile(dataFile)) {
      fragmentFiles.put(dataFile, deletes);
    } else {
      segmentFiles.put(dataFile, deletes);
    }
    for (IcebergContentFile<?> deleteFile : deletes) {
      if (deleteFile.content() == FileContent.POSITION_DELETES) {
        posDeleteFileMap
            .computeIfAbsent(deleteFile.path().toString(), delete -> Sets.newHashSet())
            .add(dataFile);
      } else {
        equalityDeleteFileMap
            .computeIfAbsent(deleteFile.path().toString(), delete -> Sets.newHashSet())
            .add(dataFile);
      }
    }
  }

  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    this.splitTasks = taskSplitter.splitTasks(targetTaskCount).stream()
        .filter(this::taskNeedExecute).collect(Collectors.toList());
    return this.splitTasks.stream()
        .map(task -> task.buildTask(buildTaskProperties()))
        .collect(Collectors.toList());
  }

  protected boolean taskNeedExecute(SplitTask task) {
    // if there are no delete files and no more than 1 rewrite files, we should not execute
    return !task.getDeleteFiles().isEmpty() || task.getRewriteDataFiles().size() > 1;
  }

  private boolean isOptimizing(IcebergDataFile dataFile) {
    return this.splitTasks.stream().anyMatch(task -> task.contains(dataFile));
  }

  protected abstract TaskSplitter buildTaskSplitter();

  protected abstract OptimizingInputProperties buildTaskProperties();

  protected boolean findAnyDelete() {
    return evaluator.getEqualityDeleteFileCount() + evaluator.getPosDeleteFileCount() > 0;
  }

  protected boolean isFragmentFile(IcebergDataFile file) {
    return file.fileSizeInBytes() <= maxFragmentSize;
  }

  protected boolean fileShouldFullOptimizing(IcebergDataFile dataFile, List<IcebergContentFile<?>> deleteFiles) {
    if (config.isFullRewriteAllFiles()) {
      return true;
    } else {
      // if a file is related any delete files or is not big enough, it should full optimizing
      return !deleteFiles.isEmpty() || dataFile.fileSizeInBytes() < config.getTargetSize() * 0.9;
    }
  }

  protected boolean canSegmentFileRewrite(IcebergDataFile icebergFile) {
    return true;
  }

  protected void markSequence(long sequence) {
    if (fromSequence == INVALID_SEQUENCE || fromSequence > sequence) {
      fromSequence = sequence;
    }
    if (toSequence == INVALID_SEQUENCE || toSequence < sequence) {
      toSequence = sequence;
    }
  }

  public long getFromSequence() {
    return fromSequence;
  }

  public long getToSequence() {
    return toSequence;
  }

  protected interface TaskSplitter {
    List<SplitTask> splitTasks(int targetTaskCount);
  }

  @Override
  public int getFragmentFileCount() {
    return evaluator.getFragmentFileCount();
  }

  @Override
  public long getFragmentFileSize() {
    return evaluator.getFragmentFileSize();
  }

  @Override
  public int getSegmentFileCount() {
    return evaluator.getSegmentFileCount();
  }

  @Override
  public long getSegmentFileSize() {
    return evaluator.getSegmentFileSize();
  }

  @Override
  public int getEqualityDeleteFileCount() {
    return evaluator.getEqualityDeleteFileCount();
  }

  @Override
  public long getEqualityDeleteFileSize() {
    return evaluator.getEqualityDeleteFileSize();
  }

  @Override
  public int getPosDeleteFileCount() {
    return evaluator.getPosDeleteFileCount();
  }

  @Override
  public long getPosDeleteFileSize() {
    return evaluator.getPosDeleteFileSize();
  }

  protected class SplitTask {
    private final Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    private final Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();
    private final Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    public SplitTask(Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles,
                     Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles) {
      if (evaluator.isFullNecessary()) {
        fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (fileShouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
        segmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (fileShouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
      } else {
        fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
          rewriteDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        });
        segmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (canSegmentFileRewrite(icebergFile) &&
              getRecordCount(deleteFileSet) >= icebergFile.recordCount() * config.getMajorDuplicateRatio()) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          } else if (equalityDeleteFileMap.containsKey(icebergFile.path().toString())) {
            rewritePosDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          } else {
            long posDeleteCount = deleteFileSet.stream()
                .filter(file -> file.content() == FileContent.POSITION_DELETES)
                .count();
            if (posDeleteCount > 1) {
              rewritePosDataFiles.add(icebergFile);
              deleteFiles.addAll(deleteFileSet);
            }
          }
        });
      }
    }

    public Set<IcebergDataFile> getRewriteDataFiles() {
      return rewriteDataFiles;
    }

    public Set<IcebergContentFile<?>> getDeleteFiles() {
      return deleteFiles;
    }

    public Set<IcebergDataFile> getRewritePosDataFiles() {
      return rewritePosDataFiles;
    }

    public boolean contains(IcebergDataFile dataFile) {
      return rewriteDataFiles.contains(dataFile) || rewritePosDataFiles.contains(dataFile);
    }

    public TaskDescriptor buildTask(OptimizingInputProperties properties) {
      Set<IcebergContentFile<?>> readOnlyDeleteFiles = Sets.newHashSet();
      Set<IcebergContentFile<?>> rewriteDeleteFiles = Sets.newHashSet();
      for (IcebergContentFile<?> deleteFile : deleteFiles) {
        Set<IcebergDataFile> relatedDataFiles;
        if (deleteFile.content() == FileContent.POSITION_DELETES) {
          relatedDataFiles = posDeleteFileMap.get(deleteFile.path().toString());
        } else {
          relatedDataFiles = equalityDeleteFileMap.get(deleteFile.path().toString());
        }
        boolean findDataFileNotOptimizing =
            relatedDataFiles.stream().anyMatch(file -> !contains(file) && !isOptimizing(file));
        if (findDataFileNotOptimizing) {
          readOnlyDeleteFiles.add(deleteFile);
        } else {
          rewriteDeleteFiles.add(deleteFile);
        }
      }
      RewriteFilesInput input = new RewriteFilesInput(
          rewriteDataFiles.toArray(new IcebergDataFile[0]),
          rewritePosDataFiles.toArray(new IcebergDataFile[0]),
          readOnlyDeleteFiles.toArray(new IcebergContentFile[0]),
          rewriteDeleteFiles.toArray(new IcebergContentFile[0]),
          tableObject);
      return new TaskDescriptor(partition, input, properties.getProperties());
    }

    private long getRecordCount(List<IcebergContentFile<?>> files) {
      return files.stream().mapToLong(ContentFile::recordCount).sum();
    }
  }
}
