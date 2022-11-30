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

package com.netease.arctic.ams.server.mapper.derby;

import com.netease.arctic.ams.server.mapper.MetricsSummaryMapper;
import com.netease.arctic.ams.server.model.MetricsSummary;
import com.netease.arctic.ams.server.mybatis.Long2TsConvertor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface DerbyMetricsSummaryMapper extends MetricsSummaryMapper {
  @Select("select metric_name, metric_value, commit_time from " + TABLE_NAME + " where metric_name = #{metricName} " +
      "order by commit_time desc first 1 rows only")
  @Results({
      @Result(column = "metric_name", property = "metricName"),
      @Result(column = "metric_value", property = "metricValue"),
      @Result(column = "commit_time", property = "commitTime",
          typeHandler = Long2TsConvertor.class)
  })
  MetricsSummary getCurrentSummary(@Param("metricName") String metricName);
}
