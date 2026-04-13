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

package org.apache.amoro.server.dashboard.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.javalin.http.Context;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.mapper.TableProcessMapper;
import org.apache.amoro.server.process.TableProcessMeta;
import org.apache.amoro.server.table.TableManager;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.table.TableIdentifier;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

public class ProcessController extends PersistentBase {

  private final TableManager tableManager;

  public ProcessController(TableManager tableManager) {
    this.tableManager = tableManager;
  }

  public void getTableProcesses(Context ctx) {
    String catalog = ctx.pathParam("catalog");
    String db = ctx.pathParam("db");
    String table = ctx.pathParam("table");
    String processType = ctx.queryParam("type");
    String status = ctx.queryParam("status");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    if (StringUtils.isBlank(processType)) {
      processType = null;
    }

    Preconditions.checkArgument(page >= 1, "page[%s] must >= 1", page);
    Preconditions.checkArgument(pageSize > 0, "pageSize[%s] must > 0", pageSize);

    TableIdentifier tableIdentifier = TableIdentifier.of(catalog, db, table);
    ServerTableIdentifier serverTableIdentifier =
        tableManager.getServerTableIdentifier(tableIdentifier.buildTableIdentifier());
    if (serverTableIdentifier == null) {
      ctx.json(OkResponse.of(PageResult.of(Collections.emptyList(), 0)));
      return;
    }

    long tableId = serverTableIdentifier.getId();
    ProcessStatus processStatus =
        StringUtils.isBlank(status) ? null : ProcessStatus.valueOf(status);

    int total = 0;
    List<TableProcessMeta> processMetaList = Collections.emptyList();
    final String finalProcessType = processType;
    try (Page<?> ignored = PageHelper.startPage(page, pageSize, true)) {
      processMetaList =
          getAs(
              TableProcessMapper.class,
              mapper -> mapper.listProcessMeta(tableId, finalProcessType, processStatus));
      PageInfo<TableProcessMeta> pageInfo = new PageInfo<>(processMetaList);
      total = (int) pageInfo.getTotal();
    }

    ctx.json(OkResponse.of(PageResult.of(processMetaList, total)));
  }
}
