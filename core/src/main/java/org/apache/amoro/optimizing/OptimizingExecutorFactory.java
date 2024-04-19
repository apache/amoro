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

package org.apache.amoro.optimizing;

import java.io.Serializable;
import java.util.Map;

/** A factory to create {@link OptimizingExecutor} */
public interface OptimizingExecutorFactory<I extends TableOptimizing.OptimizingInput>
    extends Serializable {

  /**
   * After constructing the factory through a parameterless constructor, it is necessary to call the
   * initialize method to initialize the factory.
   */
  void initialize(Map<String, String> properties);

  /** Create factory by input */
  OptimizingExecutor createExecutor(I input);
}
