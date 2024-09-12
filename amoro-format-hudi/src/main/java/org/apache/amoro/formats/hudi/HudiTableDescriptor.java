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

package org.apache.amoro.formats.hudi;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessStatus;
import org.apache.amoro.process.ProcessTaskStatus;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.amoro.table.descriptor.AMSColumnInfo;
import org.apache.amoro.table.descriptor.AMSPartitionField;
import org.apache.amoro.table.descriptor.AmoroSnapshotsOfTable;
import org.apache.amoro.table.descriptor.ConsumerInfo;
import org.apache.amoro.table.descriptor.DDLInfo;
import org.apache.amoro.table.descriptor.FilesStatistics;
import org.apache.amoro.table.descriptor.FormatTableDescriptor;
import org.apache.amoro.table.descriptor.OperationType;
import org.apache.amoro.table.descriptor.OptimizingProcessInfo;
import org.apache.amoro.table.descriptor.OptimizingTaskInfo;
import org.apache.amoro.table.descriptor.PartitionBaseInfo;
import org.apache.amoro.table.descriptor.PartitionFileBaseInfo;
import org.apache.amoro.table.descriptor.ServerTableMeta;
import org.apache.amoro.table.descriptor.TableSummary;
import org.apache.amoro.table.descriptor.TagOrBranchInfo;
import org.apache.amoro.utils.CommonUtil;
import org.apache.avro.Schema;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hudi.avro.model.HoodieClusteringGroup;
import org.apache.hudi.avro.model.HoodieClusteringPlan;
import org.apache.hudi.avro.model.HoodieClusteringStrategy;
import org.apache.hudi.avro.model.HoodieCompactionOperation;
import org.apache.hudi.avro.model.HoodieCompactionPlan;
import org.apache.hudi.avro.model.HoodieCompactionStrategy;
import org.apache.hudi.avro.model.HoodieRequestedReplaceMetadata;
import org.apache.hudi.avro.model.HoodieSliceInfo;
import org.apache.hudi.common.model.FileSlice;
import org.apache.hudi.common.model.HoodieBaseFile;
import org.apache.hudi.common.model.HoodieCommitMetadata;
import org.apache.hudi.common.model.HoodieTableType;
import org.apache.hudi.common.model.HoodieWriteStat;
import org.apache.hudi.common.model.WriteOperationType;
import org.apache.hudi.common.table.HoodieTableConfig;
import org.apache.hudi.common.table.HoodieTableMetaClient;
import org.apache.hudi.common.table.TableSchemaResolver;
import org.apache.hudi.common.table.timeline.HoodieActiveTimeline;
import org.apache.hudi.common.table.timeline.HoodieDefaultTimeline;
import org.apache.hudi.common.table.timeline.HoodieInstant;
import org.apache.hudi.common.table.timeline.HoodieInstantTimeGenerator;
import org.apache.hudi.common.table.timeline.HoodieTimeline;
import org.apache.hudi.common.table.timeline.TimelineMetadataUtils;
import org.apache.hudi.common.table.view.SyncableFileSystemView;
import org.apache.hudi.common.util.Option;
import org.apache.hudi.common.util.StringUtils;
import org.apache.hudi.metadata.HoodieTableMetadata;
import org.apache.hudi.table.HoodieJavaTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Table descriptor for hudi. */
public class HudiTableDescriptor implements FormatTableDescriptor {

  private static final Logger LOG = LoggerFactory.getLogger(HudiTableDescriptor.class);
  private static final String COMPACTION = "compaction";
  private static final String CLUSTERING = "clustering";

  private ExecutorService ioExecutors;

  @Override
  public void withIoExecutor(ExecutorService ioExecutor) {
    this.ioExecutors = ioExecutor;
  }

