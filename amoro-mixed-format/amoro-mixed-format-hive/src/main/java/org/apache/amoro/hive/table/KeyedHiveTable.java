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

import org.apache.amoro.TableFormat;
import org.apache.amoro.api.TableMeta;
import org.apache.amoro.hive.HMSClientPool;
import org.apache.amoro.hive.op.BaseSchemaUpdate;
import org.apache.amoro.hive.utils.HiveMetaSynchronizer;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.io.AuthenticatedHadoopFileIO;
import org.apache.amoro.properties.HiveTableProperties;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.amoro.table.BaseTable;
import org.apache.amoro.table.BasicKeyedTable;
import org.apache.amoro.table.BasicUnkeyedTable;
import org.apache.amoro.table.ChangeTable;
import org.apache.amoro.table.KeyedTable;
import org.apache.amoro.table.PrimaryKeySpec;
import org.apache.amoro.table.TableIdentifier;
import org.apache.iceberg.MixedChangeTableScan;
import org.apache.iceberg.Table;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.util.PropertyUtil;

import java.util.Map;

/** Implementation of {@link KeyedTable} with Hive table as base store. */
public class KeyedHiveTable extends BasicKeyedTable implements SupportHive {

  private final HMSClientPool hiveClient;

  public KeyedHiveTable(
      TableMeta tableMeta,
      String tableLocation,
      PrimaryKeySpec primaryKeySpec,
      HMSClientPool hiveClient,
      UnkeyedHiveTable baseTable,
      ChangeTable changeTable) {
    super(tableLocation, primaryKeySpec, baseTable, changeTable);
    this.hiveClient = hiveClient;
    if (enableSyncHiveSchemaToMixedTable()) {
      syncHiveSchemaToMixedTable();
    }
    if (enableSyncHiveDataToMixedTable()) {
      syncHiveDataToMixedTable(false);
    }
  }

  @Override
  public AuthenticatedHadoopFileIO io() {
    return (AuthenticatedHadoopFileIO) super.io();
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_HIVE;
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
  public String hiveLocation() {
    return ((SupportHive) baseTable()).hiveLocation();
  }

  @Override
  public boolean enableSyncHiveSchemaToMixedTable() {
    return PropertyUtil.propertyAsBoolean(
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
    return PropertyUtil.propertyAsBoolean(
        properties(),
        HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE,
        HiveTableProperties.AUTO_SYNC_HIVE_DATA_WRITE_DEFAULT);
  }

  @Override
  public void syncHiveDataToMixedTable(boolean force) {
    HiveMetaSynchronizer.syncHiveDataToMixedTable(this, hiveClient, force);
  }

  @Override
  public HMSClientPool getHMSClient() {
    return hiveClient;
  }

  public static class HiveChangeInternalTable extends BasicUnkeyedTable implements ChangeTable {

    public HiveChangeInternalTable(
        TableIdentifier tableIdentifier,
        Table changeIcebergTable,
        AuthenticatedFileIO authenticatedFileIO,
        Map<String, String> catalogProperties) {
      super(tableIdentifier, changeIcebergTable, authenticatedFileIO, catalogProperties);
    }

    @Override
    public TableFormat format() {
      return TableFormat.MIXED_HIVE;
    }

    @Override
    public UpdateSchema updateSchema() {
      return new BaseSchemaUpdate(this, super.updateSchema());
    }

    @Override
    public ChangeTableIncrementalScan newScan() {
      return new MixedChangeTableScan(this, schema());
    }
  }

  public static class HiveBaseInternalTable extends UnkeyedHiveTable implements BaseTable {

    public HiveBaseInternalTable(
        TableIdentifier tableIdentifier,
        Table icebergTable,
        AuthenticatedHadoopFileIO fileIO,
        String tableLocation,
        HMSClientPool hiveClient,
        Map<String, String> catalogProperties,
        boolean syncHiveChange) {
      super(
          tableIdentifier,
          icebergTable,
          fileIO,
          tableLocation,
          hiveClient,
          catalogProperties,
          syncHiveChange);
    }

    @Override
    public TableFormat format() {
      return TableFormat.MIXED_HIVE;
    }
  }
}
