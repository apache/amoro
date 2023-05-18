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
import com.netease.arctic.server.optimizing.OptimizingType;

import java.util.List;

public abstract class PartitionEvaluator {

  protected final String partition;

  public PartitionEvaluator(String partition) {
    this.partition = partition;
  }

  public String getPartition() {
    return partition;
  }

  public abstract void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes);

  public abstract boolean isNecessary();

  public abstract long getCost();

  public abstract OptimizingType getOptimizingType();

  public abstract int getFragmentFileCount();

  public abstract long getFragmentFileSize();

  public abstract int getSegmentFileCount();

  public abstract long getSegmentFileSize();

  public abstract int getEqualityDeleteFileCount();

  public abstract long getEqualityDeleteFileSize();

  public abstract int getPosDeleteFileCount();

  public abstract long getPosDeleteFileSize();

}
