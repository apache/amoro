package com.netease.arctic.hive.io;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;

import static com.netease.arctic.hive.HiveTableTestBase.HIVE_TABLE_SCHEMA;

public class HiveTestRecords {

  public static List<Record> baseRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 1, "name", "john",
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("100"))));
    builder.add(record.copy(ImmutableMap.of("id", 2, "name", "lily",
        "op_time", LocalDateTime.of(2022, 1, 2, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 2, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("101"))));
    builder.add(record.copy(ImmutableMap.of("id", 3, "name", "jake",
        "op_time", LocalDateTime.of(2022, 1, 3, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 3, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("102"))));
    builder.add(record.copy(ImmutableMap.of("id", 4, "name", "sam",
        "op_time", LocalDateTime.of(2022, 1, 4, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 4, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("103"))));

    return builder.build();
  }

  public static List<Record> changeInsertRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 5, "name", "mary",
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("104"))));
    builder.add(record.copy(ImmutableMap.of("id", 6, "name", "mack",
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("105"))));
    return builder.build();
  }

  public static List<Record> changeDeleteRecords() {
    GenericRecord record = GenericRecord.create(HIVE_TABLE_SCHEMA);
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", 5, "name", "mary",
        "op_time", LocalDateTime.of(2022, 1, 1, 12, 0, 0),
        "op_time_with_zone", OffsetDateTime.of(
            LocalDateTime.of(2022, 1, 1, 12, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal("104"))));
    return builder.build();
  }
}
