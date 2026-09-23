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

package org.apache.amoro.hive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class TestHiveTableTypeUtil {

  @Test
  public void testViewTypes() {
    assertTrue(HiveTableTypeUtil.isView(tableWithType(TableType.VIRTUAL_VIEW.name())));
    assertTrue(HiveTableTypeUtil.isView(tableWithType("MATERIALIZED_VIEW")));
    assertFalse(HiveTableTypeUtil.isView(tableWithType(TableType.EXTERNAL_TABLE.name())));
    assertFalse(HiveTableTypeUtil.isView(tableWithType(null)));
    assertTrue(
        HiveTableTypeUtil.isView(new TableMeta("database", "view", TableType.VIRTUAL_VIEW.name())));
  }

  @Test
  public void testListViewNames() throws Exception {
    HMSClient client = mock(HMSClient.class);
    when(client.getTableMeta("database", "*", HiveTableTypeUtil.viewTypes()))
        .thenReturn(
            Arrays.asList(
                new TableMeta("database", "Hive_View", "VIRTUAL_VIEW"),
                new TableMeta("database", "Materialized_View", "MATERIALIZED_VIEW"),
                new TableMeta("database", "physical_table", "EXTERNAL_TABLE")));

    assertEquals(
        new HashSet<>(Arrays.asList("hive_view", "materialized_view")),
        HiveTableTypeUtil.listViewNames(
            client, "database", Arrays.asList("Hive_View", "Materialized_View", "physical_table")));
  }

  @Test
  public void testListViewNamesSkipsEmptyCandidates() throws Exception {
    HMSClient client = mock(HMSClient.class);

    assertTrue(
        HiveTableTypeUtil.listViewNames(client, "database", Collections.emptyList()).isEmpty());
    assertTrue(HiveTableTypeUtil.listViewNames(client, "database", null).isEmpty());
    verifyNoInteractions(client);
  }

  private static Table tableWithType(String tableType) {
    Table table = new Table();
    table.setTableType(tableType);
    return table;
  }
}
