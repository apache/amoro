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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.flink.util.DataUtil;
import com.netease.arctic.flink.write.FlinkTaskWriterBaseTest;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.apache.iceberg.flink.FlinkSchemaUtil;
import org.apache.iceberg.io.TaskWriter;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LookupITCase extends CatalogITCaseBase implements FlinkTaskWriterBaseTest {
  private String db;

  public LookupITCase() {
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
    exec("create table arctic.%s.DIM (id int, name string, primary key(id) not enforced) " +
        "with ('write.upsert.enabled'='true', 'lookup.reloading.interval'='1 s')", db);
    exec("create view vi as select *, PROCTIME() as proc from arctic.%s.L", db);

    writeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "DIM"),
        Lists.newArrayList(
            DataUtil.toRowData(1, "a"),
            DataUtil.toRowData(2, "b"))
    );
    writeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "L"),
        Lists.newArrayList(
            DataUtil.toRowData(1)
        )
    );
  }

  @After
  public void drop() {
    exec("drop table arctic.%s.L", db);
    exec("drop table arctic.%s.DIM", db);
  }

  @Test()
  public void testLookup() throws Exception {
    TableResult tableResult = exec(
        "select L.id, D.name from vi L LEFT JOIN arctic.%s.DIM " +
            "for system_time as of L.proc AS D ON L.id = D.id",
        db);

    tableResult.await(1, TimeUnit.MINUTES);// wait for the first row.

    writeToChangeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "DIM"),
        Lists.newArrayList(
            DataUtil.toRowData(2, "c"),
            DataUtil.toRowData(3, "d"),
            DataUtil.toRowData(4, "e"),
            DataUtil.toRowData(5, "f")
        ),
        true);
    Thread.sleep(2000); // wait dim table commit and reload

    writeToChangeAndCommit(
        TableIdentifier.of(getCatalogName(), db, "L"),
        Lists.newArrayList(
            DataUtil.toRowData(2),
            DataUtil.toRowData(3),
            DataUtil.toRowData(4),
            DataUtil.toRowData(5),
            DataUtil.toRowData(6)
        ),
        false);

    int expected = 6, count = 0;
    Set<Row> actual = new HashSet<>();
    try (CloseableIterator<Row> rows = tableResult.collect()) {
      while (count < expected && rows.hasNext()) {
        Row row = rows.next();
        actual.add(row);
        count++;
      }
    }

    Assert.assertEquals(expected, actual.size());
    List<Object[]> expects = new LinkedList<>();
    expects.add(new Object[]{1, "a"});
    expects.add(new Object[]{2, "c"});
    expects.add(new Object[]{3, "d"});
    expects.add(new Object[]{4, "e"});
    expects.add(new Object[]{5, "f"});
    expects.add(new Object[]{6, null});
    Assert.assertEquals(DataUtil.toRowSet(expects), actual);
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
