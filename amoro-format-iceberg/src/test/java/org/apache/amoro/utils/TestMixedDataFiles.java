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

package org.apache.amoro.utils;

import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TestMixedDataFiles {

  private static final Schema SCHEMA =
      new Schema(Types.NestedField.required(1, "dt", Types.TimestampType.withoutZone()));

  private static final Schema DATE_SCHEMA =
      new Schema(Types.NestedField.optional(1, "dt", Types.DateType.get()));
  private static final Schema TIMESTAMP_TZ_SCHEMA =
      new Schema(Types.NestedField.required(1, "dt", Types.TimestampType.withZone()));
  private static final int[][] YEAR_OFFSETS = {{1969, -1}, {1970, 0}, {2024, 54}, {2025, 55}};

  @Test
  public void testMonthPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).month("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-11T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testDaysPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).day("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-11T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testHoursPartition() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).hour("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);

    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());

    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-11T09:30:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);

    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-11T12:00:00"))));
    partitionPath = spec.partitionToPath(partitionKey);
    partitionData = MixedDataFiles.data(spec, partitionPath);
    p1.set(partitionKey);
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);

    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-11T15:30:00"))));
    partitionPath = spec.partitionToPath(partitionKey);
    partitionData = MixedDataFiles.data(spec, partitionPath);
    p1.set(partitionKey);
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);

    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2022-11-12T00:00:00"))));
    partitionPath = spec.partitionToPath(partitionKey);
    partitionData = MixedDataFiles.data(spec, partitionPath);
    p1.set(partitionKey);
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testBucketPartition() {
    Schema schema = new Schema(Types.NestedField.required(1, "dt", Types.IntegerType.get()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).bucket("dt", 2).build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt", 1)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testTruncatePartition() {
    Schema schema = new Schema(Types.NestedField.required(1, "dt", Types.IntegerType.get()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).truncate("dt", 2).build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(internalRecordWrapper.wrap(record.copy("dt", 1)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testNullPartition() {
    Schema schema = new Schema(Types.NestedField.required(1, "name", Types.StringType.get()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).identity("name").build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    String partitionPath = "name=null";
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    Assert.assertNull(partitionData.get(0, Types.StringType.get().typeId().javaClass()));
  }

  @Test
  public void testSpecialCharactersPartition() {
    Schema schema =
        new Schema(
            Types.NestedField.required(1, "day", Types.StringType.get()),
            Types.NestedField.required(2, "name", Types.StringType.get()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).identity("day").identity("name").build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(
        internalRecordWrapper.wrap(
            record.copy("day", "2023-01-01").copy("name", "AAA BBB/CCC=_*-\\%")));

    String partitionToPath = spec.partitionToPath(partitionKey);
    Assert.assertEquals("day=2023-01-01/name=AAA+BBB%2FCCC%3D_*-%5C%25", partitionToPath);
    GenericRecord partitionData = MixedDataFiles.data(spec, partitionToPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);

    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testTimestampPartitionTZ() {
    Schema schema = new Schema(Types.NestedField.required(1, "dt", Types.TimestampType.withZone()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).identity("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(
        internalRecordWrapper.wrap(
            record.copy("dt", OffsetDateTime.parse("2024-08-12T11:00:00+08:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testTimestampPartitionNTZ() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).identity("dt").build();
    PartitionKey partitionKey = new PartitionKey(spec, SCHEMA);
    GenericRecord record = GenericRecord.create(SCHEMA);
    InternalRecordWrapper internalRecordWrapper = new InternalRecordWrapper(SCHEMA.asStruct());
    partitionKey.partition(
        internalRecordWrapper.wrap(record.copy("dt", LocalDateTime.parse("2024-08-12T11:00:00"))));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }

  @Test
  public void testYearPartitionDate() {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    for (int[] pair : YEAR_OFFSETS) {
      assertYearRoundTrip(spec, DATE_SCHEMA, LocalDate.of(pair[0], 3, 1), pair[1]);
    }
  }

  @Test
  public void testYearPartitionTimestampNTZ() {
    PartitionSpec spec = PartitionSpec.builderFor(SCHEMA).year("dt").build();
    for (int[] pair : YEAR_OFFSETS) {
      assertYearRoundTrip(spec, SCHEMA, LocalDateTime.of(pair[0], 3, 1, 12, 0), pair[1]);
    }
  }

  @Test
  public void testYearPartitionTimestampTZ() {
    PartitionSpec spec = PartitionSpec.builderFor(TIMESTAMP_TZ_SCHEMA).year("dt").build();
    for (int[] pair : YEAR_OFFSETS) {
      assertYearRoundTrip(
          spec,
          TIMESTAMP_TZ_SCHEMA,
          OffsetDateTime.of(pair[0], 3, 1, 12, 0, 0, 0, ZoneOffset.UTC),
          pair[1]);
    }
  }

  @Test
  public void testYearPartitionNullValue() {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    String partitionPath = "dt_year=null";
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    Assert.assertNull(partitionData.get(0, Integer.class));
  }

  @Test
  public void testIdentityIntegerPartition() {
    Schema schema = new Schema(Types.NestedField.required(1, "id", Types.IntegerType.get()));
    PartitionSpec spec = PartitionSpec.builderFor(schema).identity("id").build();
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(wrapper.wrap(record.copy("id", 2025)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    Assert.assertEquals(2025, partitionData.get(0, Integer.class).intValue());
  }

  @Test
  public void testMalformedYearStringThrows() {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    String partitionPath = "dt_year=abc";
    Assert.assertThrows(
        NumberFormatException.class, () -> MixedDataFiles.data(spec, partitionPath));
  }

  private void assertYearRoundTrip(
      PartitionSpec spec, Schema schema, Object value, int expectedOffset) {
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(wrapper.wrap(record.copy("dt", value)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);

    Assert.assertEquals(expectedOffset, partitionData.get(0, Integer.class).intValue());

    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    Assert.assertEquals(p1, p2);
  }
}
