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

package org.apache.amoro.server.dashboard;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.ProcessCategory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Format-independent queries of AMS cleanup records, without optimizing task metrics. */
public class TableProcessQuery extends PersistentBase {
  static boolean handles(FormatTableDescriptor descriptor, String category, String type) {
    return ProcessCategory.CLEANUP.getName().equalsIgnoreCase(category)
        && (StringUtils.isBlank(type)
            || descriptor.getProcessTypesByCategory(category).contains(type));
  }

  Pair<List<OptimizingProcessInfo>, Integer> list(
      TableIdentifier table,
      List<String> actions,
      String type,
      ProcessStatus status,
      int limit,
      int offset) {
    ServerTableIdentifier identifier =
        getAs(
            TableMetaMapper.class,
            mapper ->
                mapper.selectTableIdentifier(
                    table.getCatalog(), table.getDatabase(), table.getTableName()));
    return list(identifier, actions, type, status, limit, offset);
  }

  public Pair<List<OptimizingProcessInfo>, Integer> list(
      ServerTableIdentifier table,
      List<String> actions,
      String type,
      ProcessStatus status,
      int limit,
      int offset) {
    String filterType = StringUtils.isBlank(type) ? null : type;
    if (table == null || (filterType == null && (actions == null || actions.isEmpty()))) {
      return Pair.of(Collections.emptyList(), 0);
    }
    try (Page<?> ignored = PageHelper.startPage(offset / limit + 1, limit, true)) {
      List<TableProcessMeta> records =
          getAs(
              TableProcessMapper.class,
              mapper ->
                  mapper.listProcessMeta(
                      table.getId(), filterType, filterType == null ? actions : null, status));
      PageInfo<TableProcessMeta> page = new PageInfo<>(records);
      if (page.getSize() == 0) {
        return Pair.of(Collections.emptyList(), 0);
      }
      return Pair.of(
          records.stream().map(meta -> toInfo(table, meta)).collect(Collectors.toList()),
          (int) page.getTotal());
    }
  }

  private static OptimizingProcessInfo toInfo(ServerTableIdentifier table, TableProcessMeta meta) {
    OptimizingProcessInfo result = new OptimizingProcessInfo();
    result.setTableId(meta.getTableId());
    result.setCatalogName(table.getCatalog());
    result.setDbName(table.getDatabase());
    result.setTableName(table.getTableName());
    result.setProcessId(String.valueOf(meta.getProcessId()));
    result.setStartTime(meta.getCreateTime());
    result.setFinishTime(meta.getFinishTime());
    result.setDuration(
        (meta.getFinishTime() > 0 ? meta.getFinishTime() : System.currentTimeMillis())
            - meta.getCreateTime());
    result.setOptimizingType(meta.getProcessType());
    result.setStatus(meta.getStatus());
    result.setFailReason(meta.getFailMessage());
    result.setInputFiles(new FilesStatistics());
    result.setOutputFiles(new FilesStatistics());
    result.setSummary(meta.getSummary());
    return result;
  }
}
