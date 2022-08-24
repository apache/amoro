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

package com.netease.arctic.spark.hive;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.spark.writer.ArcticSparkBaseTaskWriter;
import com.netease.arctic.spark.writer.ArcticSparkTaskWriterBuilder;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.WriteOperationKind;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkParquetReaders;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.types.Decimal;
import org.apache.spark.unsafe.types.UTF8String;
import org.junit.Assert;
import org.junit.Test;
import scala.collection.mutable.ArrayBuffer;

public class TestAdaptHiveWriter extends HiveTableTestBase {

  @Test
  public void testWriteTypeFromOperateKind(){
    {
      ArcticSparkTaskWriterBuilder builder = ArcticSparkTaskWriterBuilder
          .buildFor(testKeyedHiveTable)
          .withTransactionId(1);

      Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);

      Assert.assertTrue(builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof ArcticSparkBaseTaskWriter);
    }
    {
      ArcticSparkTaskWriterBuilder builder = ArcticSparkTaskWriterBuilder
          .buildFor(testHiveTable)
          .withTransactionId(1);

      Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);

      Assert.assertTrue(builder.buildWriter(WriteOperationKind.OVERWRITE) instanceof ArcticSparkBaseTaskWriter);
    }
  }

  @Test
  public void testKeyedTableBaseWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, BaseLocationKind.INSTANT, generateInternalRow(), "base");
  }

  @Test
  public void testKeyedTableHiveWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, HiveLocationKind.INSTANT, generateInternalRow(), "hive");
  }

  @Test
  public void testUnKeyedTableChangeWriteByLocationKind() throws IOException {
    try {
      testWrite(testHiveTable, ChangeLocationKind.INSTANT, generateInternalRow(), "change");
    }catch (Exception e){
      Assert.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testUnKeyedTableBaseWriteByLocationKind() throws IOException {
    testWrite(testHiveTable, BaseLocationKind.INSTANT, generateInternalRow(), "base");
  }

  @Test
  public void testUnKeyedTableHiveWriteByLocationKind() throws IOException {
    testWrite(testHiveTable, HiveLocationKind.INSTANT, generateInternalRow(), "hive");
  }

  public void testWrite(ArcticTable table, LocationKind locationKind, List<InternalRow> records, String pathFeature) throws IOException {
    ArcticSparkTaskWriterBuilder builder = ArcticSparkTaskWriterBuilder
        .buildFor(table)
        .withDataSourceSchema(SparkSchemaUtil.convert(table.schema()))
        .withTransactionId(1);

    TaskWriter<InternalRow> changeWrite = builder.buildWriter(locationKind);
    for (InternalRow record: records) {
      changeWrite.write(record);
    }
    WriteResult complete = changeWrite.complete();
    Arrays.stream(complete.dataFiles()).forEach(s -> Assert.assertTrue(s.path().toString().contains(pathFeature)));
    CloseableIterable<InternalRow> concat =
        CloseableIterable.concat(Arrays.stream(complete.dataFiles()).map(s -> readParquet(
            table.schema(),
            s.path().toString())).collect(Collectors.toList()));
    Set<InternalRow> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    Assert.assertEquals(result, records.stream().collect(Collectors.toSet()));
  }

  private CloseableIterable<InternalRow> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
            Files.localInput(new File(path)))
        .project(schema)
        .createReaderFunc(fileSchema -> SparkParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<InternalRow> iterable = builder.build();
    return iterable;
  }

  private List<InternalRow> generateInternalRow() {
    Instant EPOCH = Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC).toInstant();
    ArrayBuffer seq = new ArrayBuffer<Object>(5);
    seq = seq.$plus$eq(1);
    seq = seq.$plus$eq(UTF8String.fromString("jack"));
    seq = seq.$plus$eq(
        ChronoUnit.MICROS.between(EPOCH, LocalDateTime.of(2022, 1, 1, 10, 0, 0)
            .atOffset(ZoneOffset.ofHours(8)).toInstant()));
    seq = seq.$plus$eq(
        ChronoUnit.MICROS.between(EPOCH, LocalDateTime.of(2022, 1, 1, 10, 0, 0)
            .atOffset(ZoneOffset.ofHours(8)).toInstant()));
    seq = seq.$plus$eq(Decimal.apply("100"));
    InternalRow internalRow = GenericInternalRow.fromSeq(seq);
    return Lists.newArrayList(internalRow);
  }
}
