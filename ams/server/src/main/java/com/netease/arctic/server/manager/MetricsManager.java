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

package com.netease.arctic.server.manager;

import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.ams.api.exception.LoadingPluginException;
import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;

public class MetricsManager extends ActivePluginManager<MetricsEmitter> {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsManager.class);
  private static final String METRICS_CONFIG_DIRECTORY = "metrics";
  private static volatile MetricsManager INSTANCE;

  private final String configPath;

  public MetricsManager() {
    this(new File(Environments.getHomePath(), METRICS_CONFIG_DIRECTORY).getPath());
  }

  public MetricsManager(String configPath) {
    this.configPath = configPath;
  }

  public static MetricsManager instance() {
    if (INSTANCE == null) {
      synchronized (MetricsManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new MetricsManager();
          INSTANCE.initialize();
        }
      }
    }
    return INSTANCE;
  }

  public void initialize() {
    File dir = new File(configPath);
    File[] yamlFiles = dir.listFiles((dir1, name) -> name.endsWith(".yaml"));
    if (yamlFiles != null) {
      Arrays.stream(yamlFiles)
          .forEach(
              file -> {
                this.install(file.getName().replace(".yaml", ""));
              });
    }
  }

  @Override
  protected Map<String, String> loadProperties(String pluginName) {
    try {
      return new Yaml().load(new FileInputStream(new File(configPath, pluginName + ".yaml")));
    } catch (FileNotFoundException e) {
      throw new LoadingPluginException("Cannot load plugin " + pluginName, e);
    }
  }

  public void emit(MetricsContent<?> metrics) {
    forEach(
        emitter -> {
          try (ClassLoaderContext ignored = new ClassLoaderContext(emitter)) {
            if (emitter.accept(metrics)) {
              emitter.emit(metrics);
            }
          } catch (Throwable throwable) {
            LOG.error("Emit metrics {} failed", metrics, throwable);
          }
        });
  }
}
