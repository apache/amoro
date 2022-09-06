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
import org.apache.iceberg.OverwriteFiles;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.ReplaceSortOrder;
import org.apache.iceberg.RewriteFiles;
import org.apache.iceberg.RewriteManifests;
import org.apache.iceberg.RowDelta;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateLocation;
import org.apache.iceberg.UpdatePartitionSpec;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.thrift.TException;

import java.util.List;

public class HiveOperationTransaction implements Transaction {

  private final UnkeyedHiveTable unkeyedHiveTable;
  private final Transaction wrapped;
  private final HMSClient client;
  private final TransactionalHMSClient transactionalClient;

  public HiveOperationTransaction(
      UnkeyedHiveTable unkeyedHiveTable,
      Transaction wrapped,
      HMSClient client) {
    this.unkeyedHiveTable = unkeyedHiveTable;
    this.wrapped = wrapped;
    this.client = client;
    this.transactionalClient = new TransactionalHMSClient();
  }

  @Override
  public Table table() {
    return wrapped.table();
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
    return new OverwriteHiveFiles(wrapped, true, unkeyedHiveTable, client, transactionalClient);
  }

  @Override
  public RowDelta newRowDelta() {
    return wrapped.newRowDelta();
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    return new ReplaceHivePartitions(wrapped, true, unkeyedHiveTable, client, transactionalClient);
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
}
