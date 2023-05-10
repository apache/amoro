package com.netease.arctic.server.optimizing.plan;

import com.netease.arctic.data.DataFileType;
import com.netease.arctic.data.DataTreeNode;
import com.netease.arctic.data.DefaultKeyedFile;
import com.netease.arctic.data.FileNameRules;
import com.netease.arctic.data.IcebergContentFile;
import com.netease.arctic.data.IcebergDataFile;
import com.netease.arctic.data.IcebergDeleteFile;
import com.netease.arctic.scan.ChangeTableIncrementalScan;
import com.netease.arctic.server.optimizing.OptimizingType;
import com.netease.arctic.server.table.TableRuntime;
import com.netease.arctic.server.utils.IcebergTableUtil;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.ChangeTable;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.table.UnkeyedTable;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import com.netease.arctic.utils.TablePropertyUtil;
import com.netease.arctic.utils.TableTypeUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.FileContent;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotSummary;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
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

public class OptimizingEvaluator {
  private static final Logger LOG = LoggerFactory.getLogger(OptimizingEvaluator.class);

  protected final ArcticTable arcticTable;
  protected final TableRuntime tableRuntime;
  protected boolean isInitEvaluator = false;

  protected Map<String, AbstractPartitionPlan> partitionEvaluatorMap = Maps.newHashMap();

  public OptimizingEvaluator(TableRuntime tableRuntime) {
    this(tableRuntime, tableRuntime.loadTable());
  }

  public OptimizingEvaluator(TableRuntime tableRuntime, ArcticTable table) {
    this.tableRuntime = tableRuntime;
    this.arcticTable = table;
  }

