package com.netease.arctic.server.optimizing.scan;

import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;

import java.util.List;
import java.util.stream.Collectors;

public class UnkeyedTableFileScanHelper extends IcebergTableFileScanHelper {

  public UnkeyedTableFileScanHelper(UnkeyedTable table, long snapshotId) {
    super(table, snapshotId);
  }

  @Override
  protected FileScanResult buildFileScanResult(FileScanTask fileScanTask) {
    DataFile dataFile = wrapBaseFile(fileScanTask.file());
    List<ContentFile<?>> deleteFiles =
        fileScanTask.deletes().stream().map(this::wrapDeleteFile).collect(Collectors.toList());
    return new FileScanResult(dataFile, deleteFiles);
  }

  private DataFile wrapBaseFile(DataFile dataFile) {
    return DefaultKeyedFile.parseBase(dataFile);
  }

  private ContentFile<?> wrapDeleteFile(DeleteFile deleteFile) {
    if (deleteFile.content() == FileContent.EQUALITY_DELETES) {
      throw new UnsupportedOperationException(
          "optimizing unkeyed table not support equality-delete");
    }
    return deleteFile;
  }
}
