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

public enum Action {
  OPTIMIZING("optimizing", 0),
  REFRESH_SNAPSHOT("refreshing", 1),
  EXPIRE_SNAPSHOTS("expiring", 2),
  EXPIRE_PROCESS("clean_meta", 3),
  CLEAN_ORPHANED_FILES("clean_orphaned", 4),
  HIVE_COMMIT_SYNC("sync_hive", 5);

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
}
