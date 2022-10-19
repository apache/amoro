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

package com.netease.arctic.ams.server.mapper;

import com.netease.arctic.ams.server.model.TableMetricsStatistic;
import com.netease.arctic.ams.server.model.TableRuntimeMetricsStatistic;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TableRuntimeMetricsStatisticMapper {
  String TABLE_NAME = "table_runtime_metric_statistics";

  @Insert("insert into " + TABLE_NAME + " (table_identifier, data_size, commit_time) values(" +
      "#{metricsStatistic.tableIdentifier, typeHandler=com.netease.arctic.ams.server.mybatis" +
      ".TableIdentifier2StringConverter}, #{metricsStatistic.dataSize},  #{metricsStatistic" +
      ".commitTime,typeHandler=com.netease.arctic.ams.server.mybatis.Long2TsConvertor})")
  void insertMetricsStatistic(@Param("metricsStatistic") TableRuntimeMetricsStatistic metricsStatistic);
}
