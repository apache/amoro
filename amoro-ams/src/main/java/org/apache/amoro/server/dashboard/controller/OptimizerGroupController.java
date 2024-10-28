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

import static org.apache.amoro.resource.ResourceGroup.RULE_SEPARATOR;
import static org.apache.amoro.resource.ResourceGroup.validateRule;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import io.javalin.http.Context;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.dashboard.model.OptimizerInstanceInfo;
import org.apache.amoro.server.dashboard.model.OptimizerResourceInfo;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.dashboard.response.ErrorResponse;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.dashboard.response.PageResult;
import org.apache.amoro.server.dashboard.utils.OptimizingUtil;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.resource.ContainerMetadata;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.ResourceContainers;
import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.server.table.TableService;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;

import java.util.*;
import java.util.stream.Collectors;

/** The controller that handles optimizer requests. */
public class OptimizerGroupController {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizerGroupController.class);

  private static final String ALL_GROUP = "all";
  private final TableService tableService;
  private final DefaultOptimizingService optimizerManager;

  private List<String> getInvalidateRules(String rules) {
    return Arrays.stream(rules.split(RULE_SEPARATOR))
        .filter(rule -> !validateRule(rule))
        .collect(Collectors.toList());
  }

  /**
   * rules: catalog.db.table, catalog\.db\.table,
   *
   * @param item
   * @return {{catalog, db, table}, {catalog,db, table}}
   */
  private List<String> splitRuleNamespace(String item) {
    item = item.trim();
    String separator = ResourceGroup.getSpaceSeparator(item);
    return Arrays.asList(item.split(separator));
  }

  /**
   * check whether rules are overlap
   *
   * @param one
   * @param other
   * @return
   */
  private boolean RegExpIntersect(String one, String other) {
    RegExp regExp1 = new RegExp(one);
    RegExp regExp2 = new RegExp(other);
    Automaton automaton1 = regExp1.toAutomaton();
    Automaton automaton2 = regExp2.toAutomaton();
    Automaton intersection = automaton1.intersection(automaton2);
    return !intersection.isEmpty();
  }

  /**
   * @param newRules
   * @param other
   * @return
   */
  private List<String> groupRuleOverlap(String newRules, String otherRules, String otherGroupName) {
    List<String> result = new ArrayList<>();
    Arrays.stream(newRules.split(RULE_SEPARATOR))
        .forEach(
            newRule -> {
              List<String> newRuleSpace = splitRuleNamespace(newRule);
              Arrays.stream(otherRules.split(RULE_SEPARATOR))
                  .forEach(
                      otherRule -> {
                        List<String> otherRuleSpace = splitRuleNamespace(otherRule);
                        // if there have some intersection, we return false;
                        if (RegExpIntersect(newRuleSpace.get(2), otherRuleSpace.get(2))
                            && RegExpIntersect(newRuleSpace.get(1), otherRuleSpace.get(1))
                            && RegExpIntersect(newRuleSpace.get(0), otherRuleSpace.get(0))) {
                          result.add(
                              String.format("%s -> %s:%s", newRule, otherGroupName, otherRule));
                        }
                      });
            });
    return result;
  }

  /**
   * check the rule whether is conflict with some other group
   *
   * @param newRules
   * @return
   */
  private List<List<String>> groupRuleOverlap(String newRules, String newName) {
    List<List<String>> result =
        optimizerManager.listResourceGroups().stream()
            .filter(item -> !Objects.equals(item.getName(), newName))
            .filter(item -> item.getOptimizeGroupRule() != null)
            .map(item -> groupRuleOverlap(newRules, item.getOptimizeGroupRule(), item.getName()))
            .filter(olItem -> olItem.size() > 0)
            .collect(Collectors.toList());
    return result;
  }

  public OptimizerGroupController(
      TableService tableService, DefaultOptimizingService optimizerManager) {
    this.tableService = tableService;
    this.optimizerManager = optimizerManager;
  }

  public void getActions(Context ctx) {
    ctx.json(
        OkResponse.of(
            Arrays.stream(OptimizingStatus.values())
                .map(OptimizingStatus::displayValue)
                .collect(Collectors.toList())));
  }

  /** Get optimize tables. * @return List of {@link TableOptimizingInfo} */
  public void getOptimizerTables(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    String dbFilterStr = ctx.queryParam("dbSearchInput");
    String tableFilterStr = ctx.queryParam("tableSearchInput");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);
    Set<String> actionFilter = new HashSet<>(ctx.queryParams("actions[]"));
    int offset = (page - 1) * pageSize;

    String optimizerGroupUsedInDbFilter = ALL_GROUP.equals(optimizerGroup) ? null : optimizerGroup;
    // get all info from underlying table table_runtime
    List<Integer> statusCodes = new ArrayList<>(actionFilter.size());
    for (String action : actionFilter) {
      OptimizingStatus status = OptimizingStatus.ofDisplayValue(action);
      if (status == null) {
        LOG.warn("Can't find optimizer status for action:{}, skip it.", action);
      } else {
        statusCodes.add(status.getCode());
      }
    }

    // use null to mark the filter as null filter
    if (statusCodes.isEmpty()) {
      statusCodes = null;
    }
    Pair<List<TableRuntimeMeta>, Integer> tableRuntimeBeans =
        tableService.getTableRuntimes(
            optimizerGroupUsedInDbFilter,
            dbFilterStr,
            tableFilterStr,
            statusCodes,
            pageSize,
            offset);

    List<TableRuntime> tableRuntimes =
        tableRuntimeBeans.getLeft().stream()
            .map(meta -> tableService.getRuntime(meta.getTableId()))
            .collect(Collectors.toList());

    PageResult<TableOptimizingInfo> amsPageResult =
        PageResult.of(
            tableRuntimes.stream()
                .map(OptimizingUtil::buildTableOptimizeInfo)
                .collect(Collectors.toList()),
            tableRuntimeBeans.getRight());
    ctx.json(OkResponse.of(amsPageResult));
  }

  /** get optimizers. */
  public void getOptimizers(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    Integer page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
    Integer pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(20);

    int offset = (page - 1) * pageSize;
    List<OptimizerInstance> optimizers;
    if (optimizerGroup.equals("all")) {
      optimizers = optimizerManager.listOptimizers();
    } else {
      optimizers = optimizerManager.listOptimizers(optimizerGroup);
    }
    List<OptimizerInstance> optimizerList = new ArrayList<>(optimizers);
    optimizerList.sort(Comparator.comparingLong(OptimizerInstance::getStartTime).reversed());
    List<OptimizerInstanceInfo> result =
        optimizerList.stream()
            .map(
                e ->
                    OptimizerInstanceInfo.builder()
                        .token(e.getToken())
                        .startTime(e.getStartTime())
                        .touchTime(e.getTouchTime())
                        .jobId(e.getResourceId())
                        .groupName(e.getGroupName())
                        .coreNumber(e.getThreadCount())
                        .memory(e.getMemoryMb())
                        .jobStatus("RUNNING")
                        .container(e.getContainerName())
                        .build())
            .collect(Collectors.toList());

    PageResult<OptimizerInstanceInfo> amsPageResult = PageResult.of(result, offset, pageSize);
    ctx.json(OkResponse.of(amsPageResult));
  }

  /** get optimizerGroup: optimizerGroupId, optimizerGroupName url = /optimizerGroups. */
  public void getOptimizerGroups(Context ctx) {
    List<Map<String, String>> result =
        optimizerManager.listResourceGroups().stream()
            .filter(
                resourceGroup ->
                    !ResourceContainers.EXTERNAL_CONTAINER_NAME.equals(
                        resourceGroup.getContainer()))
            .map(
                e -> {
                  Map<String, String> mapObj = new HashMap<>();
                  mapObj.put("optimizerGroupName", e.getName());
                  return mapObj;
                })
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(result));
  }

  /** get optimizer info: occupationCore, occupationMemory */
  public void getOptimizerGroupInfo(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    List<OptimizerInstance> optimizers;
    if (optimizerGroup.equals("all")) {
      optimizers = optimizerManager.listOptimizers();
    } else {
      optimizers = optimizerManager.listOptimizers(optimizerGroup);
    }
    OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
    optimizers.forEach(
        e -> {
          optimizerResourceInfo.addOccupationCore(e.getThreadCount());
          optimizerResourceInfo.addOccupationMemory(e.getMemoryMb());
        });
    ctx.json(OkResponse.of(optimizerResourceInfo));
  }

  /**
   * release optimizer.
   *
   * @pathParam jobId
   */
  public void releaseOptimizer(Context ctx) {
    String resourceId = ctx.pathParam("jobId");
    Preconditions.checkArgument(
        !resourceId.isEmpty(), "resource id can not be empty, maybe it's a external optimizer");

    List<OptimizerInstance> optimizerInstances =
        optimizerManager.listOptimizers().stream()
            .filter(e -> resourceId.equals(e.getResourceId()))
            .collect(Collectors.toList());
    Preconditions.checkState(
        !optimizerInstances.isEmpty(),
        String.format(
            "The resource ID %s has not been indexed" + " to any optimizer.", resourceId));
    Resource resource = optimizerManager.getResource(resourceId);
    resource.getProperties().putAll(optimizerInstances.get(0).getProperties());
    ResourceContainers.get(resource.getContainerName()).releaseOptimizer(resource);
    optimizerManager.deleteResource(resourceId);
    optimizerManager.deleteOptimizer(resource.getGroupName(), resourceId);
    ctx.json(OkResponse.of("Success to release optimizer"));
  }

  /** scale out optimizers, url:/optimizerGroups/{optimizerGroup}/optimizers. */
  public void scaleOutOptimizer(Context ctx) {
    String optimizerGroup = ctx.pathParam("optimizerGroup");
    Map<String, Integer> map = ctx.bodyAsClass(Map.class);
    int parallelism = map.get("parallelism");

    ResourceGroup resourceGroup = optimizerManager.getResourceGroup(optimizerGroup);
    Resource resource =
        new Resource.Builder(
                resourceGroup.getContainer(), resourceGroup.getName(), ResourceType.OPTIMIZER)
            .setProperties(resourceGroup.getProperties())
            .setThreadCount(parallelism)
            .build();
    ResourceContainers.get(resource.getContainerName()).requestResource(resource);
    optimizerManager.createResource(resource);
    ctx.json(OkResponse.of("success to scaleOut optimizer"));
  }

  /** get {@link List<OptimizerResourceInfo>} url = /optimize/resourceGroups */
  public void getResourceGroup(Context ctx) {
    List<OptimizerResourceInfo> result =
        optimizerManager.listResourceGroups().stream()
            .map(
                group -> {
                  List<OptimizerInstance> optimizers =
                      optimizerManager.listOptimizers(group.getName());
                  OptimizerResourceInfo optimizerResourceInfo = new OptimizerResourceInfo();
                  optimizerResourceInfo.setResourceGroup(
                      optimizerManager.getResourceGroup(group.getName()));
                  optimizers.forEach(
                      optimizer -> {
                        optimizerResourceInfo.addOccupationCore(optimizer.getThreadCount());
                        optimizerResourceInfo.addOccupationMemory(optimizer.getMemoryMb());
                      });
                  return optimizerResourceInfo;
                })
            .collect(Collectors.toList());
    ctx.json(OkResponse.of(result));
  }

  /**
   * create optimizeGroup: name, container, schedulePolicy, properties url =
   * /optimize/resourceGroups/create
   */
  public void createResourceGroup(Context ctx) {
    Map<String, Object> map = ctx.bodyAsClass(Map.class);
    String name = (String) map.get("name");
    String container = (String) map.get("container");
    Map<String, String> properties = (Map) map.get("properties");
    if (optimizerManager.getResourceGroup(name) != null) {
      throw new BadRequestException(String.format("Optimizer group:%s already existed.", name));
    }
    ResourceGroup.Builder builder = new ResourceGroup.Builder(name, container);
    builder.addProperties(properties);
    ResourceGroup newGroup = builder.build();

    String newRules = newGroup.getOptimizeGroupRule();
    List<List<String>> intersectionMsg = new ArrayList<>();
    // check whether the rules are conflict with other group
    intersectionMsg = groupRuleOverlap(newRules, name);
    if (intersectionMsg.size() > 0) {
      StringBuilder sb = new StringBuilder();
      sb.append("intersection rules:" + "\n");
      intersectionMsg.stream().forEach(item -> sb.append(String.join(",", item)));
      ctx.json(new ErrorResponse(sb.toString()));
    } else {
      optimizerManager.createResourceGroup(newGroup);
      ctx.json(OkResponse.of("The optimizer group has been successfully created."));
    }
  }

  /**
   * update optimizeGroup: name, container, schedulePolicy, properties url =
   * /optimize/resourceGroups/update
   */
  public void updateResourceGroup(Context ctx) {
    Map<String, Object> map = ctx.bodyAsClass(Map.class);
    String name = (String) map.get("name");
    String container = (String) map.get("container");
    Map<String, String> properties = (Map) map.get("properties");
    ResourceGroup.Builder builder = new ResourceGroup.Builder(name, container);
    builder.addProperties(properties);
    ResourceGroup newGroup = builder.build();
    // check if the new rules is conflict with others
    String oldRules = optimizerManager.getResourceGroup(name).getOptimizeGroupRule();
    String newRules = newGroup.getOptimizeGroupRule();
    if (!Objects.equals(oldRules, newRules)) {
      // only check the rule updated
      List<List<String>> intersectionMsg = groupRuleOverlap(newRules, name);
      if (intersectionMsg.size() > 0) {
        StringBuilder sb = new StringBuilder();
        sb.append("intersection rules:" + "\n");
        intersectionMsg.stream().forEach(item -> sb.append(String.join(",", item)));
        ctx.json(new ErrorResponse(sb.toString()));
        return;
      }
    }
    optimizerManager.updateResourceGroup(newGroup);
    ctx.json(OkResponse.of("The optimizer group has been successfully updated."));
  }

  /** delete optimizeGroup url = /optimize/resourceGroups/{resourceGroupName} */
  public void deleteResourceGroup(Context ctx) {
    String name = ctx.pathParam("resourceGroupName");
    optimizerManager.deleteResourceGroup(name);
    ctx.json(OkResponse.of("The optimizer group has been successfully deleted."));
  }

  /** check if optimizerGroup can be deleted url = /optimize/resourceGroups/delete/check */
  public void deleteCheckResourceGroup(Context ctx) {
    String name = ctx.pathParam("resourceGroupName");
    ctx.json(OkResponse.of(optimizerManager.canDeleteResourceGroup(name)));
  }

  /** check if optimizerGroup can be deleted url = /optimize/containers/get */
  public void getContainers(Context ctx) {
    ctx.json(
        OkResponse.of(
            ResourceContainers.getMetadataList().stream()
                .map(ContainerMetadata::getName)
                .collect(Collectors.toList())));
  }
}
