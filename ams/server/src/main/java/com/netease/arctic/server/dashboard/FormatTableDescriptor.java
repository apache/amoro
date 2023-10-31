/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.server.dashboard;

import com.netease.arctic.AmoroTable;
import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.server.dashboard.model.AMSTransactionsOfTable;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.OptimizingProcessInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import org.apache.iceberg.util.Pair;

import java.util.List;

/** API for obtaining metadata information of various formats. */
public interface FormatTableDescriptor {

  /** Get the format supported by this descriptor. */
  List<TableFormat> supportFormat();

  /** Get the table metadata information of the {@link AmoroTable}. */
  ServerTableMeta getTableDetail(AmoroTable<?> amoroTable);

  /** Get the transaction information of the {@link AmoroTable}. */
  List<AMSTransactionsOfTable> getTransactions(AmoroTable<?> amoroTable);

  /** Get the transaction detail information of the {@link AmoroTable}. */
  List<PartitionFileBaseInfo> getTransactionDetail(AmoroTable<?> amoroTable, long transactionId);

  /** Get the DDL information of the {@link AmoroTable}. */
  List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable);

  /** Get the partition information of the {@link AmoroTable}. */
  List<PartitionBaseInfo> getTablePartitions(AmoroTable<?> amoroTable);

  /** Get the file information of the {@link AmoroTable}. */
  List<PartitionFileBaseInfo> getTableFiles(AmoroTable<?> amoroTable, String partition);

  /** Get the paged optimizing process information of the {@link AmoroTable} and total size. */
  Pair<List<OptimizingProcessInfo>, Integer> getOptimizingProcessesInfo(
      AmoroTable<?> amoroTable, int limit, int offset);
}
