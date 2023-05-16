package com.netease.arctic.optimizing;

import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.table.ArcticTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RewriteFilesInput extends BaseOptimizingInput {
  private final IcebergDataFile[] rewrittenDataFiles;
  private final IcebergDataFile[] rePosDeletedDataFiles;
  private final IcebergContentFile<?>[] readOnlyDeleteFiles;
  private final IcebergContentFile<?>[] rewriteDeleteFiles;
  private ArcticTable table;

  public RewriteFilesInput(
      IcebergDataFile[] rewrittenDataFiles,
      IcebergDataFile[] rePosDeletedDataFiles,
      IcebergContentFile<?>[] readOnlyDeleteFiles,
      IcebergContentFile<?>[] rewriteDeleteFiles,
      ArcticTable table) {
    this.rewrittenDataFiles = rewrittenDataFiles;
    this.rePosDeletedDataFiles = rePosDeletedDataFiles;
    this.readOnlyDeleteFiles = readOnlyDeleteFiles;
    this.rewriteDeleteFiles = rewriteDeleteFiles;
    this.table = table;
  }

  public IcebergDataFile[] rewrittenDataFiles() {
    return rewrittenDataFiles;
  }

  public IcebergDataFile[] rePosDeletedDataFiles() {
    return rePosDeletedDataFiles;
  }

  public IcebergContentFile<?>[] readOnlyDeleteFiles() {
    return readOnlyDeleteFiles;
  }

  public IcebergContentFile<?>[] rewriteDeleteFiles() {
    return rewriteDeleteFiles;
  }

  public IcebergContentFile<?>[] deleteFiles() {
    List<IcebergContentFile<?>> list = new ArrayList<>();
    if (readOnlyDeleteFiles != null) {
      Arrays.stream(readOnlyDeleteFiles).forEach(list::add);
    }
    if (rewriteDeleteFiles != null) {
      Arrays.stream(rewriteDeleteFiles).forEach(list::add);
    }
    return list.toArray(new IcebergContentFile<?>[0]);
  }

  public IcebergDataFile[] dataFiles() {
    List<IcebergDataFile> list = new ArrayList<>();
    if (rewrittenDataFiles != null) {
      Arrays.stream(rewrittenDataFiles).forEach(list::add);
    }
    if (rePosDeletedDataFiles != null) {
      Arrays.stream(rePosDeletedDataFiles).forEach(list::add);
    }
    return list.toArray(new IcebergDataFile[0]);
  }

  public IcebergContentFile<?>[] allFiles() {
    List<IcebergContentFile<?>> list = new ArrayList<>();
    if (rewrittenDataFiles != null) {
      Arrays.stream(rewrittenDataFiles).forEach(list::add);
    }
    if (rePosDeletedDataFiles != null) {
      Arrays.stream(rePosDeletedDataFiles).forEach(list::add);
    }
    if (readOnlyDeleteFiles != null) {
      Arrays.stream(readOnlyDeleteFiles).forEach(list::add);
    }
    if (rewriteDeleteFiles != null) {
      Arrays.stream(rewriteDeleteFiles).forEach(list::add);
    }
    return list.toArray(new IcebergDataFile[0]);
  }

  public ArcticTable getTable() {
    return table;
  }
}
