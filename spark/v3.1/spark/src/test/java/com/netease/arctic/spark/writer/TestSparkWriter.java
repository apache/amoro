package com.netease.arctic.spark.writer;


import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.catalog.TableTestBase;
import com.netease.arctic.spark.reader.SparkParquetReaders;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkOrcReader;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.LogicalWriteInfoImpl;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import org.apache.spark.unsafe.types.UTF8String;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static com.netease.arctic.table.TableProperties.DEFAULT_FILE_FORMAT_ORC;

@RunWith(Parameterized.class)
public class TestSparkWriter extends TableTestBase {
  public TestSparkWriter(CatalogTestHelper catalogTestHelper,
      TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, false)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true, DEFAULT_FILE_FORMAT_ORC)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false, DEFAULT_FILE_FORMAT_ORC)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, true, DEFAULT_FILE_FORMAT_ORC)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, false, DEFAULT_FILE_FORMAT_ORC)}
    };
  }


  @Test
  public void testAppend() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY,WriteMode.APPEND.mode);
    testWriteData(map);
  }

  @Test
  public void testOverWriteDynamic() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY,WriteMode.OVERWRITE_DYNAMIC.mode);
    testWriteData(map);
  }

  @Test
  public void testOverWriteByFilter() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY,WriteMode.OVERWRITE_BY_FILTER.mode);
    testWriteData(map);
  }

  @Test
  public void testDeltaWrite() throws IOException {
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY,WriteMode.DELTAWRITE.mode);
    testWriteData(map);
  }


  private void testWriteData(Map<String, String> map) throws IOException {
    ArcticTable table = getArcticTable();
    StructType structType = SparkSchemaUtil.convert(table.schema());
    LogicalWriteInfoImpl info = new LogicalWriteInfoImpl("queryId", structType, new CaseInsensitiveStringMap(map));
    ArcticSparkWriteBuilder builder = new ArcticSparkWriteBuilder(table, info, getCatalog());
    BatchWrite batchWrite = builder.buildForBatch();
    DataWriter<InternalRow> writer = batchWrite.createBatchWriterFactory(null).createWriter(0,0);
    //create record
    InternalRow record =
        new GenericInternalRow(new Object[] {1, UTF8String.fromString("lily"), 0L, 1641009600000L});
    List<InternalRow> records = Arrays.asList(record);
    writer.write(record);
    WriteTaskCommit commit = (WriteTaskCommit)writer.commit();
    DataFile[] files = commit.files();
    CloseableIterable<InternalRow> concat =
        CloseableIterable.concat(Arrays.stream(files).map(
            s -> {
              switch (s.format()) {
                case PARQUET:
                  return readParquet(table.schema(), s.path().toString());
                case ORC:
                  return readOrc(table.schema(), s.path().toString());
                default:
                  throw new UnsupportedOperationException(
                      "Cannot read unknown format: " + s.format());
              }
            }
        ).collect(Collectors.toList()));
    Set<InternalRow> result = new HashSet<>();
    Iterators.addAll(result, concat.iterator());
    Assert.assertEquals(result, records.stream().collect(Collectors.toSet()));
  }


  private CloseableIterable<InternalRow> readParquet(Schema schema, String path) {
    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
            Files.localInput(path))
        .project(schema)
        .createReaderFunc(fileSchema -> SparkParquetReaders.buildReader(schema, fileSchema,new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<InternalRow> iterable = builder.build();
    return iterable;
  }

  private CloseableIterable<InternalRow> readOrc(Schema schema, String path) {
    ORC.ReadBuilder builder =
        ORC.read(Files.localInput(path))
            .project(schema)
            .createReaderFunc(fileSchema -> new SparkOrcReader(schema, fileSchema,new HashMap<>()))
            .caseSensitive(false);

    CloseableIterable<InternalRow> iterable = builder.build();
    return iterable;
  }


}

