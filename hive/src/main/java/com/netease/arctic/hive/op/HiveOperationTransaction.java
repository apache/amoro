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

package com.netease.arctic.hive.op;

import com.google.common.collect.Lists;
import com.netease.arctic.hive.HMSClient;
import com.netease.arctic.hive.table.UnkeyedHiveTable;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.DeleteFiles;
import org.apache.iceberg.ExpireSnapshots;
import org.apache.iceberg.HistoryEntry;
import org.apache.iceberg.ManageSnapshots;
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.ReplaceSortOrder;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RewriteManifests;
import org.apache.iceberg.Rollback;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Snapshot;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableScan;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateLocation;
import org.apache.iceberg.UpdatePartitionSpec;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.encryption.EncryptionManager;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.io.LocationProvider;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

public class HiveOperationTransaction implements Transaction {

  private UnkeyedHiveTable unkeyedHiveTable;
  private Transaction wrapped;
  private HMSClient client;
  private TransactionalHMSClient transactionalClient;

  private TransactionalTable transactionalTable;

  public HiveOperationTransaction(
      UnkeyedHiveTable unkeyedHiveTable,
      Transaction wrapped,
      HMSClient client) {
    this.unkeyedHiveTable = unkeyedHiveTable;
    this.wrapped = wrapped;
    this.client = client;
    this.transactionalTable = new TransactionalTable();
    this.transactionalClient = new TransactionalHMSClient();
  }

  @Override
  public Table table() {
    return transactionalTable;
  }

  @Override
  public UpdateSchema updateSchema() {
    return wrapped.updateSchema();
  }

  @Override
  public UpdatePartitionSpec updateSpec() {
    return wrapped.updateSpec();
  }

  @Override
  public UpdateProperties updateProperties() {
    return wrapped.updateProperties();
  }

  @Override
  public ReplaceSortOrder replaceSortOrder() {
    return wrapped.replaceSortOrder();
  }

  @Override
  public UpdateLocation updateLocation() {
    return wrapped.updateLocation();
  }

  @Override
  public AppendFiles newAppend() {
    return wrapped.newAppend();
  }

  @Override
  public AppendFiles newFastAppend() {
    return wrapped.newFastAppend();
  }

  @Override
  public RewriteFiles newRewrite() {
    return wrapped.newRewrite();
  }

  @Override
  public RewriteManifests rewriteManifests() {
    return wrapped.rewriteManifests();
  }

  @Override
  public OverwriteFiles newOverwrite() {
    OverwriteFiles overwriteFiles = wrapped.newOverwrite();
    return new OverwriteHiveFiles(overwriteFiles, unkeyedHiveTable, client, transactionalClient);
  }

  @Override
  public RowDelta newRowDelta() {
    return wrapped.newRowDelta();
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    ReplacePartitions r = wrapped.newReplacePartitions();
    return new ReplaceHivePartitions(r, unkeyedHiveTable, client, transactionalClient);
  }

  @Override
  public DeleteFiles newDelete() {
    return wrapped.newDelete();
  }

  @Override
  public ExpireSnapshots expireSnapshots() {
    return wrapped.expireSnapshots();
  }

  @Override
  public void commitTransaction() {
    wrapped.commitTransaction();
    transactionalClient.commit();
  }

  private class TransactionalHMSClient implements HMSClient {
    List<Action<?, HiveMetaStoreClient, TException>> pendingActions = Lists.newArrayList();

    @Override
    public <R> R run(Action<R, HiveMetaStoreClient, TException> action) {
      pendingActions.add(action);
      return null;
    }

    public void commit() {
      for (Action<?, HiveMetaStoreClient, TException> action : pendingActions) {
        try {
          client.run(action);
        } catch (TException | InterruptedException e) {
          throw new RuntimeException("execute pending hive operation failed.", e);
        }
      }
    }
  }

  private class TransactionalTable implements Table {

    @Override
    public String name() {
      return unkeyedHiveTable.name();
    }

