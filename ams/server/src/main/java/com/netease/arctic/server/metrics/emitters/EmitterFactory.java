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

package com.netease.arctic.server.metrics.emitters;

import com.netease.arctic.ams.api.metrics.MetricsDomain;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import com.netease.arctic.server.metrics.EmitterConfig;
import org.apache.iceberg.metrics.MetricsReporter;

public class EmitterFactory {

  @SuppressWarnings({"rawtypes"})
  public MetricsEmitter create(EmitterConfig meta) {
    MetricsDomain domain = MetricsDomain.valueOf(meta.getDomain());
    switch (domain) {
      case ICEBERG:
        return new IcebergEmitterWrapper((MetricsReporter) createInstance(meta.getImpl()));
      case AMORO:
        return (MetricsEmitter) createInstance(meta.getImpl());
      default:
        throw new RuntimeException("Unsupported domain: " + domain);
    }
  }

  private Object createInstance(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      return clazz.newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
