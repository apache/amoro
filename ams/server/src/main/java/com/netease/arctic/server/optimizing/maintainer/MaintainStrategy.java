package com.netease.arctic.server.optimizing.maintainer;

import com.netease.arctic.server.table.DataExpirationConfig;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;

import java.util.List;
import java.util.Map;

public interface MaintainStrategy {

  List<IcebergTableMaintainer.ExpireFiles> expiredFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness);

  void doExpireFiles(List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp);
}
