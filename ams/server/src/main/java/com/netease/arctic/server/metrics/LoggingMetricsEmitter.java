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

import com.netease.arctic.ams.api.metrics.MetricsContent;
import com.netease.arctic.ams.api.metrics.MetricsEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoggingMetricsEmitter implements MetricsEmitter {

  public static final String METRIC_LOGGER = "amoro.metric";

  private static final Logger LOG = LoggerFactory.getLogger(METRIC_LOGGER);
  private static final String NAME = "log_emitter";

  @Override
  public void open(Map<String, String> properties) {
    //do nothing
  }

  @Override
  public void close() {
    //do nothing
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public void emit(MetricsContent<?> metrics) {
    LOG.info("Thread {} received metrics named {} type {} data: {}", Thread.currentThread().getName(), metrics.name(),
        metrics.type().name(), metrics.data());
  }

  @Override
  public boolean accept(MetricsContent<?> metrics) {
    return true;
  }
}
