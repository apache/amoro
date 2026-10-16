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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.data.parquet.GenericParquetReaders;
import org.apache.iceberg.data.parquet.GenericParquetWriter;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileAppender;
import org.apache.iceberg.parquet.Parquet;
import org.apache.iceberg.types.Types;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/** Verifies Parquet reads using the Hadoop libraries supplied by the optimizer image. */
public class TestFlinkOptimizerImage {
  public static void main(String[] args) throws Throwable {
    ClassLoader parent = TestFlinkOptimizerImage.class.getClassLoader();
    // Flink itself must be able to load Hadoop, independently of the user job.
    Class<?> fileSystem = Class.forName("org.apache.hadoop.fs.FileSystem", true, parent);
    System.out.println("Hadoop FileSystem: " + fileSystem.getProtectionDomain().getCodeSource());

    Class<?> configClass = Class.forName("org.apache.flink.configuration.Configuration");
    ClassLoader userCode =
        (ClassLoader)
            Class.forName("org.apache.flink.client.ClientUtils")
                .getMethod(
                    "buildUserCodeClassLoader",
                    List.class,
                    List.class,
                    ClassLoader.class,
                    configClass)
                .invoke(
                    null,
                    Collections.singletonList(
                        Path.of("/opt/flink/usrlib/optimizer-job.jar").toUri().toURL()),
                    Collections.singletonList(Path.of("/test").toUri().toURL()),
                    parent,
                    configClass.getConstructor().newInstance());
    // Keep the loader available to Hadoop shutdown hooks until the JVM exits.
    Thread.currentThread().setContextClassLoader(userCode);
    try {
      Class.forName("TestFlinkOptimizerImage$ParquetRead", true, userCode)
          .getMethod("run")
          .invoke(null);
    } catch (InvocationTargetException failure) {
      throw failure.getCause();
    }
  }

  public static class ParquetRead {
    public static void run() throws Exception {
      System.out.println(
          "User Hadoop FileSystem: " + FileSystem.class.getProtectionDomain().getCodeSource());
      Schema schema = new Schema(Types.NestedField.required(1, "id", Types.IntegerType.get()));
      Configuration conf = new Configuration();
      HadoopFileIO io = new HadoopFileIO(conf);
      Path directory = Files.createTempDirectory("optimizer-image-test-");
      Path path = directory.resolve("data.parquet");
      String location = path.toUri().toString();
      try {
        try (FileAppender<Record> writer =
            Parquet.write(io.newOutputFile(location))
                .schema(schema)
                .createWriterFunc(GenericParquetWriter::create)
                .build()) {
          GenericRecord row = GenericRecord.create(schema);
          row.setField("id", 7);
          writer.add(row);
        }
        try (ParquetFileReader reader =
            ParquetFileReader.open(
                HadoopInputFile.fromPath(new org.apache.hadoop.fs.Path(location), conf))) {
          if (reader.getRecordCount() != 1) {
            throw new AssertionError("Unexpected Parquet footer record count");
          }
        }
        try (CloseableIterable<Record> rows =
            Parquet.read(io.newInputFile(location))
                .project(schema)
                .createReaderFunc(
                    fileSchema -> GenericParquetReaders.buildReader(schema, fileSchema))
                .build()) {
          int count = 0;
          for (Record row : rows) {
            if (!Integer.valueOf(7).equals(row.getField("id"))) {
              throw new AssertionError("Unexpected record: " + row);
            }
            count++;
          }
          if (count != 1) {
            throw new AssertionError("Unexpected record count: " + count);
          }
        }
        System.out.println("PASS: Parquet footer and Iceberg record reads");
      } finally {
        io.deleteFile(location);
        io.close();
        Files.deleteIfExists(directory);
      }
    }
  }
}
