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
    Assert.assertEquals(records1.size(),2);

    List<Expression> filters_bbb = Arrays.asList(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    List<Record> records2 = readKeyedTableWithFilters(testKeyedUpsertTable, filters_bbb);
    Assert.assertEquals(records2.size(),2);

    List<Expression> filters_aaa = Arrays.asList(
        Expressions.notNull("name"),
        Expressions.equal("name", "aaa")
    );
    List<Record> records3 = readKeyedTableWithFilters(testKeyedUpsertTable, filters_aaa);
    Assert.assertEquals(records3.size(),2);
  }

  @Test
  public void testUpsertNoPartitionKeyedTable() {
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "aaa", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "aaa", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "bbb", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "bbb", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.DELETE, writeRecords(1, "ccc", 1));
    writeChange(PK_NO_PARTITION_UPSERT_TABLE_ID, ChangeAction.UPDATE_AFTER, writeRecords(1, "ccc", 1));
    List<Record> records1 = readKeyedTable(testKeyedNoPartitionUpsertTable);
    Assert.assertEquals(records1.get(0).getField("name"),"ccc");

    List<Expression> filters_bbb = Arrays.asList(
        Expressions.notNull("name"),
        Expressions.equal("name", "bbb")
    );
    List<Record> records2 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, filters_bbb);
    Assert.assertEquals(records2.get(0).getField("name"),"ccc");

    List<Expression> filters_aaa = Arrays.asList(
        Expressions.notNull("name"),
        Expressions.equal("name", "aaa")
    );
    List<Record> records3 = readKeyedTableWithFilters(testKeyedNoPartitionUpsertTable, filters_aaa);
    Assert.assertEquals(records3.get(0).getField("name"),"ccc");
  }

  private List<Record> writeRecords(int id, String name, int day) {
    GenericRecord record = GenericRecord.create(TABLE_SCHEMA);

    ImmutableList.Builder<Record> builder = ImmutableList.builder();
    builder.add(record.copy(ImmutableMap.of("id", id, "name", name, "op_time",
        LocalDateTime.of(2022, 1, day, 12, 0, 0))));

    return builder.build();
  }
}
