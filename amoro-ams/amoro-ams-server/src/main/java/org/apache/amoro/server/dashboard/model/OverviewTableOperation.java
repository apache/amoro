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

package org.apache.amoro.server.dashboard.model;

import org.apache.amoro.api.ServerTableIdentifier;
import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

public class OverviewTableOperation {
  private final ServerTableIdentifier tableIdentifier;
  private String tableName;
  private long ts;
  private String operation;

  public OverviewTableOperation(ServerTableIdentifier tableIdentifier, DDLInfo ddlInfo) {
    this.tableIdentifier = tableIdentifier;
    this.tableName =
        tableIdentifier
            .getCatalog()
            .concat(".")
            .concat(tableIdentifier.getDatabase())
            .concat(".")
            .concat(tableIdentifier.getTableName());
    this.ts = ddlInfo.getCommitTime();
    this.operation = ddlInfo.getDdl();
  }

  public OverviewTableOperation(ServerTableIdentifier tableIdentifier, long ts, String operation) {
    this.tableIdentifier = tableIdentifier;
    this.tableName =
        tableIdentifier
            .getCatalog()
            .concat(".")
            .concat(tableIdentifier.getDatabase())
            .concat(".")
            .concat(tableIdentifier.getTableName());
    this.ts = ts;
    this.operation = operation;
  }

  public ServerTableIdentifier getTableIdentifier() {
    return tableIdentifier;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public long getTs() {
    return ts;
  }

  public void setTs(long ts) {
    this.ts = ts;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("tableName", tableName)
        .add("ts", ts)
        .add("operation", operation)
        .toString();
  }
}
