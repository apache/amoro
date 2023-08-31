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

package org.apache.iceberg;

import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.scan.BasicArcticFileScanTask;
import com.netease.arctic.scan.ChangeTableIncrementalScan;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.util.StructLikeMap;

import java.util.List;

public class ArcticChangeTableScan extends DataTableScan implements ChangeTableIncrementalScan {
  private StructLikeMap<Long> fromPartitionSequence;
  private Long toSequence;

  public ArcticChangeTableScan(Table table, Schema schema) {
    super(table, schema, ImmutableTableScanContext.builder().build());
  }

  protected ArcticChangeTableScan(Table table, Schema schema, TableScanContext context) {
    super(table, schema, context);
  }

  @Override
  public ArcticChangeTableScan useSnapshot(long scanSnapshotId) {
    TableScan scan = super.useSnapshot(scanSnapshotId);
    return newRefinedScan(table(), scan.schema(), context().useSnapshotId(scanSnapshotId));
  }

  @Override
  protected ArcticChangeTableScan newRefinedScan(Table table, Schema schema, TableScanContext context) {
    ArcticChangeTableScan scan = new ArcticChangeTableScan(table, schema, context);
    scan.fromPartitionSequence = this.fromPartitionSequence;
    scan.toSequence = this.toSequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan fromSequence(StructLikeMap<Long> partitionSequence) {
    ArcticChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.fromPartitionSequence = partitionSequence;
    return scan;
  }

  @Override
  public ChangeTableIncrementalScan toSequence(long sequence) {
    ArcticChangeTableScan scan = newRefinedScan(table(), schema(), context());
    scan.toSequence = sequence;
    return scan;
  }

  @Override
  public CloseableIterable<ContentFile<?>> planFilesWithSequence() {
    Snapshot snapshot = snapshot();

    FileIO io = table().io();
    List<ManifestFile> dataManifests = snapshot.dataManifests(io);
    List<ManifestFile> deleteManifests = snapshot.deleteManifests(io);
    scanMetrics().totalDataManifests().increment((long) dataManifests.size());
    scanMetrics().totalDeleteManifests().increment((long) deleteManifests.size());
    ArcticChangeManifestGroup manifestGroup =
        new ArcticChangeManifestGroup(io, dataManifests, deleteManifests)
            .caseSensitive(isCaseSensitive())
            .select(scanColumns())
            .filterData(filter())
            .specsById(table().specs())
            .scanMetrics(scanMetrics())
            .ignoreDeleted();

    if (shouldIgnoreResiduals()) {
      manifestGroup.ignoreResiduals();
    }

    if (dataManifests.size() > 1 && shouldPlanWithExecutor()) {
      manifestGroup.planWith(planExecutor());
    }
    CloseableIterable<ArcticChangeManifestGroup.ChangeFileScanTask>
        files = manifestGroup.planFilesWithSequence();

    files = CloseableIterable.filter(files, f -> {
      StructLike partition = f.file().partition();
      long sequenceNumber = f.getDataSequenceNumber();
      return shouldKeepFile(partition, sequenceNumber);
    });
    return CloseableIterable
        .transform(files, f -> f.file());
  }

  @Override
  public CloseableIterable<FileScanTask> doPlanFiles() {
    return CloseableIterable.transform(planFilesWithSequence(), fileWithSequence ->
        new BasicArcticFileScanTask(DefaultKeyedFile.parseChange(
            ((DataFile) fileWithSequence),
            fileWithSequence.dataSequenceNumber()), null, table().spec(), null)
    );
  }


  private boolean shouldKeepFile(StructLike partition, long sequence) {
    if (biggerThanToSequence(sequence)) {
      return false;
    }
    if (fromPartitionSequence == null || fromPartitionSequence.isEmpty()) {
      // if fromPartitionSequence is not set or is empty, return all change files
      return true;
    }
    if (table().spec().isUnpartitioned()) {
      Long fromSequence = fromPartitionSequence.entrySet().iterator().next().getValue();
      return sequence > fromSequence;
    } else {
      if (!fromPartitionSequence.containsKey(partition)) {
        // if fromPartitionSequence not contains this partition, return all files of this partition
        return true;
      } else {
        Long fromSequence = fromPartitionSequence.get(partition);
        return sequence > fromSequence;
      }
    }
  }

  private boolean biggerThanToSequence(long sequence) {
    return this.toSequence != null && sequence > this.toSequence;
  }

}
