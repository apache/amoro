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

package org.apache.amoro.formats.paimon.optimizing;

import org.apache.amoro.optimizing.OptimizingPlanResult;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.TableOptimizingPlanner;

import java.util.Collections;
import java.util.Map;

public class PaimonOptimizingPlanner implements TableOptimizingPlanner {

  @Override
  public boolean isNecessary() {
    // TODO: Implement Paimon optimization evaluation
    return false;
  }

  @Override
  public OptimizingPlanResult plan() {
    // TODO: Implement Paimon planning
    return null;
  }

  @Override
  public OptimizingType getOptimizingType() {
    return null;
  }

  @Override
  public long getProcessId() {
    return 0;
  }

  @Override
  public long getPlanTime() {
    return 0;
  }

  @Override
  public long getTargetSnapshotId() {
    return 0;
  }

  @Override
  public long getTargetChangeSnapshotId() {
    return 0;
  }

  @Override
  public Map<String, Long> getFromSequence() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, Long> getToSequence() {
    return Collections.emptyMap();
  }
}
