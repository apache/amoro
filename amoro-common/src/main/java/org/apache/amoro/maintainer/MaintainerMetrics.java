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

package org.apache.amoro.maintainer;

/**
 * Metrics collector interface for table maintenance operations. Implementations can collect metrics
 * to different monitoring systems.
 */
public interface MaintainerMetrics {

  /**
   * Record orphan data files cleaning result.
   *
   * @param expected expected number of files to clean
   * @param cleaned actual number of files cleaned
   */
  void recordOrphanDataFilesCleaned(int expected, int cleaned);

  /**
   * Record orphan metadata files cleaning result.
   *
   * @param expected expected number of files to clean
   * @param cleaned actual number of files cleaned
   */
  void recordOrphanMetadataFilesCleaned(int expected, int cleaned);

  /**
   * Record dangling delete files cleaning result.
   *
   * @param cleaned number of files cleaned
   */
  void recordDanglingDeleteFilesCleaned(int cleaned);

  /**
   * Record snapshot expiration operation result.
   *
   * @param snapshotCount number of snapshots expired
   * @param dataFilesDeleted number of data files deleted
   * @param durationMillis operation duration in milliseconds
   */
  void recordSnapshotsExpired(int snapshotCount, int dataFilesDeleted, long durationMillis);

  /**
   * Record data expiration operation result.
   *
   * @param dataFilesExpired number of data files expired
   * @param deleteFilesExpired number of delete files expired
   * @param durationMillis operation duration in milliseconds
   */
  void recordDataExpired(int dataFilesExpired, int deleteFilesExpired, long durationMillis);

  /**
   * Record tag creation operation result.
   *
   * @param tagsCreated number of tags created
   * @param durationMillis operation duration in milliseconds
   */
  void recordTagsCreated(int tagsCreated, long durationMillis);

  /**
   * Record partition expiration operation result.
   *
   * @param partitionsExpired number of partitions expired
   * @param filesExpired number of files expired
   * @param durationMillis operation duration in milliseconds
   */
  void recordPartitionsExpired(int partitionsExpired, int filesExpired, long durationMillis);

  /**
   * Record operation start.
   *
   * @param operationType operation type
   */
  void recordOperationStart(MaintainerOperationType operationType);

  /**
   * Record operation success completion.
   *
   * @param operationType operation type
   * @param durationMillis operation duration in milliseconds
   */
  void recordOperationSuccess(MaintainerOperationType operationType, long durationMillis);

  /**
   * Record operation failure.
   *
   * @param operationType operation type
   * @param durationMillis operation duration in milliseconds
   * @param throwable exception information
   */
  void recordOperationFailure(
      MaintainerOperationType operationType, long durationMillis, Throwable throwable);

  /** No-op implementation that does nothing. */
  MaintainerMetrics NOOP =
      new MaintainerMetrics() {
        @Override
        public void recordOrphanDataFilesCleaned(int expected, int cleaned) {}

        @Override
        public void recordOrphanMetadataFilesCleaned(int expected, int cleaned) {}

        @Override
        public void recordDanglingDeleteFilesCleaned(int cleaned) {}

        @Override
        public void recordSnapshotsExpired(
            int snapshotCount, int dataFilesDeleted, long durationMillis) {}

        @Override
        public void recordDataExpired(
            int dataFilesExpired, int deleteFilesExpired, long durationMillis) {}

        @Override
        public void recordTagsCreated(int tagsCreated, long durationMillis) {}

        @Override
        public void recordPartitionsExpired(
            int partitionsExpired, int filesExpired, long durationMillis) {}

        @Override
        public void recordOperationStart(MaintainerOperationType operationType) {}

        @Override
        public void recordOperationSuccess(
            MaintainerOperationType operationType, long durationMillis) {}

        @Override
        public void recordOperationFailure(
            MaintainerOperationType operationType, long durationMillis, Throwable throwable) {}
      };
}
