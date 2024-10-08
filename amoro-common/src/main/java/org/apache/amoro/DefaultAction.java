package org.apache.amoro;

public class DefaultAction extends Action {

  private static final TableFormat[] DEFAULT_FORMATS = new TableFormat[] {
      TableFormat.ICEBERG, TableFormat.MIXED_ICEBERG, TableFormat.MIXED_HIVE };

  DefaultAction(int code, int weight, String desc) {
    super(DEFAULT_FORMATS, code, weight, desc);
  }
}
