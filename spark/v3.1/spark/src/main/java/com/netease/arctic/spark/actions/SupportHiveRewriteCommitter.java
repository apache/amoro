/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.actions;

import com.netease.arctic.hive.table.SupportHive;
import com.netease.arctic.hive.utils.HivePartitionUtil;
import com.netease.arctic.hive.utils.HiveTableUtil;
import com.netease.arctic.spark.actions.optimizing.TableFileScanHelper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;
import org.apache.thrift.TException;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SupportHiveRewriteCommitter extends MixFormatRewriteCommitter {

  private static final Logger LOG = LoggerFactory.getLogger(MixFormatRewriteCommitter.class);
  private final Set<String> needRewriteHivePartition;
  Map<String, String> partitionPathMap = new HashMap<>();
  PartitionSpec spec;
  Set<String> emptyDir = Sets.newHashSet();

  public SupportHiveRewriteCommitter(
      ArcticTable table,
      Set<String> needRewriteHivePartition,
      Set<String> needRewritePartition,
      CloseableIterable<TableFileScanHelper.FileScanResult> rewriteFiles,
      long snapshotId) {
    super(table, needRewritePartition, rewriteFiles, snapshotId);
    this.spec = table.spec();
    this.needRewriteHivePartition = needRewriteHivePartition;
  }

  @Override
  public void apply() {
    Types.StructType partitionSchema = spec.partitionType();
    for (DataFile addDatafile : addDatafiles) {
      String partition = spec.partitionToPath(addDatafile.partition());
      if (!needRewriteHivePartition.contains(partition)) {
        try {
          moveFileToHive(table, partition, addDatafile, partitionSchema);
        } catch (Exception e) {
          throw new RuntimeException("move file to hive location failed", e);
        }
      }
    }
    super.apply();
  }

  @Override
  public void commitOrClean() {
    super.commitOrClean();
    emptyDir.forEach(dir -> {
      try {
        table.io().deleteFile(dir);
      } catch (Exception e) {
        LOG.warn("delete empty dir {} failed", dir, e);
      }
    });
  }

  @Override
  public BaseRewriteAction.RewriteResult result() {
    return BaseRewriteAction.RewriteResult.of(
        removeDataFile.size() + removeDeleteFile.size(),
        removeDeleteFile.size(),
        needRewritePartition.size(), needRewriteHivePartition.size(), String.join(",", needRewritePartition),
        addDatafiles.size());
  }

  private void moveFileToHive(
      ArcticTable table, String partition, DataFile targetFile,
      Types.StructType partitionSchema) throws TException, InterruptedException {
    String partitionPath = partitionPathMap.get(partition);
    if (partitionPath == null) {
      List<String> partitionValues =
          HivePartitionUtil.partitionValuesAsList(targetFile.partition(), partitionSchema);
      if (spec.isUnpartitioned()) {
        Table hiveTable = ((SupportHive) table).getHMSClient().run(client ->
            client.getTable(table.id().getDatabase(), table.id().getTableName()));
        partitionPath = hiveTable.getSd().getLocation();
      } else {
        String hiveSubdirectory = table.isKeyedTable() ?
            HiveTableUtil.newHiveSubdirectory(maxTransactionId) : HiveTableUtil.newHiveSubdirectory();

        Partition p = HivePartitionUtil.getPartition(((SupportHive) table).getHMSClient(), table,
            partitionValues);
        if (p == null) {
          partitionPath = HiveTableUtil.newHiveDataLocation(((SupportHive) table).hiveLocation(),
              spec, targetFile.partition(), hiveSubdirectory);
        } else {
          partitionPath = p.getSd().getLocation();
        }
      }
      partitionPathMap.put(partition, partitionPath);
    }
    moveTargetFiles(table, targetFile, partitionPath);
  }

  private void moveTargetFiles(ArcticTable arcticTable, DataFile targetFile, String hiveLocation) {
    String oldFilePath = targetFile.path().toString();
    String newFilePath = TableFileUtils.getNewFilePath(hiveLocation, oldFilePath);

    if (!arcticTable.io().exists(newFilePath)) {
      if (!arcticTable.io().exists(hiveLocation)) {
        LOG.debug("{} hive location {} does not exist and need to mkdir before rename", arcticTable.id(), hiveLocation);
        arcticTable.io().mkdirs(hiveLocation);
      }
      arcticTable.io().rename(oldFilePath, newFilePath);
      LOG.debug("{} move file from {} to {}", arcticTable.id(), oldFilePath, newFilePath);
    }

    ((StructLike) targetFile).set(1, newFilePath);
    emptyDir.add(TableFileUtils.getFileDir(oldFilePath));
  }
}
