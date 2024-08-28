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

package org.apache.amoro.api.resource;

import java.util.Map;

public interface ResourceContainer {

  String name();

  /**
   * Initialize the container
   *
   * @param containerProperties container properties
   */
  void init(String name, Map<String, String> containerProperties);

  /**
   * Start a new optimizer.
   *
   * @param resource resource information to start the optimizer
   */
  void requestResource(Resource resource);

  /**
   * Release a optimizer
   *
   * @param resource resource information to release the optimizer
   */
  void releaseOptimizer(Resource resource);

  ResourceStatus getStatus(String resourceId);
}
