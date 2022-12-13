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

package com.netease.arctic.io;

import com.netease.arctic.IcebergTableBase;
import com.netease.arctic.io.writer.IcebergFanoutPosDeleteWriter;
import com.netease.arctic.utils.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Files;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.MetricsModes;
import org.apache.iceberg.Schema;
import org.apache.iceberg.avro.Avro;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.avro.DataReader;
import org.apache.iceberg.data.orc.GenericOrcReader;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.DeleteSchemaUtil;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.orc.ORC;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.iceberg.TableProperties.DEFAULT_FILE_FORMAT_DEFAULT;
import static org.apache.iceberg.TableProperties.DELETE_DEFAULT_FILE_FORMAT;

public class IcebergFanoutPosDeleteWriterTest extends IcebergTableBase {

  @Test
  public void testWritePosDelete() throws IOException {
    GenericAppenderFactory appenderFactory =
        new GenericAppenderFactory(unPartitionTable.schema(), unPartitionTable.spec());
    appenderFactory.setAll(unPartitionTable.properties());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX + MetadataColumns.DELETE_FILE_PATH.name(),
        MetricsModes.Full.get().toString());
    appenderFactory.set(
        org.apache.iceberg.TableProperties.METRICS_MODE_COLUMN_CONF_PREFIX + MetadataColumns.DELETE_FILE_POS.name(),
        MetricsModes.Full.get().toString());
    String deleteFileFormatName =
        unPartitionTable.properties().getOrDefault(DELETE_DEFAULT_FILE_FORMAT, DEFAULT_FILE_FORMAT_DEFAULT);
    FileFormat deleteFileFormat = FileFormat.valueOf(deleteFileFormatName.toUpperCase());

    IcebergFanoutPosDeleteWriter<Record> icebergPosDeleteWriter = new IcebergFanoutPosDeleteWriter<>(
        appenderFactory, deleteFileFormat, null, unPartitionTable.io(), unPartitionTable.encryption(),
        "suffix");

    String dataDir = temp.newFolder("data").getPath();

    String dataFile1Path = new Path(FileUtil.getNewFilePath(dataDir, "data-1.parquet")).toString();
    icebergPosDeleteWriter.delete(dataFile1Path, 0);
    icebergPosDeleteWriter.delete(dataFile1Path, 1);
    icebergPosDeleteWriter.delete(dataFile1Path, 3);

    String dataFile2Path = new Path(FileUtil.getNewFilePath(dataDir, "data-2.parquet")).toString();
    icebergPosDeleteWriter.delete(dataFile2Path, 10);
    icebergPosDeleteWriter.delete(dataFile2Path, 9);
    icebergPosDeleteWriter.delete(dataFile2Path, 8);

    List<DeleteFile> deleteFiles = icebergPosDeleteWriter.complete();
    Assert.assertEquals(2, deleteFiles.size());
    Map<String, DeleteFile> deleteFileMap = deleteFiles.stream().collect(Collectors.toMap(f -> f.path().toString(),
        f -> f));
    DeleteFile deleteFile1 = deleteFileMap.get(
        new Path(FileUtil.getNewFilePath(dataDir, "data-1-delete-suffix.parquet")).toString());
    Assert.assertNotNull(deleteFile1);
    Assert.assertEquals(3, deleteFile1.recordCount());
    // Check whether the path-pos pairs are sorted as expected.
    Schema pathPosSchema = DeleteSchemaUtil.pathPosSchema();
    Record record = GenericRecord.create(pathPosSchema);
    List<Record> expectedDeletes =
        Lists.newArrayList(
            record.copy("file_path", dataFile1Path, "pos", 0L),
            record.copy("file_path", dataFile1Path, "pos", 1L),
            record.copy("file_path", dataFile1Path, "pos", 3L));
    Assert.assertEquals(expectedDeletes, readRecordsAsList(FileFormat.PARQUET, pathPosSchema, deleteFile1.path()));

    DeleteFile deleteFile2 = deleteFileMap.get(
        new Path(FileUtil.getNewFilePath(dataDir, "data-2-delete-suffix.parquet")).toString());
    Assert.assertNotNull(deleteFile2);
    Assert.assertEquals(
        new Path(FileUtil.getNewFilePath(dataDir, "data-2-delete-suffix.parquet")).toString(),
        deleteFile2.path().toString());
    Assert.assertEquals(3, deleteFile2.recordCount());
    // Check whether the path-pos pairs are sorted as expected.
    expectedDeletes =
        Lists.newArrayList(
            record.copy("file_path", dataFile2Path, "pos", 8L),
            record.copy("file_path", dataFile2Path, "pos", 9L),
            record.copy("file_path", dataFile2Path, "pos", 10L));
    Assert.assertEquals(expectedDeletes, readRecordsAsList(FileFormat.PARQUET, pathPosSchema, deleteFile2.path()));
  }


  private List<Record> readRecordsAsList(FileFormat format, Schema schema, CharSequence path) throws IOException {
    CloseableIterable<Record> iterable;

    InputFile inputFile = Files.localInput(path.toString());
    switch (format) {
      case PARQUET:
        iterable =
            Parquet.read(inputFile)
                .project(schema)
                .createReaderFunc(
                    fileSchema -> GenericParquetReaders.buildReader(schema, fileSchema))
                .build();
        break;

      case AVRO:
        iterable =
            Avro.read(inputFile).project(schema).createReaderFunc(DataReader::create).build();
        break;

      case ORC:
        iterable =
            ORC.read(inputFile)
                .project(schema)
                .createReaderFunc(fileSchema -> GenericOrcReader.buildReader(schema, fileSchema))
                .build();
        break;

      default:
        throw new UnsupportedOperationException("Unsupported file format: " + format);
    }

    try (CloseableIterable<Record> closeableIterable = iterable) {
      return Lists.newArrayList(closeableIterable);
    }
  }
}
