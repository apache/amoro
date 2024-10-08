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

import io.javalin.http.Context;
import org.apache.amoro.resource.Resource;
import org.apache.amoro.resource.ResourceGroup;
import org.apache.amoro.resource.ResourceType;
import org.apache.amoro.server.DefaultOptimizingService;
import org.apache.amoro.server.dashboard.response.OkResponse;
import org.apache.amoro.server.resource.ContainerMetadata;
import org.apache.amoro.server.resource.OptimizerInstance;
import org.apache.amoro.server.resource.ResourceContainers;
import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The controller that handles optimizer requests. */
public class OptimizerController {

  private final DefaultOptimizingService optimizerManager;

  public OptimizerController(DefaultOptimizingService optimizerManager) {
    this.optimizerManager = optimizerManager;
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

  /** scale out optimizers, url:/optimizers. */
  public void createOptimizer(Context ctx) {
    Map<String, Object> map = ctx.bodyAsClass(Map.class);
    int parallelism = Integer.parseInt(map.get("parallelism").toString());
    String optimizerGroup = map.get("optimizerGroup").toString();
    ResourceGroup resourceGroup = optimizerManager.getResourceGroup(optimizerGroup);
    Resource resource =
        new Resource.Builder(
                resourceGroup.getContainer(), resourceGroup.getName(), ResourceType.OPTIMIZER)
            .setProperties(resourceGroup.getProperties())
            .setThreadCount(parallelism)
            .build();
    ResourceContainers.get(resource.getContainerName()).requestResource(resource);
    optimizerManager.createResource(resource);
    ctx.json(OkResponse.of("success to create optimizer"));
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
