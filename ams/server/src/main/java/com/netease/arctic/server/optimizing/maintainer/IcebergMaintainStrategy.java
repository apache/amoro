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
import org.apache.iceberg.StructLike;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.io.CloseableIterable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedTransferQueue;

public class IcebergMaintainStrategy implements MaintainStrategy {
  private final IcebergTableMaintainer icebergMaintainer;

  public IcebergMaintainStrategy(IcebergTableMaintainer maintainer) {
    this.icebergMaintainer = maintainer;
  }

  @Override
  public List<IcebergTableMaintainer.ExpireFiles> expiredFileScan(
      DataExpirationConfig expirationConfig,
      Expression dataFilter,
      long expireTimestamp,
      Map<StructLike, IcebergTableMaintainer.DataFileFreshness> partitionFreshness) {
    IcebergTableMaintainer.ExpireFiles expiredFiles = new IcebergTableMaintainer.ExpireFiles();
    try (CloseableIterable<IcebergTableMaintainer.FileEntry> entries =
        icebergMaintainer.fileScan(icebergMaintainer.getTable(), dataFilter, expirationConfig)) {
      Queue<IcebergTableMaintainer.FileEntry> fileEntries = new LinkedTransferQueue<>();
      entries.forEach(
          e -> {
            if (icebergMaintainer.mayExpired(e, partitionFreshness, expireTimestamp)) {
              fileEntries.add(e);
            }
          });
      fileEntries
          .parallelStream()
          .filter(e -> icebergMaintainer.willNotRetain(e, expirationConfig, partitionFreshness))
          .forEach(expiredFiles::addFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Lists.newArrayList(expiredFiles);
  }

  @Override
  public void doExpireFiles(
      List<IcebergTableMaintainer.ExpireFiles> expiredFiles, long expireTimestamp) {
    icebergMaintainer.expireFiles(
        IcebergTableUtil.getSnapshotId(icebergMaintainer.getTable(), false),
        expiredFiles.get(0),
        expireTimestamp);
  }
}
