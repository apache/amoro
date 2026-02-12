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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MetricCategoryTest {

  @Test
  public void testMatchesSelfOptimizing() {
    assertTrue(
        MetricCategory.SELF_OPTIMIZING.matches("table_optimizing_status_idle_duration_mills"));
    assertTrue(MetricCategory.SELF_OPTIMIZING.matches("table_optimizing_process_total_count"));
    assertFalse(MetricCategory.SELF_OPTIMIZING.matches("optimizer_group_pending_tasks"));
    assertFalse(MetricCategory.SELF_OPTIMIZING.matches("table_summary_total_files"));
  }

  @Test
  public void testMatchesOptimizerGroup() {
    assertTrue(MetricCategory.OPTIMIZER_GROUP.matches("optimizer_group_pending_tasks"));
    assertTrue(MetricCategory.OPTIMIZER_GROUP.matches("optimizer_group_threads"));
    assertFalse(MetricCategory.OPTIMIZER_GROUP.matches("table_optimizing_status_in_idle"));
  }

  @Test
  public void testMatchesOrphanFiles() {
    assertTrue(MetricCategory.ORPHAN_FILES.matches("table_orphan_content_file_cleaning_count"));
    assertTrue(
        MetricCategory.ORPHAN_FILES.matches("table_expected_orphan_metadata_file_cleaning_count"));
    assertFalse(MetricCategory.ORPHAN_FILES.matches("table_summary_total_files"));
  }

  @Test
  public void testMatchesAmsJvm() {
    assertTrue(MetricCategory.AMS_JVM.matches("ams_jvm_cpu_load"));
    assertTrue(MetricCategory.AMS_JVM.matches("ams_jvm_memory_heap_used"));
    assertTrue(MetricCategory.AMS_JVM.matches("ams_ha_state"));
    assertFalse(MetricCategory.AMS_JVM.matches("table_summary_total_files"));
  }

  @Test
  public void testMatchesTableSummary() {
    assertTrue(MetricCategory.TABLE_SUMMARY.matches("table_summary_total_files"));
    assertTrue(MetricCategory.TABLE_SUMMARY.matches("table_summary_health_score"));
    assertFalse(MetricCategory.TABLE_SUMMARY.matches("table_optimizing_status_in_idle"));
  }

  @Test
  public void testFindCategory() {
    assertEquals(
        MetricCategory.SELF_OPTIMIZING,
        MetricCategory.findCategory("table_optimizing_status_idle_duration_mills"));
    assertEquals(
        MetricCategory.OPTIMIZER_GROUP,
        MetricCategory.findCategory("optimizer_group_pending_tasks"));
    assertEquals(
        MetricCategory.ORPHAN_FILES,
        MetricCategory.findCategory("table_orphan_content_file_cleaning_count"));
    assertEquals(MetricCategory.AMS_JVM, MetricCategory.findCategory("ams_jvm_cpu_load"));
    assertEquals(MetricCategory.AMS_JVM, MetricCategory.findCategory("ams_ha_state"));
    assertEquals(
        MetricCategory.TABLE_SUMMARY, MetricCategory.findCategory("table_summary_total_files"));
    assertNull(MetricCategory.findCategory("unknown_metric"));
  }

  @Test
  public void testParseDisabledCategoriesEmpty() {
    Set<MetricCategory> disabled = MetricCategory.parseDisabledCategories(Collections.emptyMap());
    assertTrue(disabled.isEmpty());
  }

  @Test
  public void testParseDisabledCategoriesSomeDisabled() {
    Map<String, String> properties = new HashMap<>();
    properties.put("category.self-optimizing.enabled", "false");
    properties.put("category.table-summary.enabled", "false");
    properties.put("category.ams-jvm.enabled", "true");

    Set<MetricCategory> disabled = MetricCategory.parseDisabledCategories(properties);

    assertEquals(2, disabled.size());
    assertTrue(disabled.contains(MetricCategory.SELF_OPTIMIZING));
    assertTrue(disabled.contains(MetricCategory.TABLE_SUMMARY));
    assertFalse(disabled.contains(MetricCategory.AMS_JVM));
    assertFalse(disabled.contains(MetricCategory.OPTIMIZER_GROUP));
  }

  @Test
  public void testParseDisabledCategoriesCaseInsensitive() {
    Map<String, String> properties = new HashMap<>();
    properties.put("category.optimizer-group.enabled", "FALSE");
    properties.put("category.orphan-files.enabled", "False");

    Set<MetricCategory> disabled = MetricCategory.parseDisabledCategories(properties);

    assertEquals(2, disabled.size());
    assertTrue(disabled.contains(MetricCategory.OPTIMIZER_GROUP));
    assertTrue(disabled.contains(MetricCategory.ORPHAN_FILES));
  }

  @Test
  public void testParseDisabledCategoriesInvalidValueTreatedAsEnabled() {
    Map<String, String> properties = new HashMap<>();
    properties.put("category.ams-jvm.enabled", "invalid");

    Set<MetricCategory> disabled = MetricCategory.parseDisabledCategories(properties);

    assertTrue(disabled.isEmpty());
  }
}
