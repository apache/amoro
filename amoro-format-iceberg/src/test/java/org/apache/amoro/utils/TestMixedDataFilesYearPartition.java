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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.InternalRecordWrapper;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TestMixedDataFilesYearPartition {

  private static final Schema DATE_SCHEMA =
      new Schema(Types.NestedField.optional(1, "dt", Types.DateType.get()));
  private static final Schema TIMESTAMP_NTZ_SCHEMA =
      new Schema(Types.NestedField.required(1, "dt", Types.TimestampType.withoutZone()));
  private static final Schema TIMESTAMP_TZ_SCHEMA =
      new Schema(Types.NestedField.required(1, "dt", Types.TimestampType.withZone()));

  @ParameterizedTest
  @CsvSource({"1969, -1", "1970, 0", "2024, 54", "2025, 55"})
  public void testYearPartitionDate(int year, int expectedOffset) {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    LocalDate date = LocalDate.of(year, 3, 1);
    assertYearRoundTrip(spec, DATE_SCHEMA, date, expectedOffset);
  }

  @ParameterizedTest
  @CsvSource({"1969, -1", "1970, 0", "2024, 54", "2025, 55"})
  public void testYearPartitionTimestampNTZ(int year, int expectedOffset) {
    PartitionSpec spec = PartitionSpec.builderFor(TIMESTAMP_NTZ_SCHEMA).year("dt").build();
    LocalDateTime dateTime = LocalDateTime.of(year, 3, 1, 12, 0);
    assertYearRoundTrip(spec, TIMESTAMP_NTZ_SCHEMA, dateTime, expectedOffset);
  }

  @ParameterizedTest
  @CsvSource({"1969, -1", "1970, 0", "2024, 54", "2025, 55"})
  public void testYearPartitionTimestampTZ(int year, int expectedOffset) {
    PartitionSpec spec = PartitionSpec.builderFor(TIMESTAMP_TZ_SCHEMA).year("dt").build();
    OffsetDateTime dateTime = OffsetDateTime.of(year, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    assertYearRoundTrip(spec, TIMESTAMP_TZ_SCHEMA, dateTime, expectedOffset);
  }

  @Test
  public void testYearPartitionNullValue() {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    String partitionPath = "dt_year=null";
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);
    assertNull(partitionData.get(0, Integer.class));
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
    assertEquals(2025, partitionData.get(0, Integer.class).intValue());
  }

  @Test
  public void testMalformedYearStringThrows() {
    PartitionSpec spec = PartitionSpec.builderFor(DATE_SCHEMA).year("dt").build();
    String partitionPath = "dt_year=abc";
    assertThrows(NumberFormatException.class, () -> MixedDataFiles.data(spec, partitionPath));
  }

  private void assertYearRoundTrip(
      PartitionSpec spec, Schema schema, Object value, int expectedOffset) {
    PartitionKey partitionKey = new PartitionKey(spec, schema);
    GenericRecord record = GenericRecord.create(schema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(schema.asStruct());
    partitionKey.partition(wrapper.wrap(record.copy("dt", value)));
    String partitionPath = spec.partitionToPath(partitionKey);
    StructLike partitionData = MixedDataFiles.data(spec, partitionPath);

    assertEquals(expectedOffset, partitionData.get(0, Integer.class).intValue());

    StructLikeWrapper p1 = StructLikeWrapper.forType(spec.partitionType());
    p1.set(partitionKey);
    StructLikeWrapper p2 = StructLikeWrapper.forType(spec.partitionType());
    p2.set(partitionData);
    assertEquals(p1, p2);
  }
}
