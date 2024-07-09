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

public class ServerStatistics {

    private int catalogCnt;
    private int tableCnt;
    private int totalDataSizeInBytes;
    private int totalCpuCores;
    private int totalMemoryInGB;

    public ServerStatistics(int catalogCnt, int tableCnt, int totalDataSizeInBytes, int totalCpuCores, int totalMemoryInGB) {
        this.catalogCnt = catalogCnt;
        this.tableCnt = tableCnt;
        this.totalDataSizeInBytes = totalDataSizeInBytes;
        this.totalCpuCores = totalCpuCores;
        this.totalMemoryInGB = totalMemoryInGB;
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

    public int getTotalDataSizeInBytes() {
        return totalDataSizeInBytes;
    }

    public void setTotalDataSizeInBytes(int totalDataSizeInBytes) {
        this.totalDataSizeInBytes = totalDataSizeInBytes;
    }

    public int getTotalCpuCores() {
        return totalCpuCores;
    }

    public void setTotalCpuCores(int totalCpuCores) {
        this.totalCpuCores = totalCpuCores;
    }

    public int getTotalMemoryInGB() {
        return totalMemoryInGB;
    }

    public void setTotalMemoryInGB(int totalMemoryInGB) {
        this.totalMemoryInGB = totalMemoryInGB;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("catalogCnt", catalogCnt)
                .add("tableCnt", tableCnt)
                .add("totalDataSizeInBytes", totalDataSizeInBytes)
                .add("totalCpuCores", totalCpuCores)
                .add("totalMemoryInGB", totalMemoryInGB)
                .toString();
    }
}
