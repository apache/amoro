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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.hadoop.hive.metastore.TableType;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.api.TableMeta;
import org.junit.jupiter.api.Test;

public class TestHiveTableTypeUtil {

  @Test
  public void testViewTypes() {
    assertTrue(HiveTableTypeUtil.isView(tableWithType(TableType.VIRTUAL_VIEW.name())));
    assertTrue(HiveTableTypeUtil.isView(tableWithType(TableType.MATERIALIZED_VIEW.name())));
    assertFalse(HiveTableTypeUtil.isView(tableWithType(TableType.EXTERNAL_TABLE.name())));
    assertFalse(HiveTableTypeUtil.isView(tableWithType(null)));
    assertTrue(
        HiveTableTypeUtil.isView(new TableMeta("database", "view", TableType.VIRTUAL_VIEW.name())));
  }

  private static Table tableWithType(String tableType) {
    Table table = new Table();
    table.setTableType(tableType);
    return table;
  }
}
