package com.netease.arctic.server.optimizing.maintainer;

import com.google.common.collect.Lists;
import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.utils.IcebergTableUtil;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;

public class IcebergMaintainStrategy implements MaintainStrategy {
  private final IcebergTableMaintainer icebergMaintainer;

  public IcebergMaintainStrategy(IcebergTableMaintainer maintainer) {
    this.icebergMaintainer = maintainer;
  }

  @Override
  public List<IcebergTableMaintainer.ExpireFiles> entryFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    IcebergTableMaintainer.ExpireFiles expiredFiles = new IcebergTableMaintainer.ExpireFiles();
    try (CloseableIterable<IcebergFileEntry> entries =
        icebergMaintainer.fileScan(icebergMaintainer.getTable(), dataFilter)) {
      CloseableIterable<IcebergFileEntry> mayExpiredFiles =
          CloseableIterable.withNoopClose(
              Lists.newArrayList(
                  CloseableIterable.filter(
                      entries,
                      e ->
                          icebergMaintainer.mayExpired(e, expirationConfig, partitionFreshness, expireTimestamp))));
      CloseableIterable.filter(
              mayExpiredFiles, e -> icebergMaintainer.willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(expiredFiles::addFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Lists.newArrayList(expiredFiles);
  }

  @Override
  public void doExpireFiles(List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp) {
    icebergMaintainer.expireFiles(
        IcebergTableUtil.getSnapshotId(icebergMaintainer.getTable(), false),
        expiredFiles.get(0),
        expireTimestamp);
  }
}
