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

package org.apache.amoro.server.optimizing;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutorFactory;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.TaskProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the default {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} class name for task rows
 * persisted before the multi-format refactor (C3), where the property did not yet exist.
 *
 * <p>Pre-0.9 Iceberg rows restored at AMS startup pass through {@link
 * org.apache.amoro.server.persistence.converter.TaskDescriptorTypeConverter}, which correctly falls
 * back to {@link org.apache.amoro.optimizing.RewriteStageTask} but does <b>not</b> populate the
 * missing executor-factory key on the descriptor's {@code properties} map. Once the task reaches
 * {@code OptimizerExecutor.executeTask(...)} it reads that key to instantiate the executor factory
 * via reflection and NPEs on the missing entry.
 *
 * <p>This util is the compensation hook: {@link OptimizingQueue}'s DB-restore path calls it per
 * task runtime and, when non-null, injects the returned class name into the descriptor properties.
 * For formats whose tasks always carry their own factory impl (e.g. Paimon) and for unknown
 * formats, we return {@code null} and emit a WARN so operators see the skipped row without taking
 * the process down.
 *
 * <p>Kept as a pure static util (no state) so it is trivially testable and can never diverge from
 * {@link TaskDescriptorTypeConverter}'s routing table — both must stay consistent with the default
 * factory impl each format emits at plan time.
 */
public final class LegacyExecutorFactoryDefaults {

  private static final Logger LOG = LoggerFactory.getLogger(LegacyExecutorFactoryDefaults.class);

  public static final String ICEBERG_FACTORY_IMPL = IcebergRewriteExecutorFactory.class.getName();
  public static final String MIXED_ICEBERG_FACTORY_IMPL =
      MixedIcebergRewriteExecutorFactory.class.getName();
  public static final String MIXED_HIVE_FACTORY_IMPL =
      MixedHiveRewriteExecutorFactory.class.getName();

  private LegacyExecutorFactoryDefaults() {}

  /**
   * Return the default {@code task-executor-factory-impl} class name for a legacy task row of the
   * given table format, or {@code null} if the format has no meaningful default (Paimon or
   * unknown).
   *
   * <p>Callers should treat {@code null} as "skip — the task descriptor must already carry the key,
   * or the row is unrecoverable". A WARN is logged for null cases so operators can spot stale rows
   * during restore.
   */
  public static String resolveDefaultExecutorFactoryImpl(TableFormat format) {
    if (format == null) {
      LOG.warn(
          "Cannot resolve default {} for legacy task row: table format is null",
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
      return null;
    }
    if (TableFormat.ICEBERG.equals(format)) {
      return ICEBERG_FACTORY_IMPL;
    }
    if (TableFormat.MIXED_ICEBERG.equals(format)) {
      return MIXED_ICEBERG_FACTORY_IMPL;
    }
    if (TableFormat.MIXED_HIVE.equals(format)) {
      return MIXED_HIVE_FACTORY_IMPL;
    }
    if (TableFormat.PAIMON.equals(format)) {
      // Paimon tasks always carry their own factory impl in properties (set by the Paimon planner),
      // so a missing key here indicates a corrupted row rather than a pre-refactor legacy row.
      LOG.warn(
          "Skipping default {} injection for PAIMON: Paimon tasks must carry the key at plan time",
          TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
      return null;
    }
    LOG.warn(
        "Skipping default {} injection: unknown table format {}",
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        format);
    return null;
  }
}
