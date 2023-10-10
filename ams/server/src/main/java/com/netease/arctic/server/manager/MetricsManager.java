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

import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import com.netease.arctic.server.exception.LoadingPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class MetricsManager extends ActivePluginManager<MetricsEmitter> {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsManager.class);

  private final String configPath;

  public MetricsManager(String configPath) {
    this.configPath = configPath;
  }

  @Override
  protected Map<String, String> loadProperties(String pluginName) {
    try {
      FileInputStream inputStream = new FileInputStream(configPath +
          "/" + pluginName + ".yaml");
      return new Yaml().load(inputStream);
    } catch (FileNotFoundException e) {
      throw new LoadingPluginException("Cannot load plugin " + pluginName, e);
    }
  }

  public void emit(MetricsContent<?> metrics) {
    forEach(emitter -> {
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
