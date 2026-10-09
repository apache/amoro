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

  /** Record a successful orphan-file-cleaning run. */
  void recordSuccess();

  /**
   * Record an orphan-file-cleaning failure event.
   *
   * @param reason the failure reason
   */
  void recordFailure(CleanFailureReason reason);

  /** Atomic failure reasons for orphan-file-cleaning observability. */
  enum CleanFailureReason {
    /** Another table uuid detected in the same location; cleanup skipped. */
    LOCATION_CONFLICT(1),
    /** The location-conflict check itself failed (e.g. FileIO doesn't support prefix ops). */
    LOCATION_CONFLICT_CHECK_FAILED(2),
    /** The actual cleaning execution threw an exception. */
    EXECUTION_FAILED(3);

    private final int statusCode;

    CleanFailureReason(int statusCode) {
      this.statusCode = statusCode;
    }

    public int statusCode() {
      return statusCode;
    }
  }

  /** No-op implementation that does nothing. */
  MaintainerMetrics NOOP =
      new MaintainerMetrics() {
        @Override
        public void recordOrphanDataFilesCleaned(int expected, int cleaned) {}

        @Override
        public void recordOrphanMetadataFilesCleaned(int expected, int cleaned) {}

        @Override
        public void recordSuccess() {}

        @Override
        public void recordFailure(CleanFailureReason reason) {}
      };
}
