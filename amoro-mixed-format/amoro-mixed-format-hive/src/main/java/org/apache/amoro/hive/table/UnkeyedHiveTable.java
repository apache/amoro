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

package org.apache.amoro.hive.table;

import static org.apache.amoro.properties.HiveTableProperties.BASE_HIVE_LOCATION_ROOT;

import org.apache.amoro.TableFormat;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.op.HiveOperationTransaction;
import org.apache.amoro.hive.op.HiveSchemaUpdate;
import org.apache.amoro.hive.op.OverwriteHiveFiles;
import org.apache.amoro.hive.op.ReplaceHivePartitions;
import org.apache.amoro.hive.op.RewriteHiveFiles;
import org.apache.amoro.hive.utils.HiveMetaSynchronizer;
import org.apache.amoro.hive.utils.HiveTableUtil;
import org.apache.amoro.io.AuthenticatedHadoopFileIO;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.TableIdentifier;
import org.apache.amoro.table.UnkeyedTable;
import org.apache.iceberg.ReplacePartitions;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

/** Implementation of {@link UnkeyedTable} with Hive table as base store. */
public class UnkeyedHiveTable extends BasicUnkeyedTable implements BaseTable, SupportHive {

  private final HMSClientPool hiveClient;
  private final String tableLocation;

  private boolean syncHiveChange = true;
  private final AuthenticatedHadoopFileIO fileIO;

  public UnkeyedHiveTable(
      TableIdentifier tableIdentifier,
      Table icebergTable,
      AuthenticatedHadoopFileIO fileIO,
      String tableLocation,
      HMSClientPool hiveClient,
      Map<String, String> catalogProperties) {
    this(tableIdentifier, icebergTable, fileIO, tableLocation, hiveClient, catalogProperties, true);
  }

  public UnkeyedHiveTable(
      TableIdentifier tableIdentifier,
      Table icebergTable,
      AuthenticatedHadoopFileIO fileIO,
      String tableLocation,
      HMSClientPool hiveClient,
      Map<String, String> catalogProperties,
      boolean syncHiveChange) {
    super(tableIdentifier, icebergTable, fileIO, catalogProperties);
    this.fileIO = fileIO;
    this.hiveClient = hiveClient;
    this.tableLocation = tableLocation;
    this.syncHiveChange = syncHiveChange;
    if (enableSyncHiveSchemaToMixedTable()) {
      syncHiveSchemaToMixedTable();
    }
    if (enableSyncHiveDataToMixedTable()) {
      syncHiveDataToMixedTable(false);
    }
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_HIVE;
  }

  @Override
  public AuthenticatedHadoopFileIO io() {
    return this.fileIO;
  }

  @Override
  public void refresh() {
    super.refresh();
    if (enableSyncHiveSchemaToMixedTable()) {
      syncHiveSchemaToMixedTable();
    }
    if (enableSyncHiveDataToMixedTable()) {
      syncHiveDataToMixedTable(false);
    }
  }

  @Override
  public Schema schema() {
    return super.schema();
  }

  @Override
  public ReplacePartitions newReplacePartitions() {
    return new ReplaceHivePartitions(super.newTransaction(), false, this, hiveClient, hiveClient);
  }

  @Override
  public String name() {
    return id().getTableName();
  }

  @Override
  public String hiveLocation() {
    return properties().containsKey(BASE_HIVE_LOCATION_ROOT)
        ? properties().get(BASE_HIVE_LOCATION_ROOT)
        : HiveTableUtil.hiveRootLocation(tableLocation);
  }

  @Override
  public HMSClientPool getHMSClient() {
    return hiveClient;
  }

  @Override
  public OverwriteHiveFiles newOverwrite() {
    return new OverwriteHiveFiles(super.newTransaction(), false, this, hiveClient, hiveClient);
  }

  @Override
  public RewriteHiveFiles newRewrite() {
    return new RewriteHiveFiles(super.newTransaction(), false, this, hiveClient, hiveClient);
  }

  @Override
  public Transaction newTransaction() {
    Transaction transaction = super.newTransaction();
    return new HiveOperationTransaction(this, transaction, hiveClient);
  }

  @Override
  public UpdateSchema updateSchema() {
    return new HiveSchemaUpdate(this, hiveClient, hiveClient, super.updateSchema());
  }

  @Override
  public boolean enableSyncHiveSchemaToMixedTable() {
    return syncHiveChange
        && PropertyUtil.propertyAsBoolean(
            properties(),
            HiveTableProperties.AUTO_SYNC_HIVE_SCHEMA_CHANGE,
            HiveTableProperties.AUTO_SYNC_HIVE_SCHEMA_CHANGE_DEFAULT);
  }

  @Override
  public void syncHiveSchemaToMixedTable() {
    HiveMetaSynchronizer.syncHiveSchemaToMixedTable(this, hiveClient);
  }

  @Override
  public boolean enableSyncHiveDataToMixedTable() {
    return syncHiveChange
        && PropertyUtil.propertyAsBoolean(
            properties(),
            HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE,
            HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE_DEFAULT);
  }

  @Override
  public void syncHiveDataToMixedTable(boolean force) {
    HiveMetaSynchronizer.syncHiveDataToMixedTable(this, hiveClient, force);
  }
}
