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

package org.apache.amoro.server.table.cleanup;

/**
 * Table cleanup operation enum. Defines different operation types for table cleanup tasks and their
 * corresponding codes. Each operation type has a unique code, which is used to identify the
 * operation type when persisting the cleanup process record in the table_cleanup_process table.
 */
public enum CleanupOperation {
  DANGLING_DELETE_FILES_CLEANING(11),
  ORPHAN_FILES_CLEANING(22),
  DATA_EXPIRING(33),
  SNAPSHOTS_EXPIRING(44),
  //  NONE(-1) indicates operation types where no cleanup process records are
  //  saved in the table_cleanup_process table.
  NONE(-1);

  private final int code;

  CleanupOperation(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static CleanupOperation fromCode(int code) {
    for (CleanupOperation op : values()) {
      if (op.code == code) {
        return op;
      }
    }
    return NONE;
  }
}
