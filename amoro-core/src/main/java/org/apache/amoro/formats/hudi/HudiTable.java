package org.apache.amoro.formats.hudi;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hudi.table.HoodieJavaTable;

import java.util.Map;

public class HudiTable implements AmoroTable<HoodieJavaTable> {
  private final TableIdentifier identifier;
  private final HoodieJavaTable hoodieTable;
  public HudiTable(TableIdentifier identifier, HoodieJavaTable hoodieTable) {
    this.identifier = identifier;
    this.hoodieTable = hoodieTable;
  }

  @Override
  public TableIdentifier id() {
    return identifier;
  }
  @Override
  public TableFormat format() {
    return TableFormat.HUDI;
  }

  @Override
  public Map<String, String> properties() {
    return hoodieTable.getMetaClient().getTableConfig().propsMap();
  }

  @Override
  public HoodieJavaTable originalTable() {
    return hoodieTable;
  }

  @Override
  public TableSnapshot currentSnapshot() {
    throw new IllegalStateException("The method is not implement.");
  }
}
