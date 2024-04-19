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

package org.apache.amoro.server.optimizing.commit;

import org.apache.amoro.BasicTableTestHelper;
import org.apache.amoro.TableFormat;
import org.apache.amoro.TableTestHelper;
import org.apache.amoro.catalog.BasicCatalogTestHelper;
import org.apache.amoro.catalog.CatalogTestHelper;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.DefaultKeyedFile;
import org.apache.amoro.optimizing.RewriteFilesInput;
import org.apache.amoro.optimizing.RewriteFilesOutput;
import org.apache.amoro.scan.CombinedScanTask;
import org.apache.amoro.scan.KeyedTableScanTask;
import org.apache.amoro.scan.MixedFileScanTask;
import org.apache.amoro.server.exception.OptimizingCommitException;
import org.apache.amoro.server.optimizing.KeyedTableCommit;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.utils.ContentFiles;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DataFiles;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.data.GenericRecord;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RunWith(Parameterized.class)
public class TestMixIcebergCommit extends TestUnKeyedTableCommit {

  public TestMixIcebergCommit(
      CatalogTestHelper catalogTestHelper, TableTestHelper tableTestHelper) {
    super(catalogTestHelper, tableTestHelper);
  }

  @Parameterized.Parameters(name = "commit_test")
  public static Object[] parameters() {
    return new Object[][] {
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, true)},
      {new BasicCatalogTestHelper(TableFormat.MIXED_ICEBERG), new BasicTableTestHelper(true, false)}
    };
  }

  @Before
  public void initTableFile() {
    mixedTable = getArcticTable();
    spec = mixedTable.spec();
    partitionData = GenericRecord.create(spec.schema());
    partitionData.set(0, 1);
    partitionPath = spec.partitionToPath(partitionData);
  }

  protected void addFile(DataFile dataFile) {
    mixedTable.asKeyedTable().changeTable().newAppend().appendFile(dataFile).commit();
  }

  protected Map<String, ContentFile<?>> getAllFiles() {
    Map<String, ContentFile<?>> maps = new HashMap<>();
    CloseableIterable<CombinedScanTask> combinedScanTasks =
        mixedTable.asKeyedTable().newScan().planTasks();
    for (CombinedScanTask combinedScanTask : combinedScanTasks) {
      for (KeyedTableScanTask keyedTableScanTask : combinedScanTask.tasks()) {
        for (MixedFileScanTask task : keyedTableScanTask.dataTasks()) {
          maps.put(task.file().path().toString(), task.file());
          for (DeleteFile deleteFile : task.deletes()) {
            maps.put(deleteFile.path().toString(), deleteFile);
          }
        }
        for (MixedFileScanTask task : keyedTableScanTask.arcticEquityDeletes()) {
          maps.put(task.file().path().toString(), task.file());
        }
      }
    }
    return maps;
  }

  protected DataFile getEqualityDeleteFile() {
    return DataFiles.builder(spec)
        .withPath(String.format("1-ED-0-00000-0-00-%s.parquet", fileSeq++))
        .withFileSizeInBytes(10)
        .withPartitionPath(partitionPath)
        .withRecordCount(1)
        .withFormat(FileFormat.PARQUET)
        .build();
  }

  protected void addDelete(ContentFile<?> contentFile) {
    addFile((DataFile) contentFile);
  }

  protected void execute(
      DataFile[] rewriteData,
      DataFile[] rewritePos,
      ContentFile<?>[] deletes,
      DataFile[] dataOutput,
      DeleteFile[] deleteOutput)
      throws OptimizingCommitException {
    RewriteFilesInput input = getRewriteInput(rewriteData, rewritePos, deletes);
    RewriteFilesOutput output = new RewriteFilesOutput(dataOutput, deleteOutput, null);
    StructLikeMap<Long> fromSequence = getFromSequenceOfPartitions(input);
    StructLikeMap<Long> toSequence = getToSequenceOfPartitions(input);
    TaskRuntime taskRuntime = Mockito.mock(TaskRuntime.class);
    Mockito.when(taskRuntime.getPartition()).thenReturn(partitionPath);
    Mockito.when(taskRuntime.getInput()).thenReturn(input);
    Mockito.when(taskRuntime.getOutput()).thenReturn(output);
    KeyedTableCommit commit =
        new KeyedTableCommit(
            getArcticTable(),
            Collections.singletonList(taskRuntime),
            Optional.ofNullable(mixedTable.asKeyedTable().baseTable().currentSnapshot())
                .map(Snapshot::snapshotId)
                .orElse(null),
            fromSequence,
            toSequence);
    commit.commit();
  }

  private StructLikeMap<Long> getFromSequenceOfPartitions(RewriteFilesInput input) {
    long minSequence = Long.MAX_VALUE;
    for (ContentFile<?> contentFile : input.allFiles()) {
      if (ContentFiles.isDeleteFile(contentFile)) {
        continue;
      }
      DataFileType type = ((DefaultKeyedFile) (ContentFiles.asDataFile(contentFile))).type();
      if (type == DataFileType.INSERT_FILE || type == DataFileType.EQ_DELETE_FILE) {
        minSequence = Math.min(minSequence, contentFile.dataSequenceNumber());
      }
    }
    StructLikeMap<Long> structLikeMap = StructLikeMap.create(spec.partitionType());
    if (minSequence != Long.MAX_VALUE) {
      structLikeMap.put(partitionData, minSequence);
    }
    return structLikeMap;
  }

  private StructLikeMap<Long> getToSequenceOfPartitions(RewriteFilesInput input) {
    long minSequence = -1;
    for (ContentFile<?> contentFile : input.allFiles()) {
      if (ContentFiles.isDeleteFile(contentFile)) {
        continue;
      }
      DataFileType type = ((DefaultKeyedFile) (ContentFiles.asDataFile(contentFile))).type();
      if (type == DataFileType.INSERT_FILE || type == DataFileType.EQ_DELETE_FILE) {
        minSequence = Math.max(minSequence, contentFile.dataSequenceNumber());
      }
    }
    StructLikeMap<Long> structLikeMap = StructLikeMap.create(spec.partitionType());
    if (minSequence != -1) {
      structLikeMap.put(partitionData, minSequence);
    }
    return structLikeMap;
  }

  private RewriteFilesInput getRewriteInput(
      DataFile[] rewriteDataFiles, DataFile[] rePositionDataFiles, ContentFile<?>[] deleteFiles) {
    Map<String, ContentFile<?>> allFiles = getAllFiles();

    DataFile[] rewriteData = null;
    if (rewriteDataFiles != null) {
      rewriteData =
          Arrays.stream(rewriteDataFiles)
              .map(s -> (DefaultKeyedFile) allFiles.get(s.path().toString()))
              .toArray(DataFile[]::new);
    }

    DataFile[] rewritePos = null;
    if (rePositionDataFiles != null) {
      rewritePos =
          Arrays.stream(rePositionDataFiles)
              .map(s -> (DefaultKeyedFile) allFiles.get(s.path().toString()))
              .toArray(DataFile[]::new);
    }

    ContentFile<?>[] delete = null;
    if (deleteFiles != null) {
      delete =
          Arrays.stream(deleteFiles)
              .map(s -> allFiles.get(s.path().toString()))
              .map(
                  s -> {
                    if (s instanceof DefaultKeyedFile) {
                      return s;
                    } else {
                      return s;
                    }
                  })
              .toArray(ContentFile[]::new);
    }
    return new RewriteFilesInput(rewriteData, rewritePos, null, delete, mixedTable);
  }
}
