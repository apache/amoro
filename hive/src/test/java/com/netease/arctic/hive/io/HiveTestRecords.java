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

package com.netease.arctic.hive.io;

import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.netease.arctic.hive.HiveTableTestBase.HIVE_TABLE_SCHEMA;

public class HiveTestRecords {

  public static List<Record> baseRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 3,
        "op_time", LocalDateTime.of(2022, 1, 3, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 3, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("102"), "name", "jake")));
    builder.add(record.copy(ImmutableMap.of("id", 4,
        "op_time", LocalDateTime.of(2022, 1, 4, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 4, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("103"), "name", "sam")));

    return builder.build();
  }

  public static List<Record> hiveRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 1,
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("100"), "name", "john")));
    builder.add(record.copy(ImmutableMap.of("id", 2,
        "op_time", LocalDateTime.of(2022, 1, 2, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 2, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("101"), "name", "lily")));
    return builder.build();
  }

  public static List<Record> changeInsertRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 5,
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("104"), "name", "mary")));
    builder.add(record.copy(ImmutableMap.of("id", 6,
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("105"), "name", "mack")));
    return builder.build();
  }

  public static List<Record> changeDeleteRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 5,
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("104"), "name", "mary")));
    builder.add(record.copy(ImmutableMap.of("id", 1,
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("100"), "name", "john")));
    builder.add(record.copy(ImmutableMap.of("id", 3,
        "op_time", LocalDateTime.of(2022, 1, 3, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 3, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("102"), "name", "jake")));
    return builder.build();
  }
}
