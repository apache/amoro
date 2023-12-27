/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.netease.arctic.ams.api;

import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public enum Action {
  MINOR_OPTIMIZING("minor-optimizing", 0),
  MAJOR_OPTIMIZING("minor-optimizing", 1),
  EXTERNAL_OPTIMIZING("external-optimizing", 2),
  REFRESH_SNAPSHOT("refreshing", 10),
  EXPIRE_SNAPSHOTS("expiring", 11),
  CLEAN_ORPHANED_FILES("clean_orphaned", 12),
  HIVE_COMMIT_SYNC("sync_hive", 13);

  private static final Set<Action> ARBITRARY_ACTIONS =
      Collections.unmodifiableSet(
          Sets.newHashSet(
              REFRESH_SNAPSHOT, EXPIRE_SNAPSHOTS, CLEAN_ORPHANED_FILES, HIVE_COMMIT_SYNC));

  public static boolean isArbitrary(Action action) {
    return ARBITRARY_ACTIONS.contains(action);
  }

  private final String description;
  private final int dbValue;

  Action(String description, int dbValue) {
    this.description = description;
    this.dbValue = dbValue;
  }

  public String getDescription() {
    return description;
  }

  public int getDbValue() {
    return dbValue;
  }

  public static Action of(int dbValue) {
    switch (dbValue) {
      case 0:
        return MINOR_OPTIMIZING;
      case 1:
        return MAJOR_OPTIMIZING;
      case 2:
        return EXTERNAL_OPTIMIZING;
      case 10:
        return REFRESH_SNAPSHOT;
      case 11:
        return EXPIRE_SNAPSHOTS;
      case 12:
        return CLEAN_ORPHANED_FILES;
      case 13:
        return HIVE_COMMIT_SYNC;
      default:
        throw new IllegalArgumentException("Unknown dbValue: " + dbValue);
    }
  }
}
