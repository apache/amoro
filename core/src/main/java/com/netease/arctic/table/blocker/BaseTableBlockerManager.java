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

package com.netease.arctic.table.blocker;

import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;

/**
 * Base {@link TableBlockerManager} implementation.
 */
public class BaseTableBlockerManager implements TableBlockerManager {

  private final TableIdentifier tableIdentifier;

  public BaseTableBlockerManager(TableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  // TODO
  private static final Blocker TEMP_BLOCKER = new BaseBlocker(
      "temp",
      Collections.emptyList(),
      System.currentTimeMillis(),
      System.currentTimeMillis() +
          TableProperties.TABLE_BLOCKER_TIMEOUT_DEFAULT,
      Maps.newHashMap());

  @Override
  public Blocker block(List<BlockableOperation> operations) {
    // TODO 
    return TEMP_BLOCKER;
  }

  @Override
  public void release(Blocker blocker) {
    // TODO
  }

  @Override
  public List<Blocker> getBlockers() {
    // TODO
    return Collections.singletonList(TEMP_BLOCKER);
  }

  public TableIdentifier tableIdentifier() {
    return tableIdentifier;
  }
}
