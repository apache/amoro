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

import org.apache.amoro.server.table.DefaultTableRuntime;

import java.util.Comparator;

public class BalancedSorter implements SorterFactory {

  private static final String IDENTIFIER = "balanced";

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public Comparator<DefaultTableRuntime> createComparator() {
    return new Comparator<DefaultTableRuntime>() {
      @Override
      public int compare(DefaultTableRuntime one, DefaultTableRuntime another) {
        return Long.compare(
            Math.max(
                one.getOptimizingState().getLastFullOptimizingTime(),
                Math.max(
                    one.getOptimizingState().getLastMinorOptimizingTime(),
                    one.getOptimizingState().getLastMajorOptimizingTime())),
            Math.max(
                another.getOptimizingState().getLastFullOptimizingTime(),
                Math.max(
                    another.getOptimizingState().getLastMinorOptimizingTime(),
                    another.getOptimizingState().getLastMajorOptimizingTime())));
      }
    };
  }
}
