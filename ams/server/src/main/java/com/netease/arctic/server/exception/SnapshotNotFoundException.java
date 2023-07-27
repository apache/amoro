package com.netease.arctic.server.exception;

public class SnapshotNotFoundException extends ArcticRuntimeException {
  private final long snapshotId;

  public SnapshotNotFoundException(long snapshotId) {
    super(String.format("Snapshot: %s not fount, it might be expired", snapshotId));
    this.snapshotId = snapshotId;
  }

  public long getSnapshotId() {
    return snapshotId;
  }
}
