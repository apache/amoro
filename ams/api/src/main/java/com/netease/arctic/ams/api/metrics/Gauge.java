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

/**
 * A gauge metric is an instantaneous reading of a particular value. To instrument a queue's depth,
 * for example: final Queue<String> queue = new ConcurrentLinkedQueue<String>(); final
 * Gauge<Integer> queueDepth = new Gauge<Integer>() { public Integer getValue() { return
 * queue.size(); } }; Type parameters:
 *
 * @param <T> – the type of the metric's value
 */
@FunctionalInterface
public interface Gauge<T extends Number> extends Metric {
  /** @return The current value of metric */
  T getValue();
}