    @Override
    public void refresh() {

    }

    @Override
    public TableScan newScan() {
      throw new UnsupportedOperationException("Transaction tables do not support scans");
    }

    @Override
    public Schema schema() {
      return unkeyedHiveTable.schema();
    }

    @Override
    public Map<Integer, Schema> schemas() {
      return unkeyedHiveTable.schemas();
    }

    @Override
    public PartitionSpec spec() {
      return unkeyedHiveTable.spec();
    }

    @Override
    public Map<Integer, PartitionSpec> specs() {
      return unkeyedHiveTable.specs();
    }

    @Override
    public SortOrder sortOrder() {
      return unkeyedHiveTable.sortOrder();
    }

    @Override
    public Map<Integer, SortOrder> sortOrders() {
      return unkeyedHiveTable.sortOrders();
    }

    @Override
    public Map<String, String> properties() {
      return unkeyedHiveTable.properties();
    }

    @Override
    public String location() {
      return unkeyedHiveTable.location();
    }

    @Override
    public Snapshot currentSnapshot() {
      return unkeyedHiveTable.currentSnapshot();
    }

    @Override
    public Snapshot snapshot(long snapshotId) {
      return unkeyedHiveTable.currentSnapshot();
    }

    @Override
    public Iterable<Snapshot> snapshots() {
      return unkeyedHiveTable.snapshots();
    }

    @Override
    public List<HistoryEntry> history() {
      return unkeyedHiveTable.history();
    }

    @Override
    public UpdateSchema updateSchema() {
      return HiveOperationTransaction.this.updateSchema();
    }

    @Override
    public UpdatePartitionSpec updateSpec() {
      return HiveOperationTransaction.this.updateSpec();
    }

    @Override
    public UpdateProperties updateProperties() {
      return HiveOperationTransaction.this.updateProperties();
    }

    @Override
    public ReplaceSortOrder replaceSortOrder() {
      return HiveOperationTransaction.this.replaceSortOrder();
    }

    @Override
    public UpdateLocation updateLocation() {
      return HiveOperationTransaction.this.updateLocation();
    }

    @Override
    public AppendFiles newAppend() {
      return HiveOperationTransaction.this.newAppend();
    }

    @Override
    public AppendFiles newFastAppend() {
      return HiveOperationTransaction.this.newFastAppend();
    }

    @Override
    public RewriteFiles newRewrite() {
      return HiveOperationTransaction.this.newRewrite();
    }

    @Override
    public RewriteManifests rewriteManifests() {
      return HiveOperationTransaction.this.rewriteManifests();
    }

    @Override
    public OverwriteFiles newOverwrite() {
      return HiveOperationTransaction.this.newOverwrite();
    }

    @Override
    public RowDelta newRowDelta() {
      return HiveOperationTransaction.this.newRowDelta();
    }

    @Override
    public ReplacePartitions newReplacePartitions() {
      return HiveOperationTransaction.this.newReplacePartitions();
    }

    @Override
    public DeleteFiles newDelete() {
      return HiveOperationTransaction.this.newDelete();
    }

    @Override
    public ExpireSnapshots expireSnapshots() {
      return HiveOperationTransaction.this.expireSnapshots();
    }

    @Override
    public Rollback rollback() {
      throw new UnsupportedOperationException("Transaction tables do not support rollback");
    }

    @Override
    public ManageSnapshots manageSnapshots() {
      throw new UnsupportedOperationException("Transaction tables do not support rollback");
    }

    @Override
    public Transaction newTransaction() {
      throw new UnsupportedOperationException("Transaction tables do not support rollback");
    }

    @Override
    public FileIO io() {
      return unkeyedHiveTable.io();
    }

    @Override
    public EncryptionManager encryption() {
      return unkeyedHiveTable.encryption();
    }

    @Override
    public LocationProvider locationProvider() {
      return unkeyedHiveTable.locationProvider();
    }
  }
}
