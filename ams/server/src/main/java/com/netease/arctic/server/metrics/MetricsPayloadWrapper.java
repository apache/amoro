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
import com.netease.arctic.ams.api.metrics.MetricsPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MetricsPayloadWrapper {

  static Map<MetricsDomain, Class<?>> domainToClass = new HashMap<>();

  public MetricsPayloadWrapper(Map<String, String> domainToClass) {
    domainToClass.forEach((domain, className) -> {
      try {
        MetricsPayloadWrapper.domainToClass.put(MetricsDomain.valueOf(domain.toUpperCase()), Class.forName(className));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Class not found: " + className, e);
      } catch (IllegalArgumentException e1) {
        throw new RuntimeException("Invalid domain: " + domain, e1);
      }
    });
  }

  public <T> MetricsPayload<T> wrap(T metrics) {
    Optional<Map.Entry<MetricsDomain, Class<?>>> domainEntry =
        domainToClass.entrySet()
            .stream()
            .filter(entry -> entry.getValue().isAssignableFrom(metrics.getClass()))
            .findFirst();
    if (domainEntry.isPresent()) {
      return wrap(domainEntry.get().getKey(), metrics);
    } else {
      throw new RuntimeException("No domain found for metrics: " + metrics.getClass().getName());
    }
  }

  public <T> MetricsPayload<T> wrap(MetricsDomain domain, T payload) {
    return new MetricsPayload<T>() {
      @Override
      public MetricsDomain domain() {
        return domain;
      }

      @Override
      public T payload() {
        return payload;
      }
    };
  }
}
