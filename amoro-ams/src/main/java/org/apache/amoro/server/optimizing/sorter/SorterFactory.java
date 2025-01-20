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

package org.apache.amoro.server.optimizing.sorter;

import org.apache.amoro.server.optimizing.SchedulingPolicy;

import java.util.Comparator;

/**
 * A factory for sorter. Sorter instantiates a comparator, which is automatically loaded by the
 * {@link SchedulingPolicy} as a plugin, as long as the sorter is constructed and the {@link
 * /resources/META-INF/services/org.apache.amoro.server.optimizing.sorter.SorterFactory} file
 * contains the full qualified class name for the sorter. The comparator sorts the tableRuntimes
 * based on the parameters of each tableRuntime in the input tableRuntimeList, and determines the
 * scheduling priority for optimization of each tableRuntime.
 */
public interface SorterFactory {

  /**
   * Returns a globally unique identifier for the sorter instance, such as Balanced、balanced and
   * BALANCED represent the different sorter instance. The {@link SchedulingPolicy} will check the
   * values of {@link SchedulingPolicy#policyName} and {@link SorterFactory#getIdentifier} to decide
   * which sorter instance to use. If sorter instances sorterA and sorterB have the same identifier
   * id1, the {@link SchedulingPolicy} loads sorter instances in the order of their loading, first
   * loading sorterA, then sorterB. Ultimately, sorterB will replace sorterA as the final sorter
   * instance associated with id1, which is stored in {@link SchedulingPolicy#sorterFactoryCache}.
   */
  String getIdentifier();

  /** Create a comparator for sorter. */
  Comparator createComparator();
}
