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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.metrics.MetricReporter;
import com.netease.arctic.server.metrics.MetricRegistry;
import com.netease.arctic.server.utils.Configurations;

import java.util.Map;

public class MetricManager extends ActivePluginManager<MetricReporter> {

  private static volatile MetricManager INSTANCE;

  public static MetricManager getInstance() {
    if (INSTANCE == null) {
      synchronized (MetricManager.class) {
        if (INSTANCE == null) {
          throw new IllegalStateException("MetricManager is not initialized");
        }
      }
    }
    return INSTANCE;
  }

  public static void initialize(Configurations serverConfig) {
    synchronized (MetricManager.class) {
      if (INSTANCE != null) {
        throw new IllegalStateException("MetricManger has been already initialized.");
      }
      INSTANCE = new MetricManager();
    }
  }

  private final MetricRegistry globalRegistry = new MetricRegistry();

  @Override
  protected Map<String, String> loadProperties(String pluginName) {
    return null;
  }
  
  public MetricRegistry getGlobalRegistry() {
    return this.globalRegistry;
  }
}
