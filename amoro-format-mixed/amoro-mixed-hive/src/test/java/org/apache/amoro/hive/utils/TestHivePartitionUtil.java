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

package org.apache.amoro.hive.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.iceberg.PartitionData;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link HivePartitionUtil#buildPartitionData(List, PartitionSpec)}. Callers such as
 * {@code HiveMetaSynchronizer} and {@code UpgradeHiveTableUtil} pass the raw values of a Hive
 * partition, and such a value is an arbitrary string that has to reach the matching partition field
 * unchanged.
 */
public class TestHivePartitionUtil {

  private static final String DT = "dt";
  private static final String ID = "id";

  @Test
  public void testValueContainingSlashRoundTripsExactly() {
    PartitionSpec spec = stringPartitionSpec(DT);

    assertStringValueRoundTrips(spec, "2024/01");
  }

  @Test
  public void testStringValuesArePreservedVerbatim() {
    PartitionSpec spec = stringPartitionSpec(DT);
    // the last value is a multi-byte string, written with escapes so that this file stays ASCII
    List<String> values = Arrays.asList("abc", "2024-01", "a=b", "", "null", "\u5206\u533a-\u00e9");

    for (String value : values) {
      assertStringValueRoundTrips(spec, value);
    }
  }

  /** Raw Hive values are converted field by field, never through an encoded partition path. */
  @Test
  public void testPercentAndPlusAreNotUrlDecoded() {
    PartitionSpec spec = stringPartitionSpec(DT);
    List<String> values = Arrays.asList("50%25", "a+b", "2024%2F01", "%");

    for (String value : values) {
      assertStringValueRoundTrips(spec, value);
    }
  }

  @Test
  public void testMultiColumnStringAndIntPartitionIsPreserved() {
    PartitionSpec spec = stringAndIntPartitionSpec();
    List<String> hiveValues = Arrays.asList("2024/01", "5");

    StructLike partitionData = HivePartitionUtil.buildPartitionData(hiveValues, spec);

    assertEquals("2024/01", partitionData.get(0, String.class));
    assertEquals(Integer.valueOf(5), partitionData.get(1, Integer.class));
    assertEquals(
        hiveValues, HivePartitionUtil.partitionValuesAsList(partitionData, spec.partitionType()));
  }

  /**
   * The callers look partitions up in maps keyed by Iceberg partition data, so data built from Hive
   * values must be equal to the partition data of the real data files.
   */
  @Test
  public void testSlashValueResolvesToRealPartitionKey() {
    PartitionSpec spec = stringPartitionSpec(DT);
    String value = "2024/01";
    PartitionData realPartitionData = new PartitionData(spec.partitionType());
    realPartitionData.set(0, value);
    StructLikeMap<String> partitions = StructLikeMap.create(spec.partitionType());
    partitions.put(realPartitionData, "data-file");

    StructLike partitionData =
        HivePartitionUtil.buildPartitionData(Collections.singletonList(value), spec);

    assertEquals("data-file", partitions.get(partitionData));
  }

  private static void assertStringValueRoundTrips(PartitionSpec spec, String value) {
    List<String> hiveValues = Collections.singletonList(value);

    StructLike partitionData = HivePartitionUtil.buildPartitionData(hiveValues, spec);

    assertEquals(value, partitionData.get(0, String.class), "value: " + value);
    assertEquals(
        hiveValues,
        HivePartitionUtil.partitionValuesAsList(partitionData, spec.partitionType()),
        "value: " + value);
  }

  private static PartitionSpec stringPartitionSpec(String fieldName) {
    Schema schema = new Schema(Types.NestedField.optional(1, fieldName, Types.StringType.get()));
    return PartitionSpec.builderFor(schema).identity(fieldName).build();
  }

  private static PartitionSpec stringAndIntPartitionSpec() {
    Schema schema =
        new Schema(
            Types.NestedField.optional(1, DT, Types.StringType.get()),
            Types.NestedField.optional(2, ID, Types.IntegerType.get()));
    return PartitionSpec.builderFor(schema).identity(DT).identity(ID).build();
  }
}
