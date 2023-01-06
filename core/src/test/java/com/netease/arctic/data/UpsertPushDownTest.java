package com.netease.arctic.data;

import com.netease.arctic.TableTestBase;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpsertPushDownTest extends TableTestBase {

  @Test
  public void testUpsertKeyedTable() {
    List<DataFile> dataFiles1 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "aaa", 1));
    List<DataFile> dataFiles2 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "aaa", 1));
    List<DataFile> dataFiles3 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "bbb", 2));
    List<DataFile> dataFiles4 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "bbb", 2));
    List<DataFile> dataFiles5 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "ccc", 2));
    List<DataFile> dataFiles6 = writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "ccc", 2));
    List<Record> records = readKeyedTable(testKeyedUpsertTable);
    Assert.assertEquals(records.size(), 2);
    Assert.assertTrue(recordToNameList(records).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

    Expression partition_and_np = Expressions.and(
        Expressions.and(
            Expressions.notNull("op_time"),
            Expressions.equal("op_time", "2022-01-02T12:00:00")
        ),
        Expressions.and(
            Expressions.notNull("name"),
            Expressions.equal("name", "bbb")
        )
    );
    Pair<List<Record>, List<String>> pair1 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_and_np);
    List<Record> records1 = pair1.getLeft();
    List<String> path1 = pair1.getRight();
    assertPath(Lists.newArrayList(dataFiles3,dataFiles4,dataFiles5,dataFiles6),path1);
    Assert.assertEquals(records1.size(), 1);
    Assert.assertTrue(recordToNameList(records1).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression partition_or_np = Expressions.or(
        Expressions.and(
            Expressions.notNull("op_time"),
            Expressions.equal("op_time", "2022-01-02T12:00:00")
        ),
        Expressions.and(
            Expressions.notNull("name"),
            Expressions.equal("name", "bbb")
        )
    );
    Pair<List<Record>, List<String>> pair2 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_or_np);
    List<Record> records2 = pair2.getLeft();
    List<String> path2 = pair2.getRight();
    assertPath(Lists.newArrayList(dataFiles1,dataFiles2,dataFiles3,dataFiles4,dataFiles5,dataFiles6),path2);
    Assert.assertEquals(records2.size(), 2);
    Assert.assertTrue(recordToNameList(records2).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

    Expression only_partition = Expressions.and(
        Expressions.notNull("op_time"),
        Expressions.equal("op_time", "2022-01-02T12:00:00")
    );
    Pair<List<Record>, List<String>> pair3 = readKeyedTableWithFilters(testKeyedUpsertTable, only_partition);
    List<Record> records3 = pair3.getLeft();
    List<String> path3 = pair3.getRight();
    assertPath(Lists.newArrayList(dataFiles3,dataFiles4,dataFiles5,dataFiles6),path3);
    Assert.assertEquals(records3.size(), 1);
    Assert.assertTrue(recordToNameList(records3).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression only_np = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    Pair<List<Record>, List<String>> pair4 = readKeyedTableWithFilters(testKeyedUpsertTable, only_np);
    List<Record> records4 = pair4.getLeft();
    List<String> path4 = pair4.getRight();
    assertPath(Lists.newArrayList(dataFiles1,dataFiles2,dataFiles3,dataFiles4,dataFiles5,dataFiles6),path4);
    Assert.assertEquals(records4.size(), 2);
    Assert.assertTrue(recordToNameList(records4).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

    Expression partition_and_np_gt = Expressions.and(
        Expressions.and(
            Expressions.notNull("op_time"),
            Expressions.greaterThan("op_time", "2022-01-01T12:00:00")
        ),
        Expressions.and(
            Expressions.notNull("name"),
            Expressions.equal("name", "bbb")
        )
    );
    Pair<List<Record>, List<String>> pair5 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_and_np_gt);
    List<Record> records5 = pair5.getLeft();
    List<String> path5 = pair5.getRight();
    assertPath(Lists.newArrayList(dataFiles3,dataFiles4,dataFiles5,dataFiles6),path5);
    Assert.assertEquals(records5.size(), 1);
    Assert.assertTrue(recordToNameList(records5).containsAll(Arrays.asList(new String[]{"ccc"})));
  }

  @Test
  public void testUpsertNoPartitionKeyedTable() {
    List<DataFile> dataFiles1 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "aaa", 1));
    List<DataFile> dataFiles2 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "aaa", 1));
    List<DataFile> dataFiles3 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "bbb", 2));
    List<DataFile> dataFiles4 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "bbb", 2));
    List<DataFile> dataFiles5 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "ccc", 2));
    List<DataFile> dataFiles6 = writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "ccc", 2));
    List<Record> records = readKeyedTable(testKeyedNoPartitionUpsertTable);
    Assert.assertEquals(records.size(), 1);
    Assert.assertTrue(recordToNameList(records).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression exp_bbb = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    Pair<List<Record>, List<String>> pair1 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, exp_bbb);
    List<Record> records1 = pair1.getLeft();
    List<String> path1 = pair1.getRight();
    assertPath(Lists.newArrayList(dataFiles1,dataFiles2,dataFiles3,dataFiles4,dataFiles5,dataFiles6),path1);
    Assert.assertEquals(records1.size(), 1);
    Assert.assertTrue(recordToNameList(records1).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression exp_aaa = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "aaa")
    );
    Pair<List<Record>, List<String>> pair2 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, exp_aaa);
    List<Record> records2 = pair2.getLeft();
    List<String> path2 = pair2.getRight();
    assertPath(Lists.newArrayList(dataFiles1,dataFiles2,dataFiles3,dataFiles4,dataFiles5,dataFiles6),path2);
    Assert.assertEquals(records2.size(), 1);
    Assert.assertTrue(recordToNameList(records2).containsAll(Arrays.asList(new String[]{"ccc"})));
  }

  @Test
  public void testUpsertUnionPartitionKeyedTable() {
    List<DataFile> dataFiles1 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "aaa", "2023-1-1", 1));
    List<DataFile> dataFiles2 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "aaa", "2023-1-1", 1));
    List<DataFile> dataFiles3 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "bbb", "2023-1-1", 2));
    List<DataFile> dataFiles4 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "bbb", "2023-1-1", 2));
    List<DataFile> dataFiles5 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "ccc", "2023-1-1", 2));
    List<DataFile> dataFiles6 = writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "ccc", "2023-1-1", 2));
    List<Record> records1 = readKeyedTable(testKeyedUnionPartitionUpsertTable);
    Assert.assertEquals(records1.size(), 2);
    Assert.assertTrue(recordToNameList(records1).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

    Expression partition_and_np = Expressions.and(
        Expressions.and(
            Expressions.notNull("num"),
            Expressions.greaterThan("num", 1)
        ),
        Expressions.and(
            Expressions.notNull("name"),
            Expressions.equal("name", "bbb")
        )
    );
    Pair<List<Record>, List<String>> pair = readKeyedTableWithFilters(testKeyedUnionPartitionUpsertTable, partition_and_np);
    List<Record> records2 = pair.getLeft();
    List<String> path = pair.getRight();
    assertPath(Lists.newArrayList(dataFiles3,dataFiles4,dataFiles5,dataFiles6),path);
    Assert.assertEquals(records2.size(), 1);
    Assert.assertTrue(recordToNameList(records2).containsAll(Arrays.asList(new String[]{"ccc"})));
  }

  private List<Record> writeRecords(int id, String name, int day) {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", id, "name", name, "op_time",
        LocalDateTime.of(2022, 1, day, 12, 0, 0))));

    return builder.build();
  }

  private List<Record> writeUnionRecords(int id, String name, String time, int num) {
    GenericRecord record = GenericRecord.create(UNION_TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", id, "name", name, "time", time, "num", num)));

    return builder.build();
  }

  private List<String> recordToNameList(List<Record> list) {
    return list.stream().map(r -> r.getField("name").toString()).collect(Collectors.toList());
  }

  private void assertPath(List<List<DataFile>> dataFileList, List<String> path) {
    Assert.assertTrue(path.size() == dataFileList.size());
    for (List<DataFile> dataFile : dataFileList) {
      Assert.assertTrue(path.contains(dataFile.get(0).path().toString()));
    }
  }
}
