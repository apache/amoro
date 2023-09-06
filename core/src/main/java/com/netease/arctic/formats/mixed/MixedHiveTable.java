package com.netease.arctic.formats.mixed;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.table.ArcticTable;

public class MixedHiveTable extends MixedIcebergTable {
  public MixedHiveTable(ArcticTable arcticTable) {
    super(arcticTable);
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_HIVE;
  }
}
