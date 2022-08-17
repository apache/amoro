package com.netease.arctic.spark.hive;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.spark.writer.ArcticSparkBaseTaskWriter;
import com.netease.arctic.spark.writer.ArcticSparkTaskWriterBuilder;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.types.Decimal;
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

      Assert.assertTrue(builder.buildWriter(OperateKinds.OVERWRITE) instanceof ArcticSparkBaseTaskWriter);
    }
    {
      ArcticSparkTaskWriterBuilder builder = ArcticSparkTaskWriterBuilder
          .buildFor(testHiveTable)
          .withTransactionId(1);

      Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof ArcticSparkBaseTaskWriter);

      Assert.assertTrue(builder.buildWriter(OperateKinds.OVERWRITE) instanceof ArcticSparkBaseTaskWriter);
    }
  }

  @Test
  public void testKeyedTableChangeWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, ChangeLocationKind.INSTANT, generateInternalRow(), "change");
  }

  @Test
  public void testKeyedTableBaseWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, BaseLocationKind.INSTANT, generateInternalRow(), "base");
  }

  @Test
  public void testKeyedTableHiveWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, HiveLocationKind.INSTANT, generateInternalRow(), "hive_data");
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
    testWrite(testHiveTable, HiveLocationKind.INSTANT, generateInternalRow(), "hive_data");
  }

  public void testWrite(ArcticTable table, LocationKind locationKind, List<InternalRow> records, String pathFeature) throws IOException {
    ArcticSparkTaskWriterBuilder builder = ArcticSparkTaskWriterBuilder
        .buildFor(table)
        .withTransactionId(1);

    TaskWriter<InternalRow> changeWrite = builder.buildWriter(locationKind);
    for (InternalRow record: records) {
      changeWrite.write(record);
    }
    WriteResult complete = changeWrite.complete();
    Arrays.stream(complete.dataFiles()).forEach(s -> Assert.assertTrue(s.path().toString().contains(pathFeature)));
    CloseableIterable<Record> concat =
        CloseableIterable.concat(Arrays.stream(complete.dataFiles()).map(s -> readParquet(
            table.schema(),
            s.path().toString())).collect(Collectors.toList()));
    Set<Record> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    Assert.assertEquals(result, records.stream().collect(Collectors.toSet()));
  }

  private CloseableIterable<Record> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
            Files.localInput(new File(path)))
        .project(schema)
        .createReaderFunc(fileSchema -> AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    return iterable;
  }

  private List<InternalRow> generateInternalRow() {
    ArrayBuffer seq = new ArrayBuffer<Object>(5);
    seq = seq.$plus$eq(1);
    seq = seq.$plus$eq("jark");
    seq = seq.$plus$eq(
        LocalDateTime.of(2022, 1, 1, 10, 0, 0)
            .atOffset(ZoneOffset.ofHours(8)).toInstant().toEpochMilli());
    seq = seq.$plus$eq(
        LocalDateTime.of(2022, 1, 1, 10, 0, 0)
            .atOffset(ZoneOffset.ofHours(8)).toInstant().toEpochMilli());
    seq = seq.$plus$eq(Decimal.apply("100"));
    InternalRow internalRow = GenericInternalRow.fromSeq(seq);
    return Lists.newArrayList(internalRow);
  }
}
