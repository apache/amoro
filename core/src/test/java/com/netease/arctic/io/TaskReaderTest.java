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

import com.google.common.collect.Sets;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.data.ChangeAction;
import com.netease.arctic.io.reader.BaseIcebergPosDeleteReader;
import com.netease.arctic.io.reader.GenericArcticDataReader;
import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.scan.KeyedTableScanTask;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.MetadataColumns;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.IdentityPartitionConverters;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.io.WriteResult;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TaskReaderTest extends TableTestBaseWithInitData {

  @Test
  public void testRead(){
    CloseableIterable<CombinedScanTask> combinedScanTasks = testKeyedTable.newScan().planTasks();
    Schema schema = testKeyedTable.schema();
    GenericArcticDataReader genericArcticDataReader = new GenericArcticDataReader(
        testKeyedTable.io(),
        schema,
        schema,
        testKeyedTable.primaryKeySpec(),
        null,
        true,
        IdentityPartitionConverters::convertConstant
    );
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (CombinedScanTask combinedScanTask: combinedScanTasks){
      for (KeyedTableScanTask keyedTableScanTask: combinedScanTask.tasks()){
        builder.addAll(genericArcticDataReader.readData(keyedTableScanTask));
      }
    }
    List<Record> records = builder.build();
    Set resultIds = records.stream().map(s -> s.get(0)).collect(Collectors.toSet());

    Set<Integer> rightIds = Sets.newHashSet(1, 2, 3, 6);
    Assert.assertEquals(rightIds, resultIds);
  }

  @Test
  public void testReadPosDelete() {
    BaseIcebergPosDeleteReader baseIcebergPosDeleteReader =
        new BaseIcebergPosDeleteReader(testKeyedTable.io(), Arrays.asList(deleteFileOfPositionDelete));
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    baseIcebergPosDeleteReader.readDeletes().forEach(record -> builder.add(record.copy()));

    List<Record> resultRecords = builder.build();

    GenericRecord r = GenericRecord.create(POS_DELETE_SCHEMA);
    r.set(0, dataFileForPositionDelete.path().toString());
    r.set(1, 0L);
    List<Record> sourceRecords = Arrays.asList(r);
    Assert.assertEquals(resultRecords.size(), sourceRecords.size());
    Set<String> resultPaths = resultRecords.stream().map(baseIcebergPosDeleteReader::readPath).collect(Collectors.toSet());
    Set<String> resourcePaths = sourceRecords.stream()
        .map(record -> (String) record.getField(MetadataColumns.DELETE_FILE_PATH.name())).collect(Collectors.toSet());
    Assert.assertEquals(resultPaths, resourcePaths);
    Set<Long> resultPos = resultRecords.stream().map(baseIcebergPosDeleteReader::readPos).collect(Collectors.toSet());
    Set<Long> resourcePos = sourceRecords.stream()
        .map(record -> (long) record.getField(MetadataColumns.DELETE_FILE_POS.name())).collect(Collectors.toSet());
    Assert.assertEquals(resultPos, resourcePos);
  }

  @Test
  public void testReadNegate(){
    CloseableIterable<CombinedScanTask> combinedScanTasks = testKeyedTable.newScan().planTasks();
    Schema schema = testKeyedTable.schema();
    GenericArcticDataReader genericArcticDataReader = new GenericArcticDataReader(
        testKeyedTable.io(),
        schema,
        schema,
        testKeyedTable.primaryKeySpec(),
        null,
        true,
        IdentityPartitionConverters::convertConstant
    );
    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    for (CombinedScanTask combinedScanTask: combinedScanTasks){
      for (KeyedTableScanTask keyedTableScanTask: combinedScanTask.tasks()){
        builder.addAll(genericArcticDataReader.readDeletedData(keyedTableScanTask));
      }
    }
    List<Record> records = builder.build();
    Set resultIds = records.stream().map(s -> s.get(0)).collect(Collectors.toSet());

    Set<Integer> rightIds = Sets.newHashSet(5);
    Assert.assertEquals(rightIds, resultIds);
  }
}
