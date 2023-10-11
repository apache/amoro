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
import com.netease.arctic.server.dashboard.component.reverser.DDLReverser;
import com.netease.arctic.server.dashboard.component.reverser.PaimonTableMetaExtract;
import com.netease.arctic.server.dashboard.model.DDLInfo;
import com.netease.arctic.server.dashboard.model.PartitionBaseInfo;
import com.netease.arctic.server.dashboard.model.PartitionFileBaseInfo;
import com.netease.arctic.server.dashboard.model.ServerTableMeta;
import com.netease.arctic.server.dashboard.model.TransactionsOfTable;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;

import java.util.List;
import org.apache.paimon.table.DataTable;

/**
 * Descriptor for Paimon format tables.
 */
public class PaimonTableDescriptor implements FormatTableDescriptor {
  @Override
  public List<TableFormat> supportFormat() {
    return Lists.newArrayList(TableFormat.PAIMON);
  }

  @Override
  public ServerTableMeta getTableDetail(AmoroTable<?> amoroTable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<TransactionsOfTable> getTransactions(AmoroTable<?> amoroTable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<PartitionFileBaseInfo> getTransactionDetail(AmoroTable<?> amoroTable, long transactionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DDLInfo> getTableOperations(AmoroTable<?> amoroTable) throws Exception {
    DataTable table = getTable(amoroTable);
    PaimonTableMetaExtract extract = new PaimonTableMetaExtract();
    DDLReverser<DataTable> ddlReverser = new DDLReverser<>(extract);
    return ddlReverser.reverse(table, amoroTable.id());
  }

  @Override
  public List<PartitionBaseInfo> getTablePartition(AmoroTable<?> amoroTable) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<PartitionFileBaseInfo> getTableFile(AmoroTable<?> amoroTable, String partition) {
    throw new UnsupportedOperationException();
  }

  private DataTable getTable(AmoroTable<?> amoroTable) {
    return (DataTable) amoroTable.originalTable();
  }
}
