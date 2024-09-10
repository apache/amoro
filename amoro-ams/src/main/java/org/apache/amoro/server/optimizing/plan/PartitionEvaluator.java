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

import org.apache.amoro.server.optimizing.OptimizingType;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.util.Pair;

import java.util.List;

/** PartitionEvaluator is used to evaluate whether a partition is necessary to be optimized. */
public interface PartitionEvaluator {

  /**
   * Weight determines the priority of partition execution, with higher weights having higher
   * priority.
   */
  interface Weight extends Comparable<Weight> {}

  /**
   * Get the partition represented by a Pair of {@link PartitionSpec#specId()} and partition {@link
   * StructLike}.
   *
   * @return the Pair of the partition spec id and the partition
   */
  Pair<Integer, StructLike> getPartition();

  /**
   * Add a Data file and its related Delete files to this evaluator
   *
   * @param dataFile - Data file
   * @param deletes - Delete files
   * @return true if the file is added successfully, false if the file will not be optimized
   */
  boolean addFile(DataFile dataFile, List<ContentFile<?>> deletes);

  /**
   * Whether this partition is necessary to optimize.
   *
   * @return true for is necessary to optimize, false for not necessary
   */
  boolean isNecessary();

  /**
   * Get the cost of optimizing for this partition.
   *
   * @return the cost of optimizing
   */
  long getCost();

  /**
   * Get the weight of this partition which determines the priority of partition execution.
   *
   * @return the weight of this partition
   */
  Weight getWeight();

  /**
   * Get the optimizing type of this partition.
   *
   * @return the OptimizingType
   */
  OptimizingType getOptimizingType();

  /** Get health score of this partition. */
  int getHealthScore();

  /** Get the count of fragment files involved in optimizing. */
  int getFragmentFileCount();

  /** Get the total size of fragment files involved in optimizing. */
  long getFragmentFileSize();

  /** Get the total records of fragment files involved in optimizing. */
  long getFragmentFileRecords();

  /** Get the count of segment files involved in optimizing. */
  int getSegmentFileCount();

  /** Get the total size of segment files involved in optimizing. */
  long getSegmentFileSize();

  /** Get the total records of segment files involved in optimizing. */
  long getSegmentFileRecords();

  /** Get the count of equality delete files involved in optimizing. */
  int getEqualityDeleteFileCount();

  /** Get the total size of equality delete files involved in optimizing. */
  long getEqualityDeleteFileSize();

  /** Get the total records of equality delete files involved in optimizing. */
  long getEqualityDeleteFileRecords();

  /** Get the count of positional delete files involved in optimizing. */
  int getPosDeleteFileCount();

  /** Get the total size of positional delete files involved in optimizing. */
  long getPosDeleteFileSize();

  /** Get the total records of positional delete files involved in optimizing. */
  long getPosDeleteFileRecords();
}
