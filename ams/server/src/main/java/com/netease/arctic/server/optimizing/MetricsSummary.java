package com.netease.arctic.server.optimizing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.netease.arctic.optimizing.RewriteFilesInput;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;

import java.util.Collection;

public class MetricsSummary {
  @JsonIgnoreProperties private long newDataSize = 0;

  @JsonIgnoreProperties private int newDataFileCnt = 0;
  @JsonIgnoreProperties private long newDataRecordCnt = 0;
  /** Only position delete files will be generated */
  @JsonIgnoreProperties private long newDeleteSize = 0;

  @JsonIgnoreProperties private int newDeleteFileCnt = 0;
  @JsonIgnoreProperties private long newDeleteRecordCnt = 0;
  private long rewriteDataSize = 0;
  private long rewritePosDataSize = 0;
  private long equalityDeleteSize = 0;
  @JsonIgnoreProperties private long positionDeleteSize = 0;
  private int rewriteDataFileCnt = 0;
  @JsonIgnoreProperties private int rewritePosDataFileCnt = 0;
  private int eqDeleteFileCnt = 0;
  private int posDeleteFileCnt = 0;
  @JsonIgnoreProperties private int rewriteDataRecordCnt = 0;
  @JsonIgnoreProperties private int rewritePosDataRecordCnt = 0;
  @JsonIgnoreProperties private int eqDeleteRecordCnt = 0;
  @JsonIgnoreProperties private int posDeleteRecordCnt = 0;

  public MetricsSummary() {}

  protected MetricsSummary(RewriteFilesInput input) {
    rewriteDataFileCnt = input.rewrittenDataFiles().length;
    rewritePosDataFileCnt = input.rePosDeletedDataFiles().length;
    for (DataFile rewriteFile : input.rewrittenDataFiles()) {
      rewriteDataSize += rewriteFile.fileSizeInBytes();
      rewriteDataRecordCnt += rewriteFile.recordCount();
    }
    for (DataFile rewritePosDataFile : input.rePosDeletedDataFiles()) {
      rewritePosDataSize += rewritePosDataFile.fileSizeInBytes();
      rewritePosDataRecordCnt += rewritePosDataFile.recordCount();
    }
    for (ContentFile<?> delete : input.deleteFiles()) {
      if (delete.content() == FileContent.POSITION_DELETES) {
        positionDeleteSize += delete.fileSizeInBytes();
        posDeleteRecordCnt += delete.recordCount();
        posDeleteFileCnt++;
      } else {
        equalityDeleteSize += delete.fileSizeInBytes();
        eqDeleteRecordCnt += delete.recordCount();
        eqDeleteFileCnt++;
      }
    }
  }

  public MetricsSummary(Collection<TaskRuntime> taskRuntimes) {
    taskRuntimes.stream()
        .map(TaskRuntime::getMetricsSummary)
        .forEach(
            metrics -> {
              newDataFileCnt += metrics.getNewDataFileCnt();
              newDataSize += metrics.getNewDataSize();
              newDataRecordCnt += metrics.getNewDataRecordCnt();
              newDeleteSize += metrics.getNewDeleteSize();
              newDeleteFileCnt += metrics.getNewDeleteFileCnt();
              newDeleteRecordCnt += metrics.getNewDeleteRecordCnt();
              rewriteDataFileCnt += metrics.getRewriteDataFileCnt();
              rewritePosDataFileCnt += metrics.getRewritePosDataFileCnt();
              rewriteDataSize += metrics.getRewriteDataSize();
              rewritePosDataSize += metrics.getRewritePosDataSize();
              posDeleteFileCnt += metrics.getPosDeleteFileCnt();
              positionDeleteSize += metrics.getPositionDeleteSize();
              eqDeleteFileCnt += metrics.getEqDeleteFileCnt();
              equalityDeleteSize += metrics.getEqualityDeleteSize();
              rewriteDataRecordCnt += metrics.getRewriteDataRecordCnt();
              rewritePosDataRecordCnt += metrics.getRewritePosDataRecordCnt();
              eqDeleteRecordCnt += metrics.getEqDeleteRecordCnt();
              posDeleteRecordCnt += metrics.getPosDeleteRecordCnt();
            });
  }

