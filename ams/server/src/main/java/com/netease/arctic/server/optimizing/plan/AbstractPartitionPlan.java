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
import com.netease.arctic.server.optimizing.OptimizingConfig;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractPartitionPlan extends PartitionEvaluator {
  public static final int INVALID_SEQUENCE = -1;

  protected final OptimizingConfig config;
  protected final TableRuntime tableRuntime;
  protected final long fragmentSize;
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
                               ArcticTable table, String partition, long planTime) {
    super(partition);
    this.tableObject = table;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
    this.planTime = planTime;
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

  @Override
  public boolean isNecessary() {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    return taskSplitter.isNecessary();
  }

  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    return taskSplitter.splitTasks(targetTaskCount);
  }

  @Override
  public OptimizingType getOptimizingType() {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    return taskSplitter.getOptimizingType();
  }

  @Override
  public long getCost() {
    if (taskSplitter == null) {
      taskSplitter = buildTaskSplitter();
    }
    return taskSplitter.getCost();
  }

  public long getFromSequence() {
    return fromSequence;
  }

  public long getToSequence() {
    return toSequence;
  }

  protected interface TaskSplitter {
    List<TaskDescriptor> splitTasks(int targetTaskCount);

    boolean isNecessary();
    
    OptimizingType getOptimizingType();

    long getCost();
  }
}
