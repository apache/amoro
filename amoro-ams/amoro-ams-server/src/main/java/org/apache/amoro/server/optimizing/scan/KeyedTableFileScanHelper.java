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

package org.apache.amoro.server.optimizing.scan;

import org.apache.amoro.data.DataFileType;
import org.apache.amoro.data.DataTreeNode;
import org.apache.amoro.data.DefaultKeyedFile;
import org.apache.amoro.data.FileNameRules;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.table.KeyedTableSnapshot;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.TableProperties;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.amoro.utils.CompatiblePropertyUtil;
import org.apache.amoro.utils.MixedTableUtil;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.util.PropertyUtil;
import org.apache.iceberg.util.StructLikeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyedTableFileScanHelper implements TableFileScanHelper {
  private static final Logger LOG = LoggerFactory.getLogger(KeyedTableFileScanHelper.class);

  private final KeyedTable keyedTable;
  private final long changeSnapshotId;
  private final long baseSnapshotId;
  private Expression partitionFilter = Expressions.alwaysTrue();

  public KeyedTableFileScanHelper(KeyedTable keyedTable, KeyedTableSnapshot snapshot) {
    this.keyedTable = keyedTable;
    this.baseSnapshotId = snapshot.baseSnapshotId();
    this.changeSnapshotId = snapshot.changeSnapshotId();
  }

  /**
   * Select all the files whose sequence <= maxSequence as Selected-Files, seek the maxSequence to
   * find as many Selected-Files as possible, and also - the cnt of these Selected-Files must <=
   * maxFileCntLimit - the max TransactionId of the Selected-Files must > the min TransactionId of
   * all the left files
   *
   * @param snapshotFileGroups snapshotFileGroups
   * @param maxFileCntLimit maxFileCntLimit
   * @return the max sequence of selected file, return Long.MAX_VALUE if all files should be
   *     selected, Long.MIN_VALUE means no files should be selected
   */
  static long getMaxSequenceKeepingTxIdInOrder(
      List<SnapshotFileGroup> snapshotFileGroups, long maxFileCntLimit) {
    if (maxFileCntLimit <= 0 || snapshotFileGroups == null || snapshotFileGroups.isEmpty()) {
      return Long.MIN_VALUE;
    }
    // 1.sort sequence
    Collections.sort(snapshotFileGroups);
    // 2.find the max index where all file cnt <= maxFileCntLimit
    int index = -1;
    int fileCnt = 0;
    for (int i = 0; i < snapshotFileGroups.size(); i++) {
      fileCnt += snapshotFileGroups.get(i).getFileCnt();
      if (fileCnt <= maxFileCntLimit) {
        index = i;
      } else {
        break;
      }
    }
    // all files cnt <= maxFileCntLimit, return all files
    if (fileCnt <= maxFileCntLimit) {
      return Long.MAX_VALUE;
    }
    // if only check the first file groups, then the file cnt > maxFileCntLimit, no files should be
    // selected
    if (index == -1) {
      return Long.MIN_VALUE;
    }

    // 3.wrap file group with the max TransactionId before and min TransactionId after
    List<SnapshotFileGroupWrapper> snapshotFileGroupWrappers =
        wrapMinMaxTransactionId(snapshotFileGroups);
    // 4.find the valid snapshotFileGroup
    while (true) {
      SnapshotFileGroupWrapper current = snapshotFileGroupWrappers.get(index);
      // check transaction id inorder: max transaction id before(inclusive) < min transaction id
      // after
      if (Math.max(current.getFileGroup().getTransactionId(), current.getMaxTransactionIdBefore())
          < current.getMinTransactionIdAfter()) {
        return current.getFileGroup().getSequence();
      }
      index--;
      if (index == -1) {
        return Long.MIN_VALUE;
      }
    }
  }

  private static List<SnapshotFileGroupWrapper> wrapMinMaxTransactionId(
      List<SnapshotFileGroup> snapshotFileGroups) {
    List<SnapshotFileGroupWrapper> wrappedList = new ArrayList<>();
    for (SnapshotFileGroup snapshotFileGroup : snapshotFileGroups) {
      wrappedList.add(new SnapshotFileGroupWrapper(snapshotFileGroup));
    }
    long maxValue = Long.MIN_VALUE;
    for (SnapshotFileGroupWrapper wrapper : wrappedList) {
      wrapper.setMaxTransactionIdBefore(maxValue);
      if (wrapper.getFileGroup().getTransactionId() > maxValue) {
        maxValue = wrapper.getFileGroup().getTransactionId();
      }
    }
    long minValue = Long.MAX_VALUE;
    for (int i = wrappedList.size() - 1; i >= 0; i--) {
      SnapshotFileGroupWrapper wrapper = wrappedList.get(i);
      wrapper.setMinTransactionIdAfter(minValue);
      if (wrapper.getFileGroup().getTransactionId() < minValue) {
        minValue = wrapper.getFileGroup().getTransactionId();
      }
    }
    return wrappedList;
  }

  @Override
  public CloseableIterable<FileScanResult> scan() {
    CloseableIterable<FileScanResult> changeScanResult = CloseableIterable.empty();
    ChangeFiles changeFiles = new ChangeFiles(keyedTable);
    UnkeyedTable baseTable = keyedTable.baseTable();
    ChangeTable changeTable = keyedTable.changeTable();
    if (changeSnapshotId != AmoroServiceConstants.INVALID_SNAPSHOT_ID) {
      StructLikeMap<Long> optimizedSequence =
          baseSnapshotId == AmoroServiceConstants.INVALID_SNAPSHOT_ID
              ? StructLikeMap.create(keyedTable.spec().partitionType())
              : MixedTableUtil.readOptimizedSequence(keyedTable, baseSnapshotId);
      long maxSequence = getMaxSequenceLimit(keyedTable, changeSnapshotId, optimizedSequence);
      if (maxSequence != Long.MIN_VALUE) {
        ChangeTableIncrementalScan changeTableIncrementalScan =
            changeTable
                .newScan()
                .filter(partitionFilter)
                .fromSequence(optimizedSequence)
                .toSequence(maxSequence)
                .useSnapshot(changeSnapshotId);
        try (CloseableIterable<FileScanTask> fileScanTasks =
            changeTableIncrementalScan.planFiles()) {
          for (FileScanTask fileScanTask : fileScanTasks) {
            changeFiles.addFile(wrapChangeFile(fileScanTask.file()));
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
        PartitionSpec partitionSpec = changeTable.spec();
        changeScanResult =
            CloseableIterable.withNoopClose(
                changeFiles
                    .allInsertFiles()
                    .map(
                        insertFile -> {
                          List<ContentFile<?>> relatedDeleteFiles =
                              changeFiles.getRelatedDeleteFiles(insertFile);
                          return new FileScanResult(insertFile, relatedDeleteFiles);
                        })
                    .collect(Collectors.toList()));
      }
    }

    CloseableIterable<FileScanResult> baseScanResult = CloseableIterable.empty();
    if (baseSnapshotId != AmoroServiceConstants.INVALID_SNAPSHOT_ID) {
      baseScanResult =
          CloseableIterable.transform(
              baseTable.newScan().filter(partitionFilter).useSnapshot(baseSnapshotId).planFiles(),
              fileScanTask -> {
                DataFile dataFile = wrapBaseFile(fileScanTask.file());
                List<ContentFile<?>> deleteFiles = new ArrayList<>(fileScanTask.deletes());
                List<ContentFile<?>> relatedChangeDeleteFiles =
                    changeFiles.getRelatedDeleteFiles(dataFile);
                deleteFiles.addAll(relatedChangeDeleteFiles);
                return new FileScanResult(dataFile, deleteFiles);
              });
    }

    return CloseableIterable.concat(Lists.newArrayList(changeScanResult, baseScanResult));
  }

  @Override
  public KeyedTableFileScanHelper withPartitionFilter(Expression partitionFilter) {
    this.partitionFilter = partitionFilter;
    return this;
  }

  private DataFile wrapChangeFile(DataFile dataFile) {
    return DefaultKeyedFile.parseChange(dataFile);
  }

  private DataFile wrapBaseFile(DataFile dataFile) {
    return DefaultKeyedFile.parseBase(dataFile);
  }

  private long getMaxSequenceLimit(
      KeyedTable keyedTable,
      long changeSnapshotId,
      StructLikeMap<Long> partitionOptimizedSequence) {
    ChangeTable changeTable = keyedTable.changeTable();
    Snapshot changeSnapshot = changeTable.snapshot(changeSnapshotId);
    int totalFilesInSummary =
        PropertyUtil.propertyAsInt(
            changeSnapshot.summary(), SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
    int maxFileCntLimit =
        CompatiblePropertyUtil.propertyAsInt(
            keyedTable.properties(),
            TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT,
            TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT);
    // not scan files to improve performance
    if (totalFilesInSummary <= maxFileCntLimit) {
      return Long.MAX_VALUE;
    }
    // scan and get all change files grouped by sequence(snapshot)
    ChangeTableIncrementalScan changeTableIncrementalScan =
        changeTable
            .newScan()
            .filter(partitionFilter)
            .fromSequence(partitionOptimizedSequence)
            .useSnapshot(changeSnapshot.snapshotId());
    Map<Long, SnapshotFileGroup> changeFilesGroupBySequence = new HashMap<>();
    try (CloseableIterable<FileScanTask> tasks = changeTableIncrementalScan.planFiles()) {
      for (FileScanTask task : tasks) {
        SnapshotFileGroup fileGroup =
            changeFilesGroupBySequence.computeIfAbsent(
                task.file().dataSequenceNumber(),
                key -> {
                  long txId =
                      FileNameRules.parseChangeTransactionId(
                          task.file().path().toString(), task.file().dataSequenceNumber());
                  return new SnapshotFileGroup(task.file().dataSequenceNumber(), txId);
                });
        fileGroup.addFile();
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close table scan of " + keyedTable.name(), e);
    }

    if (changeFilesGroupBySequence.isEmpty()) {
      LOG.debug(
          "{} get no change files to optimize with partitionOptimizedSequence {}",
          keyedTable.name(),
          partitionOptimizedSequence);
      return Long.MIN_VALUE;
    }

    long maxSequence =
        getMaxSequenceKeepingTxIdInOrder(
            new ArrayList<>(changeFilesGroupBySequence.values()), maxFileCntLimit);
    if (maxSequence == Long.MIN_VALUE) {
      LOG.warn(
          "{} get no change files with self-optimizing.max-file-count={}, change it to a bigger value",
          keyedTable.name(),
          maxFileCntLimit);
    } else if (maxSequence != Long.MAX_VALUE) {
      LOG.warn(
          "{} not all change files optimized with self-optimizing.max-file-count={}, maxSequence={}",
          keyedTable.name(),
          maxFileCntLimit,
          maxSequence);
    }
    return maxSequence;
  }

  private static class ChangeFiles {
    private final KeyedTable keyedTable;
    private final Map<String, Map<DataTreeNode, List<ContentFile<?>>>> cachedRelatedDeleteFiles =
        Maps.newHashMap();

    private final Map<String, Map<DataTreeNode, Set<ContentFile<?>>>> equalityDeleteFiles =
        Maps.newHashMap();
    private final Map<String, Map<DataTreeNode, Set<DataFile>>> insertFiles = Maps.newHashMap();

    public ChangeFiles(KeyedTable keyedTable) {
      this.keyedTable = keyedTable;
    }

    public void addFile(DataFile file) {
      String partition = keyedTable.spec().partitionToPath(file.partition());
      DataTreeNode node = FileNameRules.parseFileNodeFromFileName(file.path().toString());
      DataFileType type = FileNameRules.parseFileTypeForChange(file.path().toString());
      switch (type) {
        case EQ_DELETE_FILE:
          equalityDeleteFiles
              .computeIfAbsent(partition, key -> Maps.newHashMap())
              .computeIfAbsent(node, key -> Sets.newHashSet())
              .add(file);
          break;
        case INSERT_FILE:
          insertFiles
              .computeIfAbsent(partition, key -> Maps.newHashMap())
              .computeIfAbsent(node, key -> Sets.newHashSet())
              .add(file);
          break;
        default:
          throw new IllegalStateException("illegal file type in change store " + type);
      }
    }

    public Stream<DataFile> allInsertFiles() {
      return insertFiles.values().stream()
          .flatMap(dataTreeNodeSetMap -> dataTreeNodeSetMap.values().stream())
          .flatMap(Collection::stream);
    }

    public List<ContentFile<?>> getRelatedDeleteFiles(DataFile file) {
      String partition = keyedTable.spec().partitionToPath(file.partition());
      if (!equalityDeleteFiles.containsKey(partition)) {
        return Collections.emptyList();
      }
      DataTreeNode node = FileNameRules.parseFileNodeFromFileName(file.path().toString());
      Map<DataTreeNode, List<ContentFile<?>>> partitionDeleteFiles =
          cachedRelatedDeleteFiles.computeIfAbsent(partition, key -> Maps.newHashMap());
      if (partitionDeleteFiles.containsKey(node)) {
        return partitionDeleteFiles.get(node);
      } else {
        List<ContentFile<?>> result = Lists.newArrayList();
        for (Map.Entry<DataTreeNode, Set<ContentFile<?>>> entry :
            equalityDeleteFiles.get(partition).entrySet()) {
          DataTreeNode deleteNode = entry.getKey();
          if (node.isSonOf(deleteNode) || deleteNode.isSonOf(node)) {
            result.addAll(entry.getValue());
          }
        }
        partitionDeleteFiles.put(node, result);
        return result;
      }
    }
  }

  /** Files grouped by snapshot, but only with the file cnt. */
  static class SnapshotFileGroup implements Comparable<SnapshotFileGroup> {
    private final long sequence;
    private final long transactionId;
    private int fileCnt = 0;

    public SnapshotFileGroup(long sequence, long transactionId) {
      this.sequence = sequence;
      this.transactionId = transactionId;
    }

    public SnapshotFileGroup(long sequence, long transactionId, int fileCnt) {
      this.sequence = sequence;
      this.transactionId = transactionId;
      this.fileCnt = fileCnt;
    }

    public void addFile() {
      fileCnt++;
    }

    public long getTransactionId() {
      return transactionId;
    }

    public int getFileCnt() {
      return fileCnt;
    }

    public long getSequence() {
      return sequence;
    }

    @Override
    public int compareTo(SnapshotFileGroup o) {
      return Long.compare(this.sequence, o.sequence);
    }
  }

  /** Wrap SnapshotFileGroup with max and min Transaction Id */
  private static class SnapshotFileGroupWrapper {
    private final SnapshotFileGroup fileGroup;
    // in the ordered file group list, the max transaction before this file group, Long.MIN_VALUE
    // for the first
    private long maxTransactionIdBefore;
    // in the ordered file group list, the min transaction after this file group, Long.MAX_VALUE for
    // the last
    private long minTransactionIdAfter;

    public SnapshotFileGroupWrapper(SnapshotFileGroup fileGroup) {
      this.fileGroup = fileGroup;
    }

    public SnapshotFileGroup getFileGroup() {
      return fileGroup;
    }

    public long getMaxTransactionIdBefore() {
      return maxTransactionIdBefore;
    }

    public void setMaxTransactionIdBefore(long maxTransactionIdBefore) {
      this.maxTransactionIdBefore = maxTransactionIdBefore;
    }

    public long getMinTransactionIdAfter() {
      return minTransactionIdAfter;
    }

    public void setMinTransactionIdAfter(long minTransactionIdAfter) {
      this.minTransactionIdAfter = minTransactionIdAfter;
    }
  }
}
