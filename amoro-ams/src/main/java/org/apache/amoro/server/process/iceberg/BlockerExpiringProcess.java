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

package org.apache.amoro.server.process.iceberg;

import org.apache.amoro.Action;
import org.apache.amoro.IcebergActions;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.LocalProcess;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableBlockerMapper;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** Local table process for expiring expired table blockers. */
public class BlockerExpiringProcess extends TableProcess implements LocalProcess {

  private static final Logger LOG = LoggerFactory.getLogger(BlockerExpiringProcess.class);

  private final Persistency persistency = new Persistency();

  public BlockerExpiringProcess(TableRuntime tableRuntime, ExecuteEngine engine) {
    super(tableRuntime, engine);
  }

  @Override
  public String tag() {
    return getAction().getName().toLowerCase();
  }

  @Override
  public void run() {
    try {
      persistency.doExpiring(tableRuntime);
    } catch (Throwable t) {
      LOG.error("table {} expire blocker failed.", tableRuntime.getTableIdentifier(), t);
      throw new RuntimeException(t);
    }
  }

  @Override
  public Action getAction() {
    return IcebergActions.EXPIRE_BLOCKER;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Maps.newHashMap();
  }

  @Override
  public Map<String, String> getSummary() {
    return Maps.newHashMap();
  }

  private static class Persistency extends PersistentBase {

    public void doExpiring(TableRuntime tableRuntime) {
      String catalog = tableRuntime.getTableIdentifier().getCatalog();
      String database = tableRuntime.getTableIdentifier().getDatabase();
      String table = tableRuntime.getTableIdentifier().getTableName();
      doAs(
          TableBlockerMapper.class,
          mapper ->
              mapper.deleteExpiredBlockers(catalog, database, table, System.currentTimeMillis()));
    }
  }
}
