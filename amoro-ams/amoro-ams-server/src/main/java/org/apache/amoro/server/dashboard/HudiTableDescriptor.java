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
import org.apache.amoro.server.dashboard.model.AMSColumnInfo;
import org.apache.amoro.server.dashboard.model.AmoroSnapshotsOfTable;
import org.apache.amoro.server.dashboard.model.DDLInfo;
import org.apache.amoro.server.dashboard.model.OperationType;
import org.apache.amoro.server.dashboard.model.OptimizingProcessInfo;
import org.apache.amoro.server.dashboard.model.OptimizingTaskInfo;
import org.apache.amoro.server.dashboard.model.PartitionBaseInfo;
import org.apache.amoro.server.dashboard.model.PartitionFileBaseInfo;
import org.apache.amoro.server.dashboard.model.ServerTableMeta;
import org.apache.amoro.server.dashboard.model.TagOrBranchInfo;
import org.apache.avro.Schema;
import org.apache.hudi.common.table.HoodieTableMetaClient;
import org.apache.hudi.common.table.TableSchemaResolver;
import org.apache.hudi.common.table.timeline.HoodieDefaultTimeline;
import org.apache.hudi.common.table.timeline.HoodieTimeline;
import org.apache.hudi.table.HoodieJavaTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.util.Pair;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


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
        columnInfo.setType(field.schema().getType().getName());
        columnInfo.setRequired(true);
        columnInfo.setComment(field.doc());
        columns.add(columnInfo);
      });
    } catch (Exception e) {
      throw new IllegalStateException("Error when parse table schema", e);
    }
    meta.setSchema(columns);
    meta.setProperties(amoroTable.properties());
    meta.setBaseLocation(metaClient.getBasePathV2().toString());
//    meta.setCreateTime(metaClient.get);

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
    return timeline.getInstantsAsStream().map(
        i -> {
          AmoroSnapshotsOfTable s = new AmoroSnapshotsOfTable();
          s.setSnapshotId(i.getTimestamp());
          s.setOperation(i.getAction());
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
    return Lists.newArrayList();
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFiles(AmoroTable<?> amoroTable, String partition, Integer specId) {
    return Lists.newArrayList();
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
    return null;
  }

  @Override
  public List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable) {
    return null;
  }
}
