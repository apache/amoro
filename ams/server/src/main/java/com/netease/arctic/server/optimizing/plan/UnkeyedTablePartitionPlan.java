package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.hive.optimizing.MixFormatRewriteExecutorFactory;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.commons.collections.CollectionUtils;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.BinPacking;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnkeyedTablePartitionPlan extends AbstractPartitionPlan {

  private boolean findAnyDelete = false;

  public UnkeyedTablePartitionPlan(TableRuntime tableRuntime,
                                   ArcticTable table, String partition, long planTime) {
    super(tableRuntime, table, partition, planTime);
  }

  @Override
  public void addFile(IcebergDataFile dataFile, List<IcebergContentFile<?>> deletes) {
    if (!deletes.isEmpty()) {
      findAnyDelete = true;
    }
    if (isFragmentFile(dataFile)) {
      fragmentFiles.put(dataFile, deletes);
      fragmentFileSize += dataFile.fileSizeInBytes();
      smallFileCount += 1;
    } else {
      segmentFiles.put(dataFile, deletes);
      segmentFileSize += dataFile.fileSizeInBytes();
    }
    for (IcebergContentFile<?> deleteFile : deletes) {
      if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
        throw new UnsupportedOperationException("optimizing unkeyed table not support equality-delete");
      }
    }
  }

  protected boolean isFragmentFile(IcebergDataFile dataFile) {
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile.internalFile();
    if (file.type() == DataFileType.BASE_FILE) {
      return dataFile.fileSizeInBytes() <= fragmentSize;
    } else {
      throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
    }
  }

  protected boolean findAnyDelete() {
    return findAnyDelete;
  }

  protected boolean canRewriteFile(IcebergDataFile dataFile) {
    return true;
  }

  protected boolean shouldFullOptimizing(IcebergDataFile dataFile, List<IcebergContentFile<?>> deleteFiles) {
    if (config.isFullRewriteAllFiles()) {
      return true;
    } else {
      return !deleteFiles.isEmpty() || dataFile.fileSizeInBytes() < config.getTargetSize() * 0.9;
    }
  }

  protected void fillTaskProperties(OptimizingInputProperties properties) {
    properties.setExecutorFactoryImpl(MixFormatRewriteExecutorFactory.class.getName());
  }

  @Override
  protected AbstractPartitionPlan.TaskSplitter buildTaskSplitter() {
    return new TaskSplitter();
  }

  public boolean partitionShouldFullOptimizing() {
    return config.getFullTriggerInterval() > 0 &&
        planTime - tableRuntime.getLastFullOptimizingTime() > config.getFullTriggerInterval();
  }

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

  private class SplitTask {
    private final Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    private final Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();
    private final Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    long cost = -1;

    public SplitTask(List<FileTask> allFiles) {
      if (partitionShouldFullOptimizing()) {
        allFiles.forEach(f -> {
          if (shouldFullOptimizing(f.getFile(), f.getDeleteFiles())) {
            rewriteDataFiles.add(f.getFile());
            deleteFiles.addAll(f.getDeleteFiles());
          }
        });
      } else {
        allFiles.stream().filter(FileTask::isSegment).forEach(f -> {
          IcebergDataFile icebergFile = f.getFile();
          List<IcebergContentFile<?>> deleteFileSet = f.getDeleteFiles();
          if (canRewriteFile(icebergFile) &&
              getRecordCount(deleteFileSet) >= icebergFile.recordCount() * config.getMajorDuplicateRatio()) {
            rewriteDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          } else if (equalityRelatedFiles.contains(icebergFile)) {
            // for unkeyed table, equalityRelatedFiles is supposed to be empty
            rewritePosDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          } else {
            boolean posDeleteExist = deleteFileSet.stream()
                .anyMatch(file -> file.content() == FileContent.POSITION_DELETES);
            if (posDeleteExist) {
              rewritePosDataFiles.add(icebergFile);
              deleteFiles.addAll(deleteFileSet);
            }
          }
        });
        allFiles.stream().filter(FileTask::isFragment).forEach(f -> {
          rewriteDataFiles.add(f.getFile());
          deleteFiles.addAll(f.getDeleteFiles());
        });
      }
    }

    private long getRecordCount(List<IcebergContentFile<?>> files) {
      return files.stream().mapToLong(ContentFile::recordCount).sum();
    }

    public boolean hasRewritePosDataFiles() {
      return rewritePosDataFiles.size() > 0;
    }

    public long getCost() {
      if (cost < 0) {
        cost = rewriteDataFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum() * 4 +
            rewritePosDataFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum() / 10 +
            deleteFiles.stream().mapToLong(file -> file.fileSizeInBytes()).sum();
      }
      return cost;
    }

    public boolean isEmpty() {
      return rewriteDataFiles.isEmpty() && rewritePosDataFiles.isEmpty();
    }

    public boolean isNotEmpty() {
      return !isEmpty();
    }

    public TaskDescriptor getTask() {
      if (isEmpty()) {
        return null;
      }
      RewriteFilesInput input = new RewriteFilesInput(
          rewriteDataFiles.toArray(new IcebergDataFile[rewriteDataFiles.size()]),
          rewritePosDataFiles.toArray(new IcebergDataFile[rewritePosDataFiles.size()]),
          deleteFiles.toArray(new IcebergContentFile[deleteFiles.size()]),
          tableObject);
      OptimizingInputProperties properties = new OptimizingInputProperties();
      fillTaskProperties(properties);
      return new TaskDescriptor(partition, input, properties.getProperties());
    }

    // TODO
    public OptimizingType getOptimizingType() {
      if (partitionShouldFullOptimizing()) {
        return OptimizingType.FULL_MAJOR;
      }
      return OptimizingType.MAJOR;
    }
  }

  private class TaskSplitter implements AbstractPartitionPlan.TaskSplitter {

    private final List<SplitTask> splitTasks;

    private long cost = -1;

    public TaskSplitter() {
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
      splitTasks = Lists.newArrayList();
      for (List<FileTask> files : packed) {
        if (CollectionUtils.isNotEmpty(files)) {
          splitTasks.add(new SplitTask(files));
        }
      }
    }

    public boolean isNecessary() {
      return partitionShouldFullOptimizing() ||
          smallFileCount >= config.getMinorLeastFileCount() ||
          config.getMinorLeastInterval() > 0 &&
              planTime - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval() &&
              splitTasks.stream().anyMatch(SplitTask::hasRewritePosDataFiles);
    }

    public long getCost() {
      if (cost < 0) {
        cost = splitTasks.stream().mapToLong(SplitTask::getCost).sum();
      }
      return cost;
    }

    public List<TaskDescriptor> splitTasks(int targetTaskCount) {
      return splitTasks.stream().filter(SplitTask::isNotEmpty).map(
              SplitTask::getTask)
          .collect(Collectors.toList());
    }

    public OptimizingType getOptimizingType() {
      if (partitionShouldFullOptimizing()) {
        return OptimizingType.FULL_MAJOR;
      }
      if (splitTasks.stream().anyMatch(t -> t.getOptimizingType() == OptimizingType.MAJOR)) {
        return OptimizingType.MAJOR;
      } else {
        return OptimizingType.MINOR;
      }
    }
  }
}
