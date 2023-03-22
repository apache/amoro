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

package com.netease.arctic.io;

import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.TableTestBase;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.ams.api.properties.MetaTableProperties;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class BuildTableTrashManagerTest extends TableTestBase {
  
  private final boolean keyedTable;

  public BuildTableTrashManagerTest(boolean keyedTable) {
    this.keyedTable = keyedTable;
  }

  @Parameterized.Parameters(name = "keyedTable = {0}")
  public static Object[][] parameters() {
    return new Object[][] {{true}, {false}};
  }
  
  private ArcticTable getArcticTable() {
    if (keyedTable) {
      return testKeyedTable;
    } else {
      return testTable;
    }
  }

  @Test
  public void build() throws TException {
    TableIdentifier id = getArcticTable().id();
    TableTrashManager trashManager = TableTrashManagers.build(getArcticTable());
    Assert.assertEquals(id, trashManager.tableId());
    Assert.assertEquals(getTableTrashLocation(id), trashManager.getTrashLocation());
  }

  private String getTableTrashLocation(TableIdentifier id) throws TException {
    TableMeta table = new PooledAmsClient(AMS.getUrl()).getTable(id.buildTableIdentifier());
    String tableLocation = table.getLocations().get(MetaTableProperties.LOCATION_KEY_TABLE);
    return String.format("%s/%s", tableLocation, TableTrashManagers.DEFAULT_TRASH_DIR);
  }

}
