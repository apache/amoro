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

import com.netease.arctic.ams.server.mapper.TableMetricsStatisticMapper;
import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.mybatis.TableIdentifier2StringConverter;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DerbyTableMetricsStatisticMapper extends TableMetricsStatisticMapper {
  @Insert("insert into metric_statistics_summary (metric_name, metric_value,commit_time) select #{metricName}, " +
      "sum(metric_value), #{commitTime, typeHandler=com.netease.arctic.ams.server.mybatis" +
      ".Long2TsConvertor} from " + TABLE_NAME + " where metric_name = #{metricName}")
  void summaryMetrics(
      @Param("metricName") String metricName,
      @Param("commitTime") long commitTime);

  @Select("select table_identifier, metric_name, sum(metric_value) as sum_metric from " + TABLE_NAME +
      " where metric_name = #{metricName} group by table_identifier, metric_name order by sum_metric" +
      " #{orderExpression} first #{limit} rows only")
  @Results({
      @Result(column = "table_identifier", property = "tableIdentifier",
          typeHandler = TableIdentifier2StringConverter.class),
      @Result(column = "metric_name", property = "metricName"),
      @Result(column = "sum_metric", property = "metricValue")
  })
  List<TableMetricsStatistic> getTableMetricsOrdered(@Param("orderExpression") String orderExpression,
      @Param("metricName") String metricName,
      @Param("limit") Integer limit);
}
