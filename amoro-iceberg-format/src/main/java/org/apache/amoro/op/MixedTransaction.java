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

package org.apache.amoro.op;

import org.apache.amoro.table.MixedTable;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ExpireSnapshots;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.HistoryEntry;
import org.apache.iceberg.ManageSnapshots;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.ReplaceSortOrder;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RewriteManifests;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SnapshotRef;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.StatisticsFile;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateLocation;
import org.apache.iceberg.UpdatePartitionSpec;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.UpdateStatistics;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class MixedTransaction implements Transaction {

  private final MixedTable mixedTable;
  private final Transaction transaction;

  private final Table transactionTable;

  public MixedTransaction(MixedTable mixedTable, Transaction transaction) {
    this.mixedTable = mixedTable;
    this.transaction = transaction;
    this.transactionTable = new TransactionTable();
  }

  @Override
  public Table table() {
    return transactionTable;
  }

  @Override
  public UpdateSchema updateSchema() {
    return transaction.updateSchema();
  }

  @Override
  public UpdatePartitionSpec updateSpec() {
    return transaction.updateSpec();
  }

  @Override
  public UpdateProperties updateProperties() {
    return transaction.updateProperties();
  }

  @Override
  public ReplaceSortOrder replaceSortOrder() {
    return transaction.replaceSortOrder();
  }

  @Override
  public UpdateLocation updateLocation() {
    return transaction.updateLocation();
  }

  @Override
  public AppendFiles newAppend() {
    return MixedAppendFiles.buildFor(mixedTable, false).inTransaction(transaction).build();
  }

  @Override
  public AppendFiles newFastAppend() {
    return MixedAppendFiles.buildFor(mixedTable, true).inTransaction(transaction).build();
  }

  @Override
  public RewriteFiles newRewrite() {
    return MixedRewriteFiles.buildFor(mixedTable).inTransaction(transaction).build();
  }

  @Override
  public RewriteManifests rewriteManifests() {
    return transaction.rewriteManifests();
  }

  @Override
  public OverwriteFiles newOverwrite() {
    return MixedOverwriteFiles.buildFor(mixedTable).inTransaction(transaction).build();
  }

  @Override
  public RowDelta newRowDelta() {
    return MixedRowDelta.buildFor(mixedTable).inTransaction(transaction).build();
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    return MixedReplacePartitions.buildFor(mixedTable).inTransaction(transaction).build();
  }

  @Override
  public DeleteFiles newDelete() {
    return MixedDeleteFiles.buildFor(mixedTable).inTransaction(transaction).build();
  }

  @Override
  public ExpireSnapshots expireSnapshots() {
    return transaction.expireSnapshots();
  }

  @Override
  public UpdateStatistics updateStatistics() {
    return transaction.updateStatistics();
  }

  @Override
  public void commitTransaction() {
    transaction.commitTransaction();
  }

  class TransactionTable implements Table, HasTableOperations, Serializable {

    Table transactionTable;

    public TransactionTable() {
      transactionTable = transaction.table();
    }

    @Override
    public TableOperations operations() {
      if (transactionTable instanceof HasTableOperations) {
        return ((HasTableOperations) transactionTable).operations();
      }
      throw new IllegalStateException("table does not support operations");
    }

    @Override
    public String name() {
      return transactionTable.name();
    }

    @Override
    public void refresh() {
      transactionTable.refresh();
    }

    @Override
    public TableScan newScan() {
      return transactionTable.newScan();
    }

    @Override
    public Schema schema() {
      return transactionTable.schema();
    }

    @Override
    public Map<Integer, Schema> schemas() {
      return transactionTable.schemas();
    }

    @Override
    public PartitionSpec spec() {
      return transactionTable.spec();
    }

    @Override
    public Map<Integer, PartitionSpec> specs() {
      return transactionTable.specs();
    }

    @Override
    public SortOrder sortOrder() {
      return transactionTable.sortOrder();
    }

    @Override
    public Map<Integer, SortOrder> sortOrders() {
      return transactionTable.sortOrders();
    }

    @Override
    public Map<String, String> properties() {
      return transactionTable.properties();
    }

    @Override
    public String location() {
      return transactionTable.location();
    }

    @Override
    public Snapshot currentSnapshot() {
      return transactionTable.currentSnapshot();
    }

    @Override
    public Snapshot snapshot(long snapshotId) {
      return transactionTable.snapshot(snapshotId);
    }

    @Override
    public Iterable<Snapshot> snapshots() {
      return transactionTable.snapshots();
    }

    @Override
    public List<HistoryEntry> history() {
      return transactionTable.history();
    }

    @Override
    public UpdateSchema updateSchema() {
      return transactionTable.updateSchema();
    }

    @Override
    public UpdatePartitionSpec updateSpec() {
      return transactionTable.updateSpec();
    }

    @Override
    public UpdateProperties updateProperties() {
      return transactionTable.updateProperties();
    }

    @Override
    public ReplaceSortOrder replaceSortOrder() {
      return transactionTable.replaceSortOrder();
    }

    @Override
    public UpdateLocation updateLocation() {
      return transactionTable.updateLocation();
    }

    @Override
    public AppendFiles newAppend() {
      return transactionTable.newAppend();
    }

    @Override
    public AppendFiles newFastAppend() {
      return transactionTable.newFastAppend();
    }

    @Override
    public RewriteFiles newRewrite() {
      return transactionTable.newRewrite();
    }

    @Override
    public RewriteManifests rewriteManifests() {
      return transactionTable.rewriteManifests();
    }

    @Override
    public OverwriteFiles newOverwrite() {
      return transactionTable.newOverwrite();
    }

    @Override
    public RowDelta newRowDelta() {
      return transactionTable.newRowDelta();
    }

    @Override
    public ReplacePartitions newReplacePartitions() {
      return transactionTable.newReplacePartitions();
    }

    @Override
    public DeleteFiles newDelete() {
      return transactionTable.newDelete();
    }

    @Override
    public ExpireSnapshots expireSnapshots() {
      return transactionTable.expireSnapshots();
    }

    @Override
    public ManageSnapshots manageSnapshots() {
      return transactionTable.manageSnapshots();
    }

    @Override
    public Transaction newTransaction() {
      return transactionTable.newTransaction();
    }

    @Override
    public FileIO io() {
      return transactionTable.io();
    }

    @Override
    public EncryptionManager encryption() {
      return transactionTable.encryption();
    }

    @Override
    public LocationProvider locationProvider() {
      return transactionTable.locationProvider();
    }

    @Override
    public List<StatisticsFile> statisticsFiles() {
      return transactionTable.statisticsFiles();
    }

    @Override
    public Map<String, SnapshotRef> refs() {
      return transactionTable.refs();
    }

    @Override
    public UpdateStatistics updateStatistics() {
      return transactionTable.updateStatistics();
    }

    @Override
    public String toString() {
      return transactionTable.toString();
    }
  }
}
