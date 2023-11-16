package com.netease.arctic.server.optimizing.maintainer;

import com.google.common.collect.Lists;
import com.netease.arctic.IcebergFileEntry;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;

public class MixedMaintainStrategy implements MaintainStrategy {

  private final MixedTableMaintainer mixedTableMaintainer;

  public MixedMaintainStrategy(MixedTableMaintainer mixedTableMaintainer) {
    this.mixedTableMaintainer = mixedTableMaintainer;
  }


  private List<IcebergTableMaintainer.ExpireFiles> keyedEntryFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    KeyedTable keyedTable = mixedTableMaintainer.getArcticTable().asKeyedTable();
    ChangeTable changeTable = keyedTable.changeTable();
    BaseTable baseTable = keyedTable.baseTable();

    CloseableIterable<IcebergFileEntry> changeEntries =
        mixedTableMaintainer.getChangeMaintainer().fileScan(changeTable, dataFilter);
    CloseableIterable<IcebergFileEntry> baseEntries =
        mixedTableMaintainer.getBaseMaintainer().fileScan(baseTable, dataFilter);
    IcebergTableMaintainer.ExpireFiles changeExpiredFiles = new IcebergTableMaintainer.ExpireFiles();
    IcebergTableMaintainer.ExpireFiles baseExpiredFiles = new IcebergTableMaintainer.ExpireFiles();

    CloseableIterable<IcebergTableMaintainer.FileEntry> changed =
        CloseableIterable.transform(changeEntries, e -> new IcebergTableMaintainer.FileEntry(e, true));
    CloseableIterable<IcebergTableMaintainer.FileEntry> based =
        CloseableIterable.transform(baseEntries, e -> new IcebergTableMaintainer.FileEntry(e, false));

    try (CloseableIterable<IcebergTableMaintainer.FileEntry> entries =
        CloseableIterable.withNoopClose(com.google.common.collect.Iterables.concat(changed, based))) {
      CloseableIterable<IcebergTableMaintainer.FileEntry> mayExpiredFiles =
          CloseableIterable.withNoopClose(
              Lists.newArrayList(
                  CloseableIterable.filter(
                      entries,
                      e -> mixedTableMaintainer.getChangeMaintainer().mayExpired(
                          e, expirationConfig, partitionFreshness, expireTimestamp))));
      CloseableIterable.filter(
              mayExpiredFiles,
              e -> mixedTableMaintainer.getChangeMaintainer().willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(
              e -> {
                if (e.isChange()) {
                  changeExpiredFiles.addFile(e);
                } else {
                  baseExpiredFiles.addFile(e);
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return Lists.newArrayList(changeExpiredFiles, baseExpiredFiles);
  }

  @Override
  public List<IcebergTableMaintainer.ExpireFiles> entryFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    return mixedTableMaintainer.getArcticTable().isKeyedTable() ?
        keyedEntryFileScan(expirationConfig, dataFilter, expireTimestamp, partitionFreshness) :
        new IcebergMaintainStrategy(mixedTableMaintainer.getBaseMaintainer())
            .entryFileScan(expirationConfig, dataFilter, expireTimestamp, partitionFreshness);
  }

  @Override
  public void doExpireFiles(List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp) {
    AtomicInteger index = new AtomicInteger();
    Optional.ofNullable(mixedTableMaintainer.getChangeMaintainer())
            .ifPresent(c ->
                c.expireFiles(
                    IcebergTableUtil.getSnapshotId(mixedTableMaintainer.getChangeMaintainer().getTable(), false),
                    expiredFiles.get(index.getAndIncrement()),
                    expireTimestamp)
            );
    mixedTableMaintainer.getBaseMaintainer()
        .expireFiles(
            IcebergTableUtil.getSnapshotId(mixedTableMaintainer.getBaseMaintainer().getTable(), false),
            expiredFiles.get(index.get()),
            expireTimestamp);
  }
}
