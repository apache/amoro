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

import org.apache.amoro.shade.guava32.com.google.common.base.MoreObjects;

public class OverviewSummary {

  private int catalogCnt;
  private int tableCnt;
  private long tableTotalSize;
  private int totalCpu;
  private long totalMemory;

  public OverviewSummary() {}

  public OverviewSummary(
      int catalogCnt, int tableCnt, long tableTotalSize, int totalCpu, long totalMemory) {
    this.catalogCnt = catalogCnt;
    this.tableCnt = tableCnt;
    this.tableTotalSize = tableTotalSize;
    this.totalCpu = totalCpu;
    this.totalMemory = totalMemory;
  }

  public int getCatalogCnt() {
    return catalogCnt;
  }

  public void setCatalogCnt(int catalogCnt) {
    this.catalogCnt = catalogCnt;
  }

  public int getTableCnt() {
    return tableCnt;
  }

  public void setTableCnt(int tableCnt) {
    this.tableCnt = tableCnt;
  }

  public long getTableTotalSize() {
    return tableTotalSize;
  }

  public void setTableTotalSize(long tableTotalSize) {
    this.tableTotalSize = tableTotalSize;
  }

  public int getTotalCpu() {
    return totalCpu;
  }

  public void setTotalCpu(int totalCpu) {
    this.totalCpu = totalCpu;
  }

  public long getTotalMemory() {
    return totalMemory;
  }

  public void setTotalMemory(long totalMemory) {
    this.totalMemory = totalMemory;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("catalogCnt", catalogCnt)
        .add("tableCnt", tableCnt)
        .add("tableTotalSize", tableTotalSize)
        .add("totalCpu", totalCpu)
        .add("totalMemory", totalMemory)
        .toString();
  }
}
