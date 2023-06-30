/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing;

import com.netease.arctic.iceberg.InternalRecordWrapper;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TestIcebergHadoopOptimizing extends AbstractOptimizingTest {
  private final Table table;
  private final BaseOptimizingChecker checker;

  public TestIcebergHadoopOptimizing(TableIdentifier tb, Table table) {
    this.table = table;
    this.checker = new BaseOptimizingChecker(tb);
  }

  public void testIcebergTableOptimizing() throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizingProcessMeta optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    checker.assertIds(readRecords(table), 1, 2, 3, 4, 5, 6);

    // Step 2: insert delete file and Minor Optimize
    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 2, 3, 4, 5, 6);

    // Step 3: insert 2 delete file and Minor Optimize(big file)
    long dataFileSize = getDataFileSize(table);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / (dataFileSize - 100) + "");

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(2, "aaa", quickDateWithZone(3))
    ), partitionData);

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 4, 5, 6);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "10");

    // Step 4: insert 1 delete and full optimize
    // insertEqDeleteFiles(table, Lists.newArrayList(
    //     newRecord(4, "bbb", quickDateWithZone(3))
    // ));
    rowDelta(table, Lists.newArrayList(
        newRecord(7, "aaa", quickDateWithZone(3)),
        newRecord(8, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    // wait FullMajor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MAJOR, 3, 1);

    assertIds(readRecords(table), 5, 6, 7, 8);
    checker.assertOptimizeHangUp();
  }

  public void testV1IcebergTableOptimizing() throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizingProcessMeta optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 1, 2, 3, 4, 5, 6);

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(7, "ccc", quickDateWithZone(3)),
        newRecord(8, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 1, 2, 3, 4, 5, 6, 7, 8);
    checker.assertOptimizeHangUp();
  }

  public void testPartitionIcebergTableOptimizing() throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    // Step 1: insert 2 data file and Minor Optimize
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "bbb", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "bbb", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "ccc", quickDateWithZone(3)),
        newRecord(6, "ddd", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    OptimizingProcessMeta optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 1, 2, 3, 4, 5, 6);

    // Step 2: insert delete file and Minor Optimize
    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 2, 3, 4, 5, 6);

    // Step 3: insert 2 delete file and Minor Optimize(big file)
    long dataFileSize = getDataFileSize(table);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT / (dataFileSize - 100) + "");

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(2, "aaa", quickDateWithZone(3))
    ), partitionData);

    insertEqDeleteFiles(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3))
    ), partitionData);

    // wait Minor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 2, 1);
    assertIds(readRecords(table), 4, 5, 6);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "10");

    rowDelta(table, Lists.newArrayList(
        newRecord(7, "aaa", quickDateWithZone(3)),
        newRecord(8, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    // wait FullMajor Optimize result
    optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MAJOR, 3, 1);

    assertIds(readRecords(table), 5, 6, 7, 8);

    checker.assertOptimizeHangUp();
  }

  public void testIcebergTableFullOptimize() throws IOException {
    StructLike partitionData = partitionData(table.schema(), table.spec(), quickDateWithZone(3));

    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "100");

    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDelta(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDelta(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    rowDeltaWithPos(table, Lists.newArrayList(
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(3)),
        newRecord(2, "aaa", quickDateWithZone(3)),
        newRecord(3, "aaa", quickDateWithZone(3)),
        newRecord(4, "aaa", quickDateWithZone(3))
    ), partitionData);

    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "eee", quickDateWithZone(3))
    ), partitionData);

    assertIds(readRecords(table), 4, 5);

    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAJOR_TRIGGER_DUPLICATE_RATIO, "0");

    OptimizingProcessMeta optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 9, 1);

    assertIds(readRecords(table), 4, 5);

    checker.assertOptimizeHangUp();
  }

  public void testPartitionIcebergTablePartialOptimizing() throws IOException {
    // Step 1: insert 6 data files for two partitions
    StructLike partitionData1 = partitionData(table.schema(), table.spec(), quickDateWithZone(1));
    insertDataFile(table, Lists.newArrayList(
        newRecord(1, "aaa", quickDateWithZone(1))
    ), partitionData1);
    insertDataFile(table, Lists.newArrayList(
        newRecord(2, "bbb", quickDateWithZone(1))
    ), partitionData1);
    StructLike partitionData2 = partitionData(table.schema(), table.spec(), quickDateWithZone(2));
    insertDataFile(table, Lists.newArrayList(
        newRecord(3, "ccc", quickDateWithZone(2))
    ), partitionData2);
    insertDataFile(table, Lists.newArrayList(
        newRecord(4, "ddd", quickDateWithZone(2))
    ), partitionData2);
    StructLike partitionData3 = partitionData(table.schema(), table.spec(), quickDateWithZone(3));
    insertDataFile(table, Lists.newArrayList(
        newRecord(5, "eee", quickDateWithZone(3))
    ), partitionData3);
    insertDataFile(table, Lists.newArrayList(
        newRecord(6, "fff", quickDateWithZone(3))
    ), partitionData3);

    updateProperties(table, TableProperties.SELF_OPTIMIZING_MINOR_TRIGGER_FILE_CNT, "2");
    updateProperties(table, TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT, "4");

    // wait Minor Optimize result
    OptimizingProcessMeta optimizeHistory = checker.waitOptimizeResult();
    checker.assertOptimizingProcess(optimizeHistory, OptimizingType.MINOR, 6, 3);
    assertIds(readRecords(table), 1, 2, 3, 4, 5, 6);

    checker.assertOptimizeHangUp();
  }

  private Record newRecord(Object... val) {
    return newRecord(table.schema(), val);
  }

  private StructLike partitionData(Schema tableSchema, PartitionSpec spec, Object... partitionValues) {
    GenericRecord record = GenericRecord.create(tableSchema);
    int index = 0;
    Set<Integer> partitionField = Sets.newHashSet();
    spec.fields().forEach(f -> partitionField.add(f.sourceId()));
    List<Types.NestedField> tableFields = tableSchema.columns();
    for (int i = 0; i < tableFields.size(); i++) {
      // String sourceColumnName = tableSchema.findColumnName(i);
      Types.NestedField sourceColumn = tableFields.get(i);
      if (partitionField.contains(sourceColumn.fieldId())) {
        Object partitionVal = partitionValues[index];
        index++;
        record.set(i, partitionVal);
      } else {
        record.set(i, 0);
      }
    }

    PartitionKey pd = new PartitionKey(spec, tableSchema);
    InternalRecordWrapper wrapper = new InternalRecordWrapper(tableSchema.asStruct());
    wrapper = wrapper.wrap(record);
    pd.partition(wrapper);
    return pd;
  }
}
