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

package com.netease.arctic.io;

import com.netease.arctic.IcebergTableBase;
import com.netease.arctic.io.reader.GenericCombinedIcebergDataReader;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.types.Types;
import org.junit.Test;

public class GenericCombinedIcebergDataReaderTest extends IcebergTableBase {

  protected Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.LongType.get())
  );

  @Test
  public void read(){
    GenericCombinedIcebergDataReader dataReader = new GenericCombinedIcebergDataReader(
        new ArcticFileIoDummy(table.io()),
        schema,
        schema,
        null,
        false,
        IdentityPartitionConverters::convertConstant,
        false
    );

    CloseableIterable<Record> records = dataReader.readData(combinedIcebergScanTask);
    records.forEach(System.out::println);
  }

}
