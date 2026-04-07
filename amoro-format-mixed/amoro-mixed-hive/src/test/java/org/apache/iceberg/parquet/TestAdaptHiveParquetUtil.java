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

package org.apache.iceberg.parquet;

import static org.apache.iceberg.types.Types.NestedField.required;

import org.apache.iceberg.FieldMetrics;
import org.apache.iceberg.Metrics;
import org.apache.iceberg.MetricsConfig;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SchemaParser;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;
import org.apache.parquet.column.Encoding;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.hadoop.metadata.BlockMetaData;
import org.apache.parquet.hadoop.metadata.ColumnChunkMetaData;
import org.apache.parquet.hadoop.metadata.ColumnPath;
import org.apache.parquet.hadoop.metadata.FileMetaData;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Tests for {@link AdaptHiveParquetUtil#footerMetrics}, verifying that INT96 timestamp statistics
 * are correctly re-ordered.
 *
 * <p>Parquet stores INT96 timestamps as 12 bytes (8-byte LE nanoseconds-of-day + 4-byte LE Julian
 * day). Byte-wise comparison compares the nanos first, which does not match chronological order
 * when timestamps cross day boundaries. Without the fix, stats.genericGetMin() could be
 * chronologically *later* than stats.genericGetMax(), causing inverted lower/upper bounds in
 * Iceberg metrics.
 */
public class TestAdaptHiveParquetUtil {

  private static final int JULIAN_EPOCH_OFFSET = 2_440_588;

  /**
   * Encodes a unix-epoch-millisecond timestamp into a 12-byte INT96 Parquet binary (LE
   * nanoseconds-of-day + LE Julian day).
   */
  private static byte[] toInt96Bytes(long epochMillis) {
    long epochDay = Math.floorDiv(epochMillis, TimeUnit.DAYS.toMillis(1));
    long milliOfDay = epochMillis - epochDay * TimeUnit.DAYS.toMillis(1);
    long nanoOfDay = milliOfDay * 1_000_000L;
    int julianDay = (int) (epochDay + JULIAN_EPOCH_OFFSET);

    ByteBuffer buf = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);
    buf.putLong(nanoOfDay);
    buf.putInt(julianDay);
    return buf.array();
  }

  /**
   * Verifies that footerMetrics swaps INT96 min/max when byte-wise ordering disagrees with
   * chronological ordering.
   *
   * <p>We construct two timestamps crossing a day boundary:
   *
   * <ul>
   *   <li>earlier: 2020-01-01 23:59:59.999 UTC (large nanos-of-day, end of day)
   *   <li>later: 2020-01-02 00:00:00.001 UTC (small nanos-of-day, start of next day)
   * </ul>
   *
   * In INT96 little-endian format, the nanos-of-day occupy the first 8 bytes. The earlier timestamp
   * has a much larger nanos value (86399999 ms → ~8.6e13 nanos), so byte-wise it appears "greater"
   * than the later timestamp (1 ms → 1e6 nanos). Parquet therefore sets stats.genericGetMin() =
   * later, stats.genericGetMax() = earlier. The fix detects this inversion for INT96 columns and
   * swaps them.
   */
  @Test
  public void testInt96MinMaxSwappedWhenByteOrderReversed() {
    // 2020-01-01 23:59:59.999 UTC — large nanos-of-day (end of day)
    long earlierMillis = 1577923199999L;
    // 2020-01-02 00:00:00.001 UTC — small nanos-of-day (start of next day)
    long laterMillis = 1577923200001L;

    byte[] earlierBytes = toInt96Bytes(earlierMillis);
    byte[] laterBytes = toInt96Bytes(laterMillis);

    // Verify our premise: byte-wise, laterBytes < earlierBytes
    // (because nanos-of-day for 01:00 < nanos-of-day for 23:00, and nanos come first in LE)
    Assert.assertTrue(
        "Precondition: byte-wise earlier > later for these INT96 values",
        compareBytewise(earlierBytes, laterBytes) > 0);

    // Build Parquet schema with INT96 column carrying Iceberg field id
    MessageType parquetSchema =
        new MessageType(
            "test",
            Collections.singletonList(
                org.apache.parquet.schema.Types.required(PrimitiveType.PrimitiveTypeName.INT96)
                    .id(1)
                    .named("ts")));

    // Build statistics: Parquet's byte-wise min = laterBytes, max = earlierBytes
    Statistics<?> stats =
        Statistics.getBuilderForReading(parquetSchema.getType("ts").asPrimitiveType())
            .withMin(laterBytes)
            .withMax(earlierBytes)
            .withNumNulls(0)
            .build();

    // Column chunk metadata
    Set<Encoding> encodings = new HashSet<>();
    encodings.add(Encoding.PLAIN);
    ColumnChunkMetaData columnMeta =
        ColumnChunkMetaData.get(
            ColumnPath.get("ts"),
            parquetSchema.getType("ts").asPrimitiveType(),
            org.apache.parquet.hadoop.metadata.CompressionCodecName.UNCOMPRESSED,
            null,
            encodings,
            stats,
            0L,
            0L,
            1L,
            100L,
            100L);

    BlockMetaData block = new BlockMetaData();
    block.addColumn(columnMeta);
    block.setRowCount(1);

    Schema icebergSchema = new Schema(required(1, "ts", Types.TimestampType.withoutZone()));
    Map<String, String> keyValueMetadata = new HashMap<>();
    keyValueMetadata.put("iceberg.schema", SchemaParser.toJson(icebergSchema));
    FileMetaData fileMetaData = new FileMetaData(parquetSchema, keyValueMetadata, "test");
    ParquetMetadata metadata = new ParquetMetadata(fileMetaData, Collections.singletonList(block));

    // Call footerMetrics
    Metrics metrics =
        AdaptHiveParquetUtil.footerMetrics(
            metadata, Stream.<FieldMetrics<?>>empty(), MetricsConfig.getDefault(), icebergSchema);

    // Extract lower and upper bounds for field 1 (ts)
    ByteBuffer lowerBuf = metrics.lowerBounds().get(1);
    ByteBuffer upperBuf = metrics.upperBounds().get(1);

    Assert.assertNotNull("Lower bound should exist for ts", lowerBuf);
    Assert.assertNotNull("Upper bound should exist for ts", upperBuf);

    long lowerMicros = Conversions.fromByteBuffer(Types.TimestampType.withoutZone(), lowerBuf);
    long upperMicros = Conversions.fromByteBuffer(Types.TimestampType.withoutZone(), upperBuf);

    Assert.assertTrue(
        "Lower bound (earlier timestamp) must be <= upper bound (later timestamp), "
            + "but got lower="
            + lowerMicros
            + ", upper="
            + upperMicros,
        lowerMicros <= upperMicros);
  }

  /**
   * Verifies that non-INT96 columns are not affected by the swap logic — their min/max follow
   * Parquet's original order.
   */
  @Test
  public void testNonInt96ColumnMinMaxPreserved() {
    PrimitiveType int32Type =
        org.apache.parquet.schema.Types.required(PrimitiveType.PrimitiveTypeName.INT32)
            .id(1)
            .named("id");
    MessageType parquetSchema = new MessageType("test", Collections.singletonList(int32Type));

    // min = 10, max = 42
    byte[] minBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(10).array();
    byte[] maxBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(42).array();

    Statistics<?> stats =
        Statistics.getBuilderForReading(int32Type)
            .withMin(minBytes)
            .withMax(maxBytes)
            .withNumNulls(0)
            .build();

    Set<Encoding> encodings = new HashSet<>();
    encodings.add(Encoding.PLAIN);
    ColumnChunkMetaData columnMeta =
        ColumnChunkMetaData.get(
            ColumnPath.get("id"),
            int32Type,
            org.apache.parquet.hadoop.metadata.CompressionCodecName.UNCOMPRESSED,
            null,
            encodings,
            stats,
            0L,
            0L,
            2L,
            100L,
            100L);

    BlockMetaData block = new BlockMetaData();
    block.addColumn(columnMeta);
    block.setRowCount(2);

    Schema icebergSchema = new Schema(required(1, "id", Types.IntegerType.get()));
    Map<String, String> keyValueMetadata = new HashMap<>();
    keyValueMetadata.put("iceberg.schema", SchemaParser.toJson(icebergSchema));
    FileMetaData fileMetaData = new FileMetaData(parquetSchema, keyValueMetadata, "test");
    ParquetMetadata metadata = new ParquetMetadata(fileMetaData, Collections.singletonList(block));

    Metrics metrics =
        AdaptHiveParquetUtil.footerMetrics(
            metadata, Stream.<FieldMetrics<?>>empty(), MetricsConfig.getDefault(), icebergSchema);

    ByteBuffer lowerBuf = metrics.lowerBounds().get(1);
    ByteBuffer upperBuf = metrics.upperBounds().get(1);
    Assert.assertNotNull(lowerBuf);
    Assert.assertNotNull(upperBuf);

    int lower = Conversions.fromByteBuffer(Types.IntegerType.get(), lowerBuf);
    int upper = Conversions.fromByteBuffer(Types.IntegerType.get(), upperBuf);
    Assert.assertEquals(10, lower);
    Assert.assertEquals(42, upper);
  }

  private static int compareBytewise(byte[] a, byte[] b) {
    for (int i = 0; i < Math.min(a.length, b.length); i++) {
      int cmp = Byte.compareUnsigned(a[i], b[i]);
      if (cmp != 0) {
        return cmp;
      }
    }
    return Integer.compare(a.length, b.length);
  }
}
