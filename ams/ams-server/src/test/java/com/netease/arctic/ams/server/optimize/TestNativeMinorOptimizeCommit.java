package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeStatus;
import com.netease.arctic.ams.server.model.BaseOptimizeTask;
import com.netease.arctic.ams.server.model.BaseOptimizeTaskRuntime;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.ams.server.utils.JDBCSqlSessionFactoryProvider;
import com.netease.arctic.utils.SerializationUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileScanTask;
import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@PrepareForTest({
    JDBCSqlSessionFactoryProvider.class
})
@PowerMockIgnore({"org.apache.logging.log4j.*", "javax.management.*", "org.apache.http.conn.ssl.*",
    "com.amazonaws.http.conn.ssl.*",
    "javax.net.ssl.*", "org.apache.hadoop.*", "javax.*", "com.sun.org.apache.*", "org.apache.xerces.*"})
public class TestNativeMinorOptimizeCommit extends TestNativeIcebergBase {
  @Test
  public void testNoPartitionTableMinorOptimizeCommit() throws Exception {
    icebergTable.asUnkeyedTable().updateProperties()
        .set(com.netease.arctic.table.TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD, "1000")
        .commit();
    List<DataFile> dataFiles = insertDataFiles(icebergTable.asUnkeyedTable());
    insertEqDeleteFiles(icebergTable.asUnkeyedTable());
    insertPosDeleteFiles(icebergTable.asUnkeyedTable(), dataFiles);
    Map<DataFile, List<DeleteFile>> dataFileListMap = new HashMap<>();
    for (FileScanTask fileScanTask : icebergTable.asUnkeyedTable().newScan().planFiles()) {
      dataFileListMap.put(fileScanTask.file(), fileScanTask.deletes());
    }
    Set<String> oldDataFilesPath = new HashSet<>();
    Set<String> oldDeleteFilesPath = new HashSet<>();
    icebergTable.asUnkeyedTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          if (fileScanTask.file().fileSizeInBytes() <= 1000) {
            oldDataFilesPath.add((String) fileScanTask.file().path());
            fileScanTask.deletes().forEach(deleteFile -> oldDeleteFilesPath.add((String) deleteFile.path()));
          }
        });

    NativeMinorOptimizePlan optimizePlan = new NativeMinorOptimizePlan(icebergTable,
        new TableOptimizeRuntime(icebergTable.id()), dataFileListMap,
        new HashMap<>(), 1, System.currentTimeMillis());
    List<BaseOptimizeTask> tasks = optimizePlan.plan();

    List<DataFile> resultFiles = insertOptimizeTargetDataFiles(icebergTable.asUnkeyedTable());
    List<OptimizeTaskItem> taskItems = tasks.stream().map(task -> {
      BaseOptimizeTaskRuntime optimizeRuntime = new BaseOptimizeTaskRuntime(task.getTaskId());
      optimizeRuntime.setPreparedTime(System.currentTimeMillis());
      optimizeRuntime.setStatus(OptimizeStatus.Prepared);
      optimizeRuntime.setReportTime(System.currentTimeMillis());
      if (resultFiles != null) {
        optimizeRuntime.setNewFileSize(resultFiles.get(0).fileSizeInBytes());
        optimizeRuntime.setTargetFiles(resultFiles.stream().map(SerializationUtil::toByteBuffer).collect(Collectors.toList()));
      }
      List<ByteBuffer> finalTargetFiles = optimizeRuntime.getTargetFiles();
      finalTargetFiles.addAll(task.getInsertFiles());
      optimizeRuntime.setTargetFiles(finalTargetFiles);
      optimizeRuntime.setNewFileCnt(finalTargetFiles.size());
      // 1min
      optimizeRuntime.setCostTime(60 * 1000);
      return new OptimizeTaskItem(task, optimizeRuntime);
    }).collect(Collectors.toList());
    Map<String, List<OptimizeTaskItem>> partitionTasks = taskItems.stream()
        .collect(Collectors.groupingBy(taskItem -> taskItem.getOptimizeTask().getPartition()));

    NativeOptimizeCommit optimizeCommit = new NativeOptimizeCommit(icebergTable, partitionTasks);
    optimizeCommit.commit(icebergTable.asUnkeyedTable().currentSnapshot().snapshotId());

    Set<String> newDataFilesPath = new HashSet<>();
    Set<String> newDeleteFilesPath = new HashSet<>();
    icebergTable.asUnkeyedTable().newScan().planFiles()
        .forEach(fileScanTask -> {
          newDataFilesPath.add((String) fileScanTask.file().path());
          fileScanTask.deletes().forEach(deleteFile -> newDeleteFilesPath.add((String) deleteFile.path()));
        });

    Assert.assertNotEquals(oldDataFilesPath, newDataFilesPath);
    Assert.assertEquals(oldDeleteFilesPath, newDeleteFilesPath);
  }
}
