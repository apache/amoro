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

/** Table summary for page of details */
public class TableSummary {
  private long file;
  private String size;
  private String averageFile;
  private String tableFormat;

  public TableSummary() {}

  public TableSummary(long file, String size, String averageFile, String tableFormat) {
    this.file = file;
    this.size = size;
    this.averageFile = averageFile;
    this.tableFormat = tableFormat;
  }

  /** Total size of table in human readable. */
  public String getSize() {
    return size;
  }

  /** Average file size in human readable */
  public String getAverageFile() {
    return averageFile;
  }

  /** Table type of specified table. */
  public String getTableFormat() {
    return tableFormat;
  }

  /** Total file nums. */
  public long getFile() {
    return file;
  }
}
