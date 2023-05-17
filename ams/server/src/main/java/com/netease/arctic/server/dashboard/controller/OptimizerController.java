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

package com.netease.arctic.server.dashboard.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.ams.api.resource.ResourceGroup;
import com.netease.arctic.ams.api.resource.ResourceType;
import com.netease.arctic.server.dashboard.model.OptimizerResourceInfo;
import com.netease.arctic.server.dashboard.model.TableOptimizingInfo;
import com.netease.arctic.server.dashboard.response.ErrorResponse;
import com.netease.arctic.server.dashboard.response.OkResponse;
import com.netease.arctic.server.dashboard.response.PageResult;
import com.netease.arctic.server.dashboard.utils.OptimizingUtil;
import com.netease.arctic.server.resource.OptimizerInstance;
import com.netease.arctic.server.resource.OptimizerManager;
import com.netease.arctic.server.resource.ResourceContainers;
import com.netease.arctic.server.table.ServerTableIdentifier;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.table.TableService;
import io.javalin.http.Context;
import io.javalin.http.HttpCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * optimize controller.
 *
 * @Description: optimizer is a task to compact small files in arctic table.
 * OptimizerController is the optimizer interface's controller, through this interface, you can get the optimized table,
 * optimizer task, optimizer group information, scale out or release optimizer, etc.
 */
