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
public class UnkeyedPartitionPlan extends AbstractMixedTablePartitionPlan {

  public UnkeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
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