/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic;

import com.google.common.collect.Maps;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.scan.CombinedIcebergScanTask;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.hadoop.HadoopFileIO;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.io.UnpartitionedWriter;
import org.apache.iceberg.types.Types;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import static org.apache.iceberg.TableProperties.PARQUET_ROW_GROUP_SIZE_BYTES;

public class IcebergTableBase {

  protected Table table;

  protected String name = "test";

  protected Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.LongType.get())
  );

  protected CombinedIcebergScanTask allFileTask;

  protected CombinedIcebergScanTask onlyDataTask;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Before
  public void setupTables() throws IOException {
    //create table
    Configuration conf = new Configuration();
    File tableDir = temp.newFolder();
    HadoopCatalog catalog = new HadoopCatalog(conf, tableDir.getAbsolutePath());
    TableIdentifier tableIdentifier = TableIdentifier.of(name);
    if (catalog.tableExists(tableIdentifier)){
      table = catalog.loadTable(tableIdentifier);
    }else {
      Map<String, String> map = Maps.newHashMap();
      map.put(TableProperties.FORMAT_VERSION, "2");
      table = catalog.createTable(tableIdentifier, schema, PartitionSpec.unpartitioned(), map);
    }

    Record record = GenericRecord.create(schema);

    IcebergContentFile avroData = new IcebergContentFile(insert(Arrays.asList(
        record.copy("id", 1L)
    ), FileFormat.AVRO), 1L);

    IcebergContentFile parquetData = new IcebergContentFile(insert(Arrays.asList(
        record.copy("id", 2L)
    ), FileFormat.PARQUET), 2L);

    IcebergContentFile orcData = new IcebergContentFile(insert(Arrays.asList(
        record.copy("id", 3L)
    ), FileFormat.ORC),3L);

    IcebergContentFile eqDeleteFile = new IcebergContentFile(eqDelete(Arrays.asList(
        record.copy("id", 1L)
    ), FileFormat.PARQUET),4L);

    IcebergContentFile posDeleteFile = new IcebergContentFile(posDelete(Arrays.asList(
        PositionDelete.<Record>create().set(parquetData.asDataFile().path(), 0, record.copy("id", 2L))
    ), FileFormat.AVRO),5L);

    allFileTask = new CombinedIcebergScanTask(
        new IcebergContentFile[]{avroData, parquetData, orcData},
        new IcebergContentFile[]{eqDeleteFile, posDeleteFile},
        PartitionSpec.unpartitioned(),
        null
    );

    onlyDataTask = new CombinedIcebergScanTask(
        new IcebergContentFile[]{avroData, parquetData, orcData},
        null,
        PartitionSpec.unpartitioned(),
        null
    );
  }

  private DataFile insert(List<Record> records, FileFormat fileFormat) throws IOException {
    GenericAppenderFactory fileAppenderFactory = new GenericAppenderFactory(schema);
    OutputFileFactory outputFileFactory = OutputFileFactory.builderFor(table, 0, 1)
        .format(fileFormat).build();
    DataWriter<Record> recordDataWriter =
        fileAppenderFactory.newDataWriter(outputFileFactory.newOutputFile(), fileFormat, null);
    for (Record record: records){
      recordDataWriter.write(record);
    }
    recordDataWriter.close();
    return recordDataWriter.toDataFile();
  }

  private DeleteFile eqDelete(List<Record> records, FileFormat fileFormat) throws IOException {
    GenericAppenderFactory fileAppenderFactory = new GenericAppenderFactory(schema, PartitionSpec.unpartitioned(),
        new int[]{1}, schema, schema);
    OutputFileFactory outputFileFactory = OutputFileFactory.builderFor(table, 0, 1)
        .format(fileFormat).build();
    EqualityDeleteWriter<Record> recordDataWriter =
        fileAppenderFactory.newEqDeleteWriter(outputFileFactory.newOutputFile(), fileFormat, null);
    recordDataWriter.write(records);
    recordDataWriter.close();
    return recordDataWriter.toDeleteFile();
  }

  private DeleteFile posDelete(List<PositionDelete<Record>> positionDeletes, FileFormat fileFormat) throws IOException {
    GenericAppenderFactory fileAppenderFactory = new GenericAppenderFactory(schema);
    OutputFileFactory outputFileFactory = OutputFileFactory.builderFor(table, 0, 1)
        .format(fileFormat).build();
    PositionDeleteWriter<Record> recordDataWriter =
        fileAppenderFactory.newPosDeleteWriter(outputFileFactory.newOutputFile(), fileFormat, null);
    recordDataWriter.write(positionDeletes);
    recordDataWriter.close();
    return recordDataWriter.toDeleteFile();
  }


  @After
  public void clean(){
    temp.delete();
  }
}
