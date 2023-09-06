package com.netease.arctic.formats.paimon;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.TableSnapshot;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.TableIdentifier;
import java.util.Map;
import org.apache.paimon.Snapshot;
import org.apache.paimon.table.DataTable;
import org.apache.paimon.table.Table;

public class PaimonTable implements AmoroTable<Table> {

  private TableIdentifier tableIdentifier;

  private Table table;

  public PaimonTable(TableIdentifier tableIdentifier, Table table) {
    this.tableIdentifier = tableIdentifier;
    this.table = table;
  }

  @Override
  public TableIdentifier id() {
    return tableIdentifier;
  }

  @Override
  public TableFormat format() {
    return TableFormat.PAIMON;
  }

  @Override
  public Map<String, String> properties() {
    return table.options();
  }

  @Override
  public Table originalTable() {
    return table;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    if (!(table instanceof DataTable)) {
      return null;
    }

    Snapshot snapshot = ((DataTable) table).snapshotManager().latestSnapshot();
    return snapshot == null ? null : new PaimonSnapshot(snapshot);
  }
}
