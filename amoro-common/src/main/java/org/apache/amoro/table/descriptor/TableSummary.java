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

package org.apache.amoro.table.descriptor;

/** Table summary for page of details */
public class TableSummary {
  private long file;
  private String size;
  private String averageFile;
  private String tableFormat;
  private long records;
  private String optimizingStatus;
  private int healthScore = -1; // -1 means not calculated
  private int smallFileScore = 0;
  private int equalityDeleteScore = 0;
  private int positionalDeleteScore = 0;

  public TableSummary() {}

  public TableSummary(
      long file, String size, String averageFile, long records, String tableFormat) {
    this.file = file;
    this.size = size;
    this.averageFile = averageFile;
    this.records = records;
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

  /** Total records of table. */
  public long getRecords() {
    return records;
  }

  /** Current table optimizing status */
  public String getOptimizingStatus() {
    return optimizingStatus;
  }

  public void setOptimizingStatus(String optimizingStatus) {
    this.optimizingStatus = optimizingStatus;
  }

  /** Current table health score */
  public int getHealthScore() {
    return healthScore;
  }

  public void setHealthScore(int healthScore) {
    this.healthScore = healthScore;
  }

  /** Current table small files score */
  public int getSmallFileScore() {
    return smallFileScore;
  }

  public void setSmallFileScore(int smallFileScore) {
    this.smallFileScore = smallFileScore;
  }

  /** Current table equality delete records ratio score */
  public int getEqualityDeleteScore() {
    return equalityDeleteScore;
  }

  public void setEqualityDeleteScore(int equalityDeleteScore) {
    this.equalityDeleteScore = equalityDeleteScore;
  }

  /** Current table positional delete records ratio score */
  public int getPositionalDeleteScore() {
    return positionalDeleteScore;
  }

  public void setPositionalDeleteScore(int positionalDeleteScore) {
    this.positionalDeleteScore = positionalDeleteScore;
  }
}
