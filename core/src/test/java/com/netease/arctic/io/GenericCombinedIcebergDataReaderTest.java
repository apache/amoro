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

import com.google.common.collect.Iterables;
import com.netease.arctic.IcebergTableBase;
import com.netease.arctic.io.reader.GenericCombinedIcebergDataReader;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GenericCombinedIcebergDataReaderTest extends IcebergTableBase {

  protected Schema schema = new Schema(
      Types.NestedField.required(1, "id", Types.LongType.get())
  );

  protected GenericCombinedIcebergDataReader dataReader;

  @Before
  public void init() {
    dataReader = new GenericCombinedIcebergDataReader(
        new ArcticFileIoDummy(table.io()),
        schema,
        schema,
        null,
        false,
        IdentityPartitionConverters::convertConstant,
        false
    );
  }

  @Test
  public void readAllData(){
    CloseableIterable<Record> records = dataReader.readData(allFileTask);
    Assert.assertTrue(Iterables.size(records) == 1);
    Record record = Iterables.getFirst(records, null);
    Assert.assertEquals(record.get(0), 3L);
  }

  @Test
  public void readAllDataNegate(){
    CloseableIterable<Record> records = dataReader.readDeleteData(allFileTask);
    Assert.assertTrue(Iterables.size(records) == 2);
    Record first = Iterables.getFirst(records, null);
    Assert.assertEquals(first.get(0), 1L);
    Record last = Iterables.getLast(records);
    Assert.assertEquals(last.get(0), 2L);
  }

  @Test
  public void readOnlyData(){
    CloseableIterable<Record> records = dataReader.readData(onlyDataTask);
    Assert.assertEquals(Iterables.size(records), 3);
  }

  @Test
  public void readOnlyDataNegate(){
    CloseableIterable<Record> records = dataReader.readDeleteData(onlyDataTask);
    Assert.assertEquals(Iterables.size(records), 0);
  }
}