  @Override
  public List<TableFormat> supportFormat() {
    return Lists.newArrayList(TableFormat.HUDI);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    HoodieTableMetaClient metaClient = hoodieTable.getMetaClient();
    HoodieTableConfig hoodieTableConfig = metaClient.getTableConfig();
    TableSchemaResolver schemaResolver = new TableSchemaResolver(metaClient);
    ServerTableMeta meta = new ServerTableMeta();
    meta.setTableIdentifier(amoroTable.id());
    meta.setTableType(TableFormat.HUDI.name());
    List<AMSColumnInfo> columns = Lists.newArrayList();
    try {
      Schema scheme = schemaResolver.getTableAvroSchema(false);
      scheme
          .getFields()
          .forEach(
              field -> {
                AMSColumnInfo columnInfo = new AMSColumnInfo();
                columnInfo.setField(field.name());
                columnInfo.setType(
                    HudiTableUtil.convertAvroSchemaToFieldType(field.schema()).toLowerCase());
                columnInfo.setRequired(true);
                columnInfo.setComment(field.doc());
                columns.add(columnInfo);
              });
    } catch (Exception e) {
      throw new IllegalStateException("Error when parse table schema", e);
    }
    Map<String, AMSColumnInfo> columnMap =
        columns.stream().collect(Collectors.toMap(AMSColumnInfo::getField, Function.identity()));
    meta.setSchema(columns);
    meta.setProperties(amoroTable.properties());
    meta.setBaseLocation(metaClient.getBasePathV2().toString());

    if (hoodieTableConfig.isTablePartitioned()) {
      String[] partitionFields = hoodieTableConfig.getPartitionFields().get();
      List<AMSPartitionField> partitions = new ArrayList<>(partitionFields.length);

      for (String f : partitionFields) {
        if (columnMap.containsKey(f)) {
          partitions.add(new AMSPartitionField(f, null, null, null, null));
        }
      }
      meta.setPartitionColumnList(partitions);
    }
    if (hoodieTableConfig.getRecordKeyFields().map(f -> f.length > 0).orElse(false)) {
      String[] recordFields = hoodieTableConfig.getRecordKeyFields().get();
      List<AMSColumnInfo> primaryKeys = Lists.newArrayList();
      for (String field : recordFields) {
        if (columnMap.containsKey(field)) {
          primaryKeys.add(columnMap.get(field));
        }
      }
      meta.setPkList(primaryKeys);
    }

    HoodieTableMetadata hoodieTableMetadata = hoodieTable.getMetadata();
    List<String> partitions;
    try {
      partitions = hoodieTableMetadata.getAllPartitionPaths();
    } catch (IOException e) {
      throw new RuntimeException("Error when load partitions for table: " + amoroTable.id(), e);
    }

    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Map<String, HudiTableUtil.HoodiePartitionMetric> metrics =
        HudiTableUtil.statisticPartitionsMetric(partitions, fileSystemView, ioExecutors);
    long baseFileCount = 0;
    long logFileCount = 0;
    long totalBaseSizeInByte = 0;
    long totalLogSizeInByte = 0;
    for (HudiTableUtil.HoodiePartitionMetric m : metrics.values()) {
      baseFileCount += m.getBaseFileCount();
      logFileCount += m.getLogFileCount();
      totalBaseSizeInByte += m.getTotalBaseFileSizeInBytes();
      totalLogSizeInByte += m.getTotalLogFileSizeInBytes();
    }
    long totalFileCount = baseFileCount + logFileCount;
    long totalFileSize = totalBaseSizeInByte + totalLogSizeInByte;
    String averageFileSize =
        CommonUtil.byteToXB(totalFileCount == 0 ? 0 : totalFileSize / totalFileCount);

    String tableType = metaClient.getTableType() == HoodieTableType.COPY_ON_WRITE ? "cow" : "mor";
    String tableFormat = "Hudi(" + tableType + ")";
    TableSummary tableSummary =
        new TableSummary(
            totalFileCount, CommonUtil.byteToXB(totalFileSize), averageFileSize, 0, tableFormat);
    meta.setTableSummary(tableSummary);

    Map<String, Object> baseSummary = new HashMap<>();
    baseSummary.put("totalSize", CommonUtil.byteToXB(totalBaseSizeInByte));
    baseSummary.put("fileCount", baseFileCount);
    baseSummary.put(
        "averageFileSize",
        CommonUtil.byteToXB(baseFileCount == 0 ? 0 : totalBaseSizeInByte / baseFileCount));
    meta.setBaseMetrics(baseSummary);
    if (HoodieTableType.MERGE_ON_READ == metaClient.getTableType()) {
      Map<String, Object> logSummary = new HashMap<>();
      logSummary.put("totalSize", CommonUtil.byteToXB(totalLogSizeInByte));
      logSummary.put("fileCount", logFileCount);
      logSummary.put(
          "averageFileSize",
          CommonUtil.byteToXB(logFileCount == 0 ? 0 : totalLogSizeInByte / logFileCount));
      meta.setChangeMetrics(logSummary);
    }
    return meta;
  }

