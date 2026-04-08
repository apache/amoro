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

import org.apache.amoro.Action;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableRuntime;
import org.apache.amoro.process.ProcessFactory;
import org.apache.amoro.process.ProcessTriggerStrategy;
import org.apache.amoro.process.RecoverProcessFailedException;
import org.apache.amoro.process.TableProcess;
import org.apache.amoro.process.TableProcessStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PaimonOptimizingProcessFactory implements ProcessFactory {

  @Override
  public String name() {
    return "paimon-optimizing-process-factory";
  }

  @Override
  public Map<TableFormat, Set<Action>> supportedActions() {
    return new HashMap<>();
  }

  @Override
  public ProcessTriggerStrategy triggerStrategy(TableFormat format, Action action) {
    return ProcessTriggerStrategy.METADATA_TRIGGER;
  }

  @Override
  public Optional<TableProcess> trigger(TableRuntime tableRuntime, Action action) {
    // TODO: Implement Paimon compaction
    return Optional.empty();
  }

  @Override
  public TableProcess recover(TableRuntime tableRuntime, TableProcessStore store)
      throws RecoverProcessFailedException {
    // TODO: Implement recovery
    throw new RecoverProcessFailedException("Paimon process recovery not yet implemented");
  }

  @Override
  public void open(Map<String, String> properties) {
    // No initialization needed
  }

  @Override
  public void close() {
    // No cleanup needed
  }
}
