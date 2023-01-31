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

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.BlockableOperation;
import com.netease.arctic.ams.api.OperationConflictException;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base {@link TableBlockerManager} implementation.
 */
public class BaseTableBlockerManager implements TableBlockerManager {
  private static final Logger LOG = LoggerFactory.getLogger(BaseTableBlockerManager.class);

  private final TableIdentifier tableIdentifier;
  private final AmsClient client;

  public BaseTableBlockerManager(TableIdentifier tableIdentifier, AmsClient client) {
    this.tableIdentifier = tableIdentifier;
    this.client = client;
  }

  public static TableBlockerManager build(TableIdentifier tableIdentifier, AmsClient amsClient) {
    return new BaseTableBlockerManager(tableIdentifier, amsClient);
  }

  @Override
  public Blocker block(List<BlockableOperation> operations, Map<String, String> properties)
      throws OperationConflictException {
    try {
      Blocker blocker =
          BlockerFactory.buildBlocker(client.block(tableIdentifier.buildTableIdentifier(), operations, properties));
      if (blocker instanceof RenewableBlocker) {
        ((RenewableBlocker) blocker).onBlocked(client, tableIdentifier);
      }
      return blocker;
    } catch (OperationConflictException e) {
      throw e;
    } catch (TException e) {
      throw new IllegalStateException("failed to block table " + tableIdentifier + " with " + operations, e);
    }
  }

  @Override
  public void release(Blocker blocker) {
    try {
      client.releaseBlocker(tableIdentifier.buildTableIdentifier(), blocker.blockerId());
      if (blocker instanceof RenewableBlocker) {
        ((RenewableBlocker) blocker).onReleased(tableIdentifier);
      }
    } catch (TException e) {
      throw new IllegalStateException("failed to release " + tableIdentifier + "'s blocker " + blocker.blockerId(), e);
    }
  }

  @Override
  public List<Blocker> getBlockers() {
    try {
      return client.getBlockers(tableIdentifier.buildTableIdentifier())
          .stream().map(RenewableBlocker::of).collect(Collectors.toList());
    } catch (TException e) {
      throw new IllegalStateException("failed to get blockers of " + tableIdentifier, e);
    }
  }

  public TableIdentifier tableIdentifier() {
    return tableIdentifier;
  }

  public static class BlockerFactory {
    public static Blocker buildBlocker(com.netease.arctic.ams.api.Blocker blocker) {
      if (blocker.getProperties() != null &&
          blocker.getProperties().get(RenewableBlocker.EXPIRATION_TIME_PROPERTY) != null) {
        return RenewableBlocker.of(blocker);
      }
      throw new IllegalArgumentException("illegal blocker " + blocker);
    }
  }
}