public class OptimizerController extends RestBaseController {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerController.class);

  private static final String ALL_GROUP = "all";
  private final TableService tableService;
  private final OptimizerManager optimizerManager;

  public OptimizerController(TableService tableService, OptimizerManager optimizerManager) {
    this.tableService = tableService;
    this.optimizerManager = optimizerManager;
  }

  /**
   * get optimize tables.
   * * @return List of {@link TableOptimizingInfo}
   */
  public void getOptimizerTables(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    int offset = (page - 1) * pageSize;

    try {
      List<TableRuntime> tableRuntimes = new ArrayList<>();
      List<ServerTableIdentifier> tables = tableService.listTables();
      for (ServerTableIdentifier identifier : tables) {
        TableRuntime tableRuntime = tableService.get(identifier);
        if (tableRuntime == null) {
          continue;
        }
        if (ALL_GROUP.equals(optimizerGroup) || tableRuntime.getOptimizerGroup().equals(optimizerGroup)) {
          tableRuntimes.add(tableRuntime);
        }
      }
      tableRuntimes.sort((o1, o2) -> {
        // first we compare the status , and then we compare the start time when status are equal;
        int statDiff = o1.getOptimizingStatus().compareTo(o2.getOptimizingStatus());
        // status order is asc, startTime order is desc
        if (statDiff == 0) {
          long timeDiff = o1.getCurrentStatusStartTime() - o2.getCurrentStatusStartTime();
          return timeDiff >= 0 ? (timeDiff == 0 ? 0 : -1) : 1;
        } else {
          return statDiff;
        }
      });
      PageResult<TableRuntime, TableOptimizingInfo> amsPageResult = PageResult.of(tableRuntimes,
          offset, pageSize, OptimizingUtil::buildTableOptimizeInfo);
      ctx.json(OkResponse.of(amsPageResult));
    } catch (Exception e) {
      LOG.error("Failed to get optimizerGroup tables", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get optimizerGroup tables", ""));
    }
  }

  /**
   * get optimizers.
   */
  public void getOptimizers(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    int offset = (page - 1) * pageSize;

    try {
      List<OptimizerInstance> optimizers;
      if (optimizerGroup.equals("all")) {
        optimizers = optimizerManager.listOptimizers();
      } else {
        optimizers = optimizerManager.listOptimizers(optimizerGroup);
      }
      List<JSONObject> result = optimizers.stream().map(e -> {
        JSONObject jsonObject = (JSONObject) JSON.toJSON(e);
        jsonObject.put("jobId", e.getResourceId());
        jsonObject.put("optimizerGroup", e.getGroupName());
        jsonObject.put("coreNumber", e.getThreadCount());
        jsonObject.put("memory", e.getMemoryMb());
        jsonObject.put("jobStatus", "RUNNING");
        jsonObject.put("container", e.getContainerName());
        return jsonObject;
      }).collect(Collectors.toList());

      PageResult<JSONObject, JSONObject> amsPageResult = PageResult.of(result,
          offset, pageSize);
      ctx.json(OkResponse.of(amsPageResult));
    } catch (Exception e) {
      LOG.error("Failed to get optimizer", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST,
          "Failed to get optimizer", ""));
    }
  }

  /**
   * get optimizerGroup: optimizerGroupId, optimizerGroupName
   * url = /optimizerGroups.
   */
  public void getOptimizerGroups(Context ctx) {
    try {
      List<JSONObject> result = optimizerManager.listResourceGroups().stream()
          .filter(resourceGroup -> !ResourceContainers.EXTERNAL_CONTAINER_NAME.equals(resourceGroup.getContainer()))
          .map(e -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("optimizerGroupName", e.getName());
            return jsonObject;
          }).collect(Collectors.toList());
      ctx.json(OkResponse.of(result));
    } catch (Exception e) {
      LOG.error("Failed to get optimizerGroups", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST,
          "Failed to get optimizerGroups", ""));
    }
  }

  /**
   * get optimizer info: occupationCore, occupationMemory
   */
  public void getOptimizerGroupInfo(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    try {
      List<OptimizerInstance> optimizers;
      if (optimizerGroup.equals("all")) {
        optimizers = optimizerManager.listOptimizers();
      } else {
        optimizers = optimizerManager.listOptimizers(optimizerGroup);
      }
      OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
      optimizers.forEach(e -> {
        optimizerResourceInfo.addOccupationCore(e.getThreadCount());
        optimizerResourceInfo.addOccupationMemory(e.getMemoryMb());
      });
      ctx.json(OkResponse.of(optimizerResourceInfo));
    } catch (Exception e) {
      LOG.error("Failed to get optimizerGroup info", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to get optimizerGroup info", ""));
    }
  }

  /**
   * release optimizer.
   *
   * @pathParam jobId
   */
  public void releaseOptimizer(Context ctx) {
    String resourceId = ctx.pathParam("jobId");
    if (resourceId.isEmpty()) {
      ctx.json(ErrorResponse.of("resource id can not be empty, maybe it's a external optimizer"));
      return;
    }
    try {
      List<OptimizerInstance> optimizerInstances = optimizerManager.listOptimizers()
          .stream()
          .filter(e -> resourceId.equals(e.getResourceId()))
          .collect(Collectors.toList());
      Preconditions.checkState(optimizerInstances.size() > 0, String.format("The resource ID %s has not been indexed" +
          " to any optimizer.", resourceId));
      Resource resource = optimizerManager.getResource(resourceId);
      resource.getProperties().putAll(optimizerInstances.get(0).getProperties());
      ResourceContainers.get(resource.getContainerName()).releaseOptimizer(resource);
      optimizerManager.deleteResource(resourceId);
      optimizerManager.deleteOptimizer(resource.getGroupName(), resourceId);
      ctx.json(OkResponse.of("Success to release optimizer"));
    } catch (Exception e) {
      LOG.error("Failed to release optimizer", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to release optimizer", ""));
    }
  }

  /**
   * scale out optimizers, url:/optimizerGroups/{optimizerGroup}/optimizers.
   */
  public void scaleOutOptimizer(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    Map<String, Integer> map = ctx.bodyAsClass(Map.class);
    int parallelism = map.get("parallelism");

    ResourceGroup resourceGroup = optimizerManager.getResourceGroup(optimizerGroup);
    Resource resource = new Resource.Builder(resourceGroup.getContainer(), resourceGroup.getName(),
        ResourceType.OPTIMIZER)
        .setProperties(resourceGroup.getProperties())
        .setThreadCount(parallelism)
        .build();
    try {
      ResourceContainers.get(resource.getContainerName()).requestResource(resource);
      optimizerManager.createResource(resource);
      ctx.json(OkResponse.of("success to scaleOut optimizer"));
    } catch (Exception e) {
      LOG.error("Failed to scaleOut optimizer", e);
      ctx.json(new ErrorResponse(HttpCode.BAD_REQUEST, "Failed to scaleOut optimizer", ""));
    }
  }
}

