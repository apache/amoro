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

import com.netease.arctic.server.ArcticManagementConf;
import com.netease.arctic.server.dashboard.response.OkResponse;
import com.netease.arctic.server.resource.ContainerMetadata;
import com.netease.arctic.server.resource.OptimizerManager;
import com.netease.arctic.server.resource.ResourceContainers;
import com.netease.arctic.server.utils.Configurations;
import io.javalin.http.Context;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SettingController extends RestBaseController {
  private static final String MASK_STRING = "******";
  private static final Set<String> MASK_CONFIGURATION_SET = Sets.newHashSet();

  static {
    MASK_CONFIGURATION_SET.add(ArcticManagementConf.DB_PASSWORD.key());
    MASK_CONFIGURATION_SET.add(ArcticManagementConf.ADMIN_PASSWORD.key());
  }

  private final OptimizerManager optimizerManager;
  private final Configurations serviceConfig;

  public SettingController(Configurations serviceConfig, OptimizerManager optimizerManager) {
    this.optimizerManager = optimizerManager;
    this.serviceConfig = serviceConfig;
  }

  /**
   * get system settings.
   */
  public void getSystemSetting(Context ctx) {
    Map<String, String> config = new HashMap<>();
    //Show core configuration first
    putSetting(config, ArcticManagementConf.SERVER_EXPOSE_HOST.key(),
        serviceConfig.get(ArcticManagementConf.SERVER_EXPOSE_HOST));
    putSetting(config, ArcticManagementConf.THRIFT_BIND_PORT.key(),
        serviceConfig.get(ArcticManagementConf.THRIFT_BIND_PORT));
    putSetting(config, ArcticManagementConf.HTTP_SERVER_PORT.key(),
        serviceConfig.get(ArcticManagementConf.HTTP_SERVER_PORT));
    putSetting(config, ArcticManagementConf.DB_TYPE.key(), serviceConfig.get(ArcticManagementConf.DB_TYPE));
    putSetting(config, ArcticManagementConf.DB_CONNECTION_URL.key(),
        serviceConfig.get(ArcticManagementConf.DB_CONNECTION_URL));
    putSetting(config, ArcticManagementConf.DB_CONNECTION_URL.key(),
        serviceConfig.get(ArcticManagementConf.DB_CONNECTION_URL));

    serviceConfig.toMap().forEach((k, v) -> {
      if (!config.containsKey(k)) {
        putSetting(config, k, v);
      }
    });

    LinkedHashMap<String, String> result = new LinkedHashMap<>();
    config.entrySet().stream()
        .sorted(Map.Entry.comparingByKey()).forEachOrdered(entry -> result.put(entry.getKey(), entry.getValue()));
    ctx.json(OkResponse.of(result));
  }

  private void putSetting(Map<String, String> settingMap, String key, Object value) {
    if (MASK_CONFIGURATION_SET.contains(key)) {
      value = MASK_STRING;
    }
    settingMap.put(key, String.valueOf(value));
  }

  /**
   * get container settings.
   */
  public void getContainerSetting(Context ctx) {
    List<ContainerMetadata> containerMetas = ResourceContainers.getMetadatas();
    List<Map<String, Object>> result = new ArrayList<>();
    Objects.requireNonNull(containerMetas).forEach(container -> {
      List<Map<String, String>> optimizeGroups =
          optimizerManager.listResourceGroups(container.getName()).stream().map(group -> {
            Map<String, String> optimizeGroupItem = new HashMap<>();
            optimizeGroupItem.put("name", group.getName());
            // local type only need memory
            if (container.getName().equalsIgnoreCase("local")) {
              if (group.getProperties() != null) {
                optimizeGroupItem.put("memory", group.getProperties().get("memory"));
              }
            } else {
              if (group.getProperties() != null) {
                optimizeGroupItem.put("tmMemory", group.getProperties()
                    .getOrDefault("taskmanager.memory", "-1"));
                optimizeGroupItem.put("jmMemory", group.getProperties()
                    .getOrDefault("jobmanager.memory", "-1"));
              }
            }
            return optimizeGroupItem;
          }).collect(Collectors.toList());
      Map<String, Object> obj = new HashMap<>();
      obj.put("name", container.getName());
      obj.put("classpath", container.getImplClass());
      obj.put("properties", container.getProperties());
      obj.put("optimizeGroup", optimizeGroups);
      result.add(obj);
    });

    ctx.json(OkResponse.of(result));
  }
}
