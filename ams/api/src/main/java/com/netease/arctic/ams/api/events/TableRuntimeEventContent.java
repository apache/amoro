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

package com.netease.arctic.ams.api.events;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.ams.api.TableIdentifier;

/**
 * Event content for {@link EventType#TableRuntimeAdded} and {@link EventType#TableRuntimeRemoved}
 */
public class TableRuntimeEventContent {

  private final TableIdentifier identifier;

  private final boolean internalCatalog;

  private final boolean onLoad;

  private final TableFormat format;

  public TableRuntimeEventContent(
      TableIdentifier identifier, TableFormat format, boolean internalCatalog, boolean onLoad) {
    this.identifier = identifier;
    this.format = format;
    this.internalCatalog = internalCatalog;
    this.onLoad = onLoad;
  }

  /**
   * Table identifier when table runtime is added or removed
   *
   * @return Table identifier
   */
  public TableIdentifier getIdentifier() {
    return identifier;
  }

  /**
   * Is this table under internal catalog
   *
   * @return True if event comes from internal catalog.
   */
  public boolean isInternalCatalog() {
    return internalCatalog;
  }

  /**
   * Is this event triggered when table runtime is loaded from metadata database.
   *
   * @return True if event triggered on load.
   */
  public boolean isOnLoad() {
    return onLoad;
  }

  /**
   * Table format of event
   *
   * @return Table format
   */
  public TableFormat getFormat() {
    return format;
  }
}
