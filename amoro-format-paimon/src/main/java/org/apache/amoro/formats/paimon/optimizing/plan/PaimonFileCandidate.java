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

import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.io.DataFileMeta;
import org.apache.paimon.table.source.DeletionFile;

import javax.annotation.Nullable;

import java.util.Optional;

final class PaimonFileCandidate {

  private final BinaryRow partition;
  private final DataFileMeta file;
  private final String dvGroupKey;
  private final boolean hasDeletionVector;
  private final boolean smallFile;
  private final boolean undersizedFile;
  private final boolean highDeleteRatio;

  private PaimonFileCandidate(
      BinaryRow partition,
      DataFileMeta file,
      String dvGroupKey,
      boolean hasDeletionVector,
      boolean smallFile,
      boolean undersizedFile,
      boolean highDeleteRatio) {
    this.partition = partition;
    this.file = file;
    this.dvGroupKey = dvGroupKey;
    this.hasDeletionVector = hasDeletionVector;
    this.smallFile = smallFile;
    this.undersizedFile = undersizedFile;
    this.highDeleteRatio = highDeleteRatio;
  }

  static PaimonFileCandidate from(
      BinaryRow partition,
      DataFileMeta file,
      PaimonPlanContext context,
      @Nullable DeletionFile deletionFile,
      @Nullable String dvGroupKey) {
    boolean small = file.fileSize() < context.smallFileBoundary();
    boolean undersized =
        file.fileSize() >= context.smallFileBoundary() && file.fileSize() < context.targetSize();
    boolean highDelete = isHighDeleteRatio(file, deletionFile, context);
    String groupKey =
        deletionFile == null ? null : dvGroupKey == null ? deletionFile.path() : dvGroupKey;
    return new PaimonFileCandidate(
        partition, file, groupKey, deletionFile != null, small, undersized, highDelete);
  }

  private static boolean isHighDeleteRatio(
      DataFileMeta file, @Nullable DeletionFile deletionFile, PaimonPlanContext context) {
    if (deletionFile != null) {
      Long cardinality = deletionFile.cardinality();
      return cardinality == null || cardinality > file.rowCount() * context.deleteRatioThreshold();
    }
    Optional<Long> deleteRowCount = file.deleteRowCount();
    return deleteRowCount.isPresent()
        && deleteRowCount.get() > file.rowCount() * context.deleteRatioThreshold();
  }

  BinaryRow partition() {
    return partition;
  }

  DataFileMeta file() {
    return file;
  }

  String fileName() {
    return file.fileName();
  }

  long fileSize() {
    return file.fileSize();
  }

  long rowCount() {
    return file.rowCount();
  }

  @Nullable
  String dvGroupKey() {
    return dvGroupKey;
  }

  boolean hasDeletionVector() {
    return hasDeletionVector;
  }

  boolean isSmallFile() {
    return smallFile;
  }

  boolean isUndersizedFile() {
    return undersizedFile;
  }

  boolean isHighDeleteRatio() {
    return highDeleteRatio;
  }

  boolean isProblemFile() {
    return smallFile || undersizedFile || highDeleteRatio;
  }
}
