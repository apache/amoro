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
  protected final long fragmentSize;
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

  public AbstractPartitionPlan(TableRuntime tableRuntime,
                               ArcticTable table, String partition, long planTime, BasicPartitionEvaluator evaluator) {
    super(partition);
    this.tableObject = table;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
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
    return taskSplitter.splitTasks(targetTaskCount).stream()
        .filter(SplitTask::isNotEmpty)
        .map(task -> task.buildTask(buildTaskProperties()))
        .collect(Collectors.toList());
  }

  protected abstract TaskSplitter buildTaskSplitter();

  protected abstract OptimizingInputProperties buildTaskProperties();

  protected boolean findAnyDelete() {
    return evaluator.getEqualityDeleteFileCount() + evaluator.getPosDeleteFileCount() > 0;
  }

  protected boolean isFragmentFile(IcebergDataFile file) {
    return file.fileSizeInBytes() <= fragmentSize;
  }

  protected boolean fileShouldFullOptimizing(IcebergDataFile dataFile, List<IcebergContentFile<?>> deleteFiles) {
    if (config.isFullRewriteAllFiles()) {
      return true;
    } else {
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

  protected class SplitTask {
    private final Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    private final Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();

    private final Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    public SplitTask(Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles,
                     Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles) {
      if (evaluator.isFullNecessary()) {
        segmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (fileShouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
        fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (fileShouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
      } else {
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
        fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
          rewriteDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        });
      }
    }

    public TaskDescriptor buildTask(OptimizingInputProperties properties) {
      if (isEmpty()) {
        return null;
      }
      RewriteFilesInput input = new RewriteFilesInput(
          rewriteDataFiles.toArray(new IcebergDataFile[rewriteDataFiles.size()]),
          rewritePosDataFiles.toArray(new IcebergDataFile[rewritePosDataFiles.size()]),
          deleteFiles.toArray(new IcebergContentFile[deleteFiles.size()]),
          tableObject);
      return new TaskDescriptor(partition, input, properties.getProperties());
    }

    public boolean isEmpty() {
      return rewriteDataFiles.isEmpty() && rewritePosDataFiles.isEmpty();
    }

    public boolean isNotEmpty() {
      return !isEmpty();
    }

    private long getRecordCount(List<IcebergContentFile<?>> files) {
      return files.stream().mapToLong(ContentFile::recordCount).sum();
    }
  }
}
