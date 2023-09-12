package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.Table;

public interface TableMaintainer {

  void cleanContentFiles(long lastTime);

  void cleanMetadata(long lastTime);

  void cleanDanglingDeleteFiles();

  void expireSnapshots(TableRuntime tableRuntime);

  void expireSnapshots(long mustOlderThan);

  static TableMaintainer createMaintainer(AmoroTable<?> amoroTable) {
    TableFormat format = amoroTable.format();
    if (format == TableFormat.MIXED_HIVE || format == TableFormat.MIXED_ICEBERG) {
      return new MixedTableMaintainer((ArcticTable) amoroTable.originalTable());
    } else if (format == TableFormat.ICEBERG) {
      return new IcebergTableMaintainer((Table) amoroTable.originalTable());
    } else {
      throw new RuntimeException("Unsupported table type" + amoroTable.originalTable().getClass());
    }
  }
}