  @Override
  public List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType) {
    HudiTable hudiTable = (HudiTable) amoroTable;

    return hudiTable.getSnapshotList(ioExecutors).stream()
        .filter(
            s ->
                OperationType.ALL == operationType
                    || operationType.name().equalsIgnoreCase(s.getOperationType()))
        .map(
            s -> {
              AmoroSnapshotsOfTable snapshotsOfTable = new AmoroSnapshotsOfTable();
              snapshotsOfTable.setSnapshotId(s.getSnapshotId());
              snapshotsOfTable.setCommitTime(s.getCommitTimestamp());
              snapshotsOfTable.setFileCount(s.getTotalFileCount());
              snapshotsOfTable.setFileSize((int) s.getTotalFileSize());
              snapshotsOfTable.setSummary(s.getSummary());
              snapshotsOfTable.setOperation(s.getOperation());
              Map<String, String> fileSummary = new HashMap<>();
              fileSummary.put("delta-files", "0");
              fileSummary.put("data-files", String.valueOf(s.getBaseFileCount()));
              fileSummary.put("changelogs", String.valueOf(s.getLogFileCount()));
              snapshotsOfTable.setFilesSummaryForChart(fileSummary);
              return snapshotsOfTable;
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(
      AmoroTable<?> amoroTable, String snapshotId) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Map<String, Stream<FileSlice>> ptFsMap =
        fileSystemView.getAllLatestFileSlicesBeforeOrOn(snapshotId);
    List<PartitionFileBaseInfo> files = Lists.newArrayList();
    for (String partition : ptFsMap.keySet()) {
      Stream<FileSlice> fsStream = ptFsMap.get(partition);
      List<PartitionFileBaseInfo> fileInPartition =
          fsStream.flatMap(fs -> fileSliceToFileStream(partition, fs)).collect(Collectors.toList());
      files.addAll(fileInPartition);
    }
    return files;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    // hudi doesn't support schema version track.
    return Lists.newArrayList();
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    HudiTable hudiTable = (HudiTable) amoroTable;
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    List<String> partitions = hudiTable.getPartitions();
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Map<String, HudiTableUtil.HoodiePartitionMetric> metrics =
        HudiTableUtil.statisticPartitionsMetric(partitions, fileSystemView, ioExecutors);
    return metrics.entrySet().stream()
        .map(
            e -> {
              PartitionBaseInfo p = new PartitionBaseInfo();
              p.setPartition(e.getKey());
              p.setFileCount(e.getValue().getBaseFileCount() + e.getValue().getLogFileCount());
              p.setFileSize(
                  e.getValue().getTotalBaseFileSizeInBytes()
                      + e.getValue().getTotalLogFileSizeInBytes());
              return p;
            })
        .collect(Collectors.toList());
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(
      AmoroTable<?> amoroTable, String partition, Integer specId) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Stream<FileSlice> fileSliceStream = fileSystemView.getLatestFileSlices(partition);
    return fileSliceStream
        .flatMap(fs -> fileSliceToFileStream(partition, fs))
        .collect(Collectors.toList());
  }

  private Stream<PartitionFileBaseInfo> fileSliceToFileStream(String partition, FileSlice fs) {
    List<PartitionFileBaseInfo> files = Lists.newArrayList();
    if (fs.getBaseFile().isPresent()) {
      HoodieBaseFile baseFile = fs.getBaseFile().get();
      long commitTime = parseHoodieCommitTime(baseFile.getCommitTime());
      PartitionFileBaseInfo file =
          new PartitionFileBaseInfo(
              baseFile.getCommitTime(),
              "BASE_FILE",
              commitTime,
              partition,
              0,
              baseFile.getPath(),
              baseFile.getFileSize());
      files.add(file);
    }
    fs.getLogFiles()
        .forEach(
            l -> {
              // TODO: can't get commit time from log file
              PartitionFileBaseInfo file =
                  new PartitionFileBaseInfo(
                      "", "LOG_FILE", 0L, partition, 0, l.getPath().toString(), l.getFileSize());
              files.add(file);
            });
    return files.stream();
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, String type, ProcessStatus status, int limit, int offset) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    HoodieDefaultTimeline timeline = new HoodieActiveTimeline(hoodieTable.getMetaClient(), false);
    List<HoodieInstant> instants = timeline.getInstants();
    Map<String, HoodieInstant> instantMap = Maps.newHashMap();
    for (HoodieInstant instant : instants) {
      instantMap.put(instant.getTimestamp() + "_" + instant.getState().name(), instant);
    }
    Set<String> optimizingActions =
        Sets.newHashSet(
            HoodieTimeline.COMPACTION_ACTION,
            HoodieTimeline.CLEAN_ACTION,
            HoodieTimeline.REPLACE_COMMIT_ACTION);

    List<String> timestamps =
        instants.stream()
            .filter(i -> i.getState() == HoodieInstant.State.REQUESTED)
            .filter(i -> optimizingActions.contains(i.getAction()))
            .map(HoodieInstant::getTimestamp)
            .collect(Collectors.toList());

    List<OptimizingProcessInfo> infos =
        timestamps.stream()
            .map(
                t -> {
                  OptimizingProcessInfo processInfo = null;
                  try {
                    processInfo = getOptimizingInfo(t, instantMap, timeline);
                    if (processInfo == null) {
                      return null;
                    }
                    processInfo.setCatalogName(amoroTable.id().getCatalog());
                    processInfo.setDbName(amoroTable.id().getDatabase());
                    processInfo.setTableName(amoroTable.id().getTableName());
                    return processInfo;
                  } catch (IOException e) {
                    throw new RuntimeException(e);
                  }
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    infos =
        infos.stream()
            .filter(
                i ->
                    StringUtils.isNullOrEmpty(type) || type.equalsIgnoreCase(i.getOptimizingType()))
            .filter(i -> status == null || status == i.getStatus())
            .collect(Collectors.toList());
    int total = infos.size();
    infos = infos.stream().skip(offset).limit(limit).collect(Collectors.toList());
    return Pair.of(infos, total);
  }

  @Override
  public Map<String, String> getTableOptimizingTypes(AmoroTable<?> amoroTable) {
    Map<String, String> types = Maps.newHashMap();
    types.put(COMPACTION, COMPACTION);
    types.put(CLUSTERING, CLUSTERING);
    return types;
  }

  protected OptimizingProcessInfo getOptimizingInfo(
      String instantTimestamp, Map<String, HoodieInstant> instantMap, HoodieTimeline timeline)
      throws IOException {
    OptimizingProcessInfo processInfo = new OptimizingProcessInfo();
    processInfo.setProcessId(instantTimestamp);
    HoodieInstant request =
        instantMap.get(instantTimestamp + "_" + HoodieInstant.State.REQUESTED.name());
    if (request == null) {
      return null;
    }
    long startTime = parseHoodieCommitTime(request.getStateTransitionTime());
    processInfo.setStartTime(startTime);
    Option<byte[]> detail = timeline.getInstantDetails(request);
    if (!detail.isPresent()) {
      return null;
    }
    processInfo.setSummary(Maps.newHashMap());
    if (HoodieTimeline.COMPACTION_ACTION.equals(request.getAction())) {
      fillCompactProcessInfo(processInfo, detail.get());
    } else if (HoodieTimeline.REPLACE_COMMIT_ACTION.equals(request.getAction())) {
      processInfo = fillClusterProcessInfo(processInfo, detail.get());
    } else {
      return null;
    }
    if (processInfo == null) {
      // replace commit is a insert_overwrite
      return null;
    }

    HoodieInstant commit =
        instantMap.get(instantTimestamp + "_" + HoodieInstant.State.COMPLETED.name());

    if (commit != null) {
      processInfo.setSuccessTasks(processInfo.getTotalTasks());
      long commitTimestamp = parseHoodieCommitTime(commit.getStateTransitionTime());
      processInfo.setDuration(commitTimestamp - startTime);
      processInfo.setFinishTime(commitTimestamp);
      processInfo.setStatus(ProcessStatus.SUCCESS);
      Option<byte[]> commitDetail = timeline.getInstantDetails(commit);
      HoodieCommitMetadata commitMetadata =
          HoodieCommitMetadata.fromBytes(commitDetail.get(), HoodieCommitMetadata.class);
      Map<String, List<HoodieWriteStat>> commitInfo = commitMetadata.getPartitionToWriteStats();
      int outputFile = 0;
      long outputFileSize = 0;
      for (String partition : commitInfo.keySet()) {
        List<HoodieWriteStat> writeStats = commitInfo.get(partition);
        for (HoodieWriteStat stat : writeStats) {
          outputFile += 1;
          outputFileSize += stat.getFileSizeInBytes();
        }
      }
      processInfo.setOutputFiles(FilesStatistics.build(outputFile, outputFileSize));
    } else {
      HoodieInstant inf =
          instantMap.get(instantTimestamp + "_" + HoodieInstant.State.INFLIGHT.name());
      if (inf != null) {
        processInfo.setStatus(ProcessStatus.ACTIVE);
      }
    }
    return processInfo;
  }

  private void fillCompactProcessInfo(OptimizingProcessInfo processInfo, byte[] requestDetails)
      throws IOException {
    HoodieCompactionPlan compactionPlan =
        TimelineMetadataUtils.deserializeCompactionPlan(requestDetails);
    int inputFileCount = 0;
    long inputFileSize = 0;
    for (HoodieCompactionOperation operation : compactionPlan.getOperations()) {
      if (StringUtils.nonEmpty(operation.getDataFilePath())) {
        inputFileCount += 1;
      }
      inputFileCount += operation.getDeltaFilePaths().size();
      inputFileSize += operation.getMetrics().getOrDefault("TOTAL_LOG_FILES_SIZE", 0.0);
    }
    processInfo.setInputFiles(FilesStatistics.build(inputFileCount, inputFileSize));
    int tasks = compactionPlan.getOperations().size();
    processInfo.setTotalTasks(tasks);
    HoodieCompactionStrategy strategy = compactionPlan.getStrategy();
    if (strategy != null) {
      processInfo.getSummary().put("strategy", strategy.getCompactorClassName());
      processInfo.getSummary().putAll(strategy.getStrategyParams());
    }
    processInfo.setOptimizingType(COMPACTION);
  }

  private OptimizingProcessInfo fillClusterProcessInfo(
      OptimizingProcessInfo processInfo, byte[] requestDetails) throws IOException {
    HoodieRequestedReplaceMetadata requestedReplaceMetadata =
        TimelineMetadataUtils.deserializeRequestedReplaceMetadata(requestDetails);
    String operationType = requestedReplaceMetadata.getOperationType();
    if (!WriteOperationType.CLUSTER.name().equalsIgnoreCase(operationType)) {
      return null;
    }
    HoodieClusteringPlan plan = requestedReplaceMetadata.getClusteringPlan();
    int inputFileCount = 0;
    long inputFileSize = 0;
    for (HoodieClusteringGroup group : plan.getInputGroups()) {
      for (HoodieSliceInfo slice : group.getSlices()) {
        inputFileCount++;
        if (slice.getDeltaFilePaths() != null) {
          inputFileCount += slice.getDeltaFilePaths().size();
        }
      }
      inputFileSize += group.getMetrics().getOrDefault("TOTAL_LOG_FILES_SIZE", 0.0);
    }
    processInfo.setInputFiles(FilesStatistics.build(inputFileCount, inputFileSize));
    int tasks = plan.getInputGroups().size();
    processInfo.setTotalTasks(tasks);
    processInfo.setOptimizingType(CLUSTERING);

    HoodieClusteringStrategy strategy = plan.getStrategy();
    if (strategy != null) {
      processInfo.getSummary().put("strategy", strategy.getStrategyClassName());
      processInfo.getSummary().putAll(strategy.getStrategyParams());
    }
    return processInfo;
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(
      AmoroTable<?> amoroTable, String processId) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    HoodieDefaultTimeline timeline = new HoodieActiveTimeline(hoodieTable.getMetaClient(), false);
    List<HoodieInstant> instants = timeline.getInstants();
    HoodieInstant request = null;
    HoodieInstant complete = null;
    for (HoodieInstant instant : instants) {
      if (processId.equals(instant.getTimestamp())) {
        if (instant.getState() == HoodieInstant.State.REQUESTED) {
          request = instant;
        } else if (instant.getState() == HoodieInstant.State.COMPLETED) {
          complete = instant;
        }
      }
    }
    if (request == null) {
      return Lists.newArrayList();
    }
    long startTime = parseHoodieCommitTime(request.getStateTransitionTime());
    long endTime = -1;
    long costTime = -1;
    ProcessTaskStatus status = ProcessTaskStatus.ACKED;
    Option<byte[]> requestDetails = timeline.getInstantDetails(request);
    if (!requestDetails.isPresent()) {
      return Lists.newArrayList();
    }
    byte[] commitDetails = null;
    if (complete != null) {
      status = ProcessTaskStatus.ACKED;
      endTime = parseHoodieCommitTime(complete.getStateTransitionTime());
      costTime = endTime - startTime;
      Option<byte[]> commitDetail = timeline.getInstantDetails(complete);
      if (commitDetail.isPresent()) {
        commitDetails = commitDetail.get();
      }
    }

    List<OptimizingTaskInfo> tasks = Lists.newArrayList();

    try {
      if (HoodieTimeline.COMPACTION_ACTION.equalsIgnoreCase(request.getAction())) {
        tasks = getCompactTasks(requestDetails.get(), commitDetails);
      } else if (HoodieTimeline.REPLACE_COMMIT_ACTION.equalsIgnoreCase(request.getAction())) {
        tasks = getClusterTasks(requestDetails.get(), commitDetails);
      }
    } catch (IOException e) {
      throw new RuntimeException("", e);
    }
    for (OptimizingTaskInfo task : tasks) {
      task.setStartTime(startTime);
      task.setEndTime(endTime);
      task.setStatus(status);
      task.setCostTime(costTime);
    }
    return tasks;
  }

  private List<OptimizingTaskInfo> getCompactTasks(byte[] requestDetails, byte[] commitDetails)
      throws IOException {
    Map<String, Pair<String, FilesStatistics>> inputFileStatistic =
        getCompactInputTaskStatistics(requestDetails);
    Map<String, FilesStatistics> outputFileStatistic = Maps.newHashMap();
    if (commitDetails != null) {
      HoodieCommitMetadata commitMetadata =
          HoodieCommitMetadata.fromBytes(commitDetails, HoodieCommitMetadata.class);
      Map<String, List<HoodieWriteStat>> commitInfo = commitMetadata.getPartitionToWriteStats();
      for (String partition : commitInfo.keySet()) {
        List<HoodieWriteStat> writeStats = commitInfo.get(partition);
        for (HoodieWriteStat stat : writeStats) {
          FilesStatistics fs = new FilesStatistics(1, stat.getFileSizeInBytes());
          outputFileStatistic.put(stat.getFileId(), fs);
        }
      }
    }
    List<OptimizingTaskInfo> results = Lists.newArrayList();
    int taskId = 0;
    for (String fileGroupId : inputFileStatistic.keySet()) {
      FilesStatistics input = inputFileStatistic.get(fileGroupId).getRight();
      String partition = inputFileStatistic.get(fileGroupId).getLeft();
      OptimizingTaskInfo task =
          new OptimizingTaskInfo(
              -1L,
              null,
              taskId++,
              partition,
              null,
              0,
              "",
              0,
              0,
              0,
              0,
              "",
              input,
              outputFileStatistic.get(fileGroupId),
              Maps.newHashMap(),
              Maps.newHashMap());
      results.add(task);
    }
    return results;
  }

  private Map<String, Pair<String, FilesStatistics>> getCompactInputTaskStatistics(
      byte[] requestDetails) throws IOException {
    HoodieCompactionPlan compactionPlan =
        TimelineMetadataUtils.deserializeCompactionPlan(requestDetails);
    Map<String, Pair<String, FilesStatistics>> inputFileGroups = Maps.newHashMap();
    for (HoodieCompactionOperation operation : compactionPlan.getOperations()) {
      int inputFileCount = operation.getDeltaFilePaths().size();
      long inputFileSize =
          operation.getMetrics().getOrDefault("TOTAL_LOG_FILES_SIZE", 0.0).longValue();
      FilesStatistics statistics = FilesStatistics.build(inputFileCount, inputFileSize);
      inputFileGroups.put(operation.getFileId(), Pair.of(operation.getPartitionPath(), statistics));
    }
    return inputFileGroups;
  }

  private List<OptimizingTaskInfo> getClusterTasks(byte[] requestDetails, byte[] commitDetails)
      throws IOException {
    HoodieRequestedReplaceMetadata requestedReplaceMetadata =
        TimelineMetadataUtils.deserializeRequestedReplaceMetadata(requestDetails);
    String operationType = requestedReplaceMetadata.getOperationType();
    if (!WriteOperationType.CLUSTER.name().equalsIgnoreCase(operationType)) {
      return Lists.newArrayList();
    }
    Map<String, FilesStatistics> outputStatisticMap = Maps.newHashMap();
    if (commitDetails != null) {
      HoodieCommitMetadata commitMetadata =
          HoodieCommitMetadata.fromBytes(commitDetails, HoodieCommitMetadata.class);
      Map<String, List<HoodieWriteStat>> commitInfo = commitMetadata.getPartitionToWriteStats();
      for (String partition : commitInfo.keySet()) {
        List<HoodieWriteStat> writeStats = commitInfo.get(partition);
        int fileCount = 0;
        long fileSize = 0;
        for (HoodieWriteStat stat : writeStats) {
          fileCount += 1;
          fileSize += stat.getFileSizeInBytes();
        }
        outputStatisticMap.put(partition, FilesStatistics.build(fileCount, fileSize));
      }
    }

    List<OptimizingTaskInfo> taskInfoList = Lists.newArrayList();
    HoodieClusteringPlan plan = requestedReplaceMetadata.getClusteringPlan();
    int taskId = 0;
    for (HoodieClusteringGroup group : plan.getInputGroups()) {
      FilesStatistics inputStatistic = getClusterGroupStatistic(group);
      String partition = group.getSlices().get(0).getPartitionPath();
      FilesStatistics outputStatistic = outputStatisticMap.get(partition);
      OptimizingTaskInfo task =
          new OptimizingTaskInfo(
              -1L,
              null,
              taskId++,
              partition,
              null,
              0,
              "",
              0,
              0,
              0,
              0,
              "",
              inputStatistic,
              outputStatistic,
              Maps.newHashMap(),
              Maps.newHashMap());
      taskInfoList.add(task);
    }

    return taskInfoList;
  }

  private FilesStatistics getClusterGroupStatistic(HoodieClusteringGroup group) {
    int inputFileCount = 0;
    long inputFileSize = 0;
    for (HoodieSliceInfo slice : group.getSlices()) {
      inputFileCount++;
      if (slice.getDeltaFilePaths() != null) {
        inputFileCount += slice.getDeltaFilePaths().size();
      }
    }
    inputFileSize += group.getMetrics().getOrDefault("TOTAL_LOG_FILES_SIZE", 0.0);
    return FilesStatistics.build(inputFileCount, inputFileSize);
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    // hudi doesn't support tags and branch
    return Collections.emptyList();
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    return Lists.newArrayList(
        new TagOrBranchInfo("hoodie-timeline", -1, -1, 0L, 0L, TagOrBranchInfo.BRANCH));
  }

  @Override
  public List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  private long parseHoodieCommitTime(String commitTime) {
    try {
      Date date = HoodieInstantTimeGenerator.parseDateFromInstantTime(commitTime);
      return date.getTime();
    } catch (ParseException e) {
      throw new RuntimeException("Error when parse timestamp:" + commitTime, e);
    }
  }
}
