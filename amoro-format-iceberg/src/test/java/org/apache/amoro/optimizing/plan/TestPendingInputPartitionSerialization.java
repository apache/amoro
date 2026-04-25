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

package org.apache.amoro.optimizing.plan;

import org.apache.amoro.TableFormat;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator.PendingInput;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.shade.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.amoro.table.MixedTable;
import org.apache.amoro.utils.MixedDataFiles;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for PendingInput partition serialization round-trip with custom partition transforms
 * (month, hour) and unpartitioned tables. Verifies that MixedDataFiles.data() correctly handles
 * transforms that Iceberg's native DataFiles.data() cannot parse.
 */
public class TestPendingInputPartitionSerialization {

  private static final Schema SCHEMA =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "ts", Types.LongType.get()),
          Types.NestedField.required(3, "op_time", Types.TimestampType.withoutZone()));

  private MixedTable mockTable(PartitionSpec spec) {
    MixedTable mock = Mockito.mock(MixedTable.class);
    Mockito.when(mock.format()).thenReturn(TableFormat.MIXED_ICEBERG);
    Mockito.when(mock.spec()).thenReturn(spec);
    return mock;
  }

  /**
   * Test rebuildPartitions with month partition transform. Month partitions from Mixed-format are
   * serialized as "yyyy-MM" (e.g., "month=2024-01"), which Iceberg's native DataFiles.data() cannot
   * parse (it expects an integer month offset). MixedDataFiles.data() handles this correctly.
   */
  @Test
  public void testMonthPartitionTransformRebuild() throws Exception {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).month("op_time", "month").build();
    MixedTable mockTable = mockTable(spec);

    // Directly construct partition path as Mixed-format serializes it: "month=2024-01"
    String partitionPath = "month=2024-01";
    String fullPath = spec.specId() + ":" + partitionPath;

    PendingInput input = new PendingInput();
    input.getPartitionPaths().add(fullPath);

    // Verify MixedDataFiles.data() can parse this
    StructLike parsed = MixedDataFiles.data(spec, partitionPath);
    Assert.assertNotNull("MixedDataFiles should parse month partition", parsed);

    // Rebuild partitions from paths
    input.rebuildPartitions(mockTable);
    Assert.assertFalse("Partitions should be rebuilt", input.getPartitions().isEmpty());
    Assert.assertEquals(1, input.getPartitions().get(spec.specId()).size());
  }

  /**
   * Test rebuildPartitions with hour partition transform. Hour partitions from Mixed-format are
   * serialized as "yyyy-MM-dd-HH", which Iceberg's native parser cannot handle.
   */
  @Test
  public void testHourPartitionTransformRebuild() throws Exception {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).hour("op_time", "hour").build();
    MixedTable mockTable = mockTable(spec);

    // Directly construct partition path as Mixed-format serializes it: "hour=2024-01-15-10"
    String partitionPath = "hour=2024-01-15-10";
    String fullPath = spec.specId() + ":" + partitionPath;

    PendingInput input = new PendingInput();
    input.getPartitionPaths().add(fullPath);

    // Verify MixedDataFiles.data() can parse this
    StructLike parsed = MixedDataFiles.data(spec, partitionPath);
    Assert.assertNotNull("MixedDataFiles should parse hour partition", parsed);

    // Rebuild partitions from paths
    input.rebuildPartitions(mockTable);
    Assert.assertFalse("Partitions should be rebuilt", input.getPartitions().isEmpty());
    Assert.assertEquals(1, input.getPartitions().get(spec.specId()).size());
  }

  /**
   * Test full round-trip: buildPartitionPaths -> JSON serialize -> JSON deserialize ->
   * rebuildPartitions for month transform. This simulates the complete DB persistence cycle.
   */
  @Test
  public void testMonthPartitionFullRoundTrip() throws Exception {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).month("op_time", "month").build();
    MixedTable mockTable = mockTable(spec);

    PendingInput input = new PendingInput();
    // Add two different month partitions
    input.getPartitionPaths().add(spec.specId() + ":month=2024-01");
    input.getPartitionPaths().add(spec.specId() + ":month=2024-06");

    // JSON round-trip
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(input);
    PendingInput deserialized = mapper.readValue(json, PendingInput.class);

    Assert.assertEquals(2, deserialized.getPartitionPaths().size());

    // Rebuild
    deserialized.rebuildPartitions(mockTable);
    Assert.assertEquals(2, deserialized.getPartitions().get(spec.specId()).size());
  }

  /**
   * Test round-trip for unpartitioned tables. Unpartitioned tables use EMPTY_STRUCT and should
   * serialize as "specId:" with empty path.
   */
  @Test
  public void testUnpartitionedTableRoundTrip() throws Exception {
    PartitionSpec spec = PartitionSpec.unpartitioned();
    MixedTable mockTable = mockTable(spec);

    PendingInput input = new PendingInput();
    input
        .getPartitions()
        .computeIfAbsent(spec.specId(), k -> Sets.newHashSet())
        .add(TablePropertyUtil.EMPTY_STRUCT);

    // Build partition paths
    input.buildPartitionPaths(mockTable);
    Assert.assertFalse(input.getPartitionPaths().isEmpty());

    String path = input.getPartitionPaths().iterator().next();
    Assert.assertTrue(
        "Unpartitioned path should be specId: with empty path, got: " + path,
        path.equals(spec.specId() + ":"));

    // JSON round-trip
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(input);
    PendingInput deserialized = mapper.readValue(json, PendingInput.class);
    Assert.assertEquals(input.getPartitionPaths(), deserialized.getPartitionPaths());

    // Rebuild
    deserialized.rebuildPartitions(mockTable);
    Assert.assertFalse(deserialized.getPartitions().isEmpty());
    Assert.assertEquals(1, deserialized.getPartitions().get(spec.specId()).size());

    // Verify the rebuilt struct has 0 fields (EMPTY_STRUCT)
    StructLike rebuilt = deserialized.getPartitions().get(spec.specId()).iterator().next();
    Assert.assertEquals(0, rebuilt.size());
  }

  /** Test that Hive null sentinel is handled without throwing exceptions. */
  @Test
  public void testHiveNullSentinel() throws Exception {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).month("op_time", "month").build();
    MixedTable mockTable = mockTable(spec);

    PendingInput input = new PendingInput();
    input.getPartitionPaths().add(spec.specId() + ":month=__HIVE_DEFAULT_PARTITION__");

    // Rebuild should not throw
    input.rebuildPartitions(mockTable);
    // Result may vary - key is no exception
  }

  /** Test that empty PendingInput (no partitions) round-trips correctly. */
  @Test
  public void testEmptyPendingInput() throws Exception {
    PartitionSpec spec = PartitionSpec.unpartitioned();
    MixedTable mockTable = mockTable(spec);

    PendingInput input = new PendingInput();
    // Both partitions and partitionPaths are empty

    input.buildPartitionPaths(mockTable);
    Assert.assertTrue(
        "Empty input should have no partition paths", input.getPartitionPaths().isEmpty());

    input.rebuildPartitions(mockTable);
    Assert.assertTrue("Empty input should have no partitions", input.getPartitions().isEmpty());
  }
}
