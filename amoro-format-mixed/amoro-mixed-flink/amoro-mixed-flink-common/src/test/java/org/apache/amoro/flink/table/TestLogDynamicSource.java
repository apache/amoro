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

package org.apache.amoro.flink.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.table.MixedTable;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.table.data.RowData;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

public class TestLogDynamicSource {

  @Test
  public void testCopyPreservesProjectionAndWatermark() {
    MixedTable mixedTable = mock(MixedTable.class);
    when(mixedTable.properties()).thenReturn(Collections.emptyMap());
    when(mixedTable.name()).thenReturn("test_table");

    Schema schema =
        new Schema(
            Types.NestedField.required(1, "id", Types.IntegerType.get()),
            Types.NestedField.optional(2, "name", Types.StringType.get()));

    LogDynamicSource source =
        new LogDynamicSource(new Properties(), schema, new Configuration(), mixedTable);

    WatermarkStrategy<RowData> watermarkStrategy = WatermarkStrategy.noWatermarks();
    source.applyWatermark(watermarkStrategy);
    source.applyProjection(new int[][] {{1}});

    LogDynamicSource copy = (LogDynamicSource) source.copy();

    assertThat(copy.watermarkStrategy).isSameAs(watermarkStrategy);
    assertThat(copy.projectedFields).containsExactly(1);
  }
}
