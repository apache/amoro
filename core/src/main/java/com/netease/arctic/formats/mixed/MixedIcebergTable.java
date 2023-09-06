package com.netease.arctic.formats.mixed;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableSnapshot;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import java.util.Map;
import java.util.Optional;
import org.apache.iceberg.Snapshot;

public class MixedIcebergTable implements AmoroTable<ArcticTable> {

  private ArcticTable arcticTable;

  public MixedIcebergTable(ArcticTable arcticTable) {
    this.arcticTable = arcticTable;
  }

  @Override
  public TableIdentifier id() {
    return arcticTable.id();
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public Map<String, String> properties() {
    return arcticTable.properties();
  }

  @Override
  public ArcticTable originalTable() {
    return arcticTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    Optional<Snapshot> changeSnapshot;
    Optional<Snapshot> baseSnapshot;
    if (arcticTable.isKeyedTable()) {
      changeSnapshot = Optional.ofNullable(arcticTable.asKeyedTable().changeTable().currentSnapshot());
      baseSnapshot = Optional.ofNullable(arcticTable.asKeyedTable().baseTable().currentSnapshot());
    } else {
      changeSnapshot = Optional.empty();
      baseSnapshot = Optional.ofNullable(arcticTable.asUnkeyedTable().currentSnapshot());
    }

    if (!changeSnapshot.isPresent() && !baseSnapshot.isPresent()) {
      return null;
    }

    return new MixedSnapshot(changeSnapshot, baseSnapshot);
  }
}
