package com.netease.arctic.optimizing;

import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.IcebergDeleteFile;
import com.netease.arctic.table.ArcticTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RewriteFilesInput extends BaseOptimizingInput {
  private final IcebergDataFile[] rewrittenDataFiles;
  private final IcebergDataFile[] rePosDeletedDataFiles;
  private final IcebergContentFile<?>[] readOnlyDeleteFiles;
  private final IcebergContentFile<?>[] rewrittenDeleteFiles;
  private ArcticTable table;

  public RewriteFilesInput(
      IcebergDataFile[] rewrittenDataFiles,
      IcebergDataFile[] rePosDeletedDataFiles,
      IcebergContentFile<?>[] readOnlyDeleteFiles,
      IcebergContentFile<?>[] rewrittenDeleteFiles,
      ArcticTable table) {
    this.rewrittenDataFiles = rewrittenDataFiles;
    this.rePosDeletedDataFiles = rePosDeletedDataFiles;
    this.readOnlyDeleteFiles = readOnlyDeleteFiles;
    this.rewrittenDeleteFiles = rewrittenDeleteFiles;
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

  public IcebergContentFile<?>[] rewrittenDeleteFiles() {
    return rewrittenDeleteFiles;
  }

  public IcebergContentFile<?>[] deleteFiles() {
    List<IcebergContentFile<?>> list = new ArrayList<>();
    if (readOnlyDeleteFiles != null) {
      list.addAll(Arrays.asList(readOnlyDeleteFiles));
    }
    if (rewrittenDeleteFiles != null) {
      list.addAll(Arrays.asList(rewrittenDeleteFiles));
    }
    return list.toArray(new IcebergContentFile<?>[0]);
  }

  public IcebergDataFile[] dataFiles() {
    List<IcebergDataFile> list = new ArrayList<>();
    if (rewrittenDataFiles != null) {
      list.addAll(Arrays.asList(rewrittenDataFiles));
    }
    if (rePosDeletedDataFiles != null) {
      list.addAll(Arrays.asList(rePosDeletedDataFiles));
    }
    return list.toArray(new IcebergDataFile[0]);
  }

  public IcebergContentFile<?>[] allFiles() {
    List<IcebergContentFile<?>> list = new ArrayList<>();
    if (rewrittenDataFiles != null) {
      list.addAll(Arrays.asList(rewrittenDataFiles));
    }
    if (rePosDeletedDataFiles != null) {
      list.addAll(Arrays.asList(rePosDeletedDataFiles));
    }
    if (readOnlyDeleteFiles != null) {
      Arrays.stream(readOnlyDeleteFiles).forEach(list::add);
    }
    if (rewrittenDeleteFiles != null) {
      Arrays.stream(rewrittenDeleteFiles).forEach(list::add);
    }
    return list.toArray(new IcebergContentFile<?>[0]);
  }

  public ArcticTable getTable() {
    return table;
  }
}
