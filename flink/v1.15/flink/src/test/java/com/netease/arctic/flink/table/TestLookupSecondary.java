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

package com.netease.arctic.flink.table;

import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.flink.util.DataUtil;
import com.netease.arctic.flink.write.FlinkTaskWriterBaseTest;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.CloseableIterator;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestLookupSecondary extends CatalogITCaseBase implements FlinkTaskWriterBaseTest {
  private String db;

  public TestLookupSecondary() {
    super(
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(true, false));
  }

  @Before
  public void setup() throws IOException {
    List<String> dbs = getCatalog().listDatabases();
    if (dbs.isEmpty()) {
      db = "test_db";
      getCatalog().createDatabase(db);
    } else {
      db = dbs.get(0);
    }
    exec("create catalog arctic with ('type'='arctic', 'metastore.url'='%s')", getCatalogUrl());
    exec("create table arctic.%s.L (id int) " +
        "with ('scan.startup.mode'='earliest', 'monitor-interval'='1 s')", db);
    exec("create table arctic.%s.DIM_2 (id int, name string, cls bigint, primary key(id, name) not enforced) " +
        "with ('write.upsert.enabled'='true', 'lookup.reloading.interval'='1 s')", db);
    exec("create view vi as select *, PROCTIME() as proc from arctic.%s.L", db);

    writeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "L"),
        Lists.newArrayList(
            DataUtil.toRowData(1),
            DataUtil.toRowData(2),
            DataUtil.toRowData(3),
            DataUtil.toRowData(4)
        )
    );
    writeToChangeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "DIM_2"),
        Lists.newArrayList(
            DataUtil.toRowData(1, "a", 1L),
            DataUtil.toRowData(1, "b", 1L),
            DataUtil.toRowData(2, "c", 2L),
            DataUtil.toRowData(3, "d", 3L)
        ),
        true);
  }

  @After
  public void drop() {
    exec("drop table arctic.%s.L", db);
    exec("drop table arctic.%s.DIM_2", db);
  }

  @Test()
  public void testLookup() throws Exception {
    TableResult tableResult = exec(
        "select L.id, D.cls from vi L LEFT JOIN arctic.%s.DIM_2 " +
            "for system_time as of L.proc AS D ON L.id = D.id",
        db);

    tableResult.await(1, TimeUnit.MINUTES);// wait for the first row.

    List<Object[]> expects = new LinkedList<>();
    expects.add(new Object[]{1, 1L});
    expects.add(new Object[]{1, 1L});
    expects.add(new Object[]{2, 2L});
    expects.add(new Object[]{3, 3L});
    expects.add(new Object[]{4, null});
    int expected = expects.size(), count = 0;
    List<Row> actual = new ArrayList<>();
    try (CloseableIterator<Row> rows = tableResult.collect()) {
      while (count < expected && rows.hasNext()) {
        Row row = rows.next();
        actual.add(row);
        count++;
      }
    }

    Assert.assertEquals(expected, actual.size());
    List<Row> rows = expects.stream().map(r ->
        r[0] instanceof RowKind ? Row.ofKind((RowKind) r[0], ArrayUtils.subarray(r, 1, r.length)) :
            Row.of(r)).collect(Collectors.toList());
    Assert.assertEquals(
        rows.stream().sorted(Comparator.comparing(Row::toString)).collect(Collectors.toList()),
        actual.stream().sorted(Comparator.comparing(Row::toString)).collect(Collectors.toList()));
  }

  @Override
  public String getMetastoreUrl() {
    return getCatalogUrl();
  }

  @Override
  public String getCatalogName() {
    return getCatalog().name();
  }

  @Override
  public boolean upsertEnabled() {
    return true;
  }

  private void writeAndCommit(
      TableIdentifier table, List<RowData> expected) throws IOException {
    writeAndCommit(table, expected, true, false);
  }

  private void writeToChangeAndCommit(
      TableIdentifier table, List<RowData> expected, boolean upsertEnabled) throws IOException {
    writeAndCommit(table, expected, false, upsertEnabled);
  }

  private void writeAndCommit(
      TableIdentifier table, List<RowData> expected, boolean writeToBaseStore, boolean upsertEnabled) throws IOException {
    ArcticTable arcticTable = getCatalog().loadTable(table);
    Assert.assertNotNull(arcticTable);
    RowType rowType = FlinkSchemaUtil.convert(arcticTable.schema());
    for (RowData rowData : expected) {
      try (TaskWriter<RowData> taskWriter =
               writeToBaseStore
                   ? createBaseTaskWriter(arcticTable, rowType)
                   : createTaskWriter(arcticTable, rowType)) {
        if (writeToBaseStore) {
          writeAndCommit(rowData, taskWriter, arcticTable);
        } else {
          writeAndCommit(rowData, taskWriter, arcticTable, upsertEnabled);
        }
      }
    }
  }

}
