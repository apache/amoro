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

package org.apache.amoro;

import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessState;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

public enum Action {
  MINOR_OPTIMIZING("minor-optimizing", 0),
  MAJOR_OPTIMIZING("major-optimizing", 1),
  EXTERNAL_OPTIMIZING("external-optimizing", 2),
  // refresh all metadata including snapshots, watermark, configurations, schema, etc.
  REFRESH_METADATA("refresh-metadata", 10),
  // expire all metadata and data files necessarily.
  EXPIRE_DATA("expire-data", 11),
  DELETE_ORPHAN_FILES("delete-orphan-files", 12),
  SYNC_HIVE_COMMIT("sync-hive-commit", 13);

  /**
   * Arbitrary actions are actions that can be handled by a single optimizer. The processes they
   * related to like refreshing, expiring, cleaning and syncing all share the same basic
   * implementations which are {@link TableProcess} and {@link TableProcessState} and they won't
   * have any spitted stages like optimizing processes(plan, execute, commit), so they can be easily
   * triggered and managed. If you want to add a new action which is handled stand-alone, you should
   * add it to this set, and you would find it's easy to implement the process and state.
   */
  public static final Set<Action> ARBITRARY_ACTIONS =
      Collections.unmodifiableSet(
          Sets.newHashSet(REFRESH_METADATA, EXPIRE_DATA, DELETE_ORPHAN_FILES, SYNC_HIVE_COMMIT));

  public static boolean isArbitrary(Action action) {
    return ARBITRARY_ACTIONS.contains(action);
  }

  private final String description;
  private final int code;

  Action(String description, int dbValue) {
    this.description = description;
    this.code = dbValue;
  }

  public String getDescription() {
    return description;
  }

  public int getCode() {
    return code;
  }

  public static Action of(int code) {
    for (Action action : Action.values()) {
      if (action.code == code) {
        return action;
      }
    }
    throw new IllegalArgumentException("No action with code: " + code);
  }
}
