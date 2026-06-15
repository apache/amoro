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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import org.apache.amoro.formats.paimon.optimizing.PaimonCompactionExecutorFactory;
import org.apache.amoro.formats.paimon.optimizing.PaimonMetricsSummary;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.MetricsSummary;
import org.apache.amoro.optimizing.TaskProperties;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TestTaskDescriptorSummaryConverter {

  private static final Gson GSON = new Gson();
  private final TaskDescriptorSummaryConverter converter = new TaskDescriptorSummaryConverter();

  @Test
  public void getResultReturnsPaimonMetricsSummaryForPaimonFactory() throws Exception {
    Object summary =
        converter.getResult(
            resultSet(
                "{\"compactedFileCount\":7,\"compactedFileSize\":700,"
                    + "\"producedFileCount\":2,\"producedFileSize\":200}",
                PaimonCompactionExecutorFactory.class.getName()),
            "metrics_summary");

    PaimonMetricsSummary paimonSummary = assertInstanceOf(PaimonMetricsSummary.class, summary);
    assertEquals(7L, paimonSummary.getCompactedFileCount());
    assertEquals(700L, paimonSummary.getCompactedFileSize());
    assertEquals(2L, paimonSummary.getProducedFileCount());
    assertEquals(200L, paimonSummary.getProducedFileSize());
  }

  @Test
  public void getResultReturnsMetricsSummaryForIcebergFactory() throws Exception {
    Object summary =
        converter.getResult(
            resultSet(
                "{\"rewriteDataFileCnt\":3,\"rewriteDataSize\":300,"
                    + "\"newDataFileCnt\":1,\"newDataSize\":100}",
                IcebergRewriteExecutorFactory.class.getName()),
            "metrics_summary");

    MetricsSummary metricsSummary = assertInstanceOf(MetricsSummary.class, summary);
    assertEquals(3, metricsSummary.getRewriteDataFileCnt());
    assertEquals(300L, metricsSummary.getRewriteDataSize());
    assertEquals(1, metricsSummary.getNewDataFileCnt());
    assertEquals(100L, metricsSummary.getNewDataSize());
  }

  @Test
  public void getResultReturnsLegacyMetricsSummaryWhenFactoryImplIsMissing() throws Exception {
    Object summary =
        converter.getResult(
            resultSet("{\"rewriteDataFileCnt\":5,\"rewriteDataSize\":50}", null),
            "metrics_summary");

    MetricsSummary metricsSummary = assertInstanceOf(MetricsSummary.class, summary);
    assertEquals(5, metricsSummary.getRewriteDataFileCnt());
    assertEquals(50L, metricsSummary.getRewriteDataSize());
  }

  @Test
  public void getResultReturnsNullForEmptySummary() throws Exception {
    assertNull(
        converter.getResult(
            resultSet(null, PaimonCompactionExecutorFactory.class.getName()), "metrics_summary"));
    assertNull(
        converter.getResult(
            resultSet("", PaimonCompactionExecutorFactory.class.getName()), "metrics_summary"));
  }

  @Test
  public void getResultRejectsUnknownFactoryImpl() throws Exception {
    ResultSet rs =
        resultSet("{\"rewriteDataFileCnt\":5,\"rewriteDataSize\":50}", "unknown.Factory");

    assertThrows(IllegalArgumentException.class, () -> converter.getResult(rs, "metrics_summary"));
  }

  private static ResultSet resultSet(String summaryJson, String factoryImpl) throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    when(rs.getString("metrics_summary")).thenReturn(summaryJson);
    when(rs.getString("properties")).thenReturn(propertiesJson(factoryImpl));
    return rs;
  }

  private static String propertiesJson(String factoryImpl) {
    Map<String, String> properties = new HashMap<>();
    if (factoryImpl != null) {
      properties.put(TaskProperties.TASK_EXECUTOR_FACTORY_IMPL, factoryImpl);
    }
    return GSON.toJson(properties);
  }
}
