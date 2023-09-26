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

package com.netease.arctic.server.metrics;

import com.netease.arctic.ams.api.metrics.MetricsDomain;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import com.netease.arctic.ams.api.metrics.MetricsPayload;
import com.netease.arctic.server.metrics.emitters.EmitterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manage metrics and metrics emitter
 * Register emitter by adding the following configuration in the system configuration:
 * emitters:
 *   - name: emitter1
 *     domain: AMORO
 *     impl: {emitter class}
 *     properties:
 *       host: 127.0.0.1
 *       port: 8080
 */
public class MetricsManager {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsManager.class);

  private final MetricsPayloadWrapper metricsWrapper;
  private final EmitterFactory factory = new EmitterFactory();
  private final Map<MetricsDomain, Map<String, MetricsEmitter<?>>> emitters = new ConcurrentHashMap<>();

  public MetricsManager(List<EmitterConfig> metas, Map<String, String> domainToClass) {
    metas.forEach(this::register);
    this.metricsWrapper = new MetricsPayloadWrapper(domainToClass);
  }

  public void register(EmitterConfig meta) {
    MetricsDomain domain = MetricsDomain.valueOf(meta.getDomain());
    register(domain, meta.getName(), this.factory.create(meta));
  }

  public void register(MetricsDomain domain, String name, MetricsEmitter<?> emitter) {
    emitters.computeIfAbsent(domain, k -> new HashMap<>()).put(name, emitter);
  }

  public void unregister(String name) {
    emitters.values().forEach(namedEmitters -> {
      if (namedEmitters.containsKey(name)) {
        namedEmitters.remove(name).close();
      }
    });
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public <T> void emit(T metrics) {
    MetricsPayload<T> payload = metricsWrapper.wrap(metrics);
    Map<String, MetricsEmitter<?>> domainEmitter = this.emitters.get(payload.domain());
    if (domainEmitter == null) {
      return;
    }
    for (MetricsEmitter emitter : domainEmitter.values()) {
      try {
        emitter.emit(payload);
      } catch (Exception e) {
        LOG.error("Failed to emit metrics", e);
      }
    }
  }

  public void shutdown() {
    this.emitters.values().forEach(namedEmitters -> namedEmitters.keySet().forEach(this::unregister));
  }
}
