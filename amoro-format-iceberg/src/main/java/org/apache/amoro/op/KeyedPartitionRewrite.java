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

package org.apache.amoro.op;

import org.apache.amoro.shade.guava32.com.google.common.base.Preconditions;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.amoro.utils.StatisticsFileUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.events.CreateSnapshotEvent;
import org.apache.iceberg.util.StructLikeMap;

import java.util.Collections;
import java.util.List;

/** Replace {@link BaseTable} partition files and change max transaction id map */
public class KeyedPartitionRewrite extends PartitionTransactionOperation
    implements RewritePartitions {

  protected List<DataFile> addFiles = Lists.newArrayList();
  private Long optimizedSequence;

  public KeyedPartitionRewrite(KeyedTable keyedTable) {
    super(keyedTable);
  }

  @Override
  public KeyedPartitionRewrite addDataFile(DataFile dataFile) {
    this.addFiles.add(dataFile);
    return this;
  }

  @Override
  public KeyedPartitionRewrite updateOptimizedSequenceDynamically(long sequence) {
    this.optimizedSequence = sequence;
    return this;
  }

  @Override
  protected List<StatisticsFile> apply(Transaction transaction) {
    PartitionSpec spec = transaction.table().spec();
    if (this.addFiles.isEmpty()) {
      return Collections.emptyList();
    }

    Preconditions.checkNotNull(this.optimizedSequence, "optimized sequence must be set.");
    Preconditions.checkArgument(this.optimizedSequence > 0, "optimized sequence must > 0.");

    ReplacePartitions replacePartitions = transaction.newReplacePartitions();
    replacePartitions.set(MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE_EXIST, "true");
    addFiles.forEach(replacePartitions::addFile);
    replacePartitions.commit();
    CreateSnapshotEvent newSnapshot = (CreateSnapshotEvent) replacePartitions.updateEvent();

    StructLikeMap<Long> oldOptimizedSequence = MixedTableUtil.readOptimizedSequence(keyedTable);
    StructLikeMap<Long> optimizedSequence = StructLikeMap.create(spec.partitionType());
    if (oldOptimizedSequence != null) {
      optimizedSequence.putAll(oldOptimizedSequence);
    }
    addFiles.forEach(f -> optimizedSequence.put(f.partition(), this.optimizedSequence));

    Table table = transaction.table();
    StatisticsFile statisticsFile =
        StatisticsFileUtil.writerBuilder(table)
            .withSnapshotId(newSnapshot.snapshotId())
            .build()
            .add(
                MixedTableUtil.BLOB_TYPE_OPTIMIZED_SEQUENCE,
                optimizedSequence,
                StatisticsFileUtil.createPartitionDataSerializer(table.spec(), Long.class))
            .complete();
    return Collections.singletonList(statisticsFile);
  }

  @Override
  protected boolean isEmptyCommit() {
    return this.addFiles.isEmpty();
  }
}
