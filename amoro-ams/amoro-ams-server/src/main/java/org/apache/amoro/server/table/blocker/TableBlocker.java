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
import org.apache.amoro.api.TableIdentifier;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.blocker.RenewableBlocker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableBlocker {
  private String catalog;
  private String database;
  private String tableName;
  private long blockerId;
  private List<String> operations;
  private long createTime;
  private long expirationTime;
  private Map<String, String> properties;

  public static boolean conflict(
      List<BlockableOperation> blockableOperations, List<TableBlocker> blockers) {
    return blockableOperations.stream().anyMatch(operation -> conflict(operation, blockers));
  }

  public static boolean conflict(
      BlockableOperation blockableOperation, List<TableBlocker> blockers) {
    return blockers.stream()
        .anyMatch(blocker -> blocker.getOperations().contains(blockableOperation.name()));
  }

  public static TableBlocker buildTableBlocker(
      TableIdentifier tableIdentifier,
      List<BlockableOperation> operations,
      Map<String, String> properties,
      long now,
      long blockerTimeout) {
    TableBlocker tableBlocker = new TableBlocker();
    tableBlocker.setCatalog(tableIdentifier.getCatalog());
    tableBlocker.setDatabase(tableIdentifier.getDatabase());
    tableBlocker.setTableName(tableIdentifier.getTableName());
    tableBlocker.setCreateTime(now);
    tableBlocker.setExpirationTime(now + blockerTimeout);
    tableBlocker.setOperations(
        operations.stream().map(BlockableOperation::name).collect(Collectors.toList()));
    HashMap<String, String> propertiesOfTableBlocker = new HashMap<>(properties);
    propertiesOfTableBlocker.put(RenewableBlocker.BLOCKER_TIMEOUT, blockerTimeout + "");
    tableBlocker.setProperties(propertiesOfTableBlocker);
    return tableBlocker;
  }

  public TableBlocker() {}

  public String getCatalog() {
    return catalog;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public void setTableIdentifier(TableIdentifier tableIdentifier) {
    this.catalog = tableIdentifier.getCatalog();
    this.database = tableIdentifier.getDatabase();
    this.tableName = tableIdentifier.getTableName();
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
        .add("catalog", catalog)
        .add("database", database)
        .add("tableName", tableName)
        .add("blockerId", blockerId)
        .add("operations", operations)
        .add("createTime", createTime)
        .add("expirationTime", expirationTime)
        .add("properties", properties)
        .toString();
  }
}
