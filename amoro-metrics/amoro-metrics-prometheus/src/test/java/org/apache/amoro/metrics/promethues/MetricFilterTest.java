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

package org.apache.amoro.metrics.promethues;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MetricFilterTest {

  @Test
  public void testAcceptAllMatchesEverything() {
    MetricFilter filter = MetricFilter.ACCEPT_ALL;
    assertTrue(filter.matches("table_optimizing_status_in_idle"));
    assertTrue(filter.matches("optimizer_group_pending_tasks"));
    assertTrue(filter.matches("any_metric_name"));
  }

  @Test
  public void testFromPropertiesEmptyReturnsAcceptAll() {
    MetricFilter filter = MetricFilter.fromProperties(Collections.emptyMap());
    assertSame(MetricFilter.ACCEPT_ALL, filter);
  }

  @Test
  public void testIncludesOnly() {
    Map<String, String> props = new HashMap<>();
    props.put("metric-filter.includes", "table_optimizing_.*|optimizer_group_.*");

    MetricFilter filter = MetricFilter.fromProperties(props);
    assertTrue(filter.matches("table_optimizing_status_in_idle"));
    assertTrue(filter.matches("optimizer_group_pending_tasks"));
    assertFalse(filter.matches("table_summary_total_files"));
    assertFalse(filter.matches("ams_jvm_cpu_load"));
  }

  @Test
  public void testExcludesOnly() {
    Map<String, String> props = new HashMap<>();
    props.put("metric-filter.excludes", "table_summary_.*");

    MetricFilter filter = MetricFilter.fromProperties(props);
    assertTrue(filter.matches("table_optimizing_status_in_idle"));
    assertTrue(filter.matches("ams_jvm_cpu_load"));
    assertFalse(filter.matches("table_summary_total_files"));
    assertFalse(filter.matches("table_summary_health_score"));
  }

  @Test
  public void testIncludesAndExcludes() {
    Map<String, String> props = new HashMap<>();
    props.put("metric-filter.includes", "table_.*");
    props.put("metric-filter.excludes", "table_summary_.*");

    MetricFilter filter = MetricFilter.fromProperties(props);
    assertTrue(filter.matches("table_optimizing_status_in_idle"));
    assertTrue(filter.matches("table_orphan_content_file_cleaning_count"));
    assertFalse(filter.matches("table_summary_total_files"));
    assertFalse(filter.matches("optimizer_group_pending_tasks"));
  }

  @Test
  public void testExcludesTakesPrecedenceOverIncludes() {
    Map<String, String> props = new HashMap<>();
    props.put("metric-filter.includes", ".*");
    props.put("metric-filter.excludes", "ams_jvm_.*");

    MetricFilter filter = MetricFilter.fromProperties(props);
    assertTrue(filter.matches("table_optimizing_status_in_idle"));
    assertFalse(filter.matches("ams_jvm_cpu_load"));
    assertFalse(filter.matches("ams_jvm_memory_heap_used"));
  }

  @Test
  public void testExactMatchPattern() {
    Map<String, String> props = new HashMap<>();
    props.put("metric-filter.includes", "ams_jvm_cpu_load");

    MetricFilter filter = MetricFilter.fromProperties(props);
    assertTrue(filter.matches("ams_jvm_cpu_load"));
    assertFalse(filter.matches("ams_jvm_cpu_time"));
  }
}
