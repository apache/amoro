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

package org.apache.amoro.maintainer;

import java.io.Serializable;
import java.util.Map;

/**
 * Factory interface for creating MaintainerExecutor instances. Follows the same pattern as
 * OptimizingExecutorFactory.
 *
 * @param <I> the maintainer input type
 */
public interface MaintainerExecutorFactory<I extends MaintainerInput> extends Serializable {

  /**
   * Initialize the factory with task properties. Called after constructing the factory through a
   * parameterless constructor.
   *
   * @param properties the task properties
   */
  void initialize(Map<String, String> properties);

  /**
   * Create an executor from the given input.
   *
   * @param input the maintainer input
   * @return the maintainer executor
   */
  MaintainerExecutor<?, ?> createExecutor(I input);
}
