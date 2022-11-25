package com.netease.arctic.optimizer.operator.executor;

import com.google.common.collect.Iterables;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.optimizer.OptimizerConfig;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

public class TestIcebergMinorExecutor extends TestIcebergExecutorBase {

  @Test
  public void testCompactSmallDataFiles() throws Exception {

    // sequence 0
    IcebergContentFile smallDataFile1 = IcebergContentFile.of(insertDataFiles(10, 0), 0);
    IcebergContentFile smallDataFile2 = IcebergContentFile.of(insertDataFiles(8, 10), 1);

    // 2 small data files
    NodeTask nodeTask = constructNodeTask(
        Lists.newArrayList(),
        Lists.newArrayList(smallDataFile1, smallDataFile2),
        Lists.newArrayList(),
        Lists.newArrayList());

    String[] arg = new String[0];
    OptimizerConfig optimizerConfig = new OptimizerConfig(arg);
    optimizerConfig.setOptimizerId("UnitTest");
    IcebergMajorExecutor icebergMajorExecutor = new IcebergMajorExecutor(nodeTask, icebergTable,
        System.currentTimeMillis(), optimizerConfig);
    OptimizeTaskResult<DataFile> result = icebergMajorExecutor.execute();
    Assert.assertEquals(Iterables.size(result.getTargetFiles()), 1);
    ContentFile<?> resultFile = result.getTargetFiles().iterator().next();
    Assert.assertEquals(FileContent.DATA, resultFile.content());
    Assert.assertEquals(18, resultFile.recordCount());
  }

  @Test
  public void testCompactSmallDataFilesWithDeleteFiles() throws Exception {
    // sequence 0
    IcebergContentFile equDeleteFile = IcebergContentFile.of(insertEqDeleteFiles(2, 6), 0);
    IcebergContentFile smallDataFile1 = IcebergContentFile.of(insertDataFiles(10, 0), 0);

    // sequence 1
    IcebergContentFile posDeleteFile = IcebergContentFile.of(insertPosDeleteFiles(smallDataFile1.asDataFile(), 1, 5), 1);
    IcebergContentFile smallDataFile2 = IcebergContentFile.of(insertDataFiles(8, 10), 1);

    // 2 small data files, 1 positional delete file, 1 equality delete file
    NodeTask nodeTask = constructNodeTask(
        Lists.newArrayList(),
        Lists.newArrayList(smallDataFile1, smallDataFile2),
        Lists.newArrayList(posDeleteFile),
        Lists.newArrayList(equDeleteFile));

    String[] arg = new String[0];
    OptimizerConfig optimizerConfig = new OptimizerConfig(arg);
    optimizerConfig.setOptimizerId("UnitTest");
    IcebergMajorExecutor icebergMajorExecutor = new IcebergMajorExecutor(nodeTask, icebergTable,
        System.currentTimeMillis(), optimizerConfig);
    OptimizeTaskResult<DataFile> result = icebergMajorExecutor.execute();
    Assert.assertEquals(Iterables.size(result.getTargetFiles()), 1);
    ContentFile<?> resultFile = result.getTargetFiles().iterator().next();
    Assert.assertEquals(FileContent.DATA, resultFile.content());
    Assert.assertEquals(16, resultFile.recordCount());
  }

  @Test
  public void testCompactBigDataFiles() throws Exception {

    // sequence 0
    IcebergContentFile equDeleteFile1 = IcebergContentFile.of(insertEqDeleteFiles(2, 6), 0);
    IcebergContentFile dataFile1 = IcebergContentFile.of(insertDataFiles(10, 0), 0);
    IcebergContentFile dataFile2 = IcebergContentFile.of(insertDataFiles(8, 10), 0);

    // sequence 1
    IcebergContentFile posDeleteFile = IcebergContentFile.of(insertPosDeleteFiles(dataFile1.asDataFile(), 1, 5), 1);
    IcebergContentFile equDeleteFile2 = IcebergContentFile.of(insertEqDeleteFiles(3, 7, 10, 13), 1);

    // 2 data file, 2 quality delete file, 1 positional delete file
    NodeTask nodeTask = constructNodeTask(
        Lists.newArrayList(dataFile1, dataFile2),
        Lists.newArrayList(),
        Lists.newArrayList(equDeleteFile1, equDeleteFile2),
        Lists.newArrayList(posDeleteFile));

    String[] arg = new String[0];
    OptimizerConfig optimizerConfig = new OptimizerConfig(arg);
    optimizerConfig.setOptimizerId("UnitTest");
    IcebergMajorExecutor icebergMajorExecutor = new IcebergMajorExecutor(nodeTask, icebergTable,
        System.currentTimeMillis(), optimizerConfig);
    OptimizeTaskResult<DataFile> result = icebergMajorExecutor.execute();
    Assert.assertEquals(Iterables.size(result.getTargetFiles()), 2);
    result.getTargetFiles().forEach(file -> {
      Assert.assertEquals(FileContent.POSITION_DELETES, file.content());
      Assert.assertEquals(2, file.recordCount());
    });
  }
}
