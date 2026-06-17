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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.PaimonMetricsSummary;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.primary.PaimonPrimaryKeyCompactionTask;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutorFactory;
import org.apache.amoro.optimizing.BaseOptimizingInput;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.process.StagedTaskDescriptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for restoring persisted optimizing task descriptors and their typed payloads.
 *
 * <p>Rows without {@link TaskProperties#TASK_EXECUTOR_FACTORY_IMPL} are legacy Iceberg rows and
 * intentionally fall back to {@link RewriteStageTask}. Rows with an unknown factory fail fast so a
 * new lake format cannot be silently restored with the wrong descriptor, input, output, or summary
 * class.
 */
public final class TaskDescriptorRecoveryTypes {

  private static final Gson GSON = new Gson();
  private static final RecoveryTypes LEGACY_ICEBERG_TYPES =
      new RecoveryTypes(
          RewriteStageTask.class,
          RewriteFilesInput.class,
          RewriteFilesOutput.class,
          MetricsSummary.class);
  private static final Map<String, RecoveryTypes> FACTORY_IMPL_TO_RECOVERY_TYPES;

  static {
    Map<String, RecoveryTypes> mappings = new HashMap<>();
    mappings.put(IcebergRewriteExecutorFactory.class.getName(), LEGACY_ICEBERG_TYPES);
    mappings.put(MixedIcebergRewriteExecutorFactory.class.getName(), LEGACY_ICEBERG_TYPES);
    mappings.put(MixedHiveRewriteExecutorFactory.class.getName(), LEGACY_ICEBERG_TYPES);
    mappings.put(
        PaimonCompactionExecutorFactory.class.getName(),
        new RecoveryTypes(
            PaimonCompactionTask.class,
            PaimonCompactionInput.class,
            PaimonCompactionOutput.class,
            PaimonMetricsSummary.class));
    mappings.put(
        PaimonPrimaryKeyCompactionExecutorFactory.class.getName(),
        new RecoveryTypes(
            PaimonPrimaryKeyCompactionTask.class,
            PaimonPrimaryKeyCompactionInput.class,
            PaimonPrimaryKeyCompactionOutput.class,
            PaimonMetricsSummary.class));
    FACTORY_IMPL_TO_RECOVERY_TYPES = Collections.unmodifiableMap(mappings);
  }

  private TaskDescriptorRecoveryTypes() {}

  public static RecoveryTypes resolve(String factoryImpl) {
    if (factoryImpl == null || factoryImpl.isEmpty()) {
      return LEGACY_ICEBERG_TYPES;
    }
    RecoveryTypes types = FACTORY_IMPL_TO_RECOVERY_TYPES.get(factoryImpl);
    if (types == null) {
      throw new IllegalArgumentException(
          "Unknown "
              + TaskProperties.TASK_EXECUTOR_FACTORY_IMPL
              + " = "
              + factoryImpl
              + "; register it in TaskDescriptorRecoveryTypes");
    }
    return types;
  }

  public static void validateRecoveredTask(
      StagedTaskDescriptor<?, ?, ?> descriptor, BaseOptimizingInput input) {
    validateRecoveredTask(descriptor, input, null);
  }

  public static void validateRecoveredTask(
      StagedTaskDescriptor<?, ?, ?> descriptor,
      BaseOptimizingInput input,
      TableFormat tableFormat) {
    if (descriptor == null) {
      throw new IllegalArgumentException("Recovered task descriptor must not be null");
    }
    String factoryImpl = factoryImpl(descriptor.getProperties());
    if ((factoryImpl == null || factoryImpl.isEmpty())
        && !allowsLegacyMissingFactory(tableFormat)) {
      throw new IllegalArgumentException(
          "Recovered task is missing "
              + TaskProperties.TASK_EXECUTOR_FACTORY_IMPL
              + " for table format "
              + tableFormat);
    }
    RecoveryTypes types = resolve(factoryImpl);
    validateType("descriptor", descriptor, types.descriptorClass());
    validateType("input", input, types.inputClass());
    validateType("output", descriptor.getOutput(), types.outputClass());
    validateType("summary", descriptor.getSummary(), types.summaryClass());
  }

  static Map<String, Class<? extends StagedTaskDescriptor<?, ?, ?>>> descriptorMappings() {
    Map<String, Class<? extends StagedTaskDescriptor<?, ?, ?>>> mappings = new HashMap<>();
    FACTORY_IMPL_TO_RECOVERY_TYPES.forEach(
        (factoryImpl, types) -> mappings.put(factoryImpl, types.descriptorClass()));
    return Collections.unmodifiableMap(mappings);
  }

  static String readFactoryImpl(ResultSet rs) throws SQLException {
    String raw;
    try {
      raw = rs.getString("properties");
    } catch (SQLException e) {
      return null;
    }
    if (raw == null || raw.isEmpty()) {
      return null;
    }
    Map<String, String> props;
    try {
      props = GSON.fromJson(raw, new TypeToken<Map<String, String>>() {}.getType());
    } catch (RuntimeException e) {
      return null;
    }
    return factoryImpl(props);
  }

  private static String factoryImpl(Map<String, String> properties) {
    if (properties == null) {
      return null;
    }
    return properties.get(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL);
  }

  private static boolean allowsLegacyMissingFactory(TableFormat tableFormat) {
    return tableFormat == null
        || TableFormat.ICEBERG.equals(tableFormat)
        || TableFormat.MIXED_ICEBERG.equals(tableFormat)
        || TableFormat.MIXED_HIVE.equals(tableFormat);
  }

  private static void validateType(String name, Object value, Class<?> expectedClass) {
    if (value == null || expectedClass.isInstance(value)) {
      return;
    }
    throw new IllegalArgumentException(
        "Recovered task "
            + name
            + " type mismatch, expected "
            + expectedClass.getName()
            + " but got "
            + value.getClass().getName());
  }

  public static final class RecoveryTypes {
    private final Class<? extends StagedTaskDescriptor<?, ?, ?>> descriptorClass;
    private final Class<? extends BaseOptimizingInput> inputClass;
    private final Class<?> outputClass;
    private final Class<?> summaryClass;

    private RecoveryTypes(
        Class<? extends StagedTaskDescriptor<?, ?, ?>> descriptorClass,
        Class<? extends BaseOptimizingInput> inputClass,
        Class<?> outputClass,
        Class<?> summaryClass) {
      this.descriptorClass = descriptorClass;
      this.inputClass = inputClass;
      this.outputClass = outputClass;
      this.summaryClass = summaryClass;
    }

    public Class<? extends StagedTaskDescriptor<?, ?, ?>> descriptorClass() {
      return descriptorClass;
    }

    public Class<? extends BaseOptimizingInput> inputClass() {
      return inputClass;
    }

    public Class<?> outputClass() {
      return outputClass;
    }

    public Class<?> summaryClass() {
      return summaryClass;
    }
  }
}
