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

package org.apache.amoro.hive.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.amoro.NoSuchTableException;
import org.apache.amoro.client.ClientPool;
import org.apache.amoro.hive.HMSClient;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestHiveTableUtil {

  @Test
  public void testGetAllHiveTablesExcludesViews() throws Exception {
    HMSClient client = mock(HMSClient.class);
    when(client.getAllTables("database")).thenReturn(Arrays.asList("physical_table", "hive_view"));
    when(client.getTableMeta(eq("database"), eq("*"), anyList()))
        .thenReturn(
            Collections.singletonList(
                new TableMeta("database", "hive_view", TableType.VIRTUAL_VIEW.name())));

    List<String> tableNames =
        HiveTableUtil.getAllHiveTables(new TestingHMSClientPool(client), "database");

    assertEquals(Collections.singletonList("physical_table"), tableNames);
  }

  @Test
  public void testGetAllHiveTablesFallsBackWhenTableMetaIsUnsupported() throws Exception {
    HMSClient client = mock(HMSClient.class);
    when(client.getAllTables("database")).thenReturn(Arrays.asList("physical_table", "hive_view"));
    when(client.getTableMeta(eq("database"), eq("*"), anyList()))
        .thenThrow(new UnsupportedOperationException("not supported"));
    when(client.getTableObjectsByName(eq("database"), anyList()))
        .thenReturn(
            Arrays.asList(
                table("physical_table", TableType.EXTERNAL_TABLE),
                table("hive_view", TableType.VIRTUAL_VIEW)));

    List<String> tableNames =
        HiveTableUtil.getAllHiveTables(new TestingHMSClientPool(client), "database");

    assertEquals(Collections.singletonList("physical_table"), tableNames);
  }

  @Test
  public void testGetAllHiveTablesFallsBackWhenTableMetaIsNull() throws Exception {
    HMSClient client = mock(HMSClient.class);
    when(client.getAllTables("database")).thenReturn(Arrays.asList("physical_table", "hive_view"));
    when(client.getTableMeta(eq("database"), eq("*"), anyList())).thenReturn(null);
    when(client.getTableObjectsByName(eq("database"), anyList()))
        .thenReturn(
            Arrays.asList(
                table("physical_table", TableType.EXTERNAL_TABLE),
                table("hive_view", "MATERIALIZED_VIEW")));

    List<String> tableNames =
        HiveTableUtil.getAllHiveTables(new TestingHMSClientPool(client), "database");

    assertEquals(Collections.singletonList("physical_table"), tableNames);
  }

  @Test
  public void testLoadPhysicalHmsTableRejectsView() throws Exception {
    HMSClient client = mock(HMSClient.class);
    when(client.getTable("database", "hive_view"))
        .thenReturn(table("hive_view", TableType.VIRTUAL_VIEW));
    TableIdentifier identifier = TableIdentifier.of("catalog", "database", "hive_view");

    assertThrows(
        NoSuchTableException.class,
        () -> HiveTableUtil.loadPhysicalHmsTable(new TestingHMSClientPool(client), identifier));
  }

  @Test
  public void testLoadPhysicalHmsTable() throws Exception {
    HMSClient client = mock(HMSClient.class);
    Table physicalTable = table("physical_table", TableType.EXTERNAL_TABLE);
    when(client.getTable("database", "physical_table")).thenReturn(physicalTable);
    TableIdentifier identifier = TableIdentifier.of("catalog", "database", "physical_table");

    assertEquals(
        physicalTable,
        HiveTableUtil.loadPhysicalHmsTable(new TestingHMSClientPool(client), identifier));
  }

  private static Table table(String name, TableType tableType) {
    return table(name, tableType.name());
  }

  private static Table table(String name, String tableType) {
    Table table = new Table();
    table.setDbName("database");
    table.setTableName(name);
    table.setTableType(tableType);
    return table;
  }

  private static class TestingHMSClientPool implements HMSClientPool {

    private final HMSClient client;

    private TestingHMSClientPool(HMSClient client) {
      this.client = client;
    }

    @Override
    public <R> R run(ClientPool.Action<R, HMSClient, TException> action)
        throws TException, InterruptedException {
      return action.run(client);
    }

    @Override
    public <R> R run(ClientPool.Action<R, HMSClient, TException> action, boolean retry)
        throws TException, InterruptedException {
      return action.run(client);
    }
  }
}
