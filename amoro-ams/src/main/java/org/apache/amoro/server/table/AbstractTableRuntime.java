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

package org.apache.amoro.server.table;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.SupportsProcessPlugins;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.table.TableRuntimeStore;

public abstract class AbstractTableRuntime extends PersistentBase
    implements TableRuntime, SupportsProcessPlugins {

  private final TableRuntimeStore store;

  protected AbstractTableRuntime(TableRuntimeStore store) {
    this.store = store;
  }

  public TableRuntimeStore store() {
    return store;
  }

  @Override
  public ServerTableIdentifier getTableIdentifier() {
    return store().getTableIdentifier();
  }

  @Override
  public TableConfiguration getTableConfiguration() {
    return TableConfigurations.parseTableConfig(store().getTableConfig());
  }

  @Override
  public String getGroupName() {
    return store().getGroupName();
  }

  public int getStatusCode() {
    return store().getStatusCode();
  }

  @Override
  public void dispose() {
    store().dispose();
  }
}