  public Map<String, Long> getFromSequence() {
    return partitionEvaluatorMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getFromSequence()));
  }

  public Map<String, Long> getToSequence() {
    return partitionEvaluatorMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getToSequence()));
  }

  protected void initEvaluator() {
    if (TableTypeUtil.isIcebergTableFormat(arcticTable)) {
      initPartitionPlans();
    } else {
      initMixedFormatPartitionPlans();
    }
    isInitEvaluator = true;
  }

  private void initPartitionPlans() {
    long targetFileSize = tableRuntime.getTargetSize();
    long maxFragmentSize = targetFileSize / tableRuntime.getOptimizingConfig().getFragmentRatio();
    List<FileScanTask> fileScanTasks;
    try (CloseableIterable<FileScanTask> filesIterable =
             arcticTable.asUnkeyedTable().newScan().useSnapshot(tableRuntime.getCurrentSnapshotId()).planFiles()) {
      fileScanTasks = Lists.newArrayList(filesIterable);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close table scan of " + arcticTable.id(), e);
    }
    PartitionSpec partitionSpec = arcticTable.asUnkeyedTable().spec();
    for (FileScanTask fileScanTask : fileScanTasks) {
      StructLike partition = fileScanTask.file().partition();
      String partitionPath = partitionSpec.partitionToPath(partition);
      if (fiterPartition(partitionPath)) {
        continue;
      }
      AbstractPartitionPlan evaluator = partitionEvaluatorMap.computeIfAbsent(partitionPath, this::buildEvaluator);
      evaluator.addFile(fileScanTask.file(), fileScanTask.deletes());
    }
    finishAddingFiles();
    partitionEvaluatorMap.values().removeIf(plan -> !plan.isNecessary());
  }
  
  private void finishAddingFiles() {
    partitionEvaluatorMap.values().forEach(AbstractPartitionPlan::finishAddFiles);
  }

  private void initMixedFormatPartitionPlans() {
    long targetFileSize = tableRuntime.getTargetSize();
    long maxFragmentSize = targetFileSize / tableRuntime.getOptimizingConfig().getFragmentRatio();
    ChangeFiles changeFiles = new ChangeFiles(arcticTable);
    UnkeyedTable baseTable;
    long baseSnapshotId;
    if (arcticTable.isKeyedTable()) {
      baseTable = arcticTable.asKeyedTable().baseTable();
      ChangeTable changeTable = arcticTable.asKeyedTable().changeTable();
      // TODO
      baseSnapshotId = IcebergTableUtil.getSnapshotId(baseTable, true);
      StructLikeMap<Long> partitionOptimizedSequence =
          TablePropertyUtil.getPartitionOptimizedSequence(arcticTable.asKeyedTable());
      StructLikeMap<Long> legacyPartitionMaxTransactionId =
          TablePropertyUtil.getLegacyPartitionMaxTransactionId(arcticTable.asKeyedTable());
      Snapshot changeSnapshot = IcebergTableUtil.getSnapshot(changeTable, true);
      if (changeSnapshot != null) {
        long maxSequence = getMaxSequenceLimit(arcticTable, changeSnapshot, partitionOptimizedSequence,
            legacyPartitionMaxTransactionId);
        if (maxSequence != Long.MIN_VALUE) {
          ChangeTableIncrementalScan changeTableIncrementalScan = changeTable.newChangeScan()
              .fromSequence(partitionOptimizedSequence)
              .fromLegacyTransaction(legacyPartitionMaxTransactionId)
              .toSequence(maxSequence)
              .useSnapshot(changeSnapshot.snapshotId());
          for (IcebergContentFile<?> icebergContentFile : changeTableIncrementalScan.planFilesWithSequence()) {
            changeFiles.addFile(wrapChangeFile(((IcebergDataFile) icebergContentFile)));
          }
          PartitionSpec partitionSpec = arcticTable.spec();
          changeFiles.allInsertFiles().forEach(insertFile -> {
            StructLike partition = insertFile.partition();
            String partitionPath = partitionSpec.partitionToPath(partition);
            if (!fiterPartition(partitionPath)) {
              List<IcebergDataFile> relatedDeleteFiles = changeFiles.getRelatedDeleteFiles(insertFile);
              AbstractPartitionPlan evaluator =
                  partitionEvaluatorMap.computeIfAbsent(partitionPath, this::buildEvaluator);
              evaluator.addFile(insertFile.asDataFile(), Collections.emptyList(), relatedDeleteFiles);
            }
          });
        }
      }
    } else {
      baseTable = arcticTable.asUnkeyedTable();
      baseSnapshotId = IcebergTableUtil.getSnapshotId(baseTable, true);
    }

    List<FileScanTask> fileScanTasks;
    try (CloseableIterable<FileScanTask> filesIterable =
             baseTable.newScan().useSnapshot(baseSnapshotId).planFiles()) {
      fileScanTasks = Lists.newArrayList(filesIterable);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close table scan of " + arcticTable.id(), e);
    }
    PartitionSpec partitionSpec = baseTable.spec();
    for (FileScanTask fileScanTask : fileScanTasks) {
      StructLike partition = fileScanTask.file().partition();
      String partitionPath = partitionSpec.partitionToPath(partition);
      if (fiterPartition(partitionPath)) {
        continue;
      }
      AbstractPartitionPlan evaluator = partitionEvaluatorMap.computeIfAbsent(partitionPath, this::buildEvaluator);
      List<IcebergDataFile> relatedChangeDeleteFiles = changeFiles.getRelatedDeleteFiles(fileScanTask.file());
      List<DeleteFile> deleteFiles =
          fileScanTask.deletes().stream().map(OptimizingEvaluator::wrapDeleteFile).collect(Collectors.toList());
      evaluator.addFile(wrapBaseFile(fileScanTask.file()), deleteFiles, relatedChangeDeleteFiles);
    }
    finishAddingFiles();
    partitionEvaluatorMap.values().removeIf(plan -> !plan.isNecessary());
  }

  private static IcebergDataFile wrapChangeFile(IcebergDataFile icebergDataFile) {
    DefaultKeyedFile defaultKeyedFile = DefaultKeyedFile.parseChange(icebergDataFile.internalFile(),
        icebergDataFile.getSequenceNumber());
    return new IcebergDataFile(defaultKeyedFile, icebergDataFile.getSequenceNumber());
  }

  private static IcebergDataFile wrapBaseFile(DataFile dataFile) {
    DefaultKeyedFile defaultKeyedFile = DefaultKeyedFile.parseBase(dataFile);
    long transactionId = FileNameRules.parseTransactionId(dataFile.path().toString());
    return new IcebergDataFile(defaultKeyedFile, transactionId);
  }

  private static DeleteFile wrapDeleteFile(DeleteFile deleteFile) {
    long transactionId = FileNameRules.parseTransactionId(deleteFile.path().toString());
    return new IcebergDeleteFile(deleteFile, transactionId);
  }

  private long getMaxSequenceLimit(ArcticTable arcticTable,
                                   Snapshot changeSnapshot,
                                   StructLikeMap<Long> partitionOptimizedSequence,
                                   StructLikeMap<Long> legacyPartitionMaxTransactionId) {
    int totalFilesInSummary = PropertyUtil
        .propertyAsInt(changeSnapshot.summary(), SnapshotSummary.TOTAL_DATA_FILES_PROP, 0);
    int maxFileCntLimit = CompatiblePropertyUtil.propertyAsInt(arcticTable.properties(),
        TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT, TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT);
    // not scan files to improve performance
    if (totalFilesInSummary <= maxFileCntLimit) {
      return Long.MAX_VALUE;
    }
    // scan and get all change files grouped by sequence(snapshot)
    ChangeTableIncrementalScan changeTableIncrementalScan =
        arcticTable.asKeyedTable().changeTable().newChangeScan()
            .fromSequence(partitionOptimizedSequence)
            .fromLegacyTransaction(legacyPartitionMaxTransactionId)
            .useSnapshot(changeSnapshot.snapshotId());
    Map<Long, SnapshotFileGroup> changeFilesGroupBySequence = new HashMap<>();
    try (CloseableIterable<IcebergContentFile<?>> files = changeTableIncrementalScan.planFilesWithSequence()) {
      for (IcebergContentFile<?> file : files) {
        SnapshotFileGroup fileGroup =
            changeFilesGroupBySequence.computeIfAbsent(file.getSequenceNumber(), key -> {
              long txId = FileNameRules.parseChangeTransactionId(file.path().toString(), file.getSequenceNumber());
              return new SnapshotFileGroup(file.getSequenceNumber(), txId);
            });
        fileGroup.addFile();
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to close table scan of " + arcticTable.name(), e);
    }

    if (changeFilesGroupBySequence.isEmpty()) {
      LOG.debug("{} get no change files to optimize with partitionOptimizedSequence {}", arcticTable.name(),
          partitionOptimizedSequence);
      return Long.MIN_VALUE;
    }

    long maxSequence =
        getMaxSequenceKeepingTxIdInOrder(new ArrayList<>(changeFilesGroupBySequence.values()), maxFileCntLimit);
    if (maxSequence == Long.MIN_VALUE) {
      LOG.warn("{} get no change files with self-optimizing.max-file-count={}, change it to a bigger value",
          arcticTable.name(), maxFileCntLimit);
    } else if (maxSequence != Long.MAX_VALUE) {
      LOG.warn("{} not all change files optimized with self-optimizing.max-file-count={}, maxSequence={}",
          arcticTable.name(), maxFileCntLimit, maxSequence);
    }
    return maxSequence;
  }

  protected AbstractPartitionPlan buildEvaluator(String partitionPath) {
    return new PartitionEvaluatorImpl(tableRuntime, partitionPath);
  }

  protected boolean fiterPartition(String partition) {
    return false;
  }

  public boolean isNecessary() {
    if (!isInitEvaluator) {
      initEvaluator();
    }
    return !partitionEvaluatorMap.isEmpty();
  }

  public PendingInput getPendingInput() {
    if (!isInitEvaluator) {
      initEvaluator();
    }
    return new PendingInput(partitionEvaluatorMap.values());
  }

  public static class PendingInput {

    private final Set<String> partitions = Sets.newHashSet();

    private int dataFileCount = 0;
    private long dataFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;

    public PendingInput(Collection<AbstractPartitionPlan> evaluators) {
      for (AbstractPartitionPlan e : evaluators) {
        PartitionEvaluatorImpl evaluator = (PartitionEvaluatorImpl) e;
        partitions.add(evaluator.getPartition());
        dataFileCount += evaluator.fragementFileCount + evaluator.segmentFileCount;
        dataFileSize += evaluator.fragementFileSize + evaluator.segmentFileSize;
        positionalDeleteBytes += evaluator.positionalDeleteBytes;
        positionalDeleteFileCount += evaluator.positionalDeleteFileCount;
        equalityDeleteBytes += evaluator.equalityDeleteBytes;
        equalityDeleteFileCount += evaluator.equalityDeleteFileCount;
      }
    }

    public Set<String> getPartitions() {
      return partitions;
    }

    public int getDataFileCount() {
      return dataFileCount;
    }

    public long getDataFileSize() {
      return dataFileSize;
    }

    public int getEqualityDeleteFileCount() {
      return equalityDeleteFileCount;
    }

    public int getPositionalDeleteFileCount() {
      return positionalDeleteFileCount;
    }

    public long getPositionalDeleteBytes() {
      return positionalDeleteBytes;
    }

    public long getEqualityDeleteBytes() {
      return equalityDeleteBytes;
    }
  }

  private class PartitionEvaluatorImpl extends AbstractPartitionPlan {

    private int fragementFileCount = 0;
    private long fragementFileSize = 0;
    private int segmentFileCount = 0;
    private long segmentFileSize = 0;
    private int equalityDeleteFileCount = 0;
    private int positionalDeleteFileCount = 0;
    private long positionalDeleteBytes = 0L;
    private long equalityDeleteBytes = 0L;
    private long rewriteSegmentFileSize = 0L;

    public PartitionEvaluatorImpl(TableRuntime tableRuntime, String partition) {
      super(tableRuntime, arcticTable, partition);
    }

    @Override
    public void addFile(DataFile dataFile, List<DeleteFile> deletes) {
      addFile(dataFile, deletes, Collections.emptyList());
    }

    @Override
    public void addFile(DataFile dataFile, List<DeleteFile> deletes, List<IcebergDataFile> changeDeletes) {
      boolean isSegment = false;
      int posDeleteCount = 0;
      if (dataFile.fileSizeInBytes() <= fragmentSize) {
        fragementFileSize += dataFile.fileSizeInBytes();
        fragementFileCount += 1;
      } else {
        segmentFileSize += dataFile.fileSizeInBytes();
        segmentFileCount += 1;
        isSegment = true;
      }

      for (DeleteFile delete : deletes) {
        if (delete.content() == FileContent.EQUALITY_DELETES) {
          equalityDeleteFileCount += 1;
          equalityDeleteBytes += delete.fileSizeInBytes();
        } else {
          if (++posDeleteCount > 1 || isSegment &&
              delete.recordCount() >= dataFile.recordCount() * config.getMajorDuplicateRatio()) {
            rewriteSegmentFileSize += dataFile.fileSizeInBytes();
          }
          posDeleteCount += 1;
          positionalDeleteBytes += delete.fileSizeInBytes();
        }
      }

      for (IcebergDataFile delete : changeDeletes) {
        equalityDeleteFileCount += 1;
        equalityDeleteBytes += delete.fileSizeInBytes();
      }
    }

    @Override
    public boolean isNecessary() {
      return isMajorNecessary() || isMinorNecessary();
    }

    @Override
    public long getCost() {
      throw new UnsupportedOperationException();
    }

    @Override
    public OptimizingType getOptimizingType() {
      return isMajorNecessary() ? OptimizingType.MAJOR : OptimizingType.MINOR;
    }

    private boolean isMajorNecessary() {
      return rewriteSegmentFileSize > 0;
    }

    private boolean isMinorNecessary() {
      int sourceFileCount = fragementFileCount + equalityDeleteFileCount;
      return sourceFileCount >= config.getMinorLeastFileCount() ||
          (sourceFileCount > 1 &&
              System.currentTimeMillis() - tableRuntime.getLastMinorOptimizingTime() > config.getMinorLeastInterval());
    }
  }

  private static class ChangeFiles {
    private final ArcticTable arcticTable;
    private final Map<String, Map<DataTreeNode, List<IcebergDataFile>>> cachedRelatedDeleteFiles = Maps.newHashMap();

    private final Map<String, Map<DataTreeNode, Set<IcebergDataFile>>> equalityDeleteFiles = Maps.newHashMap();
    private final Map<String, Map<DataTreeNode, Set<IcebergDataFile>>> insertFiles = Maps.newHashMap();

    public ChangeFiles(ArcticTable arcticTable) {
      this.arcticTable = arcticTable;
    }

    public void addFile(IcebergDataFile file) {
      String partition = arcticTable.spec().partitionToPath(file.partition());
      DataTreeNode node = FileNameRules.parseFileNodeFromFileName(file.path().toString());
      DataFileType type = FileNameRules.parseFileTypeForChange(file.path().toString());
      switch (type) {
        case EQ_DELETE_FILE:
          equalityDeleteFiles.computeIfAbsent(partition, key -> Maps.newHashMap())
              .computeIfAbsent(node, key -> Sets.newHashSet())
              .add(file);
          break;
        case INSERT_FILE:
          insertFiles.computeIfAbsent(partition, key -> Maps.newHashMap())
              .computeIfAbsent(node, key -> Sets.newHashSet())
              .add(file);
          break;
        default:
          throw new IllegalStateException("illegal file type in change store " + type);
      }
    }

    public Stream<IcebergDataFile> allInsertFiles() {
      return insertFiles.values().stream()
          .flatMap(dataTreeNodeSetMap -> dataTreeNodeSetMap.values().stream())
          .flatMap(Collection::stream);
    }

    public List<IcebergDataFile> getRelatedDeleteFiles(DataFile file) {
      String partition = arcticTable.spec().partitionToPath(file.partition());
      if (!equalityDeleteFiles.containsKey(partition)) {
        return Collections.emptyList();
      }
      DataTreeNode node = FileNameRules.parseFileNodeFromFileName(file.path().toString());
      Map<DataTreeNode, List<IcebergDataFile>> partitionDeleteFiles =
          cachedRelatedDeleteFiles.computeIfAbsent(partition, key -> Maps.newHashMap());
      if (partitionDeleteFiles.containsKey(node)) {
        return partitionDeleteFiles.get(node);
      } else {
        List<IcebergDataFile> result = Lists.newArrayList();
        for (Map.Entry<DataTreeNode, List<IcebergDataFile>> entry : partitionDeleteFiles.entrySet()) {
          DataTreeNode deleteNode = entry.getKey();
          if (node.isSonOf(deleteNode) || deleteNode.isSonOf(node)) {
            // TODO 去重
            result.addAll(entry.getValue());
          }
        }
        partitionDeleteFiles.put(node, result);
        return result;
      }
    }
  }

  /**
   * Files grouped by snapshot, but only with the file cnt.
   */
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


  /**
   * Select all the files whose sequence <= maxSequence as Selected-Files, seek the maxSequence to find as many
   * Selected-Files as possible, and also
   * - the cnt of these Selected-Files must <= maxFileCntLimit
   * - the max TransactionId of the Selected-Files must > the min TransactionId of all the left files
   *
   * @param snapshotFileGroups snapshotFileGroups
   * @param maxFileCntLimit    maxFileCntLimit
   * @return the max sequence of selected file, return Long.MAX_VALUE if all files should be selected,
   * Long.MIN_VALUE means no files should be selected
   */
  static long getMaxSequenceKeepingTxIdInOrder(List<SnapshotFileGroup> snapshotFileGroups, long maxFileCntLimit) {
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
    // if only check the first file groups, then the file cnt > maxFileCntLimit, no files should be selected
    if (index == -1) {
      return Long.MIN_VALUE;
    }

    // 3.wrap file group with the max TransactionId before and min TransactionId after
    List<SnapshotFileGroupWrapper> snapshotFileGroupWrappers = wrapMinMaxTransactionId(snapshotFileGroups);
    // 4.find the valid snapshotFileGroup
    while (true) {
      SnapshotFileGroupWrapper current = snapshotFileGroupWrappers.get(index);
      // check transaction id inorder: max transaction id before(inclusive) < min transaction id after
      if (Math.max(current.getFileGroup().getTransactionId(), current.getMaxTransactionIdBefore()) <
          current.getMinTransactionIdAfter()) {
        return current.getFileGroup().getSequence();
      }
      index--;
      if (index == -1) {
        return Long.MIN_VALUE;
      }
    }
  }

  /**
   * Wrap SnapshotFileGroup with max and min Transaction Id
   */
  private static class SnapshotFileGroupWrapper {
    private final SnapshotFileGroup fileGroup;
    // in the ordered file group list, the max transaction before this file group, Long.MIN_VALUE for the first
    private long maxTransactionIdBefore;
    // in the ordered file group list, the min transaction after this file group, Long.MAX_VALUE for the last
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

  private static List<SnapshotFileGroupWrapper> wrapMinMaxTransactionId(List<SnapshotFileGroup> snapshotFileGroups) {
    List<SnapshotFileGroupWrapper> wrappedList = new ArrayList<>();
    for (SnapshotFileGroup snapshotFileGroup : snapshotFileGroups) {
      wrappedList.add(new SnapshotFileGroupWrapper(snapshotFileGroup));
    }
    long maxValue = Long.MIN_VALUE;
    for (int i = 0; i < wrappedList.size(); i++) {
      SnapshotFileGroupWrapper wrapper = wrappedList.get(i);
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
}
