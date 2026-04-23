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

package org.apache.amoro.server.table;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.metrics.MetricRegistry;
import org.junit.Test;

public class TestTableMaintainerMetrics {

  @Test
  public void testOrphanMetadataMetricNameCompatibility() {
    ServerTableIdentifier identifier =
        ServerTableIdentifier.of(1L, "test_catalog", "test_db", "test_table", TableFormat.ICEBERG);
    TableMaintainerMetrics tableMaintainerMetrics = new TableMaintainerMetrics(identifier);
    MetricRegistry registry = new MetricRegistry();

    tableMaintainerMetrics.register(registry);

    assertTrue(containsMetric(registry, "table_orphan_metadata_file_cleaning_count"));
    assertTrue(containsMetric(registry, "table_expected_orphan_metadata_file_cleaning_count"));
    assertFalse(containsMetric(registry, "table_orphan_metadata_files_cleaned_count"));
    assertFalse(containsMetric(registry, "table_orphan_metadata_files_cleaned_expected_count"));
  }

  private boolean containsMetric(MetricRegistry registry, String metricName) {
    return registry.getMetrics().keySet().stream()
        .anyMatch(metricKey -> metricName.equals(metricKey.getDefine().getName()));
  }
}
