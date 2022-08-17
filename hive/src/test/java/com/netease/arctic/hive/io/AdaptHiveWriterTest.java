package com.netease.arctic.hive.io;

import com.netease.arctic.hive.HiveTableTestBase;
import com.netease.arctic.hive.table.HiveLocationKind;
import com.netease.arctic.hive.write.AdaptHiveGenericTaskWriterBuilder;
import com.netease.arctic.io.writer.GenericBaseTaskWriter;
import com.netease.arctic.io.writer.GenericChangeTaskWriter;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.BaseLocationKind;
import com.netease.arctic.table.ChangeLocationKind;
import com.netease.arctic.table.LocationKind;
import com.netease.arctic.table.OperateKinds;
import java.io.File;
import java.io.IOException;
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
import org.junit.Assert;
import org.junit.Test;

public class AdaptHiveWriterTest extends HiveTableTestBase {

  @Test
  public void testWriteTypeFromOperateKind(){
    {
      AdaptHiveGenericTaskWriterBuilder builder = AdaptHiveGenericTaskWriterBuilder
          .builderFor(testKeyedHiveTable)
          .withTransactionId(1);

      Assert.assertTrue(builder.buildWriter(ChangeLocationKind.INSTANT) instanceof GenericChangeTaskWriter);
      Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof GenericBaseTaskWriter);

      Assert.assertTrue(builder.buildWriter(OperateKinds.APPEND) instanceof GenericChangeTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.OVERWRITE) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.MINOR_OPTIMIZE) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.MAJOR_OPTIMIZE) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.FULL_OPTIMIZE) instanceof GenericBaseTaskWriter);
    }
    {
      AdaptHiveGenericTaskWriterBuilder builder = AdaptHiveGenericTaskWriterBuilder
          .builderFor(testHiveTable)
          .withTransactionId(1);

      Assert.assertTrue(builder.buildWriter(BaseLocationKind.INSTANT) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(HiveLocationKind.INSTANT) instanceof GenericBaseTaskWriter);

      Assert.assertTrue(builder.buildWriter(OperateKinds.APPEND) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.OVERWRITE) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.MAJOR_OPTIMIZE) instanceof GenericBaseTaskWriter);
      Assert.assertTrue(builder.buildWriter(OperateKinds.FULL_OPTIMIZE) instanceof GenericBaseTaskWriter);
    }
  }

  @Test
  public void testKeyedTableChangeWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, ChangeLocationKind.INSTANT, HiveTestRecords.changeInsertRecords(), "change");
  }

  @Test
  public void testKeyedTableBaseWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords(), "base");
  }

  @Test
  public void testKeyedTableHiveWriteByLocationKind() throws IOException {
    testWrite(testKeyedHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.baseRecords(), "hive_data");
  }

  @Test
  public void testUnKeyedTableChangeWriteByLocationKind() throws IOException {
    try {
      testWrite(testHiveTable, ChangeLocationKind.INSTANT, HiveTestRecords.changeInsertRecords(), "change");
    }catch (Exception e){
      Assert.assertTrue(e instanceof IllegalArgumentException);
    }
  }

  @Test
  public void testUnKeyedTableBaseWriteByLocationKind() throws IOException {
    testWrite(testHiveTable, BaseLocationKind.INSTANT, HiveTestRecords.baseRecords(), "base");
  }

  @Test
  public void testUnKeyedTableHiveWriteByLocationKind() throws IOException {
    testWrite(testHiveTable, HiveLocationKind.INSTANT, HiveTestRecords.baseRecords(), "hive_data");
  }

  public void testWrite(ArcticTable table, LocationKind locationKind, List<Record> records, String pathFeature) throws IOException {
    AdaptHiveGenericTaskWriterBuilder builder = AdaptHiveGenericTaskWriterBuilder
        .builderFor(table)
        .withTransactionId(1);

    TaskWriter<Record> changeWrite = builder.buildWriter(locationKind);
    for (Record record: records) {
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

  private CloseableIterable<Record> readParquet(Schema schema, String path){
    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
            Files.localInput(new File(path)))
        .project(schema)
        .createReaderFunc(fileSchema -> AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    return iterable;
  }
}
