package com.netease.arctic.server.optimizing.plan;

import com.google.common.collect.Maps;
import com.netease.arctic.ams.api.properties.OptimizingTaskProperties;
import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.IcebergDeleteFile;
import com.netease.arctic.data.PrimaryKeyedFile;
import com.netease.arctic.optimizing.IcebergFormatRewriteFilesExecutorFactory;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.glassfish.jersey.internal.guava.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MixedIcebergPartitionPlan extends AbstractPartitionPlan {

  private TaskSplitter taskSplitter;
  private final Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
  private final Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();
  private final Set<IcebergDataFile> equalityRelatedFiles = Sets.newHashSet();
  private final Map<ContentFile<?>, Set<IcebergDataFile>> equalityDeleteFileMap = Maps.newHashMap();
  private long fragementFileSize = 0;
  private long segmentFileSize = 0;
  private long positionalDeleteBytes = 0L;
  private long equalityDeleteBytes = 0L;
  private int smallFileCount = 0;

  public MixedIcebergPartitionPlan(TableRuntime tableRuntime,
                                   ArcticTable table, String partition) {
    super(tableRuntime, table, partition);
  }

  @Override
  public void addFile(DataFile dataFile, List<DeleteFile> deletes) {
    addFile(dataFile, deletes, Collections.emptyList());
  }

  @Override
  public void addFile(DataFile dataFile, List<DeleteFile> deletes, List<IcebergDataFile> changeDeletes) {
    IcebergDataFile contentFile = createDataFile(dataFile);
    if (isChangeFile(contentFile)) {
      markSequence(contentFile.getSequenceNumber());
    }
    if (isFragmentFile(contentFile)) {
      fragmentFiles.put(
          contentFile,
          deletes.stream().map(this::createDeleteFile).collect(Collectors.toList()));
      fragementFileSize += dataFile.fileSizeInBytes();
      smallFileCount += 1;
    } else {
      segmentFiles.put(
          contentFile,
          deletes.stream().map(this::createDeleteFile).collect(Collectors.toList()));
      segmentFileSize += dataFile.fileSizeInBytes();
    }
    for (DeleteFile deleteFile : deletes) {
      if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
        equalityRelatedFiles.add(contentFile);
        equalityDeleteFileMap
            .computeIfAbsent(deleteFile, delete -> Sets.newHashSet())
            .add(contentFile);
        equalityDeleteBytes += deleteFile.fileSizeInBytes();
        smallFileCount += 1;
      }
    }

    for (IcebergDataFile deleteFile : changeDeletes) {
      equalityRelatedFiles.add(contentFile);
      equalityDeleteFileMap
          .computeIfAbsent(deleteFile, delete -> Sets.newHashSet())
          .add(contentFile);
      equalityDeleteBytes += deleteFile.fileSizeInBytes();
      smallFileCount += 1;
      markSequence(contentFile.getSequenceNumber());
    }
  }

  private boolean isFragmentFile(IcebergDataFile dataFile) {
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
    if (file.type() == DataFileType.BASE_FILE) {
      return dataFile.fileSizeInBytes() <= fragementSize;
    } else if (file.type() == DataFileType.INSERT_FILE) {
      return true;
    } else {
      throw new IllegalStateException("unexpected file type " + file.type() + " of " + file);
    }
  }

  private boolean isChangeFile(IcebergDataFile dataFile) {
    PrimaryKeyedFile file = (PrimaryKeyedFile) dataFile;
    return file.type() == DataFileType.INSERT_FILE;
  }

  private IcebergDeleteFile createDeleteFile(DeleteFile delete) {
    if (delete instanceof IcebergDeleteFile) {
      return ((IcebergDeleteFile) delete);
    } else {
      throw new IllegalStateException("delete file must be IcebergDeleteFile " + delete.path().toString());
    }
  }

  private IcebergDataFile createDataFile(DataFile dataFile) {
    if (dataFile instanceof IcebergDataFile) {
      return ((IcebergDataFile) dataFile);
    } else {
      throw new IllegalStateException("delete file must be IcebergDataFile " + dataFile.path().toString());
    }
  }

  @Override
  public boolean isNecessary() {
    if (taskSplitter == null) {
      taskSplitter = new TaskSplitter();
    }
    return taskSplitter.isNecessary();
  }

  @Override
  public long getCost() {
    if (taskSplitter == null) {
      taskSplitter = new TaskSplitter();
    }
    return taskSplitter.getCost();
  }

  @Override
  public OptimizingType getOptimizingType() {
    if (taskSplitter == null) {
      taskSplitter = new TaskSplitter();
    }
    return taskSplitter.getOptimizingType();
  }

  @Override
  public List<TaskDescriptor> splitTasks(int targetTaskCount) {
    if (taskSplitter == null) {
      taskSplitter = new TaskSplitter();
    }
    return taskSplitter.splitTasks(targetTaskCount);
  }

  private class SubFileTreeTask {
    FileTree subTree;
    Set<IcebergDataFile> rewriteDataFiles = Sets.newHashSet();
    Set<IcebergContentFile<?>> deleteFiles = Sets.newHashSet();
    Set<IcebergDataFile> rewritePosDataFiles = Sets.newHashSet();

    long cost = -1;

    public SubFileTreeTask(FileTree subTree) {
      this.subTree = subTree;
      Map<IcebergDataFile, List<IcebergContentFile<?>>> fragmentFiles = Maps.newHashMap();
      Map<IcebergDataFile, List<IcebergContentFile<?>>> segmentFiles = Maps.newHashMap();
      subTree.collectFragmentFiles(fragmentFiles);
      subTree.collectSegmentFiles(segmentFiles);
      segmentFiles.forEach((icebergFile, deleteFileSet) -> {
        long deleteCount = deleteFileSet.stream().mapToLong(file -> file.recordCount()).sum();
        if (deleteCount >= icebergFile.recordCount() * config.getMajorDuplicateRatio()) {
          rewriteDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        } else if (equalityRelatedFiles.contains(icebergFile)) {
          rewritePosDataFiles.add(icebergFile);
          deleteFiles.addAll(deleteFileSet);
        } else {
          long posDeleteCount = deleteFileSet.stream()
              .filter(file -> file.content() == FileContent.POSITION_DELETES)
              .count();
          if (posDeleteCount > 1) {
            rewritePosDataFiles.add(icebergFile);
            deleteFiles.addAll(deleteFileSet);
          }
        }
      });
      fragmentFiles.forEach((icebergFile, deleteFileSet) -> {
        rewriteDataFiles.add(icebergFile);
        deleteFiles.addAll(deleteFileSet);
      });
    }

    public boolean isNecessary() {
      return smallFileCount >= config.getMinorLeastFileCount() ||
          rewritePosDataFiles.size() > 0 && deleteFiles.size() > 0 &&
              System.currentTimeMillis() - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval();
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
          tableObject.asUnkeyedTable());
      Map<String, String> taskProperties = Maps.newHashMap();
      // TODO
      taskProperties.put(
          OptimizingTaskProperties.TASK_EXECUTOR_FACTORY_IMPL,
          IcebergFormatRewriteFilesExecutorFactory.class.getName());
      return new TaskDescriptor(partition, input, taskProperties);
    }

    // TODO
    public OptimizingType getOptimizingType() {
      return OptimizingType.MAJOR;
    }
  }

  private class TaskSplitter {

    List<SubFileTreeTask> subFileTreeTasks;

    long cost = -1;

    public TaskSplitter() {
      FileTree rootTree = FileTree.newTreeRoot();
      segmentFiles.forEach(rootTree::addSegmentFile);
      fragmentFiles.forEach(rootTree::addFragmentFile);
      rootTree.completeTree();
      List<FileTree> subTrees = Lists.newArrayList();
      rootTree.splitFileTree(subTrees, new SplitIfNoFileExists());
      subFileTreeTasks = subTrees.stream().map(SubFileTreeTask::new).collect(Collectors.toList());
    }


    public boolean isNecessary() {
      return subFileTreeTasks.stream().anyMatch(SubFileTreeTask::isNecessary);
    }

    public long getCost() {
      if (cost < 0) {
        cost = subFileTreeTasks.stream().mapToLong(SubFileTreeTask::getCost).sum();
      }
      return cost;
    }

    public List<TaskDescriptor> splitTasks(int targetTaskCount) {
      return subFileTreeTasks.stream().filter(SubFileTreeTask::isNotEmpty).map(SubFileTreeTask::getTask)
          .collect(Collectors.toList());
    }

    public OptimizingType getOptimizingType() {
      if (subFileTreeTasks.stream().anyMatch(t -> t.getOptimizingType() == OptimizingType.MAJOR)) {
        return OptimizingType.MAJOR;
      } else {
        return OptimizingType.MINOR;
      }
    }
  }

  private static class SplitIfNoFileExists implements Predicate<FileTree> {

    public SplitIfNoFileExists() {
    }

    /**
     * file tree can split if:
     * - root node isn't leaf node
     * - and no file exists in the root node
     *
     * @param fileTree - file tree to split
     * @return true if this fileTree need split
     */
    @Override
    public boolean test(FileTree fileTree) {
      return !fileTree.isLeaf() && fileTree.isRootEmpty();
    }
  }
}
