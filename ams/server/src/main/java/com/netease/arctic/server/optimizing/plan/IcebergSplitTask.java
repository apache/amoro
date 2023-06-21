package com.netease.arctic.server.optimizing.plan;


import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.optimizing.OptimizingInputProperties;
import com.netease.arctic.optimizing.RewriteFilesInput;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class IcebergSplitTask {
  private final ArcticTable arcticTable;
  private final TableRuntime tableRuntime;
  private final String partition;
  private final Set<IcebergDataFile> rewriteDataFiles;
  private final Set<IcebergContentFile<?>> deleteFiles;
  private final Set<IcebergDataFile> rewritePosDataFiles;

  public IcebergSplitTask(ArcticTable arcticTable, TableRuntime tableRuntime, String partition,
                          Set<IcebergDataFile> rewriteDataFiles,
                          Set<IcebergContentFile<?>> deleteFiles,
                          Set<IcebergDataFile> rewritePosDataFiles) {
    this.arcticTable = arcticTable;
    this.tableRuntime = tableRuntime;
    this.partition = partition;
    this.rewriteDataFiles = rewriteDataFiles;
    this.deleteFiles = deleteFiles;
    this.rewritePosDataFiles = rewritePosDataFiles;
  }

  public Set<IcebergDataFile> getRewriteDataFiles() {
    return rewriteDataFiles;
  }

  public Set<IcebergContentFile<?>> getDeleteFiles() {
    return deleteFiles;
  }

  public Set<IcebergDataFile> getRewritePosDataFiles() {
    return rewritePosDataFiles;
  }

  public boolean contains(IcebergDataFile dataFile) {
    return rewriteDataFiles.contains(dataFile) || rewritePosDataFiles.contains(dataFile);
  }

  public TaskDescriptor buildTask(Map<String, Set<IcebergDataFile>> posDeleteFileMap,
                                  Map<String, Set<IcebergDataFile>> equalityDeleteFileMap,
                                  OptimizingInputProperties properties, List<IcebergSplitTask> icebergSplitTasks) {
    Set<IcebergContentFile<?>> readOnlyDeleteFiles = Sets.newHashSet();
    Set<IcebergContentFile<?>> rewriteDeleteFiles = Sets.newHashSet();
    for (IcebergContentFile<?> deleteFile : deleteFiles) {
      Set<IcebergDataFile> relatedDataFiles;
      if (deleteFile.content() == FileContent.POSITION_DELETES) {
        relatedDataFiles = posDeleteFileMap.get(deleteFile.path().toString());
      } else {
        relatedDataFiles = equalityDeleteFileMap.get(deleteFile.path().toString());
      }
      boolean findDataFileNotOptimizing =
          relatedDataFiles.stream()
              .anyMatch(file -> !contains(file) && !isOptimizing(icebergSplitTasks, file));
      if (findDataFileNotOptimizing) {
        readOnlyDeleteFiles.add(deleteFile);
      } else {
        rewriteDeleteFiles.add(deleteFile);
      }
    }
    RewriteFilesInput input = new RewriteFilesInput(
        rewriteDataFiles.toArray(new IcebergDataFile[0]),
        rewritePosDataFiles.toArray(new IcebergDataFile[0]),
        readOnlyDeleteFiles.toArray(new IcebergContentFile[0]),
        rewriteDeleteFiles.toArray(new IcebergContentFile[0]),
        arcticTable);
    return new TaskDescriptor(tableRuntime.getTableIdentifier().getId(),
        partition, input, properties.getProperties());
  }

  private boolean isOptimizing(List<IcebergSplitTask> icebergSplitTasks, IcebergDataFile dataFile) {
    return icebergSplitTasks.stream().anyMatch(task -> task.contains(dataFile));
  }
}