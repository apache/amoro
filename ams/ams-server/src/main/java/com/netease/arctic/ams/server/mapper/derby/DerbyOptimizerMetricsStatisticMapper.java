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

import com.netease.arctic.ams.server.mapper.OptimizerMetricsStatisticMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface DerbyOptimizerMetricsStatisticMapper extends OptimizerMetricsStatisticMapper {

  String TABLE_NAME = "optimizer_metric_statistics";

  @Insert("insert into metric_statistics_summary (metric_name, metric_value,commit_time) select metric_name, VARCHAR " +
      "(CHAR(avg(DOUBLE (metric_value)))), #{commitTime, typeHandler=com.netease.arctic.ams" +
      ".server.mybatis.Long2TsConvertor} from " + TABLE_NAME + " where metric_name = #{metricName} group by " +
      "metric_name")
  void summaryMetrics(
      @Param("metricName") String metricName,
      @Param("commitTime") long commitTime);
}
