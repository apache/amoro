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
import org.apache.amoro.server.dashboard.model.AMSColumnInfo;
import org.apache.amoro.server.dashboard.model.AMSPartitionField;
import org.apache.amoro.server.dashboard.model.AmoroSnapshotsOfTable;
import org.apache.amoro.server.dashboard.model.DDLInfo;
import org.apache.amoro.server.dashboard.model.OperationType;
import org.apache.amoro.server.dashboard.model.OptimizingProcessInfo;
import org.apache.amoro.server.dashboard.model.OptimizingTaskInfo;
import org.apache.amoro.server.dashboard.model.PartitionBaseInfo;
import org.apache.amoro.server.dashboard.model.PartitionFileBaseInfo;
import org.apache.amoro.server.dashboard.model.ServerTableMeta;
import org.apache.amoro.server.dashboard.model.TableSummary;
import org.apache.amoro.server.dashboard.model.TagOrBranchInfo;
import org.apache.amoro.server.dashboard.utils.AmsUtil;
import org.apache.amoro.server.utils.HudiTableUtil;
import org.apache.avro.Schema;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.hudi.common.model.FileSlice;
import org.apache.hudi.common.model.HoodieBaseFile;
import org.apache.hudi.common.model.HoodieCommitMetadata;
import org.apache.hudi.common.model.HoodieTableType;
import org.apache.hudi.common.table.HoodieTableConfig;
import org.apache.hudi.common.table.HoodieTableMetaClient;
import org.apache.hudi.common.table.TableSchemaResolver;
import org.apache.hudi.common.table.timeline.HoodieDefaultTimeline;
import org.apache.hudi.common.table.timeline.HoodieInstantTimeGenerator;
import org.apache.hudi.common.table.timeline.HoodieTimeline;
import org.apache.hudi.common.table.view.SyncableFileSystemView;
import org.apache.hudi.common.util.Option;
import org.apache.hudi.metadata.HoodieTableMetadata;
import org.apache.hudi.table.HoodieJavaTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Table descriptor for hudi.
 */
