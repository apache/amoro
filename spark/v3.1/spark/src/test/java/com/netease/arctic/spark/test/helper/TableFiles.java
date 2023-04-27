package com.netease.arctic.spark.test.helper;

import com.clearspring.analytics.util.Lists;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TableFiles {

  public final Set<DataFile> baseDataFiles;
  public final Set<DeleteFile> baseDeleteFiles;

  public final Set<DataFile> changeInsertFiles;
  public final Set<DataFile> changeEqDeleteFiles;


  public TableFiles(Set<DataFile> baseDataFiles, Set<DeleteFile> baseDeleteFiles,
                    Set<DataFile> changeInsertFiles, Set<DataFile> changeEqDeleteFiles) {
    this.baseDataFiles = baseDataFiles;
    this.baseDeleteFiles = baseDeleteFiles;
    this.changeInsertFiles = changeInsertFiles;
    this.changeEqDeleteFiles = changeEqDeleteFiles;
  }

  public TableFiles(Set<DataFile> baseDataFiles, Set<DeleteFile> baseDeleteFiles) {
    this.baseDataFiles = baseDataFiles;
    this.baseDeleteFiles = baseDeleteFiles;
    this.changeInsertFiles = Collections.emptySet();
    this.changeEqDeleteFiles = Collections.emptySet();
  }


}
