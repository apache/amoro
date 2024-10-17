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

package org.apache.amoro.table.descriptor;

import org.apache.amoro.AmoroTable;
import org.apache.amoro.TableFormat;
import org.apache.amoro.process.ProcessStatus;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/** API for obtaining metadata information of various formats. */
public interface FormatTableDescriptor {

  /**
   * Global io-executor pool for table descriptor
   *
   * @param ioExecutor io executor pool.
   */
  void withIoExecutor(ExecutorService ioExecutor);

  /** Get the format supported by this descriptor. */
  List<TableFormat> supportFormat();

  /** Get the table metadata information of the {@link AmoroTable}. */
  ServerTableMeta getTableDetail(AmoroTable<?> amoroTable);

  /** Get the snapshot information of the {@link AmoroTable}. */
  List<AmoroSnapshotsOfTable> getSnapshots(
      AmoroTable<?> amoroTable, String ref, OperationType operationType);

  /** Get the snapshot detail information of the {@link AmoroTable}. */
  List<PartitionFileBaseInfo> getSnapshotDetail(AmoroTable<?> amoroTable, String snapshotId);

  /** Get the DDL information of the {@link AmoroTable}. */
  List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable);

  /** Get the partition information of the {@link AmoroTable}. */
  List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable);

  /** Get the file information of the {@link AmoroTable}. */
  List<PartitionFileBaseInfo> getTableFiles(
      AmoroTable<?> amoroTable, String partition, Integer specId);

  /** Get the paged optimizing process information of the {@link AmoroTable} and total size. */
  Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, String type, ProcessStatus status, int limit, int offset);

  /** Return the optimizing types of the {@link AmoroTable} is supported. */
  Map<String, String> getTableOptimizingTypes(AmoroTable<?> amoroTable);

  /** Get the paged optimizing process tasks information of the {@link AmoroTable}. */
  List<OptimizingTaskInfo> getOptimizingTaskInfos(AmoroTable<?> amoroTable, String processId);

  /** Get the tag information of the {@link AmoroTable}. */
  List<TagOrBranchInfo> getTableTags(AmoroTable<?> amoroTable);

  /** Get the branch information of the {@link AmoroTable}. */
  List<TagOrBranchInfo> getTableBranches(AmoroTable<?> amoroTable);

  /** Get the consumer information of the {@link AmoroTable}. */
  List<ConsumerInfo> getTableConsumerInfos(AmoroTable<?> amoroTable);
}
