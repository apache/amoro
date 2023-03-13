/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.hive.io;

import com.netease.arctic.hive.catalog.HiveTableTestBase;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.io.RecoverableArcticFileIO;
import com.netease.arctic.table.TableProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestRefreshHiveFileIO extends HiveTableTestBase {

  public TestRefreshHiveFileIO(boolean keyedTable, boolean partitionedTable) {
    super(keyedTable, partitionedTable);
  }

  @Parameterized.Parameters(name = "keyedTable = {0}, partitionedTable = {1}")
  public static Object[][] parameters() {
    return new Object[][] {{true, true}, {true, false}, {false, true}, {false, false}};
  }

  @Test
  public void testRefreshFileIO() {
    ArcticFileIO io = getArcticTable().io();
    Assert.assertFalse(io instanceof RecoverableArcticFileIO);
    getArcticTable().updateProperties().set(TableProperties.ENABLE_TABLE_TRASH, "true").commit();
    io = getArcticTable().io();
    Assert.assertFalse(io instanceof RecoverableArcticFileIO);
    getArcticTable().refresh();
    io = getArcticTable().io();
    Assert.assertTrue(io instanceof RecoverableArcticFileIO);
  }

}
