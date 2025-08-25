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

package org.apache.amoro.server.persistence;

import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableSummary;

import java.util.HashMap;
import java.util.Map;

/** The class for table used when transfer data from/to database. */
public class TableRuntimeMeta {
  private Long tableId;
  private String groupName;
  private Integer statusCode;
  private long statusCodeUpdateTime = System.currentTimeMillis();
  private Map<String, String> tableConfig = new HashMap<>();
  private TableSummary tableSummary = new TableSummary();

  public TableRuntimeMeta copy() {
    TableRuntimeMeta meta = new TableRuntimeMeta();
    meta.setTableId(this.tableId);
    meta.setGroupName(this.groupName);
    meta.setStatusCode(this.statusCode);
    meta.setStatusCodeUpdateTime(this.statusCodeUpdateTime);
    meta.setTableConfig(Maps.newHashMap(this.tableConfig));
    meta.setTableSummary(this.tableSummary.copy());
    return meta;
  }

  public Long getTableId() {
    return tableId;
  }

  public String getGroupName() {
    return groupName;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public long getStatusCodeUpdateTime() {
    return statusCodeUpdateTime;
  }

  public Map<String, String> getTableConfig() {
    return tableConfig;
  }

  public TableSummary getTableSummary() {
    return tableSummary == null ? new TableSummary() : tableSummary;
  }

  public void setTableId(Long tableId) {
    this.tableId = tableId;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
    this.statusCodeUpdateTime = System.currentTimeMillis();
  }

  public void setStatusCodeUpdateTime(long statusCodeUpdateTime) {
    this.statusCodeUpdateTime = statusCodeUpdateTime;
  }

  public void setTableConfig(Map<String, String> tableConfig) {
    this.tableConfig = tableConfig;
  }

  public void setTableSummary(TableSummary tableSummary) {
    this.tableSummary = tableSummary;
  }
}
