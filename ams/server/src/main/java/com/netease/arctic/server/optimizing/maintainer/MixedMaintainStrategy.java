/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.optimizing.maintainer;

import com.google.common.collect.Lists;
import com.netease.arctic.server.table.DataExpirationConfig;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.BaseTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.KeyedTable;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MixedMaintainStrategy implements MaintainStrategy {

  private final MixedTableMaintainer mixedTableMaintainer;

  public MixedMaintainStrategy(MixedTableMaintainer mixedTableMaintainer) {
    this.mixedTableMaintainer = mixedTableMaintainer;
  }

  private List<IcebergTableMaintainer.ExpireFiles> keyedExpiredFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    KeyedTable keyedTable = mixedTableMaintainer.getArcticTable().asKeyedTable();
    ChangeTable changeTable = keyedTable.changeTable();
    BaseTable baseTable = keyedTable.baseTable();

    CloseableIterable<MixedTableMaintainer.MixedFileEntry> changeEntries =
        CloseableIterable.transform(
            mixedTableMaintainer
                .getChangeMaintainer()
                .fileScan(changeTable, dataFilter, expirationConfig),
            e -> new MixedTableMaintainer.MixedFileEntry(e.getFile(), e.getTsBound(), true));
    CloseableIterable<MixedTableMaintainer.MixedFileEntry> baseEntries =
        CloseableIterable.transform(
            mixedTableMaintainer
                .getBaseMaintainer()
                .fileScan(baseTable, dataFilter, expirationConfig),
            e -> new MixedTableMaintainer.MixedFileEntry(e.getFile(), e.getTsBound(), false));
    IcebergTableMaintainer.ExpireFiles changeExpiredFiles =
        new IcebergTableMaintainer.ExpireFiles();
    IcebergTableMaintainer.ExpireFiles baseExpiredFiles = new IcebergTableMaintainer.ExpireFiles();

    try (CloseableIterable<MixedTableMaintainer.MixedFileEntry> entries =
        CloseableIterable.withNoopClose(
            com.google.common.collect.Iterables.concat(changeEntries, baseEntries))) {
      Queue<MixedTableMaintainer.MixedFileEntry> fileEntries = new LinkedTransferQueue<>();
      entries.forEach(
          e -> {
            if (mixedTableMaintainer
                .getChangeMaintainer()
                .mayExpired(e, partitionFreshness, expireTimestamp)) {
              fileEntries.add(e);
            }
          });
      fileEntries
          .parallelStream()
          .filter(
              e ->
                  mixedTableMaintainer
                      .getChangeMaintainer()
                      .willNotRetain(e, expirationConfig, partitionFreshness))
          .collect(Collectors.toList())
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
  public List<IcebergTableMaintainer.ExpireFiles> expiredFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    return mixedTableMaintainer.getArcticTable().isKeyedTable()
        ? keyedExpiredFileScan(expirationConfig, dataFilter, expireTimestamp, partitionFreshness)
        : new IcebergMaintainStrategy(mixedTableMaintainer.getBaseMaintainer())
            .expiredFileScan(expirationConfig, dataFilter, expireTimestamp, partitionFreshness);
  }

  @Override
  public void doExpireFiles(
      List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp) {
    AtomicInteger index = new AtomicInteger();
    Optional.ofNullable(mixedTableMaintainer.getChangeMaintainer())
        .ifPresent(
            c ->
                c.expireFiles(
                    IcebergTableUtil.getSnapshotId(
                        mixedTableMaintainer.getChangeMaintainer().getTable(), false),
                    expiredFiles.get(index.getAndIncrement()),
                    expireTimestamp));
    mixedTableMaintainer
        .getBaseMaintainer()
        .expireFiles(
            IcebergTableUtil.getSnapshotId(
                mixedTableMaintainer.getBaseMaintainer().getTable(), false),
            expiredFiles.get(index.get()),
            expireTimestamp);
  }
}
