package com.netease.arctic.ams.api.process;

import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Set;

public class PendingInput {

  private final Set<String> partitions = Sets.newHashSet();

  private int dataFileCount = 0;
  private long dataFileSize = 0;
  private int equalityDeleteFileCount = 0;
  private int positionalDeleteFileCount = 0;
  private long positionalDeleteBytes = 0L;
  private long equalityDeleteBytes = 0L;
  private long currentSnapshotId;
  private long currentChangeSnapshotId;
  private long currentWatermark;
  private long currentChangeWatermark;
  private boolean needMinorOptimizing = false;
  private boolean needMajorOptimizing = false;

  public PendingInput() {}

  public PendingInput(
      Set<String> partitions,
      int dataFileCount,
      long dataFileSize,
      int equalityDeleteFileCount,
      int positionalDeleteFileCount,
      long positionalDeleteBytes,
      long equalityDeleteBytes,
      boolean needMinorOptimizing,
      boolean needMajorOptimizing) {
    this.partitions.addAll(partitions);
    this.dataFileCount = dataFileCount;
    this.dataFileSize = dataFileSize;
    this.equalityDeleteFileCount = equalityDeleteFileCount;
    this.positionalDeleteFileCount = positionalDeleteFileCount;
    this.positionalDeleteBytes = positionalDeleteBytes;
    this.equalityDeleteBytes = equalityDeleteBytes;
    this.needMinorOptimizing = needMinorOptimizing;
    this.needMajorOptimizing = needMajorOptimizing;
  }

  public int getInputFileCount() {
    return dataFileCount + equalityDeleteFileCount + positionalDeleteFileCount;
  }

  public long getInputFileSize() {
    return dataFileSize + equalityDeleteBytes + positionalDeleteBytes;
  }

  public Set<String> getPartitions() {
    return partitions;
  }

  public int getDataFileCount() {
    return dataFileCount;
  }

  public long getDataFileSize() {
    return dataFileSize;
  }

  public int getEqualityDeleteFileCount() {
    return equalityDeleteFileCount;
  }

  public int getPositionalDeleteFileCount() {
    return positionalDeleteFileCount;
  }

  public long getPositionalDeleteBytes() {
    return positionalDeleteBytes;
  }

  public long getEqualityDeleteBytes() {
    return equalityDeleteBytes;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("partitions", partitions)
        .add("dataFileCount", dataFileCount)
        .add("dataFileSize", dataFileSize)
        .add("equalityDeleteFileCount", equalityDeleteFileCount)
        .add("positionalDeleteFileCount", positionalDeleteFileCount)
        .add("positionalDeleteBytes", positionalDeleteBytes)
        .add("equalityDeleteBytes", equalityDeleteBytes)
        .toString();
  }

  public long getCurrentSnapshotId() {
    return currentSnapshotId;
  }

  public long getCurrentChangeSnapshotId() {
    return currentChangeSnapshotId;
  }

  public void setCurrentSnapshotId(long currentSnapshotId) {
    this.currentSnapshotId = currentSnapshotId;
  }

  public void setCurrentChangeSnapshotId(long currentChangeSnapshotId) {
    this.currentChangeSnapshotId = currentChangeSnapshotId;
  }

  public boolean needMinorOptimizing() {
    return needMinorOptimizing;
  }

  public boolean needMajorOptimizing() {
    return needMajorOptimizing;
  }

  public int getFragmentFileCount() {
    return 0;
  }

  public long getFragmentFileSize() {
    return 0;
  }

  public int getSegmentFileCount() {
    return 0;
  }

  public long getSegmentFileSize() {
    return 0;
  }

  public long getWatermark() {
    return currentWatermark;
  }

  public long getChangeWatermark() {
    return currentChangeWatermark;
  }
}
