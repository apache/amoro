package com.netease.arctic.hive.op;

import com.netease.arctic.hive.HMSClientPool;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RewriteHiveFiles extends UpdateHiveFiles<RewriteFiles> implements RewriteFiles {

  public RewriteHiveFiles(
      Transaction transaction,
      boolean insideTransaction,
      UnkeyedHiveTable table,
      HMSClientPool hmsClient,
      HMSClientPool transactionClient) {
    super(
        transaction,
        insideTransaction,
        table,
        transaction.newRewrite(),
        hmsClient,
        transactionClient);
  }

  @Override
  public RewriteFiles rewriteFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd) {
    filesToDelete.forEach(delegate::deleteFile);
    // only add datafile not in hive location
    filesToAdd.stream().filter(this::notHiveDatafile).forEach(delegate::addFile);
    markHiveFiles(filesToDelete, filesToAdd);
    return this;
  }

  @Override
  public RewriteFiles rewriteFiles(
      Set<DataFile> filesToDelete, Set<DataFile> filesToAdd, long sequenceNumber) {
    delegate.dataSequenceNumber(sequenceNumber);
    filesToDelete.forEach(delegate::deleteFile);
    // only add datafile not in hive location
    filesToAdd.stream().filter(this::notHiveDatafile).forEach(delegate::addFile);
    markHiveFiles(filesToDelete, filesToAdd);
    return this;
  }

  @Override
  public RewriteFiles rewriteFiles(
      Set<DataFile> dataFilesToReplace,
      Set<DeleteFile> deleteFilesToReplace,
      Set<DataFile> dataFilesToAdd,
      Set<DeleteFile> deleteFilesToAdd) {
    dataFilesToReplace.forEach(delegate::deleteFile);
    deleteFilesToReplace.forEach(delegate::deleteFile);
    deleteFilesToAdd.forEach(delegate::addFile);
    // only add datafile not in hive location
    dataFilesToAdd.stream().filter(this::notHiveDatafile).forEach(delegate::addFile);
    markHiveFiles(dataFilesToReplace, dataFilesToAdd);

    return this;
  }

  private void markHiveFiles(Set<DataFile> filesToDelete, Set<DataFile> filesToAdd) {
    String hiveLocationRoot = table.hiveLocation();
    // handle filesToAdd, only handle file in hive location
    this.addFiles.addAll(getDataFilesInHiveLocation(filesToAdd, hiveLocationRoot));

    // handle filesToDelete, only handle file in hive location
    this.deleteFiles.addAll(getDataFilesInHiveLocation(filesToDelete, hiveLocationRoot));
  }

  @Override
  public RewriteFiles validateFromSnapshot(long snapshotId) {
    delegate.validateFromSnapshot(snapshotId);
    return this;
  }

  @Override
  protected void postHiveDataCommitted(List<DataFile> committedDataFile) {
    committedDataFile.forEach(delegate::addFile);
  }

  @Override
  protected RewriteFiles self() {
    return this;
  }

  private List<DataFile> getDataFilesInHiveLocation(Set<DataFile> dataFiles, String hiveLocation) {
    List<DataFile> result = new ArrayList<>();
    for (DataFile dataFile : dataFiles) {
      String dataFileLocation = dataFile.path().toString();
      if (dataFileLocation.toLowerCase().contains(hiveLocation.toLowerCase())) {
        // only handle file in hive location
        result.add(dataFile);
      }
    }

    return result;
  }

  private boolean notHiveDatafile(DataFile dataFile) {
    String hiveLocation = table.hiveLocation();
    String dataFileLocation = dataFile.path().toString();
    return !dataFileLocation.toLowerCase().contains(hiveLocation.toLowerCase());
  }
}
