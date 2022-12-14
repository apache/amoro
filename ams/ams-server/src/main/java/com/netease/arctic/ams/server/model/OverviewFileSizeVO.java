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
public class OverviewFileSizeVO {
  private final List<String> timeLine = new ArrayList<>();
  //The unit of measurement is GB
  private final List<Double> size = new ArrayList<>();

  public static OverviewFileSizeVO convert(List<MetricsSummary> data) {
    OverviewFileSizeVO overviewFileSizeVO = new OverviewFileSizeVO();
    data.forEach(e -> {
      if (e.getMetricValue() == null || e.getCommitTime() == null) {
        return;
      }
      String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.getCommitTime()), ZoneId.systemDefault())
          .format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
      double size =
          e.getMetricValue()
              .divide(BigDecimal.valueOf(Math.pow(1024, 3)))
              .setScale(2, RoundingMode.HALF_UP)
              .doubleValue();
      overviewFileSizeVO.getSize().add(size);
      overviewFileSizeVO.getTimeLine().add(time);
    });
    return overviewFileSizeVO;
  }
}
