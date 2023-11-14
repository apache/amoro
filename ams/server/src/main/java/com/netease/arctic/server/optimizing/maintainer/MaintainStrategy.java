package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.server.table.DataExpirationConfig;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;

public interface MaintainStrategy {

  List<IcebergTableMaintainer.ExpireFiles> entryFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness);

  void doExpireFiles(List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp);
}
