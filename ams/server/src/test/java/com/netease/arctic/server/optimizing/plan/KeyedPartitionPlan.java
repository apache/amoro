package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.BasicTableTestHelper;
import com.netease.arctic.TableTestHelper;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.catalog.BasicCatalogTestHelper;
import com.netease.arctic.catalog.CatalogTestHelper;
import com.netease.arctic.data.DataTreeNode;
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
public class KeyedPartitionPlan extends AbstractMixedTablePartitionPlan {

  public KeyedPartitionPlan(CatalogTestHelper catalogTestHelper,
                            TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "{0}, {1}")
  public static Object[][] parameters() {
    return new Object[][] {
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, true)},
        {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG),
            new BasicTableTestHelper(true, false)}};
  }

  @Test
  public void testFragmentFiles() {
    List<TaskDescriptor> taskDescriptors = testOptimizeFragmentFiles();
    Assert.assertEquals(4, taskDescriptors.size());
    Map<DataTreeNode, List<TableFileScanHelper.FileScanResult>> baseFiles = scanBaseFilesGroupByNode();
    Assert.assertEquals(4, baseFiles.size());
    for (Map.Entry<DataTreeNode, List<TableFileScanHelper.FileScanResult>> entry : baseFiles.entrySet()) {
      DataTreeNode key = entry.getKey();
      int taskId = 0;
      if (key.getIndex() == 0) {
        taskId = 0;
      } else if (key.getIndex() == 2) {
        taskId = 1;
      } else if (key.getIndex() == 1) {
        taskId = 2;
      } else if (key.getIndex() == 3) {
        taskId = 3;
      } else {
        Assert.fail("unexpected tree node " + key);
      }
      List<IcebergDataFile> files = entry.getValue().stream()
          .map(TableFileScanHelper.FileScanResult::file)
          .collect(Collectors.toList());
      TaskDescriptor actual = taskDescriptors.get(taskId);
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
  }

  @Override
  protected AbstractPartitionPlan getPartitionPlan() {
    return new KeyedTablePartitionPlan(tableRuntime, getArcticTable(), getPartition(), System.currentTimeMillis());
  }
}
