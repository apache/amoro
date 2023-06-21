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

package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.server.model.CoreInfo;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.model.TableTaskHistory;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.IQuotaService;
import com.netease.arctic.ams.server.service.ITableTaskHistoryService;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.util.PropertyUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class QuotaService implements IQuotaService {
  private final ITableTaskHistoryService tableTaskHistoryService;
  private final IMetaService metaService;

  public QuotaService(ITableTaskHistoryService tableTaskHistoryService, IMetaService metaService) {
    this.tableTaskHistoryService = tableTaskHistoryService;
    this.metaService = metaService;
  }

  @Override
  public CoreInfo getTableResourceInfo(TableIdentifier tableIdentifier, long period) {
    CoreInfo result = new CoreInfo();

    long endTime = System.currentTimeMillis();
    long startTime = endTime - period;
    List<TableTaskHistory> tableTaskHistoryList = tableTaskHistoryService
        .selectTaskHistoryByTableIdAndTime(tableIdentifier, startTime, endTime);

    result.setRealCoreCount(
        new BigDecimal(calculateTotalCostTime(tableTaskHistoryList, startTime, endTime)).divide(new BigDecimal(period),
            2, RoundingMode.HALF_UP).doubleValue());

    TableMetadata tableMetadata = metaService
        .loadTableMetadata(tableIdentifier);
    double quota = PropertyUtil.propertyAsDouble(tableMetadata.getProperties(),
        TableProperties.OPTIMIZE_QUOTA, TableProperties.OPTIMIZE_QUOTA_DEFAULT);
    result.setNeedCoreCount(quota);
    return result;
  }

  private long calculateTotalCostTime(List<TableTaskHistory> tableTaskHistoryList, long startTime, long endTime) {
    long totalCostTime = 0;
    for (TableTaskHistory tableTaskHistory : tableTaskHistoryList) {
      if (tableTaskHistory.getCostTime() != 0 &&
          tableTaskHistory.getStartTime() >= startTime &&
          tableTaskHistory.getEndTime() <= endTime) {
        totalCostTime = totalCostTime + tableTaskHistory.getCostTime();
      } else {
        long realStartTime = Math.max(startTime, tableTaskHistory.getStartTime());
        long realEndTime = tableTaskHistory.getEndTime() == 0 ?
            endTime : Math.min(tableTaskHistory.getEndTime(), endTime);
        totalCostTime = totalCostTime + realEndTime - realStartTime;
      }
    }

    return totalCostTime;
  }

  @Override
  public void close() throws IOException {
    
  }
}
