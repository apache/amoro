package com.netease.arctic.formats.paimon;

import com.netease.arctic.TableSnapshot;
import org.apache.paimon.Snapshot;

public class PaimonSnapshot implements TableSnapshot {

  private Snapshot snapshot;

  @Override
  public long watermark() {
    return snapshot.watermark();
  }

  @Override
  public long commitTime() {
    return snapshot.timeMillis();
  }

  @Override
  public String id() {
    return String.valueOf(snapshot.id());
  }
}
