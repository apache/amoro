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

package org.apache.amoro.server.table.blocker;

import org.apache.amoro.api.BlockableOperation;
import org.apache.amoro.api.Blocker;
import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.blocker.RenewableBlocker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableBlocker {
  private ServerTableIdentifier tableIdentifier;
  private long blockerId;
  private List<String> operations;
  private long createTime;
  private long expirationTime;
  private Map<String, String> properties;

  public TableBlocker() {}

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public void setTableIdentifier(ServerTableIdentifier tableIdentifier) {
    this.tableIdentifier = tableIdentifier;
  }

  public long getBlockerId() {
    return blockerId;
  }

  public void setBlockerId(long blockerId) {
    this.blockerId = blockerId;
  }

  public List<String> getOperations() {
    return operations;
  }

  public void setOperations(List<String> operations) {
    this.operations = operations;
  }

  public long getCreateTime() {
    return createTime;
  }

  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  public long getExpirationTime() {
    return expirationTime;
  }

  public void setExpirationTime(long expirationTime) {
    this.expirationTime = expirationTime;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public Blocker buildBlocker() {
    Map<String, String> properties = this.properties == null ? Maps.newHashMap() : this.properties;
    properties.put(RenewableBlocker.CREATE_TIME_PROPERTY, createTime + "");
    properties.put(RenewableBlocker.EXPIRATION_TIME_PROPERTY, expirationTime + "");
    List<BlockableOperation> operations =
        getOperations().stream().map(BlockableOperation::valueOf).collect(Collectors.toList());
    return new Blocker(blockerId + "", operations, properties);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableIdentifier", tableIdentifier)
        .add("blockerId", blockerId)
        .add("operations", operations)
        .add("createTime", createTime)
        .add("expirationTime", expirationTime)
        .add("properties", properties)
        .toString();
  }
}
