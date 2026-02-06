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

package org.apache.amoro.optimizer.paimon.spark.maintainer;

import org.apache.amoro.maintainer.BaseMaintainerOutput;

/** Output for Paimon snapshot expiration operation. */
public class PaimonMaintainerOutput extends BaseMaintainerOutput {

  private static final long serialVersionUID = 1L;

  /** Summary key for expired snapshot count */
  public static final String EXPIRED_SNAPSHOT_COUNT = "expired_snapshot_count";

  /** Summary key for expired data file count */
  public static final String EXPIRED_DATA_FILE_COUNT = "expired_data_file_count";

  /** Summary key for expired data file size */
  public static final String EXPIRED_DATA_FILE_SIZE = "expired_data_file_size";

  /** Create a successful maintainer output. */
  public PaimonMaintainerOutput() {
    super(true, null);
  }

  /**
   * Create a maintainer output with specified status.
   *
   * @param success whether the operation succeeded
   * @param errorMessage error message if failed, null otherwise
   */
  public PaimonMaintainerOutput(boolean success, String errorMessage) {
    super(success, errorMessage);
  }

  /**
   * Set the number of expired snapshots.
   *
   * @param count number of snapshots expired
   */
  public void setExpiredSnapshotCount(int count) {
    putSummary(EXPIRED_SNAPSHOT_COUNT, String.valueOf(count));
  }

  /**
   * Set the number of expired data files.
   *
   * @param count number of data files expired
   */
  public void setExpiredDataFileCount(int count) {
    putSummary(EXPIRED_DATA_FILE_COUNT, String.valueOf(count));
  }

  /**
   * Set the total size of expired data files.
   *
   * @param size total size in bytes
   */
  public void setExpiredDataFileSize(long size) {
    putSummary(EXPIRED_DATA_FILE_SIZE, String.valueOf(size));
  }

  /**
   * Get the number of expired snapshots.
   *
   * @return number of snapshots expired, or 0 if not set
   */
  public int getExpiredSnapshotCount() {
    String value = summary().get(EXPIRED_SNAPSHOT_COUNT);
    return value == null ? 0 : Integer.parseInt(value);
  }

  /**
   * Get the number of expired data files.
   *
   * @return number of data files expired, or 0 if not set
   */
  public int getExpiredDataFileCount() {
    String value = summary().get(EXPIRED_DATA_FILE_COUNT);
    return value == null ? 0 : Integer.parseInt(value);
  }

  /**
   * Get the total size of expired data files.
   *
   * @return total size in bytes, or 0 if not set
   */
  public long getExpiredDataFileSize() {
    String value = summary().get(EXPIRED_DATA_FILE_SIZE);
    return value == null ? 0 : Long.parseLong(value);
  }
}
