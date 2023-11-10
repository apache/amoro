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
import com.netease.arctic.spark.actions.optimizing.TableFileScanHelper;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableProperties;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.actions.BinPackStrategy;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;


public class HiveRewriteFilter implements Predicate<DataFile> {
  private final int rewriteSmallFileThreshold;
  private final Map<String, Integer> partitionSmallFiles = Maps.newHashMap();
  private final Set<String> needRewriteHivePartition = Sets.newHashSet();
  private final PartitionSpec spec;
  private final ArcticTable table;

  public HiveRewriteFilter(ArcticTable table, CloseableIterable<TableFileScanHelper.FileScanResult> fileScanResults) {
    this.table = table;
    this.spec = table.spec();
    this.rewriteSmallFileThreshold = PropertyUtil.propertyAsInt(table.properties(),
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET,
        TableProperties.BASE_FILE_INDEX_HASH_BUCKET_DEFAULT) * 2;
    findNeedRewriteHivePartition(table, fileScanResults);
  }

  @Override
  public boolean test(DataFile file) {
    String hiveLocation = ((SupportHive) table).hiveLocation();
    String partition = spec.partitionToPath(file.partition());
    return !file.path().toString().contains(hiveLocation) || needRewriteHivePartition.contains(partition);
  }

  public Set<String> needRewriteHivePartition() {
    return needRewriteHivePartition;
  }

  private void findNeedRewriteHivePartition(
      ArcticTable table,
      CloseableIterable<TableFileScanHelper.FileScanResult> fileScanResults) {
    long targetSize = PropertyUtil.propertyAsLong(
        table.properties(),
        com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
        com.netease.arctic.table.TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);

    String hiveLocation = ((SupportHive) table).hiveLocation();
    fileScanResults.forEach(fileScanResult -> {
      DataFile dataFile = fileScanResult.file();
      if (dataFile.path().toString().contains(hiveLocation)) {
        String partition = spec.partitionToPath(dataFile.partition());
        if (needRewriteHivePartition.contains(partition)) {
          return;
        }
        if (!fileScanResult.deleteFiles().isEmpty()) {
          needRewriteHivePartition.add(partition);
        } else if (dataFile.fileSizeInBytes() < targetSize * BinPackStrategy.MIN_FILE_SIZE_DEFAULT_RATIO) {
          partitionSmallFiles.compute(
              partition,
              (k, v) -> {
                if (v == null) {
                  v = 0;
                }
                return v + 1;
              });
          if (partitionSmallFiles.get(partition) > rewriteSmallFileThreshold) {
            needRewriteHivePartition.add(partition);
          }
        }
      }
    });
  }
}
