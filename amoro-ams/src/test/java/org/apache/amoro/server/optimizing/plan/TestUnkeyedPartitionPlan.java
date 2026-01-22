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
import org.apache.amoro.optimizing.MixedIcebergRewriteExecutorFactory;
import org.apache.amoro.optimizing.RewriteStageTask;
import org.apache.amoro.optimizing.TaskProperties;
import org.apache.amoro.optimizing.plan.AbstractPartitionPlan;
import org.apache.amoro.optimizing.plan.MixedIcebergPartitionPlan;
import org.apache.amoro.optimizing.scan.TableFileScanHelper;
import org.apache.amoro.optimizing.scan.UnkeyedTableFileScanHelper;
import org.apache.amoro.server.utils.IcebergTableUtil;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
public class TestUnkeyedPartitionPlan extends MixedTablePlanTestBase {

  public TestUnkeyedPartitionPlan(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(false, true)
      },
      {
        new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
        new BasicTableTestHelper(false, false)
      }
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

  @Test
  public void testRewriteAllAvroWithFragmentFile() {
    testRewriteAllAvro(
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT
            / TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT);
  }

  @Test
  public void testRewriteAllAvroWithUndersizedSegmentFile() {
    testRewriteAllAvro(
        TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT
                / TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT
            + 1);
  }

  @Test
  public void testRewriteAllAvroWithTargetSizeReachedFile() {
    testRewriteAllAvro(
        (long)
                (TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT
                    * TableProperties.SELF_OPTIMIZING_MIN_TARGET_SIZE_RATIO_DEFAULT)
            + 1);
  }

  @Test
  public void testRewriteAllAvroWithTargetSizeReachedFile2() {
    testRewriteAllAvro(TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT + 1);
  }

  private void testRewriteAllAvro(long fileSizeBytes) {
    closeFullOptimizingInterval();
    updateTableProperty(TableProperties.SELF_OPTIMIZING_REWRITE_ALL_AVRO, "false");
    DataFile avroFile = appendAvroDataFile(fileSizeBytes);
    Assert.assertTrue(planWithCurrentFiles().isEmpty());

    updateTableProperty(TableProperties.SELF_OPTIMIZING_REWRITE_ALL_AVRO, "true");
    List<RewriteStageTask> tasks = planWithCurrentFiles();
    Assert.assertEquals(1, tasks.size());
    assertTask(
        tasks.get(0),
        Collections.singletonList(avroFile),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());

    updateTableProperty(TableProperties.DEFAULT_FILE_FORMAT, TableProperties.FILE_FORMAT_AVRO);
    updateTableProperty(TableProperties.SELF_OPTIMIZING_REWRITE_ALL_AVRO, "true");
    Assert.assertTrue(planWithCurrentFiles().isEmpty());
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return new MixedIcebergPartitionPlan(
        getTableRuntime().getTableIdentifier(),
        getMixedTable(),
        getTableRuntime().getOptimizingConfig(),
        getPartition(),
        System.currentTimeMillis(),
        getTableRuntime().getLastMinorOptimizingTime(),
        getTableRuntime().getLastFullOptimizingTime(),
        getTableRuntime().getLastMajorOptimizingTime());
  }

  @Override
  protected TableFileScanHelper getTableFileScanHelper() {
    long baseSnapshotId = IcebergTableUtil.getSnapshotId(getMixedTable(), true);
    return new UnkeyedTableFileScanHelper(getMixedTable(), baseSnapshotId);
  }

  @Override
  protected UnkeyedTable getMixedTable() {
    return super.getMixedTable().asUnkeyedTable();
  }

  @Override
  protected Map<String, String> buildTaskProperties() {
    Map<String, String> properties = Maps.newHashMap();
    properties.put(
        TaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
        MixedIcebergRewriteExecutorFactory.class.getName());
    return properties;
  }
}
