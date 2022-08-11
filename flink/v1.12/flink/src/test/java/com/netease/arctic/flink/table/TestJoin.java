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

import com.netease.arctic.flink.FlinkTestBase;
import com.netease.arctic.flink.util.ArcticUtils;
import com.netease.arctic.flink.util.DataUtil;
import com.netease.arctic.table.KeyedTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.table.runtime.typeutils.InternalTypeInfo;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.table.types.logical.TimestampKind;
import org.apache.flink.table.types.logical.TimestampType;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.flink.util.CloseableIterator;
import org.apache.iceberg.io.TaskWriter;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.netease.arctic.ams.api.MockArcticMetastoreServer.TEST_CATALOG_NAME;
import static com.netease.arctic.table.TableProperties.LOCATION;
import static org.apache.flink.api.common.JobStatus.INITIALIZING;
import static org.apache.flink.table.api.Expressions.$;

public class TestJoin extends FlinkTestBase {

  public static final Logger LOG = LoggerFactory.getLogger(TestJoin.class);

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private static final String DB = PK_TABLE_ID.getDatabase();
  private static final String TABLE = "test_keyed";

  public void before() {
    super.before();
    super.config();
  }

  @After
  public void after() {
    sql("DROP TABLE IF EXISTS arcticCatalog." + DB + "." + TABLE);
  }

  @Test
  public void testLeftCdcLookupJoin() throws IOException {
    String table;

    sql("CREATE TABLE left_view (id bigint, t2 string, opt timestamp(3), watermark for opt as opt) " +
        "with (" +
        " 'connector' = 'values'," +
        " 'table-source-class' = 'com.netease.arctic.flink.table.CdcSource' " +
        " )");

    sql(String.format("CREATE CATALOG arcticCatalog WITH %s", toWithClause(props)));
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(LOCATION, tableDir.getAbsolutePath());
    table = String.format("arcticCatalog.%s.%s", DB, TABLE);
    String sql = String.format("CREATE TABLE IF NOT EXISTS %s (" +
        " test int, id bigint, name STRING" +
        ", PRIMARY KEY (id) NOT ENFORCED) WITH %s", table, toWithClause(tableProperties));
    sql(sql);

    tableProperties.clear();
    sql("create table r (op_time timestamp(3), watermark for op_time as op_time - INTERVAL '1' SECOND)" +
        " WITH ('arctic.watermark'='true')" +
        "like %s", table);

    TableSchema flinkSchema = TableSchema.builder()
        .field("test", DataTypes.INT())
        .field("id", DataTypes.BIGINT())
        .field("name", DataTypes.STRING())
        .build();
    RowType rowType = (RowType) flinkSchema.toRowDataType().getLogicalType();

    KeyedTable keyedTable = (KeyedTable) ArcticUtils.loadArcticTable(
        ArcticTableLoader.of(TableIdentifier.of(TEST_CATALOG_NAME, DB, TABLE), catalogBuilder));
    TaskWriter<RowData> taskWriter = createKeyedTaskWriter(keyedTable, rowType, 1, true);
    List<RowData> baseData = new ArrayList<RowData>() {{
      add(GenericRowData.ofKind(
          RowKind.INSERT, 123, 1L, StringData.fromString("john")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 324, 2L, StringData.fromString("lily")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 456, 3L, StringData.fromString("jake")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 463, 4L, StringData.fromString("sam")));
    }};
    for (RowData record : baseData) {
      taskWriter.write(record);
    }
    commit(keyedTable, taskWriter.complete(), true);

    CompletableFuture.runAsync(() -> {
      long i = 2;
      while (true) {
        writeChange(keyedTable, rowType, i++);
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
      }
    });

    TableResult tableResult = exec("select u.*, dim.id, dim.test from left_view as u left join r " +
        "/*+OPTIONS('streaming'='true')*/ for system_time as of u.opt as dim on u.id = dim.id");

    CloseableIterator<Row> iterator = tableResult.collect();
    while (iterator.hasNext()) {
      Row i = iterator.next();
      System.out.println("out:" + i);
    }

    tableResult.getJobClient().ifPresent(JobClient::cancel);
  }

