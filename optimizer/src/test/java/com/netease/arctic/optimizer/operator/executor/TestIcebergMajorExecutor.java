package com.netease.arctic.optimizer.operator.executor;

import com.google.common.collect.Iterables;
import com.netease.arctic.ams.api.OptimizeTaskId;
import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.optimizer.OptimizerConfig;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Schema;
import org.apache.iceberg.data.GenericAppenderFactory;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.data.Record;
import org.apache.iceberg.deletes.EqualityDeleteWriter;
import org.apache.iceberg.deletes.PositionDelete;
import org.apache.iceberg.deletes.PositionDeleteWriter;
import org.apache.iceberg.encryption.EncryptedOutputFile;
import org.apache.iceberg.io.DataWriter;
import org.apache.iceberg.io.OutputFileFactory;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestIcebergMajorExecutor extends TestIcebergExecutorBase {

  @Test
  public void testCompactDataFiles() throws Exception {
    // sequence 0
    IcebergContentFile dataFile1 = IcebergContentFile.of(insertDataFiles(10, 0), 0);

    // sequence 1
    IcebergContentFile posDeleteFile = IcebergContentFile.of(insertPosDeleteFiles(dataFile1.asDataFile(), 1, 5), 1);
    IcebergContentFile equDeleteFile = IcebergContentFile.of(insertEqDeleteFiles(2, 6, 20, 23), 1);
    IcebergContentFile dataFile2 = IcebergContentFile.of(insertDataFiles(8, 10), 1);

    // 2 small data files, 1 positional delete file, 1 equality delete file
    NodeTask nodeTask = constructNodeTask(
        Lists.newArrayList(dataFile1, dataFile2),
        Lists.newArrayList(),
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
    Assert.assertEquals(14, resultFile.recordCount());
  }
}
