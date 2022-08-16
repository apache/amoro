package com.netease.arctic.hive.io;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Files;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.AdaptHiveGenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.AdaptHiveGenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.parquet.AdaptHiveParquet;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteHiveAdaptHiveParquetTest {

  private static final Logger LOG = LoggerFactory.getLogger(ReadWriteHiveAdaptHiveParquetTest.class);

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  private File temDir;

  public void readHiveOriginalParquetFile() {
    Schema schema = new Schema(
        Types.NestedField.of(1, false, "t1", Types.TimestampType.withoutZone()),
        Types.NestedField.of(2, false, "t2", Types.TimestampType.withZone()),
        Types.NestedField.of(3, false, "d", Types.DecimalType.of(9, 7)));


    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
        Files.localInput(this.getClass().getClassLoader().getResource("hive.parquet").getFile()))
        .project(schema)
        .createReaderFunc(fileSchema -> AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    CloseableIterator<Record> iterator = iterable.iterator();
    while (iterator.hasNext()){
      Record record = iterator.next();
      Record r1 = record.copy(
          "t1", LocalDateTime.of(2020, 1, 1, 10, 0, 0),
          "t2", OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 2, 0, 0), ZoneOffset.UTC),
          "d", new BigDecimal("100"));
      Assert.assertEquals(record, r1);
    }
  }

  @Test
  public void write() throws IOException {
    this.temDir = folder.newFolder();
    LOG.info("Adapt write parquet path in " + temDir);
    Schema schema = new Schema(Types.NestedField.of(1, false, "t1", Types.TimestampType.withoutZone()),
        Types.NestedField.of(2, false, "t2", Types.TimestampType.withZone()),
        Types.NestedField.of(3, false, "d", Types.DecimalType.of(9, 0)));
    AdaptHiveGenericAppenderFactory adaptHiveGenericAppenderFactory = new AdaptHiveGenericAppenderFactory(schema);
    FileAppender<Record> recordFileAppender = adaptHiveGenericAppenderFactory.newAppender(Files.localOutput(new File(
        temDir, "out.parquet")), FileFormat.PARQUET);
    GenericRecord record = GenericRecord.create(schema);
    recordFileAppender.add(record.copy("t1", LocalDateTime.of(2022, 1, 1, 10, 0, 0),
        "t2", OffsetDateTime.of(LocalDateTime.of(2022, 1, 1, 10, 0, 0), ZoneOffset.UTC),
        "d", new BigDecimal(11)));
    recordFileAppender.close();
  }

  @Test
  public void readArcticWrite() {
    Schema schema = new Schema(
        Types.NestedField.of(1, false, "t1", Types.TimestampType.withoutZone()),
        Types.NestedField.of(2, false, "t2", Types.TimestampType.withZone()),
        Types.NestedField.of(3, false, "d", Types.DecimalType.of(9, 7)));

    AdaptHiveParquet.ReadBuilder builder = AdaptHiveParquet.read(
        Files.localInput(new File(temDir, "out.parquet")))
        .project(schema)
        .createReaderFunc(fileSchema -> AdaptHiveGenericParquetReaders.buildReader(schema, fileSchema, new HashMap<>()))
        .caseSensitive(false);

    CloseableIterable<Record> iterable = builder.build();
    CloseableIterator<Record> iterator = iterable.iterator();
    while (iterator.hasNext()){
      Record record = iterator.next();
      //Hive reading timestamp from int96 will convert to OffsetDateTime
      Assert.assertTrue((record.get(0) instanceof LocalDateTime) && (record.get(1) instanceof OffsetDateTime));
    }
  }
}