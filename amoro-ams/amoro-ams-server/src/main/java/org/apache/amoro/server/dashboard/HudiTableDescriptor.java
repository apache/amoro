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

package org.apache.amoro.server.dashboard;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.data.DataFileType;
import org.apache.amoro.formats.hudi.HudiTable;
import org.apache.amoro.server.AmoroServiceConstants;
import org.apache.amoro.server.dashboard.model.AMSColumnInfo;
import org.apache.amoro.server.dashboard.model.AMSPartitionField;
import org.apache.amoro.server.dashboard.model.AmoroSnapshotsOfTable;
import org.apache.amoro.server.dashboard.model.DDLInfo;
import org.apache.amoro.server.dashboard.model.FilesStatistics;
import org.apache.amoro.server.dashboard.model.OperationType;
import org.apache.amoro.server.dashboard.model.OptimizingProcessInfo;
import org.apache.amoro.server.dashboard.model.OptimizingTaskInfo;
import org.apache.amoro.server.dashboard.model.PartitionBaseInfo;
import org.apache.amoro.server.dashboard.model.PartitionFileBaseInfo;
import org.apache.amoro.server.dashboard.model.ServerTableMeta;
import org.apache.amoro.server.dashboard.model.TableSummary;
import org.apache.amoro.server.dashboard.model.TagOrBranchInfo;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.optimizing.OptimizingProcess;
import org.apache.amoro.server.optimizing.OptimizingType;
import org.apache.amoro.server.optimizing.TaskRuntime;
import org.apache.amoro.server.utils.HudiTableUtil;
import org.apache.avro.Schema;
import org.apache.hudi.avro.model.HoodieCompactionOperation;
import org.apache.hudi.avro.model.HoodieCompactionPlan;
import org.apache.hudi.common.model.FileSlice;
import org.apache.hudi.common.model.HoodieBaseFile;
import org.apache.hudi.common.model.HoodieCommitMetadata;
import org.apache.hudi.common.model.HoodieTableType;
import org.apache.hudi.common.model.HoodieWriteStat;
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
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.Pair;
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

  private final ExecutorService ioExecutors;

  public HudiTableDescriptor(ExecutorService ioExecutor) {
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
        AmsUtil.byteToXB(totalFileCount == 0 ? 0 : totalFileSize / totalFileCount);

    String tableType = metaClient.getTableType() == HoodieTableType.COPY_ON_WRITE ? "cow" : "mor";
    String tableFormat = "Hudi(" + tableType + ")";
    TableSummary tableSummary =
        new TableSummary(
            totalFileCount, AmsUtil.byteToXB(totalFileSize), averageFileSize, tableFormat);
    meta.setTableSummary(tableSummary);

    Map<String, Object> baseSummary = new HashMap<>();
    baseSummary.put("totalSize", AmsUtil.byteToXB(totalBaseSizeInByte));
    baseSummary.put("fileCount", baseFileCount);
    baseSummary.put(
        "averageFileSize",
        AmsUtil.byteToXB(baseFileCount == 0 ? 0 : totalBaseSizeInByte / baseFileCount));
    meta.setBaseMetrics(baseSummary);
    if (HoodieTableType.MERGE_ON_READ == metaClient.getTableType()) {
      Map<String, Object> logSummary = new HashMap<>();
      logSummary.put("totalSize", AmsUtil.byteToXB(totalLogSizeInByte));
      logSummary.put("fileCount", logFileCount);
      logSummary.put(
          "averageFileSize",
          AmsUtil.byteToXB(logFileCount == 0 ? 0 : totalLogSizeInByte / logFileCount));
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
              DataFileType.BASE_FILE,
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
                      "",
                      DataFileType.LOG_FILE,
                      0L,
                      partition,
                      0,
                      l.getPath().toString(),
                      l.getFileSize());
              files.add(file);
            });
    return files.stream();
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, int limit, int offset) {
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
    return Pair.of(infos, infos.size());
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
    HoodieCompactionPlan compactionPlan =
        TimelineMetadataUtils.deserializeCompactionPlan(detail.get());
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
    processInfo.setSuccessTasks(tasks);
    processInfo.setOptimizingType(OptimizingType.MINOR);

    HoodieInstant commit =
        instantMap.get(instantTimestamp + "_" + HoodieInstant.State.COMPLETED.name());

    if (commit != null) {
      long commitTimestamp = parseHoodieCommitTime(commit.getStateTransitionTime());
      processInfo.setDuration(commitTimestamp - startTime);
      processInfo.setStatus(OptimizingProcess.Status.SUCCESS);
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
        processInfo.setStatus(OptimizingProcess.Status.RUNNING);
      }
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
    long endTime = AmoroServiceConstants.INVALID_TIME;
    Option<byte[]> detail = timeline.getInstantDetails(request);
    TaskRuntime.Status status = TaskRuntime.Status.ACKED;
    Map<String, FilesStatistics> outputFileStatistic = Maps.newHashMap();
    if (complete != null) {
      status = TaskRuntime.Status.SUCCESS;
      endTime = parseHoodieCommitTime(complete.getStateTransitionTime());
      Option<byte[]> commitDetail = timeline.getInstantDetails(complete);
      try {
        HoodieCommitMetadata commitMetadata =
            HoodieCommitMetadata.fromBytes(commitDetail.get(), HoodieCommitMetadata.class);
        Map<String, List<HoodieWriteStat>> commitInfo = commitMetadata.getPartitionToWriteStats();

        for (String partition : commitInfo.keySet()) {
          List<HoodieWriteStat> writeStats = commitInfo.get(partition);
          for (HoodieWriteStat stat : writeStats) {
            FilesStatistics fs = new FilesStatistics(1, stat.getFileSizeInBytes());
            outputFileStatistic.put(stat.getFileId(), fs);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to read commit metadata", e);
      }
    }
    List<OptimizingTaskInfo> results = Lists.newArrayList();
    try {
      HoodieCompactionPlan compactionPlan =
          TimelineMetadataUtils.deserializeCompactionPlan(detail.get());
      int taskId = 0;
      for (HoodieCompactionOperation operation : compactionPlan.getOperations()) {
        int inputFileCount = operation.getDeltaFilePaths().size();
        long inputFileSize =
            operation.getMetrics().getOrDefault("TOTAL_LOG_FILES_SIZE", 0.0).longValue();
        OptimizingTaskInfo task =
            new OptimizingTaskInfo(
                -1L,
                processId,
                taskId++,
                operation.getPartitionPath(),
                status,
                0,
                "",
                0,
                startTime,
                endTime,
                0,
                "",
                new FilesStatistics(inputFileCount, inputFileSize),
                outputFileStatistic.get(operation.getFileId()),
                Maps.newHashMap(),
                Maps.newHashMap());
        results.add(task);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to get optimizing task info", e);
    }

    return results;
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    return Lists.newArrayList(
        new TagOrBranchInfo("hoodie-timeline", -1, -1, 0L, 0L, TagOrBranchInfo.BRANCH));
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
