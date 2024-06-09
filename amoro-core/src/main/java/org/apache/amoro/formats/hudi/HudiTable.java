package org.apache.amoro.formats.hudi;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableSnapshot;
import org.apache.amoro.table.TableIdentifier;
import org.apache.hudi.table.HoodieJavaTable;

import java.util.Collections;
import java.util.Map;

public class HudiTable implements AmoroTable<HoodieJavaTable> {
  private final TableIdentifier identifier;
  private final HoodieJavaTable hoodieTable;
  private final Map<String, String> tableProperties;

  public HudiTable(
      TableIdentifier identifier,
      HoodieJavaTable hoodieTable,
      Map<String, String> tableProperties) {
    this.identifier = identifier;
    this.hoodieTable = hoodieTable;
    this.tableProperties = tableProperties == null ?
     Collections.emptyMap(): Collections.unmodifiableMap(tableProperties);
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
    return tableProperties;
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
