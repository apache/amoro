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

package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.scan.TableFileScanHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(Parameterized.class)
public class TestUnkeyedPartitionPlan extends MixedTablePartitionPlanTestBase {

  public TestUnkeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
                                  TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(false, false)}};
  }

  @Test
  public void testFragmentFiles() {
    List<TaskDescriptor> taskDescriptors = testOptimizeFragmentFiles();
    Assert.assertEquals(1, taskDescriptors.size());
    List<TableFileScanHelper.FileScanResult> baseFiles = scanBaseFiles();
    Assert.assertEquals(2, baseFiles.size());
    List<IcebergDataFile> files = baseFiles.stream()
        .map(TableFileScanHelper.FileScanResult::file)
        .collect(Collectors.toList());
    TaskDescriptor actual = taskDescriptors.get(0);
    RewriteFilesInput rewriteFilesInput = new RewriteFilesInput(files.toArray(new IcebergDataFile[0]),
        Collections.emptySet().toArray(new IcebergDataFile[0]),
        Collections.emptySet().toArray(new IcebergContentFile[0]),
        Collections.emptySet().toArray(new IcebergContentFile[0]), getArcticTable());

    Map<String, String> properties = Maps.newHashMap();
    properties.put(OptimizingInputProperties.TASK_EXECUTOR_FACTORY_IMPL,
        MixFormatRewriteExecutorFactory.class.getName());
    TaskDescriptor expect = new TaskDescriptor(getPartition(), rewriteFilesInput, properties);
    assertTask(expect, actual);
  }

  protected AbstractPartitionPlan getPartitionPlan() {
    return new UnkeyedTablePartitionPlan(tableRuntime, getArcticTable(), getPartition(), System.currentTimeMillis());
  }
}