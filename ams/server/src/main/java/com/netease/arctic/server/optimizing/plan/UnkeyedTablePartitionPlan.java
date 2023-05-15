package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.util.BinPacking;

import java.util.List;
import java.util.Map;

public class UnkeyedTablePartitionPlan extends AbstractPartitionPlan {

  public UnkeyedTablePartitionPlan(TableRuntime tableRuntime,
                                   ArcticTable table, String partition, long planTime) {
    super(tableRuntime, table, partition, planTime, new BasicPartitionEvaluator(tableRuntime, partition));
  }

  @Override
  protected OptimizingInputProperties buildTaskProperties() {
    OptimizingInputProperties properties = new OptimizingInputProperties();
    properties.setExecutorFactoryImpl(MixFormatRewriteExecutorFactory.class.getName());
    return properties;
  }

  @Override
  protected AbstractPartitionPlan.TaskSplitter buildTaskSplitter() {
    return new TaskSplitter();
  }

  /**
   * split task with bin-packing
   */
  private class TaskSplitter implements AbstractPartitionPlan.TaskSplitter {

    @Override
    public List<AbstractPartitionPlan.SplitTask> splitTasks(int targetTaskCount) {
      // bin-packing
      List<FileTask> allDataFiles = Lists.newArrayList();
      segmentFiles.forEach((dataFile, deleteFiles) ->
          allDataFiles.add(new FileTask(dataFile, deleteFiles, false)));
      fragmentFiles.forEach((dataFile, deleteFiles) ->
          allDataFiles.add(new FileTask(dataFile, deleteFiles, true)));

      long taskSize = config.getTargetSize();
      Long sum = allDataFiles.stream().map(f -> f.getFile().fileSizeInBytes()).reduce(0L, Long::sum);
      int taskCnt = (int) (sum / taskSize) + 1;
      List<List<FileTask>> packed = new BinPacking.ListPacker<FileTask>(taskSize, taskCnt, true)
          .pack(allDataFiles, f -> f.getFile().fileSizeInBytes());

      // collect
      List<AbstractPartitionPlan.SplitTask> results = Lists.newArrayList();
      for (List<FileTask> fileTasks : packed) {
        Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
        Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();
        fileTasks.stream().filter(FileTask::isFragment)
            .forEach(f -> fragmentFiles.put(f.getFile(), f.getDeleteFiles()));
        fileTasks.stream().filter(FileTask::isSegment)
            .forEach(f -> segmentFiles.put(f.getFile(), f.getDeleteFiles()));
        results.add(new AbstractPartitionPlan.SplitTask(fragmentFiles, segmentFiles));
      }
      return results;
    }
  }

  /**
   * util class for bin-pack
   */
  private static class FileTask {
    private final IcebergDataFile file;
    private final List<IcebergContentFile<?>> deleteFiles;
    private final boolean isFragment;

    public FileTask(IcebergDataFile file, List<IcebergContentFile<?>> deleteFiles, boolean isFragment) {
      this.file = file;
      this.deleteFiles = deleteFiles;
      this.isFragment = isFragment;
    }

    public IcebergDataFile getFile() {
      return file;
    }

    public List<IcebergContentFile<?>> getDeleteFiles() {
      return deleteFiles;
    }

    public boolean isFragment() {
      return isFragment;
    }

    public boolean isSegment() {
      return !isFragment;
    }
  }
}