  @Test
  public void testRightEmptyLookupJoin() throws IOException {
    List<Object[]> data = new LinkedList<>();
    data.add(new Object[]{RowKind.INSERT, 1000004, "a", LocalDateTime.parse("2022-06-17T10:10:11.0")});
    data.add(new Object[]{RowKind.DELETE, 1000015, "b", LocalDateTime.parse("2022-06-17T10:08:11.0")});
    data.add(new Object[]{RowKind.DELETE, 1000011, "c", LocalDateTime.parse("2022-06-18T10:10:11.0")});
    data.add(new Object[]{RowKind.UPDATE_BEFORE, 1000021, "d", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    data.add(new Object[]{RowKind.UPDATE_AFTER, 1000021, "e", LocalDateTime.parse("2022-06-17T10:11:11.0")});
    data.add(new Object[]{RowKind.INSERT, 1000015, "e", LocalDateTime.parse("2022-06-17T10:10:11.0")});

    DataStream<RowData> source = getEnv().fromCollection(DataUtil.toRowData(data),
        InternalTypeInfo.ofFields(
            DataTypes.INT().getLogicalType(),
            DataTypes.VARCHAR(100).getLogicalType(),
            new TimestampType(true, TimestampKind.ROWTIME, 3)
        ));

    Table input = getTableEnv().fromDataStream(source, $("id"), $("name"), $("op_time").rowtime());
    getTableEnv().createTemporaryView("left_view", input);

    sql(String.format("CREATE CATALOG arcticCatalog WITH %s", toWithClause(props)));
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(LOCATION, tableDir.getAbsolutePath());
    String table = String.format("arcticCatalog.%s.%s", DB, TABLE);

    String sql = String.format("CREATE TABLE IF NOT EXISTS %s (" +
        " test int, id bigint, name STRING" +
        ", PRIMARY KEY (id) NOT ENFORCED) WITH %s", table, toWithClause(tableProperties));
    sql(sql);

    sql("create table r (op_time timestamp(3), watermark for op_time as op_time - INTERVAL '1' SECOND) " +
        "like %s", table);

    TableResult tableResult = exec("select u.t2, u.id, dim.test, dim.name from left_view as u left join r " +
        "/*+OPTIONS('streaming'='true')*/ for system_time as of u.opt as dim on u.id = dim.id");

    CloseableIterator<Row> iterator = tableResult.collect();
    while (iterator.hasNext()) {
      Row i = iterator.next();
      System.out.println("out:" + i);
    }

    tableResult.getJobClient().ifPresent(JobClient::cancel);
  }

  @Test
  public void testRightEmptyLookupJoinDemo() throws IOException {
    String table;

    sql("create table left_view (id bigint, t2 string, opt AS LOCALTIMESTAMP, watermark for opt as opt," +
        " primary key (id) not enforced) with ('connector'='datagen', 'rows-per-second'='1', 'fields.id.min'='1', " +
        " 'fields.id.max'='10')");

    sql(String.format("CREATE CATALOG arcticCatalog WITH %s", toWithClause(props)));
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(LOCATION, tableDir.getAbsolutePath());
    table = String.format("arcticCatalog.%s.%s", DB, TABLE);
    String sql = String.format("CREATE TABLE IF NOT EXISTS %s (" +
        " test int, id bigint, name STRING" +
        ", PRIMARY KEY (id) NOT ENFORCED) WITH %s", table, toWithClause(tableProperties));
    sql(sql);

    sql("create table r (op_time timestamp(3), watermark for op_time as op_time - INTERVAL '1' SECOND) " +
        "like %s", table);

    TableResult tableResult = exec("select u.t2, u.id, dim.test, dim.name from left_view as u left join r " +
        "/*+OPTIONS('streaming'='true')*/ for system_time as of u.opt as dim on u.id = dim.id");

    CloseableIterator<Row> iterator = tableResult.collect();
    while (iterator.hasNext()) {
      Row i = iterator.next();
      System.out.println("out:" + i);
    }

    tableResult.getJobClient().ifPresent(JobClient::cancel);
  }

  @Test
  public void testRightContinuousInsertLookupJoin() throws Exception {
    String table;

    sql("create table left_view (id bigint, t2 string, opt AS LOCALTIMESTAMP, watermark for opt as opt," +
        " primary key (id) not enforced) with ('connector'='datagen', 'rows-per-second'='1', 'fields.id.min'='1', " +
        " 'fields.id.max'='10')");

    sql(String.format("CREATE CATALOG arcticCatalog WITH %s", toWithClause(props)));
    Map<String, String> tableProperties = new HashMap<>();
    tableProperties.put(LOCATION, tableDir.getAbsolutePath());
    table = String.format("arcticCatalog.%s.%s", DB, TABLE);
    String sql = String.format("CREATE TABLE IF NOT EXISTS %s (" +
        " test int, id bigint, name STRING" +
        ", PRIMARY KEY (id) NOT ENFORCED) WITH %s", table, toWithClause(tableProperties));
    sql(sql);

    sql("create table r (op_time timestamp(3), watermark for op_time as op_time - INTERVAL '1' SECOND) " +
        "like %s", table);

    TableSchema flinkSchema = TableSchema.builder()
        .field("test", DataTypes.INT())
        .field("id", DataTypes.BIGINT())
        .field("name", DataTypes.STRING())
        .build();
    RowType rowType = (RowType) flinkSchema.toRowDataType().getLogicalType();

    KeyedTable keyedTable = (KeyedTable) ArcticUtils.loadArcticTable(
        ArcticTableLoader.of(TableIdentifier.of(TEST_CATALOG_NAME, DB, TABLE), catalogBuilder));
    TaskWriter<RowData> taskWriter = createKeyedTaskWriter(keyedTable, rowType, 1, true);
    List<RowData> baseData = new ArrayList<RowData>() {{
      add(GenericRowData.ofKind(
          RowKind.INSERT, 123, 1L, StringData.fromString("john")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 324, 2L, StringData.fromString("lily")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 456, 3L, StringData.fromString("jake")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 463, 4L, StringData.fromString("sam")));
    }};
    for (RowData record : baseData) {
      taskWriter.write(record);
    }
    commit(keyedTable, taskWriter.complete(), true);

    CompletableFuture.runAsync(() -> {
      long i = 2;
      while (true) {
        writeChange(keyedTable, rowType, i++);
        try {
          Thread.sleep(1000L);
        } catch (InterruptedException ignored) {
        }
      }
    });
    TableResult tableResult = exec("select u.t2, u.id, dim.test, dim.name from left_view as u left join r " +
        "/*+OPTIONS('streaming'='true', 'watermark.idle.timeout'='41s')*/ for system_time as of u.opt as dim on u.id = dim.id");

    JobClient jc = tableResult.getJobClient().get();
    while (INITIALIZING.equals(jc.getJobStatus().get())) {
      Thread.sleep(500L);
    }
    CloseableIterator<Row> iterator = tableResult.collect();
    while (iterator.hasNext()) {
      Row i = iterator.next();
      System.out.println("out:" + i);
    }

    tableResult.getJobClient().ifPresent(JobClient::cancel);
  }

  private void writeChange(KeyedTable keyedTable, RowType rowType, long tranctionId) {
    TaskWriter<RowData> taskWriter = createKeyedTaskWriter(keyedTable, rowType, tranctionId, false);
    List<RowData> data = new ArrayList<RowData>() {{
      add(GenericRowData.ofKind(
          RowKind.INSERT, 324, 5L, StringData.fromString("john")));
      add(GenericRowData.ofKind(
          RowKind.INSERT, 324, 6L, StringData.fromString("lily")));
      add(GenericRowData.ofKind(
          RowKind.DELETE, 324, 3L, StringData.fromString("jake")));
      add(GenericRowData.ofKind(
          RowKind.UPDATE_BEFORE, 324, 4L, StringData.fromString("sam")));
      add(GenericRowData.ofKind(
          RowKind.UPDATE_AFTER, 324, 4L, StringData.fromString("abc")));
    }};
    try {
      for (RowData record : data) {
        taskWriter.write(record);
      }
      commit(keyedTable, taskWriter.complete(), false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
