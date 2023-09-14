package com.netease.arctic.spark.writer;


import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.reader.SparkParquetReaders;
import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.Files;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.relocated.com.google.common.collect.Iterators;
import org.apache.iceberg.spark.SparkSchemaUtil;
import org.apache.iceberg.spark.data.SparkOrcReader;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow;
import org.apache.spark.sql.connector.write.BatchWrite;
import org.apache.spark.sql.connector.write.DataWriter;
import org.apache.spark.sql.connector.write.LogicalWriteInfoImpl;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;
import org.apache.spark.unsafe.types.UTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static com.netease.arctic.table.TableProperties.BASE_FILE_FORMAT;
import static com.netease.arctic.table.TableProperties.CHANGE_FILE_FORMAT;
import static com.netease.arctic.table.TableProperties.DEFAULT_FILE_FORMAT;
import static com.netease.arctic.table.TableProperties.DEFAULT_FILE_FORMAT_ORC;
import static com.netease.arctic.table.TableProperties.DEFAULT_FILE_FORMAT_PARQUET;

public class TestSparkWriter extends SparkTableTestBase {

  static final Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.IntegerType.get()),
      Types.NestedField.required(2, "data", Types.StringType.get()),
      Types.NestedField.required(3, "pt", Types.StringType.get())
  );

  static final PrimaryKeySpec idPrimaryKeySpec = PrimaryKeySpec.builderFor(schema)
      .addColumn("id").build();

  static final PartitionSpec ptSpec = PartitionSpec.builderFor(schema)
      .identity("pt").build();

  public static Stream<Arguments> testWrite() {
    return Stream.of(
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, idPrimaryKeySpec, ptSpec, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, noPrimaryKey, ptSpec, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, idPrimaryKeySpec, unpartitioned, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, noPrimaryKey, unpartitioned, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, idPrimaryKeySpec, ptSpec, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, noPrimaryKey, ptSpec, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, idPrimaryKeySpec, unpartitioned, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.APPEND, schema, noPrimaryKey, unpartitioned, DEFAULT_FILE_FORMAT_ORC),

        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, idPrimaryKeySpec, ptSpec, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, noPrimaryKey, ptSpec, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, idPrimaryKeySpec, unpartitioned, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, noPrimaryKey, unpartitioned, DEFAULT_FILE_FORMAT_PARQUET),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, idPrimaryKeySpec, ptSpec, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, noPrimaryKey, ptSpec, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, idPrimaryKeySpec, unpartitioned, DEFAULT_FILE_FORMAT_ORC),
        Arguments.of(MIXED_HIVE, WriteMode.OVERWRITE_DYNAMIC, schema, noPrimaryKey, unpartitioned, DEFAULT_FILE_FORMAT_ORC)
    );
  }

  @DisplayName("Test write mix_hive Table")
  @ParameterizedTest
  @MethodSource
  public void testWrite(TableFormat format, WriteMode writeMode, Schema schema,
      PrimaryKeySpec keySpec, PartitionSpec ptSpec, String fileFormat) throws IOException {
    ArcticTable table = createTarget(schema, tableBuilder ->
        tableBuilder.withPrimaryKeySpec(keySpec)
            .withProperty(CHANGE_FILE_FORMAT, fileFormat)
            .withProperty(BASE_FILE_FORMAT, fileFormat)
            .withProperty(DEFAULT_FILE_FORMAT, fileFormat)
            .withPartitionSpec(ptSpec));
    Map<String, String> map = new HashMap<>();
    map.put(WriteMode.WRITE_MODE_KEY,writeMode.mode);
    testWriteData(table, map);
  }

  private void testWriteData(ArcticTable table, Map<String, String> map) throws IOException {
    StructType structType = SparkSchemaUtil.convert(table.schema());
    LogicalWriteInfoImpl info = new LogicalWriteInfoImpl("queryId", structType, new CaseInsensitiveStringMap(map));
    ArcticSparkWriteBuilder builder = new ArcticSparkWriteBuilder(table, info, catalog());
    BatchWrite batchWrite = builder.buildForBatch();
    DataWriter<InternalRow> writer = batchWrite.createBatchWriterFactory(null).createWriter(0,0);
    //create record
    InternalRow record = geneRowData();
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
    Assertions.assertEquals(result, records.stream().collect(Collectors.toSet()));
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

  private InternalRow geneRowData() {
    return new GenericInternalRow(new Object[] {1, UTF8String.fromString("aaa"), UTF8String.fromString("AAA")});
  }

}
