package com.netease.arctic.optimizing;

import com.netease.arctic.data.file.DataFileWithSequence;
import com.netease.arctic.data.file.DeleteFileWithSequence;
import com.netease.arctic.table.ArcticTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RewriteFilesInput extends BaseOptimizingInput {
  private final DataFileWithSequence[] rewrittenDataFiles;
  private final DataFileWithSequence[] rePosDeletedDataFiles;
  private final DeleteFileWithSequence[] deleteFiles;
  private ArcticTable table;

  public RewriteFilesInput(
      DataFileWithSequence[] rewrittenDataFiles,
      DataFileWithSequence[] rePosDeletedDataFiles,
      DeleteFileWithSequence[] deleteFiles,
      ArcticTable table) {
    this.rewrittenDataFiles = rewrittenDataFiles;
    this.rePosDeletedDataFiles = rePosDeletedDataFiles;
    this.deleteFiles = deleteFiles;
    this.table = table;
  }

  public DataFileWithSequence[] rewrittenDataFiles() {
    return rewrittenDataFiles;
  }

  public DataFileWithSequence[] rePosDeletedDataFiles() {
    return rePosDeletedDataFiles;
  }

  public DeleteFileWithSequence[] deleteFiles() {
    return deleteFiles;
  }

  public DataFileWithSequence[] dataFiles() {
    List<DataFileWithSequence> list = new ArrayList<>();
    if (rewrittenDataFiles != null) {
      Arrays.stream(rewrittenDataFiles).forEach(list::add);
    }
    if (rePosDeletedDataFiles != null) {
      Arrays.stream(rePosDeletedDataFiles).forEach(list::add);
    }
    return list.toArray(new DataFileWithSequence[0]);
  }

  public ArcticTable getTable() {
    return table;
  }
}
