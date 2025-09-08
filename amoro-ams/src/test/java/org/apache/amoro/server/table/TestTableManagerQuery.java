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

package org.apache.amoro.server.table;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.ServerTableIdentifier;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.config.TableConfiguration;
import org.apache.amoro.optimizing.OptimizingType;
import org.apache.amoro.optimizing.plan.AbstractOptimizingEvaluator;
import org.apache.amoro.server.dashboard.model.TableOptimizingInfo;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingStatus;
import org.apache.amoro.server.persistence.PersistentBase;
import org.apache.amoro.server.persistence.TableRuntimeMeta;
import org.apache.amoro.server.persistence.mapper.TableMetaMapper;
import org.apache.amoro.server.persistence.mapper.TableRuntimeMapper;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.TableSummary;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestTableManagerQuery extends AMSTableTestBase {

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)}
    };
  }

  public TestTableManagerQuery(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper, false);
  }

  /**
   * Test the logic for {@link TableManager#queryTableOptimizingInfo(String, String, String, List,
   * int, int)}.
   */
  @Test
  public void testGetRuntimes() {
    String catalog = "catalog";
    String db1 = "db1";

    String optimizerGroup1 = "opGroup1";
    Persistent persistent = new Persistent();
    // 1 add some tables

    // table in opGroup1
    Map<String, String> properties = new HashMap<>();
    properties.put(TableProperties.SELF_OPTIMIZING_GROUP, optimizerGroup1);

    // 1.1 add tables with IDLE status
    // the status will be OptimizingStatus.IDLE default
    String idle1InGroup1 = "idle1InGroup1";
    DefaultTableRuntime idle1 =
        persistent.newTableRuntime(catalog, db1, idle1InGroup1, TableFormat.ICEBERG, properties);

    // the status will be OptimizingStatus.IDLE default
    String idle2InGroup1 = "idle2InGroup1";
    DefaultTableRuntime idle2 =
        persistent.newTableRuntime(catalog, db1, idle2InGroup1, TableFormat.ICEBERG, properties);

    // 1.2 add tables with PENDING status
    String pending1InGroup1 = "pending1InGroup1";
    DefaultTableRuntime pending1 =
        persistent.newTableRuntime(catalog, db1, pending1InGroup1, TableFormat.ICEBERG, properties);
    // update status
    pending1.setPendingInput(new AbstractOptimizingEvaluator.PendingInput());

    String pending2InGroup1 = "pending2InGroup1";
    DefaultTableRuntime pending2 =
        persistent.newTableRuntime(catalog, db1, pending2InGroup1, TableFormat.ICEBERG, properties);
    // update status
    pending2.setPendingInput(new AbstractOptimizingEvaluator.PendingInput());

    // 1.3 add tables with PLANNING status
    String db2 = "db2";
    String plan1InGroup1 = "plan1InGroup1";
    DefaultTableRuntime plan1 =
        persistent.newTableRuntime(catalog, db2, plan1InGroup1, TableFormat.ICEBERG, properties);
    plan1.beginPlanning();

    String plan2InGroup1 = "plan2InGroup1";
    DefaultTableRuntime plan2 =
        persistent.newTableRuntime(catalog, db2, plan2InGroup1, TableFormat.ICEBERG, properties);
    plan2.beginPlanning();

    // 1.4 add tables with COMMITTING status
    String committing1InGroup1 = "committing1InGroup1";
    DefaultTableRuntime committing1 =
        persistent.newTableRuntime(
            catalog, db2, committing1InGroup1, TableFormat.ICEBERG, properties);
    committing1.beginCommitting();

    String commiting2InGroup1 = "committing2InGroup1";
    DefaultTableRuntime committing2 =
        persistent.newTableRuntime(
            catalog, db2, commiting2InGroup1, TableFormat.ICEBERG, properties);
    committing2.beginCommitting();

    // 1.5 add tables with MINOR_OPTIMIZING status
    String minor1InGroup1 = "minor1InGroup1";
    DefaultTableRuntime minor1 =
        persistent.newTableRuntime(catalog, db2, minor1InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process = mock(OptimizingProcess.class);
    doReturn(1L).when(process).getProcessId();
    doReturn(OptimizingType.MINOR).when(process).getOptimizingType();
    minor1.beginProcess(process);

    String minor2InGroup1 = "minor2InGroup1";
    DefaultTableRuntime minor2 =
        persistent.newTableRuntime(catalog, db2, minor2InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process2 = mock(OptimizingProcess.class);
    doReturn(2L).when(process2).getProcessId();
    doReturn(OptimizingType.MINOR).when(process2).getOptimizingType();
    minor2.beginProcess(process2);

    // 1.6 add tables with MAJOR_OPTIMIZING status
    String major1InGroup1 = "major1InGroup1";
    DefaultTableRuntime major1 =
        persistent.newTableRuntime(catalog, db1, major1InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process3 = mock(OptimizingProcess.class);
    doReturn(3L).when(process3).getProcessId();
    doReturn(OptimizingType.MAJOR).when(process3).getOptimizingType();
    major1.beginProcess(process3);

    String major2InGroup1 = "major2InGroup1";
    DefaultTableRuntime major2 =
        persistent.newTableRuntime(catalog, db1, major2InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process4 = mock(OptimizingProcess.class);
    doReturn(4L).when(process4).getProcessId();
    doReturn(OptimizingType.MAJOR).when(process4).getOptimizingType();
    major2.beginProcess(process4);

    // 1.7 add tables with FULL_OPTIMIZING status
    String full1InGroup1 = "full1InGroup1";
    DefaultTableRuntime full1 =
        persistent.newTableRuntime(catalog, db1, full1InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process5 = mock(OptimizingProcess.class);
    doReturn(5L).when(process5).getProcessId();
    doReturn(OptimizingType.FULL).when(process5).getOptimizingType();
    full1.beginProcess(process5);

    String full2InGroup1 = "full2InGroup1";
    DefaultTableRuntime full2 =
        persistent.newTableRuntime(catalog, db1, full2InGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process6 = mock(OptimizingProcess.class);
    doReturn(6L).when(process6).getProcessId();
    doReturn(OptimizingType.FULL).when(process6).getOptimizingType();
    full2.beginProcess(process6);

    // 1.8 add tables in other group with MINOR_OPTIMIZING status
    // table in other group.
    String opGroup2 = "opGroup2-other";
    properties.put(TableProperties.SELF_OPTIMIZING_GROUP, opGroup2);
    String minor1InOtherGroup1 = "minor1-InOtherGroup";
    DefaultTableRuntime minor1Other =
        persistent.newTableRuntime(
            catalog, db1, minor1InOtherGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process7 = mock(OptimizingProcess.class);
    doReturn(7L).when(process7).getProcessId();
    doReturn(OptimizingType.MINOR).when(process7).getOptimizingType();
    minor1Other.beginProcess(process7);

    String minor2InOtherGroup1 = "minor2-InOtherGroup";
    DefaultTableRuntime minor2Other =
        persistent.newTableRuntime(
            catalog, db1, minor2InOtherGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process8 = mock(OptimizingProcess.class);
    doReturn(8L).when(process8).getProcessId();
    doReturn(OptimizingType.MINOR).when(process8).getOptimizingType();
    minor2Other.beginProcess(process8);

    String minor3InOtherGroup1 = "minor3-InOtherGroup";
    DefaultTableRuntime minor3Other =
        persistent.newTableRuntime(
            catalog, db1, minor3InOtherGroup1, TableFormat.ICEBERG, properties);
    OptimizingProcess process9 = mock(OptimizingProcess.class);
    doReturn(9L).when(process9).getProcessId();
    doReturn(OptimizingType.MINOR).when(process9).getOptimizingType();
    minor3Other.beginProcess(process9);

    // 2 test and assert the result
    // 2.1 only optimize group filter set
    Pair<List<TableOptimizingInfo>, Integer> res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, null, null, Collections.emptyList(), 10, 0);
    Integer expectedTotalinGroup1 = 14;
    Assert.assertEquals(expectedTotalinGroup1, res.getRight());
    Assert.assertEquals(10, res.getLeft().size());

    // 2.2 set optimize group and db filter
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, null, Collections.emptyList(), 5, 0);
    // there are 8 tables in db1 in optimizerGroup1
    Integer expectedTotalGroup1Db1 = 8;
    Assert.assertEquals(expectedTotalGroup1Db1, res.getRight());
    Assert.assertEquals(5, res.getLeft().size());

    // 2.3 set optimize group and table filter
    // there are 3 tables with suffix "-InOtherGroup" in opGroup2
    String fuzzyDbName = "InOtherGroup";
    res =
        tableManager()
            .queryTableOptimizingInfo(opGroup2, null, fuzzyDbName, Collections.emptyList(), 2, 0);
    Integer expectedTotalWithFuzzyDbName = 3;
    Assert.assertEquals(expectedTotalWithFuzzyDbName, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    res =
        tableManager()
            .queryTableOptimizingInfo(opGroup2, null, fuzzyDbName, Collections.emptyList(), 5, 0);
    Assert.assertEquals(expectedTotalWithFuzzyDbName, res.getRight());
    // there are only 3 tables with the suffix in opGroup2
    Assert.assertEquals(3, res.getLeft().size());

    // 2.4 set optimize group and status filter, with only one status
    List<Integer> statusCode = new ArrayList<>();
    statusCode.add(OptimizingStatus.MAJOR_OPTIMIZING.getCode());
    res = tableManager().queryTableOptimizingInfo(optimizerGroup1, null, null, statusCode, 10, 0);
    Integer expectedTotalInGroup1WithMajorStatus = 2;
    Assert.assertEquals(expectedTotalInGroup1WithMajorStatus, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    // 2.5 set optimize group and status filter with two statuses
    statusCode.clear();
    statusCode.add(OptimizingStatus.MINOR_OPTIMIZING.getCode());
    statusCode.add(OptimizingStatus.MAJOR_OPTIMIZING.getCode());
    res = tableManager().queryTableOptimizingInfo(optimizerGroup1, null, null, statusCode, 3, 0);
    Integer expectedTotalInGroup1WithMinorMajorStatus = 4;
    Assert.assertEquals(expectedTotalInGroup1WithMinorMajorStatus, res.getRight());
    Assert.assertEquals(3, res.getLeft().size());

    // 2.6 all filter set which contains result
    statusCode.clear();
    statusCode.add(OptimizingStatus.PENDING.getCode());
    statusCode.add(OptimizingStatus.FULL_OPTIMIZING.getCode());
    String tableFilter = "pending";
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, tableFilter, statusCode, 10, 0);
    Integer expectedTotalInGroup1InDb1WithTableFilterAndStatus = 2;
    Assert.assertEquals(expectedTotalInGroup1InDb1WithTableFilterAndStatus, res.getRight());
    Assert.assertEquals(2, res.getLeft().size());

    // 2.7 all filters with no result
    statusCode.clear();
    statusCode.add(OptimizingStatus.PENDING.getCode());
    statusCode.add(OptimizingStatus.FULL_OPTIMIZING.getCode());
    String wrongTableFilter2 = "noTableWithName";
    res =
        tableManager()
            .queryTableOptimizingInfo(optimizerGroup1, db1, wrongTableFilter2, statusCode, 10, 0);
    Assert.assertEquals(0, (int) res.getRight());
    Assert.assertTrue(res.getLeft().isEmpty());
  }

  private class Persistent extends PersistentBase {

    public ServerTableIdentifier newIdentifier(
        String catalog, String database, String tableName, TableFormat format) {
      ServerTableIdentifier identifier =
          ServerTableIdentifier.of(catalog, database, tableName, format);
      doAs(TableMetaMapper.class, mapper -> mapper.insertTable(identifier));
      return identifier;
    }

    public DefaultTableRuntime newTableRuntime(
        String catalog,
        String database,
        String tableName,
        TableFormat format,
        Map<String, String> properties) {
      ServerTableIdentifier identifier = newIdentifier(catalog, database, tableName, format);
      TableRuntimeMeta meta = new TableRuntimeMeta();
      meta.setTableId(identifier.getId());
      meta.setTableConfig(properties);
      TableConfiguration configuration = TableConfigurations.parseTableConfig(properties);
      meta.setStatusCode(OptimizingStatus.IDLE.getCode());
      meta.setGroupName(configuration.getOptimizingConfig().getOptimizerGroup());
      meta.setTableSummary(new TableSummary());
      doAs(TableRuntimeMapper.class, mapper -> mapper.insertRuntime(meta));
      DefaultTableRuntimeStore store =
          new DefaultTableRuntimeStore(
              identifier, meta, DefaultTableRuntime.REQUIRED_STATES, Collections.emptyList());
      return new DefaultTableRuntime(store);
    }
  }
}
