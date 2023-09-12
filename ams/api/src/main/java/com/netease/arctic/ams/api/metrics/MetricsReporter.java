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

package com.netease.arctic.ams.api.metrics;

import java.util.Map;

/**
 * This is an interface defining a reporter, which users can implement to notify metrics to a monitoring system.
 * The system calls the open method to initialize the reporter and the close method to shut it down when needed.
 * The report method is called to notify the reporter when a metric is generated.
 */
public interface MetricsReporter {

  /**
   * A custom MetricsReporter implementation must have a no-arg constructor, which will be called
   * first. {@link MetricsReporter#open(Map properties)} is called to complete the
   * initialization.
   *
   * @param properties properties
   */
  default void open(Map<String, String> properties) {

  }

  /**
   * Indicates that an operation is done by reporting a {@link MetricsContent}. A {@link
   * MetricsContent} is usually directly derived from a {@link MetricsContent} instance.
   *
   * @param metricsContent {@link MetricsContent} to report.
   */
  void report(MetricsContent metricsContent);

  /**
   * Indicates that an operation is done by reporting a {@link MetricsContent}. A {@link
   * MetricsContent} is usually directly derived from a {@link MetricsContent} instance.
   */
  default void close() {

  }
}