public class HudiTableDescriptor implements FormatTableDescriptor {

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
      scheme.getFields().forEach(field -> {
        AMSColumnInfo columnInfo = new AMSColumnInfo();
        columnInfo.setField(field.name());
        columnInfo.setType(HudiTableUtil.convertAvroSchemaToFieldType(field.schema()).toLowerCase());
        columnInfo.setRequired(true);
        columnInfo.setComment(field.doc());
        columns.add(columnInfo);
      });
    } catch (Exception e) {
      throw new IllegalStateException("Error when parse table schema", e);
    }
    Map<String, AMSColumnInfo> columnMap = columns.stream()
        .collect(Collectors.toMap(AMSColumnInfo::getField, Function.identity()));
    meta.setSchema(columns);
    meta.setProperties(amoroTable.properties());
    meta.setBaseLocation(metaClient.getBasePathV2().toString());

    if (hoodieTableConfig.isTablePartitioned()) {
      String[] partitionFields = hoodieTableConfig.getPartitionFields().get();
      List<AMSPartitionField> partitions = new ArrayList<>(partitionFields.length);

      for (String f: partitionFields) {
        if (columnMap.containsKey(f)) {
          partitions.add(new AMSPartitionField(f, null, null, null, null));
        }
      }
      meta.setPartitionColumnList(partitions);
    }
    if (hoodieTableConfig.getRecordKeyFields().map(f -> f.length > 0).orElse(false)) {
      String[] recordFields = hoodieTableConfig.getRecordKeyFields().get();
      List<AMSColumnInfo> primaryKeys = Lists.newArrayList();
      for (String field: recordFields) {
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
    Map<String, HudiTableUtil.HoodiePartitionMetric> metrics = HudiTableUtil.statisticPartitionsMetric(
        partitions, fileSystemView, ioExecutors
    );
    long baseFileCount = 0;
    long logFileCount = 0;
    long totalBaseSizeInByte = 0;
    long totalLogSizeInByte = 0;
    for (HudiTableUtil.HoodiePartitionMetric m: metrics.values()) {
      baseFileCount += m.getBaseFileCount();
      logFileCount += m.getLogFileCount();
      totalBaseSizeInByte += m.getTotalBaseFileSizeInBytes();
      totalLogSizeInByte += m.getTotalLogFileSizeInBytes();
    }
    long totalFileCount = baseFileCount + logFileCount;
    long totalFileSize = totalBaseSizeInByte + totalLogSizeInByte;
    String averageFileSize = AmsUtil.byteToXB(totalFileCount == 0 ? 0 : totalFileSize / totalFileCount);

    String tableType = metaClient.getTableType() == HoodieTableType.COPY_ON_WRITE ? "cow": "mor";
    String tableFormat = "Hudi(" + tableType + ")";
    TableSummary tableSummary = new TableSummary(
        totalFileCount,  AmsUtil.byteToXB(totalFileSize), averageFileSize, tableFormat
    );
    meta.setTableSummary(tableSummary);

    Map<String, Object> baseSummary = new HashMap<>();
    baseSummary.put("totalSize", AmsUtil.byteToXB(totalBaseSizeInByte));
    baseSummary.put("fileCount", baseFileCount);
    baseSummary.put("averageFileSize", AmsUtil.byteToXB(baseFileCount == 0?
        0: totalBaseSizeInByte / baseFileCount));
    meta.setBaseMetrics(baseSummary);
    if (HoodieTableType.MERGE_ON_READ == metaClient.getTableType()) {
      Map<String, Object> logSummary = new HashMap<>();
      logSummary.put("totalSize", AmsUtil.byteToXB(totalLogSizeInByte));
      logSummary.put("fileCount", logFileCount);
      logSummary.put("averageFileSize", AmsUtil.byteToXB(logFileCount == 0 ?
          0: totalLogSizeInByte / logFileCount));
      meta.setChangeMetrics(logSummary);
    }
    return meta;
  }

  @Override
  public List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    HoodieDefaultTimeline activeTimeline = hoodieTable.getActiveTimeline();
    HoodieTimeline timeline = hoodieTable.getMetaClient().getActiveTimeline();
    if (OperationType.ALL == operationType) {
      timeline = activeTimeline.getAllCommitsTimeline();
    } else if (OperationType.OPTIMIZING == operationType) {
      timeline = activeTimeline.getTimelineOfActions(Sets.newHashSet(
          HoodieTimeline.CLEAN_ACTION,
          HoodieTimeline.COMPACTION_ACTION,
          HoodieTimeline.REPLACE_COMMIT_ACTION,
          HoodieTimeline.INDEXING_ACTION,
          HoodieTimeline.LOG_COMPACTION_ACTION
      ));
    } else if (OperationType.NON_OPTIMIZING == operationType) {
      timeline = activeTimeline.getTimelineOfActions(Sets.newHashSet(
          HoodieTimeline.COMMIT_ACTION, HoodieTimeline.DELTA_COMMIT_ACTION,
          HoodieTimeline.SAVEPOINT_ACTION, HoodieTimeline.ROLLBACK_ACTION));
    }
    return timeline.getInstantsAsStream().parallel().map(
        i -> {
          AmoroSnapshotsOfTable s = new AmoroSnapshotsOfTable();
          s.setSnapshotId(i.getTimestamp());
          s.setOperation(i.getAction());
          Option<byte[]> optDetail = activeTimeline.getInstantDetails(i);
          if (optDetail.isPresent()) {
            byte[] detail = optDetail.get();
            try {
              HoodieCommitMetadata metadata = HoodieCommitMetadata.fromBytes(
                  detail, HoodieCommitMetadata.class);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
          return s;
        }
    ).collect(Collectors.toList());
  }

  @Override
  public List<PartitionFileBaseInfo> getSnapshotDetail(AmoroTable<?> amoroTable, long snapshotId) {
    return null;
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) {
    return Lists.newArrayList();
  }

  @Override
  public List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    HoodieTableMetadata hoodieTableMetadata = hoodieTable.getMetadata();
    List<String> partitions;
    try {
      partitions = hoodieTableMetadata.getAllPartitionPaths();
    } catch (IOException e) {
      throw new RuntimeException("Error when load partitions for table: " + amoroTable.id(), e);
    }
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Map<String, HudiTableUtil.HoodiePartitionMetric> metrics = HudiTableUtil.statisticPartitionsMetric(
        partitions, fileSystemView, ioExecutors
    );
    return metrics.entrySet().stream()
        .map(e -> {
          PartitionBaseInfo p = new PartitionBaseInfo();
          p.setPartition(e.getKey());
          p.setFileCount(e.getValue().getBaseFileCount() + e.getValue().getLogFileCount());
          p.setFileSize(e.getValue().getTotalBaseFileSizeInBytes() + e.getValue().getTotalLogFileSizeInBytes());
          return p;
        }).collect(Collectors.toList());
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(AmoroTable<?> amoroTable, String partition, Integer specId) {
    HoodieJavaTable hoodieTable = (HoodieJavaTable) amoroTable.originalTable();
    SyncableFileSystemView fileSystemView = hoodieTable.getHoodieView();
    Stream<FileSlice> fileSliceStream = fileSystemView.getLatestFileSlices(partition);
    List<PartitionFileBaseInfo> pfList = Lists.newArrayList();
    fileSliceStream.forEach( fs -> {
      if (fs.getBaseFile().isPresent()) {
        HoodieBaseFile baseFile = fs.getBaseFile().get();
        long commitTime = parseHoodieCommitTime(baseFile.getCommitTime());
        PartitionFileBaseInfo file = new PartitionFileBaseInfo(
            baseFile.getCommitTime(), DataFileType.BASE_FILE,
            commitTime, partition, 0, baseFile.getPath(),
            baseFile.getFileSize()
        );
        pfList.add(file);
      }
      fs.getLogFiles().forEach(l -> {
        //TODO: can't get commit time from log file
        PartitionFileBaseInfo file = new PartitionFileBaseInfo(
            "", DataFileType.LOG_FILE, 0L,
            partition, 0, l.getPath().toString(), l.getFileSize()
        );
        pfList.add(file);
      });

    });
    return pfList;
  }

  @Override
  public Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(AmoroTable<?> amoroTable, int limit, int offset) {
    return null;
  }

  @Override
  public List<OptimizingTaskInfo> getOptimizingTaskInfos(AmoroTable<?> amoroTable, long processId) {
    return null;
  }

  @Override
  public List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable) {
    return Collections.emptyList();
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
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
