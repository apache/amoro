package com.netease.arctic.data;

import com.netease.arctic.TableTestBase;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UpsertPushDownTest extends TableTestBase {

  @Test
  public void testUpsertKeyedTable() {
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "aaa", 1));
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "aaa", 1));
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "bbb", 2));
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "bbb", 2));
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "ccc", 2));
    writeChange(PK_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "ccc", 2));
    List<Record> records1 = readKeyedTable(testKeyedUpsertTable);
    Assert.assertEquals(records1.size(), 2);
    Assert.assertTrue(recordToNameList(records1).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

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
    List<Record> records2 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_and_np);
    Assert.assertEquals(records2.size(), 1);
    Assert.assertTrue(recordToNameList(records2).containsAll(Arrays.asList(new String[]{"ccc"})));

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
    List<Record> records3 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_or_np);
    Assert.assertEquals(records3.size(), 2);
    Assert.assertTrue(recordToNameList(records3).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

    Expression only_partition = Expressions.and(
        Expressions.notNull("op_time"),
        Expressions.equal("op_time", "2022-01-02T12:00:00")
    );
    List<Record> records4 = readKeyedTableWithFilters(testKeyedUpsertTable, only_partition);
    Assert.assertEquals(records4.size(), 1);
    Assert.assertTrue(recordToNameList(records4).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression only_np = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    List<Record> records5 = readKeyedTableWithFilters(testKeyedUpsertTable, only_np);
    Assert.assertEquals(records5.size(), 2);
    Assert.assertTrue(recordToNameList(records5).containsAll(Arrays.asList(new String[]{"aaa", "ccc"})));

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
    List<Record> records6 = readKeyedTableWithFilters(testKeyedUpsertTable, partition_and_np_gt);
    Assert.assertEquals(records6.size(), 1);
    Assert.assertTrue(recordToNameList(records6).containsAll(Arrays.asList(new String[]{"ccc"})));
  }

  @Test
  public void testUpsertNoPartitionKeyedTable() {
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "aaa", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "aaa", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "bbb", 2));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "bbb", 2));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "ccc", 2));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "ccc", 2));
    List<Record> records1 = readKeyedTable(testKeyedNoPartitionUpsertTable);
    Assert.assertEquals(records1.size(), 1);
    Assert.assertTrue(recordToNameList(records1).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression exp_bbb = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    List<Record> records2 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, exp_bbb);
    Assert.assertEquals(records2.size(), 1);
    Assert.assertTrue(recordToNameList(records2).containsAll(Arrays.asList(new String[]{"ccc"})));

    Expression exp_aaa = Expressions.and(
        Expressions.notNull("name"),
        Expressions.equal("name", "aaa")
    );
    List<Record> records3 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, exp_aaa);
    Assert.assertEquals(records3.size(), 1);
    Assert.assertTrue(recordToNameList(records3).containsAll(Arrays.asList(new String[]{"ccc"})));
  }

  @Test
  public void testUpsertUnionPartitionKeyedTable() {
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "aaa", "2023-1-1",1));
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "aaa", "2023-1-1",1));
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "bbb", "2023-1-1",2));
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "bbb", "2023-1-1",2));
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeUnionRecords(1, "ccc", "2023-1-1",2));
    writeChange(PK_UNION_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeUnionRecords(1, "ccc", "2023-1-1",2));
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
    List<Record> records2 = readKeyedTableWithFilters(testKeyedUnionPartitionUpsertTable, partition_and_np);
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
}