  public long getNewDataSize() {
    return newDataSize;
  }

  protected void setNewDataSize(long newDataSize) {
    this.newDataSize = newDataSize;
  }

  public int getNewDataFileCnt() {
    return newDataFileCnt;
  }

  protected void setNewDataFileCnt(int newDataFileCnt) {
    this.newDataFileCnt = newDataFileCnt;
  }

  public long getNewDataRecordCnt() {
    return newDataRecordCnt;
  }

  protected void setNewDataRecordCnt(long newDataRecordCnt) {
    this.newDataRecordCnt = newDataRecordCnt;
  }

  public void setNewDeleteSize(long newDeleteSize) {
    this.newDeleteSize = newDeleteSize;
  }

  public void setNewDeleteFileCnt(int newDeleteFileCnt) {
    this.newDeleteFileCnt = newDeleteFileCnt;
  }

  public long getNewDeleteSize() {
    return newDeleteSize;
  }

  public int getNewDeleteFileCnt() {
    return newDeleteFileCnt;
  }

  public long getNewDeleteRecordCnt() {
    return newDeleteRecordCnt;
  }

  protected void setNewDeleteRecordCnt(long newDeleteRecordCnt) {
    this.newDeleteRecordCnt = newDeleteRecordCnt;
  }

  public long getRewriteDataSize() {
    return rewriteDataSize;
  }

  public long getRewritePosDataSize() {
    return rewritePosDataSize;
  }

  public long getEqualityDeleteSize() {
    return equalityDeleteSize;
  }

  public long getPositionDeleteSize() {
    return positionDeleteSize;
  }

  public int getRewriteDataFileCnt() {
    return rewriteDataFileCnt;
  }

  public int getRewritePosDataFileCnt() {
    return rewritePosDataFileCnt;
  }

  public int getEqDeleteFileCnt() {
    return eqDeleteFileCnt;
  }

  public int getPosDeleteFileCnt() {
    return posDeleteFileCnt;
  }

  public int getRewriteDataRecordCnt() {
    return rewriteDataRecordCnt;
  }

  public int getRewritePosDataRecordCnt() {
    return rewritePosDataRecordCnt;
  }

  public int getEqDeleteRecordCnt() {
    return eqDeleteRecordCnt;
  }

  public int getPosDeleteRecordCnt() {
    return posDeleteRecordCnt;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("newDataSize", newDataSize)
        .add("newDataFileCnt", newDataFileCnt)
        .add("newDataRecordCnt", newDataRecordCnt)
        .add("newDeleteSize", newDeleteSize)
        .add("newDeleteFileCnt", newDeleteFileCnt)
        .add("newDeleteRecordCnt", newDeleteRecordCnt)
        .add("rewriteDataSize", rewriteDataSize)
        .add("rewritePosDataSize", rewritePosDataSize)
        .add("equalityDeleteSize", equalityDeleteSize)
        .add("positionDeleteSize", positionDeleteSize)
        .add("rewriteDataFileCnt", rewriteDataFileCnt)
        .add("rewritePosDataFileCnt", rewritePosDataFileCnt)
        .add("eqDeleteFileCnt", eqDeleteFileCnt)
        .add("posDeleteFileCnt", posDeleteFileCnt)
        .add("rewriteDataRecordCnt", rewriteDataRecordCnt)
        .add("rewritePosDataRecordCnt", rewritePosDataRecordCnt)
        .add("eqDeleteRecordCnt", eqDeleteRecordCnt)
        .add("posDeleteRecordCnt", posDeleteRecordCnt)
        .toString();
  }
}
