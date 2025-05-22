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

package org.apache.amoro.server.optimizing.plan;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.optimizing.IcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.OptimizingInputProperties;
import org.apache.amoro.optimizing.plan.AbstractPartitionPlan;
import org.apache.amoro.optimizing.plan.IcebergPartitionPlan;
import org.apache.amoro.optimizing.scan.IcebergTableFileScanHelper;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.server.table.DefaultTableRuntime;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Map;

@RunWith(Parameterized.class)
public class TestIcebergPartitionPlan extends TestUnkeyedPartitionPlan {
  public TestIcebergPartitionPlan(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, true)},
      {new BasicCatalogTestHelper(TableFormat.ICEBERG), new BasicTableTestHelper(false, false)}
    };
  }

  @Test
  public void testFragmentFiles() {
    testFragmentFilesBase();
  }

  @Test
  public void testSegmentFiles() {
    testSegmentFilesBase();
  }

  @Test
  public void testWithDeleteFiles() {
    testWithDeleteFilesBase();
  }

  @Test
  public void testOnlyOneFragmentFiles() {
    testOnlyOneFragmentFileBase();
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    DefaultTableRuntime tableRuntime = getTableRuntime();
    return new IcebergPartitionPlan(
        tableRuntime.getTableIdentifier(),
        tableRuntime.getOptimizingState().getOptimizingConfig(),
        getMixedTable(),
        getPartition(),
        System.currentTimeMillis(),
        tableRuntime.getOptimizingState().getLastMinorOptimizingTime(),
        tableRuntime.getOptimizingState().getLastFullOptimizingTime());
  }

  @Override
  protected TableFileScanHelper getTableFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable(), true);
    return new IcebergTableFileScanHelper(getMixedTable(), baseSnapshotId);
  }

  protected Map<String, String> buildTaskProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        OptimizingInputProperties.TASK_EXECUTOR_FACTORY_IMPL,
        IcebergRewriteExecutorFactory.class.getName());
    return properties;
  }
}
