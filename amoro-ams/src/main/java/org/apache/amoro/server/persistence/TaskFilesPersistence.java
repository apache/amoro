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

package org.apache.amoro.server.persistence;

import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.persistence.mapper.OptimizingProcessMapper;
import org.apache.amoro.server.utils.CompressUtil;
import org.apache.amoro.utils.SerializationUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static facade for persisting and reloading the per-task {@code OptimizingInput} blob bound to an
 * {@code OptimizingProcess}.
 *
 * <p>Storage shape is intentionally format-agnostic: we serialize a {@code Map<Integer,
 * BaseOptimizingInput>} via {@link SerializationUtil#simpleSerialize(Object)} (Java serialization)
 * and gzip it through {@link CompressUtil}. Any {@link BaseOptimizingInput} subclass that is {@code
 * Serializable} round-trips without this layer needing to know the concrete type — concretely, both
 * the Iceberg {@code RewriteFilesInput} and Paimon {@code PaimonCompactionInput} are covered.
 *
 * <p>The reason the load-side return type is {@link BaseOptimizingInput} (rather than a format
 * specific subclass) is recovery: {@code OptimizingQueue.TableOptimizingProcess#loadTaskRuntimes}
 * pushes each input into {@link StagedTaskDescriptor#setInput(Object)} whose signature is erased to
 * {@code I}. The caller and the descriptor instance (resolved by {@code
 * TaskDescriptorTypeConverter}) agree on the concrete subclass at runtime; this layer only
 * guarantees we do not downcast prematurely.
 */
public class TaskFilesPersistence {

  private static final DatabasePersistence persistence = new DatabasePersistence();

  /**
   * Persist the per-task {@code OptimizingInput} map for one process, keyed by {@code taskId}.
   *
   * <p>Accepts any {@link TaskRuntime} whose descriptor declares a {@link BaseOptimizingInput}
   * subclass — so Iceberg {@code RewriteStageTask} / {@code RewriteFilesInput} and Paimon {@code
   * PaimonCompactionTask} / {@code PaimonCompactionInput} both flow through unchanged.
   */
  public static void persistTaskInputs(
      long processId,
      Collection<
              ? extends
                  TaskRuntime<? extends StagedTaskDescriptor<? extends BaseOptimizingInput, ?, ?>>>
          tasks) {
    persistence.persistTaskInputs(
        processId,
        tasks.stream()
            .collect(
                Collectors.toMap(
                    e -> e.getTaskId().getTaskId(), task -> task.getTaskDescriptor().getInput())));
  }

  /**
   * Load the per-task inputs previously persisted for {@code processId}. Returns an empty map if
   * the row is missing or carries no blob.
   *
   * <p>Each value is a {@link BaseOptimizingInput} subclass; the concrete type is whatever {@code
   * persistTaskInputs} wrote (Java serialization preserves the runtime class). Callers that need
   * the format-specific fields are expected to obtain the matching descriptor via {@code
   * TaskDescriptorTypeConverter} and route through {@link StagedTaskDescriptor#setInput}.
   */
  public static Map<Integer, BaseOptimizingInput> loadTaskInputs(long processId) {
    List<byte[]> bytes =
        persistence.getAs(
            OptimizingProcessMapper.class, mapper -> mapper.selectProcessInputFiles(processId));
    if (bytes == null || bytes.isEmpty()) {
      return Collections.emptyMap();
    } else {
      return SerializationUtil.simpleDeserialize(CompressUtil.unGzip(bytes.get(0)));
    }
  }

  public static RewriteFilesOutput loadTaskOutput(byte[] content) {
    return SerializationUtil.simpleDeserialize(content);
  }

  private static class DatabasePersistence extends PersistentBase {

    public void persistTaskInputs(long processId, Map<Integer, BaseOptimizingInput> tasks) {
      doAs(
          OptimizingProcessMapper.class,
          mapper -> mapper.updateProcessInputFiles(processId, tasks));
    }
  }
}
