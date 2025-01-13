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

import org.apache.amoro.server.table.TableRuntime;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.Map;

public class QuotaOccupySorter implements SorterFactory {

  public static final String IDENTIFIER = "quota";

  @Override
  public String getIdentifier() {
    return IDENTIFIER;
  }

  @Override
  public Comparator<TableRuntime> createComparator() {
    return new Comparator<TableRuntime>() {
      final Map<TableRuntime, Double> tableWeightMap = Maps.newHashMap();

      @Override
      public int compare(TableRuntime one, TableRuntime another) {
        return Double.compare(
            tableWeightMap.computeIfAbsent(one, TableRuntime::calculateQuotaOccupy),
            tableWeightMap.computeIfAbsent(another, TableRuntime::calculateQuotaOccupy));
      }
    };
  }
}
