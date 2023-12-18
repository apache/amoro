package com.netease.arctic.server.process.optimizing;

import com.netease.arctic.server.ArcticServiceConstants;
import org.apache.iceberg.relocated.com.google.common.base.MoreObjects;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;

import java.util.Collection;
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

  public PendingInput() {}

  public PendingInput(Collection<PartitionEvaluator> evaluators) {
    for (PartitionEvaluator evaluator : evaluators) {
      partitions.add(evaluator.getPartition());
      dataFileCount += evaluator.getFragmentFileCount() + evaluator.getSegmentFileCount();
      dataFileSize += evaluator.getFragmentFileSize() + evaluator.getSegmentFileSize();
      positionalDeleteBytes += evaluator.getPosDeleteFileSize();
      positionalDeleteFileCount += evaluator.getPosDeleteFileCount();
      equalityDeleteBytes += evaluator.getEqualityDeleteFileSize();
      equalityDeleteFileCount += evaluator.getEqualityDeleteFileCount();
    }
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
}
