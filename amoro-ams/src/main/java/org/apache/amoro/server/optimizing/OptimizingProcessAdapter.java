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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.Action;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.process.ExecuteEngine;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;

import java.util.Map;

/**
 * Adapter that wraps an {@link OptimizingTableProcess} as a {@link TableProcess}, bridging the
 * PersistentBase-based process with the Process framework's TableProcess hierarchy.
 */
public class OptimizingProcessAdapter extends TableProcess {

  private final OptimizingTableProcess delegate;

  public OptimizingProcessAdapter(
      TableRuntime tableRuntime, ExecuteEngine engine, OptimizingTableProcess delegate) {
    super(tableRuntime, engine);
    this.delegate = delegate;
  }

  public OptimizingTableProcess getDelegate() {
    return delegate;
  }

  @Override
  public Action getAction() {
    return OptimizingActionCoordinator.OPTIMIZING_ACTION;
  }

  @Override
  public Map<String, String> getProcessParameters() {
    return Maps.newHashMap();
  }

  @Override
  public Map<String, String> getSummary() {
    MetricsSummary summary = delegate.getSummary();
    if (summary != null) {
      return summary.summaryAsMap(false);
    }
    return Maps.newHashMap();
  }
}
