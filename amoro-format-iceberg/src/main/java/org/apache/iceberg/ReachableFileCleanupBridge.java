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

package org.apache.iceberg;

/**
 * Bridge into iceberg-core to force the reachable file-cleanup strategy during snapshot expiration.
 *
 * <p>{@link RemoveSnapshots#withIncrementalCleanup(boolean)} is package-private, so this class
 * lives in {@code org.apache.iceberg} to call it without reflection. Binding at compile time means
 * a future change to that method's signature breaks the build instead of silently falling back.
 *
 * <p>By default iceberg auto-selects {@link IncrementalFileCleanup} whenever the table has a single
 * ref. That strategy walks the current snapshot's ancestor chain and terminates silently if a
 * parent snapshot is missing from metadata; snapshots below the break are then treated as
 * non-ancestors and the ADDED entries in their superseded-but-still-referenced manifests are
 * reverted, physically deleting data files the current snapshot still references. Forcing {@link
 * ReachableFileCleanup} avoids the ancestor walk entirely and is safe for any ref count.
 */
public final class ReachableFileCleanupBridge {

  private ReachableFileCleanupBridge() {}

  /**
   * Forces {@code expireSnapshots} to use {@link ReachableFileCleanup} instead of letting iceberg
   * auto-select the incremental strategy.
   *
   * @param expireSnapshots the expire operation to configure
   * @return the same operation, for fluent chaining
   */
  public static ExpireSnapshots forceReachable(ExpireSnapshots expireSnapshots) {
    if (expireSnapshots instanceof RemoveSnapshots) {
      ((RemoveSnapshots) expireSnapshots).withIncrementalCleanup(false);
    }
    return expireSnapshots;
  }
}
