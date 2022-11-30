/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.ams.server.model;

import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class OverviewQuotaVO {

  private static final OptimizerService optimizerService = ServiceContainer.getOptimizerService();
  //eg:"10-09 14:48"
  private final List<String> timeLine = new ArrayList<>();
  //eg:"83.24"
  private final List<String> usedCpu = new ArrayList<>();
  //eg:"1828核/2196核"
  private final List<String> usedCpuDivision = new ArrayList<>();
  //eg:"83.24%"
  private final List<String> usedCpuPercent = new ArrayList<>();

  public static OverviewQuotaVO convert(List<MetricsSummary> data, OptimizerResourceInfo optimizerResourceInfo) {
    OverviewQuotaVO overviewQuotaVO = new OverviewQuotaVO();
    data.forEach(e -> {
      if (e.getMetricValue() == null || e.getCommitTime() == null) {
        return;
      }
      String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.getCommitTime()), ZoneId.systemDefault())
          .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
      String usedCpu = e.getMetricValue().setScale(2, RoundingMode.HALF_UP).toString();
      long usedCore =
          e.getMetricValue()
              .multiply(BigDecimal.valueOf(optimizerResourceInfo.getOccupationCore()))
              .divide(BigDecimal.valueOf(100))
              .setScale(0, RoundingMode.HALF_UP)
              .longValue();
      overviewQuotaVO.getTimeLine().add(time);
      overviewQuotaVO.getUsedCpu().add(usedCpu);
      overviewQuotaVO.getUsedCpuDivision().add(String.format("%s核/%s核", usedCore,
          optimizerResourceInfo.getOccupationCore()));
      overviewQuotaVO.getUsedCpuPercent().add(usedCpu + "%");
    });
    return overviewQuotaVO;
  }
}
