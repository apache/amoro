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
  protected final PartitionEvaluator evaluator;
  private TaskSplitter taskSplitter;

  protected ArcticTable tableObject;
  private long fromSequence = INVALID_SEQUENCE;
  private long toSequence = INVALID_SEQUENCE;
  protected final long planTime;

  protected final Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
  protected final Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();
  protected final Set<IcebergDataFile> equalityRelatedFiles = Sets.newHashSet();
  protected final Map<ContentFile<?>, Set<IcebergDataFile>> equalityDeleteFileMap = Maps.newHashMap();
  protected long fragmentFileSize = 0;
  protected long segmentFileSize = 0;
  protected long positionalDeleteBytes = 0L;
  protected long equalityDeleteBytes = 0L;
  protected int smallFileCount = 0;

  public AbstractPartitionPlan(TableRuntime tableRuntime,
                               ArcticTable table, String partition, long planTime, PartitionEvaluator evaluator) {
    super(partition);
    this.tableObject = table;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
    this.planTime = planTime;
    this.evaluator = evaluator;
  }

  protected void markSequence(long sequence) {
    if (fromSequence == INVALID_SEQUENCE || fromSequence > sequence) {
      fromSequence = sequence;
    }
    if (toSequence == INVALID_SEQUENCE || toSequence < sequence) {
      toSequence = sequence;
    }
  }

  protected abstract TaskSplitter buildTaskSplitter();

  protected abstract boolean partitionShouldFullOptimizing();

  protected abstract OptimizingInputProperties buildTaskProperties();

  @Override
  public boolean isNecessary() {
    return evaluator.isNecessary();
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

  @Override
  public OptimizingType getOptimizingType() {
    return evaluator.getOptimizingType();
  }

  @Override
  public long getCost() {
    return evaluator.getCost();
  }

  public long getFromSequence() {
    return fromSequence;
  }

  public long getToSequence() {
    return toSequence;
  }

  protected abstract class TaskSplitter {

    public TaskSplitter() {
    }

    abstract List<SplitTask> splitTasks(int targetTaskCount);

  }

  protected class SplitTask {
    private final Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    private final Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();
    private final Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    public SplitTask(Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles,
                     Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles) {
      if (partitionShouldFullOptimizing()) {
        segmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (shouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
        fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (shouldFullOptimizing(icebergFile, deleteFileSet)) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        });
      } else {
        segmentFiles.forEach((icebergFile, deleteFileSet) -> {
          if (canRewriteFile(icebergFile) &&
              getRecordCount(deleteFileSet) >= icebergFile.recordCount() * config.getMajorDuplicateRatio()) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          } else if (equalityRelatedFiles.contains(icebergFile)) {
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

  protected boolean canRewriteFile(IcebergDataFile icebergFile) {
    return true;
  }

  protected abstract boolean shouldFullOptimizing(IcebergDataFile icebergFile, List<IcebergContentFile<?>> deleteFiles);
}
