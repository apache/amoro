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

import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;

public class DefaultActions {

  public static final Action MINOR_OPTIMIZING = new DefaultAction(1, 10, "minor");
  public static final Action MAJOR_OPTIMIZING = new DefaultAction(2, 11, "major");
  public static final Action FULL_OPTIMIZING = new DefaultAction(3, 12, "full");
  // expire all metadata and data files necessarily.
  public static final Action EXPIRE_DATA = new DefaultAction(4, 1, "expire-data");
  public static final Action DELETE_ORPHAN_FILES = new DefaultAction(5, 2, "delete-orphans");
  public static final Action SYNC_HIVE_COMMIT = new DefaultAction(6, 3, "sync-hive");

  private static final Map<Integer, Action> ACTIONS =
      ImmutableMap.<Integer, Action>builder()
          .put(MINOR_OPTIMIZING.getCode(), MINOR_OPTIMIZING)
          .put(MAJOR_OPTIMIZING.getCode(), MAJOR_OPTIMIZING)
          .put(EXPIRE_DATA.getCode(), EXPIRE_DATA)
          .put(DELETE_ORPHAN_FILES.getCode(), DELETE_ORPHAN_FILES)
          .put(SYNC_HIVE_COMMIT.getCode(), SYNC_HIVE_COMMIT)
          .put(FULL_OPTIMIZING.getCode(), FULL_OPTIMIZING)
          .build();

  public static Action of(int code) {
    return Optional.ofNullable(ACTIONS.get(code))
        .orElseThrow(() -> new IllegalArgumentException("No action with code: " + code));
  }
}
