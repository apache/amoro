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

package org.apache.amoro.server.persistence.converter;

import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Routes {@code task_runtime} rows to the correct {@link StagedTaskDescriptor} subclass based on
 * the task's {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} property.
 *
 * <p>The routing table is explicit and fail-fast: an unknown factory class name raises {@link
 * IllegalArgumentException} rather than silently downgrading to {@link RewriteStageTask}. Rows
 * whose {@code properties} column is null or missing the key default to {@link RewriteStageTask}
 * for backwards compatibility with legacy Iceberg rows produced before the multi-format refactor.
 *
 * <p>New formats register themselves in {@link TaskDescriptorRecoveryTypes}. We deliberately avoid
 * {@code ServiceLoader}: the mapping is small, bounded, and benefits from compile-time class
 * references so that renames in downstream modules break this file's build rather than surface as
 * NullPointer at runtime.
 */
public class TaskDescriptorTypeConverter implements TypeHandler<StagedTaskDescriptor<?, ?, ?>> {

  /**
   * Factory-impl class name → descriptor class.
   *
   * <p>Mixed (Iceberg/Hive) tables share {@link RewriteStageTask} with pure Iceberg — only the
   * executor differs, the descriptor/input/output types are identical.
   */
  static final Map<String, Class<? extends StagedTaskDescriptor<?, ?, ?>>>
      FACTORY_IMPL_TO_TASK_CLASS = TaskDescriptorRecoveryTypes.descriptorMappings();

  @Override
  public void setParameter(
      PreparedStatement ps, int i, StagedTaskDescriptor<?, ?, ?> parameter, JdbcType jdbcType)
      throws SQLException {}

  @Override
  public StagedTaskDescriptor<?, ?, ?> getResult(ResultSet rs, String columnName)
      throws SQLException {
    return instantiate(TaskDescriptorRecoveryTypes.readFactoryImpl(rs));
  }

  @Override
  public StagedTaskDescriptor<?, ?, ?> getResult(ResultSet rs, int columnIndex)
      throws SQLException {
    return instantiate(TaskDescriptorRecoveryTypes.readFactoryImpl(rs));
  }

  @Override
  public StagedTaskDescriptor<?, ?, ?> getResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    throw new UnsupportedOperationException(
        "TaskDescriptorTypeConverter.getResult(CallableStatement) is not supported. "
            + "ResultSet-based mapping should be used instead.");
  }

  private static StagedTaskDescriptor<?, ?, ?> instantiate(String factoryImpl) {
    if (factoryImpl == null || factoryImpl.isEmpty()) {
      // Backwards-compat with pre-refactor rows: default to Iceberg's RewriteStageTask.
      return new RewriteStageTask();
    }
    Class<? extends StagedTaskDescriptor<?, ?, ?>> clazz =
        FACTORY_IMPL_TO_TASK_CLASS.get(factoryImpl);
    if (clazz == null) {
      throw new IllegalArgumentException(
          "Unknown "
              + TaskProperties.TASK_EXECUTOR_FACTORY_IMPL
              + " = "
              + factoryImpl
              + "; register it in TaskDescriptorRecoveryTypes");
    }
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(
          "Failed to instantiate task descriptor " + clazz.getName() + " for " + factoryImpl, e);
    }
  }
}
