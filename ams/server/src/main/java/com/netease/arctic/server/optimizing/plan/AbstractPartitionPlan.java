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
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

import java.util.List;

public abstract class AbstractPartitionPlan {
  public static final int INVALID_SEQUENCE = -1;

  protected final String partition;
  protected final OptimizingConfig config;
  protected final TableRuntime tableRuntime;
  protected final long fragmentSize;

  protected ArcticTable tableObject;
  private long fromSequence = INVALID_SEQUENCE;
  private long toSequence = INVALID_SEQUENCE;
  protected final long planTime;
  
  protected boolean canAddFile = true;

  public AbstractPartitionPlan(TableRuntime tableRuntime, ArcticTable table, String partition, long planTime) {
    this.tableObject = table;
    this.partition = partition;
    this.config = tableRuntime.getOptimizingConfig();
    this.tableRuntime = tableRuntime;
    this.fragmentSize = config.getTargetSize() / config.getFragmentRatio();
    this.planTime = planTime;
  }

  public String getPartition() {
    return partition;
  }

  public abstract void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes);

  public abstract boolean isNecessary();

  public abstract long getCost();

  public abstract OptimizingType getOptimizingType();

  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
    throw new UnsupportedOperationException();
  }
  
  public void finishAddFiles() {
    canAddFile = false;
  }

  protected void checkAllFilesAdded() {
    Preconditions.checkArgument(!canAddFile, "adding files is not finished");
  }

  protected void checkSupportAddingFiles() {
    Preconditions.checkArgument(canAddFile, "can't add more files");
  }

  protected void markSequence(long sequence) {
    checkSupportAddingFiles();
    if (fromSequence == INVALID_SEQUENCE || fromSequence > sequence) {
      fromSequence = sequence;
    }
    if (toSequence == INVALID_SEQUENCE || toSequence < sequence) {
      toSequence = sequence;
    }
  }
  
  public long getFromSequence() {
    checkAllFilesAdded();
    return fromSequence;
  }
  
  public long getToSequence() {
    checkAllFilesAdded();
    return toSequence;
  }
}
