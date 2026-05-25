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

package org.apache.amoro.formats.paimon.optimizing.plan;

import org.apache.amoro.optimizing.OptimizingType;
import org.apache.paimon.data.BinaryRow;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PaimonPartitionEvaluation {

  private final BinaryRow partition;
  @Nullable private final OptimizingType optimizingType;
  private final List<PaimonFileCandidate> selectedFiles;
  private final List<PaimonFileCandidate> allFiles;
  private final int smallFileCount;
  private final int undersizedFileCount;
  private final int highDeleteFileCount;

  PaimonPartitionEvaluation(
      BinaryRow partition,
      @Nullable OptimizingType optimizingType,
      List<PaimonFileCandidate> selectedFiles,
      List<PaimonFileCandidate> allFiles,
      int smallFileCount,
      int undersizedFileCount,
      int highDeleteFileCount) {
    this.partition = partition;
    this.optimizingType = optimizingType;
    this.selectedFiles = Collections.unmodifiableList(new ArrayList<>(selectedFiles));
    this.allFiles = Collections.unmodifiableList(new ArrayList<>(allFiles));
    this.smallFileCount = smallFileCount;
    this.undersizedFileCount = undersizedFileCount;
    this.highDeleteFileCount = highDeleteFileCount;
  }

  BinaryRow partition() {
    return partition;
  }

  @Nullable
  OptimizingType optimizingType() {
    return optimizingType;
  }

  boolean necessary() {
    return optimizingType != null && !selectedFiles.isEmpty();
  }

  List<PaimonFileCandidate> selectedFiles() {
    return selectedFiles;
  }

  List<PaimonFileCandidate> allFiles() {
    return allFiles;
  }

  int smallFileCount() {
    return smallFileCount;
  }

  int undersizedFileCount() {
    return undersizedFileCount;
  }

  int highDeleteFileCount() {
    return highDeleteFileCount;
  }
}
