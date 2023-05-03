package com.netease.arctic.ams.server.optimizing;

import com.netease.arctic.table.ArcticTable;

import java.util.Collection;

public class MixedHiveCommit extends MixedIcebergCommit {

  MixedHiveCommit(long targetSnapshotId, ArcticTable table, Collection<TaskRuntime> tasks) {
    super(targetSnapshotId, table, tasks);
  }
}
