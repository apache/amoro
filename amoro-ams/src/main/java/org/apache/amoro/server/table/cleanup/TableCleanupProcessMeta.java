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
 * The class for table used when transfer data from/to database. Maps to table_cleanup_process table
 * in database.
 */
public class TableCleanupProcessMeta {
  // Maps to cleanup_process_id column in database
  private long cleanupProcessId;
  // Maps to table_id column in database
  private Long tableId;
  // Maps to catalog_name column in database
  private String catalogName;
  // Maps to db_name column in database
  private String dbName;
  // Maps to table_name column in database
  private String tableName;
  // Maps to cleanup_operation_code column in database
  private CleanupOperation cleanupOperation;
  // Maps to last_cleanup_end_time column in database
  private long lastCleanupEndTime;

  public TableCleanupProcessMeta() {}

  public TableCleanupProcessMeta(
      long cleanupProcessId,
      Long tableId,
      String catalogName,
      String dbName,
      String tableName,
      CleanupOperation cleanupOperation,
      long lastCleanupEndTime) {
    this.cleanupProcessId = cleanupProcessId;
    this.tableId = tableId;
    this.catalogName = catalogName;
    this.dbName = dbName;
    this.tableName = tableName;
    this.cleanupOperation = cleanupOperation;
    this.lastCleanupEndTime = lastCleanupEndTime;
  }

  public TableCleanupProcessMeta(
      long cleanupProcessId,
      Long tableId,
      String catalogName,
      String dbName,
      String tableName,
      CleanupOperation cleanupOperation) {
    this.cleanupProcessId = cleanupProcessId;
    this.tableId = tableId;
    this.catalogName = catalogName;
    this.dbName = dbName;
    this.tableName = tableName;
    this.cleanupOperation = cleanupOperation;
  }

  public TableCleanupProcessMeta(Long tableId, long lastCleanupEndTime) {

    this.tableId = tableId;
    this.lastCleanupEndTime = lastCleanupEndTime;
  }

  public long getCleanupProcessId() {
    return cleanupProcessId;
  }

  public void setCleanupProcessId(long cleanupProcessId) {
    this.cleanupProcessId = cleanupProcessId;
  }

  public Long getTableId() {
    return tableId;
  }

  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  public String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public String getDbName() {
    return dbName;
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public CleanupOperation getCleanupOperation() {
    return cleanupOperation;
  }

  public void setCleanupOperation(CleanupOperation cleanupOperation) {
    this.cleanupOperation = cleanupOperation;
  }

  public long getLastCleanupEndTime() {
    return lastCleanupEndTime;
  }

  public void setLastCleanupEndTime(long lastCleanupEndTime) {
    this.lastCleanupEndTime = lastCleanupEndTime;
  }
}
