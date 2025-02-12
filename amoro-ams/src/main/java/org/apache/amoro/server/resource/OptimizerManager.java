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

package org.apache.amoro.server.resource;

import org.apache.amoro.resource.ResourceManager;
import org.apache.amoro.server.optimizing.OptimizingQueue;

import java.util.List;
import java.util.Optional;

public interface OptimizerManager extends ResourceManager {
  List<OptimizerInstance> listOptimizers();

  List<OptimizerInstance> listOptimizers(String groupName);

  void deleteOptimizer(String groupName, String resourceId);

  void registerOptimizer(OptimizerInstance optimizer, boolean needPersistent);

  void unregisterOptimizer(String token);

  OptimizerInstance getOptimizerByToken(String token);

  Optional<OptimizingQueue> getOptionalQueueByGroup(String optimizerGroup);

  OptimizingQueue getQueueByGroup(String optimizerGroup);

  OptimizingQueue getQueueByToken(String token);

  boolean canDeleteResourceGroup(String name);
}
