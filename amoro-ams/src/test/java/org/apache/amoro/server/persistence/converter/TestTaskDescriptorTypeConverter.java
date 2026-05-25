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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import org.apache.amoro.TableFormat;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionInput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionOutput;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionTask;
import org.apache.amoro.formats.paimon.optimizing.PaimonMetricsSummary;
import org.apache.amoro.hive.optimizing.MixedHiveRewriteExecutorFactory;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.process.StagedTaskDescriptor;
import org.apache.amoro.server.optimizing.LegacyExecutorFactoryDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link TaskDescriptorTypeConverter}. Each scenario stubs a JDBC {@link ResultSet}
 * whose {@code properties} column is the same JSON that {@link Map2StringConverter} would have
 * produced on the write path, so we exercise the real deserialization path end-to-end.
 */
public class TestTaskDescriptorTypeConverter {

  private static final Gson GSON = new Gson();

  private final TaskDescriptorTypeConverter converter = new TaskDescriptorTypeConverter();

  @Test
  public void routesPaimonFactoryImplToPaimonCompactionTask() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            PaimonCompactionExecutorFactory.class.getName());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertNotNull(descriptor);
    Assertions.assertTrue(
        descriptor instanceof PaimonCompactionTask,
        "expected PaimonCompactionTask but got " + descriptor.getClass().getName());
  }

  @Test
  public void routesIcebergFactoryImplToRewriteStageTask() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            IcebergRewriteExecutorFactory.class.getName());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertNotNull(descriptor);
    Assertions.assertTrue(
        descriptor instanceof RewriteStageTask,
        "expected RewriteStageTask but got " + descriptor.getClass().getName());
  }

  @Test
  public void routesMixedIcebergFactoryImplToRewriteStageTask() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            MixedIcebergRewriteExecutorFactory.class.getName());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(descriptor instanceof RewriteStageTask);
  }

  @Test
  public void routesMixedHiveFactoryImplToRewriteStageTask() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            MixedHiveRewriteExecutorFactory.class.getName());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(descriptor instanceof RewriteStageTask);
  }

  @Test
  public void nullPropertiesColumnDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("properties")).thenReturn(null);

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(
        descriptor instanceof RewriteStageTask,
        "null properties column must fall back to legacy Iceberg descriptor");
  }

  @Test
  public void emptyPropertiesStringDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("properties")).thenReturn("");

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(descriptor instanceof RewriteStageTask);
  }

  @Test
  public void propertiesWithoutFactoryImplKeyDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mockResultSetWithProperties("some-other-key", "some-value");

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(
        descriptor instanceof RewriteStageTask,
        "missing TASK_EXECUTOR_FACTORY_IMPL must fall back to legacy Iceberg descriptor");
  }

  @Test
  public void missingPropertiesColumnDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString(anyString()))
        .thenThrow(new SQLException("column 'properties' not in result set"));

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(
        descriptor instanceof RewriteStageTask,
        "absent properties column must not blow up recovery of legacy rows");
  }

  @Test
  public void malformedPropertiesJsonDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("properties")).thenReturn("{not-valid-json");

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(descriptor instanceof RewriteStageTask);
  }

  @Test
  public void unknownFactoryImplThrowsIllegalArgumentException() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            "com.example.totally.UnregisteredExecutorFactory");

    IllegalArgumentException ex =
        Assertions.assertThrows(
            IllegalArgumentException.class, () -> converter.getResult(rs, "stage"));
    Assertions.assertTrue(
        ex.getMessage().contains(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL),
        "error message should name the property key");
    Assertions.assertTrue(
        ex.getMessage().contains("UnregisteredExecutorFactory"),
        "error message should include the unknown class name");
  }

  @Test
  public void columnIndexOverloadRoutesSameWay() throws SQLException {
    ResultSet rs =
        mockResultSetWithProperties(
            TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
            PaimonCompactionExecutorFactory.class.getName());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, 1);

    Assertions.assertTrue(descriptor instanceof PaimonCompactionTask);
  }

  private static ResultSet mockResultSetWithProperties(String key, String value)
      throws SQLException {
    Map<String, String> props = new HashMap<>();
    props.put(key, value);
    return mockResultSetWithProperties(props);
  }

  private static ResultSet mockResultSetWithProperties(Map<String, String> props)
      throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("properties")).thenReturn(GSON.toJson(props));
    return rs;
  }

  @Test
  public void factoryImplClassNamesConsistentAcrossConverters() {
    Map<String, Class<? extends StagedTaskDescriptor<?, ?, ?>>> mapping =
        TaskDescriptorTypeConverter.FACTORY_IMPL_TO_TASK_CLASS;

    Assertions.assertEquals(
        RewriteStageTask.class, mapping.get(IcebergRewriteExecutorFactory.class.getName()));
    Assertions.assertEquals(
        RewriteStageTask.class, mapping.get(MixedIcebergRewriteExecutorFactory.class.getName()));
    Assertions.assertEquals(
        RewriteStageTask.class, mapping.get(MixedHiveRewriteExecutorFactory.class.getName()));
    Assertions.assertEquals(
        PaimonCompactionTask.class, mapping.get(PaimonCompactionExecutorFactory.class.getName()));

    Assertions.assertEquals(
        IcebergRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.ICEBERG_FACTORY_IMPL);
    Assertions.assertEquals(
        MixedIcebergRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.MIXED_ICEBERG_FACTORY_IMPL);
    Assertions.assertEquals(
        MixedHiveRewriteExecutorFactory.class.getName(),
        LegacyExecutorFactoryDefaults.MIXED_HIVE_FACTORY_IMPL);
  }

  @Test
  public void recoveryTypesCoverDescriptorInputOutputAndSummaryForAllKnownFactories() {
    assertRecoveryTypes(
        IcebergRewriteExecutorFactory.class.getName(),
        RewriteStageTask.class,
        RewriteFilesInput.class,
        RewriteFilesOutput.class,
        MetricsSummary.class);
    assertRecoveryTypes(
        MixedIcebergRewriteExecutorFactory.class.getName(),
        RewriteStageTask.class,
        RewriteFilesInput.class,
        RewriteFilesOutput.class,
        MetricsSummary.class);
    assertRecoveryTypes(
        MixedHiveRewriteExecutorFactory.class.getName(),
        RewriteStageTask.class,
        RewriteFilesInput.class,
        RewriteFilesOutput.class,
        MetricsSummary.class);
    assertRecoveryTypes(
        PaimonCompactionExecutorFactory.class.getName(),
        PaimonCompactionTask.class,
        PaimonCompactionInput.class,
        PaimonCompactionOutput.class,
        PaimonMetricsSummary.class);
  }

  @Test
  public void recoveryValidationRejectsDescriptorInputMismatch() {
    RewriteStageTask descriptor = new RewriteStageTask();
    descriptor.ensureExecutorFactoryImpl(IcebergRewriteExecutorFactory.class.getName());
    PaimonCompactionInput input =
        new PaimonCompactionInput(null, new byte[] {1}, 2, "u", "p", 9L, 10L);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> TaskDescriptorRecoveryTypes.validateRecoveredTask(descriptor, input));
  }

  @Test
  public void recoveryValidationRejectsMissingFactoryImplForPaimonEvenWithoutInput() {
    RewriteStageTask descriptor = new RewriteStageTask();

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            TaskDescriptorRecoveryTypes.validateRecoveredTask(
                descriptor, null, TableFormat.PAIMON));
  }

  private static void assertRecoveryTypes(
      String factoryImpl,
      Class<?> descriptorClass,
      Class<?> inputClass,
      Class<?> outputClass,
      Class<?> summaryClass) {
    TaskDescriptorRecoveryTypes.RecoveryTypes types =
        TaskDescriptorRecoveryTypes.resolve(factoryImpl);
    Assertions.assertEquals(descriptorClass, types.descriptorClass());
    Assertions.assertEquals(inputClass, types.inputClass());
    Assertions.assertEquals(outputClass, types.outputClass());
    Assertions.assertEquals(summaryClass, types.summaryClass());
  }

  @Test
  public void callableStatementPathThrowsUnsupported() throws SQLException {
    CallableStatement mockCs = mock(CallableStatement.class);
    Assertions.assertThrows(
        UnsupportedOperationException.class, () -> converter.getResult(mockCs, 1));
  }

  @Test
  public void emptyPropertiesMapDefaultsToRewriteStageTask() throws SQLException {
    ResultSet rs = mockResultSetWithProperties(Collections.emptyMap());

    StagedTaskDescriptor<?, ?, ?> descriptor = converter.getResult(rs, "stage");

    Assertions.assertTrue(descriptor instanceof RewriteStageTask);
  }
}
