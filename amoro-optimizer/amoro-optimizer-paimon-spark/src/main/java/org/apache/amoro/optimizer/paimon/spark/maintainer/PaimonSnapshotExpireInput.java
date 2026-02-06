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

import org.apache.amoro.maintainer.BaseMaintainerInput;

/** Input for Paimon snapshot expiration operation. */
public class PaimonSnapshotExpireInput extends BaseMaintainerInput {

  private static final long serialVersionUID = 1L;

  private final String tableIdentifier;
  private final String tableFormat;
  private final long olderThanMillis;
  private final int retainLastCount;

  // Table metadata will be serialized and passed through options
  public static final String TABLE_LOCATION = "table.location";
  public static final String TABLE_DATABASE = "table.database";
  public static final String TABLE_NAME = "table.name";

  /**
   * Create a Paimon snapshot expiration input.
   *
   * @param tableIdentifier the full table identifier
   * @param tableFormat the table format (e.g., "PAIMON")
   * @param olderThanMillis expire snapshots older than this timestamp (milliseconds)
   * @param retainLastCount retain at least this many snapshots
   */
  public PaimonSnapshotExpireInput(
      String tableIdentifier, String tableFormat, long olderThanMillis, int retainLastCount) {
    this.tableIdentifier = tableIdentifier;
    this.tableFormat = tableFormat;
    this.olderThanMillis = olderThanMillis;
    this.retainLastCount = retainLastCount;
  }

  @Override
  public OperationType getOperationType() {
    return OperationType.SNAPSHOT_EXPIRATION;
  }

  @Override
  public String getTableIdentifier() {
    return tableIdentifier;
  }

  @Override
  public String getTableFormat() {
    return tableFormat;
  }

  /**
   * Get the expiration threshold in milliseconds.
   *
   * @return snapshots older than this timestamp should be expired
   */
  public long getOlderThanMillis() {
    return olderThanMillis;
  }

  /**
   * Get the minimum number of snapshots to retain.
   *
   * @return minimum snapshots to keep
   */
  public int getRetainLastCount() {
    return retainLastCount;
  }
}
