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

package org.apache.amoro.table;

import org.apache.amoro.TableFormat;
import org.apache.amoro.io.AuthenticatedFileIO;
import org.apache.amoro.op.KeyedPartitionRewrite;
import org.apache.amoro.op.KeyedSchemaUpdate;
import org.apache.amoro.op.OverwriteBaseFiles;
import org.apache.amoro.op.RewritePartitions;
import org.apache.amoro.op.SnapshotSummary;
import org.apache.amoro.op.UpdateKeyedTableProperties;
import org.apache.amoro.scan.BasicKeyedTableScan;
import org.apache.amoro.scan.ChangeTableIncrementalScan;
import org.apache.amoro.scan.KeyedTableScan;
import org.apache.amoro.shade.guava32.com.google.common.collect.ImmutableMap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.utils.TablePropertyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.iceberg.AppendFiles;
import org.apache.iceberg.MixedChangeTableScan;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.events.CreateSnapshotEvent;

import java.util.Map;

/**
 * Basic implementation of {@link KeyedTable}, wrapping a {@link BaseTable} and a {@link
 * ChangeTable}.
 */
public class BasicKeyedTable implements KeyedTable {
  private final String tableLocation;
  private final PrimaryKeySpec primaryKeySpec;

  protected final BaseTable baseTable;
  protected final ChangeTable changeTable;

  public BasicKeyedTable(
      String tableLocation, PrimaryKeySpec keySpec, BaseTable baseTable, ChangeTable changeTable) {
    this.tableLocation = tableLocation;
    this.primaryKeySpec = keySpec;
    this.baseTable = baseTable;
    this.changeTable = changeTable;
  }

  public BasicKeyedTable(PrimaryKeySpec keySpec, BaseTable baseTable, ChangeTable changeTable) {
    this.tableLocation = null;
    this.primaryKeySpec = keySpec;
    this.baseTable = baseTable;
    this.changeTable = changeTable;
  }

  @Override
  public Schema schema() {
    KeyedSchemaUpdate.syncSchema(this);
    return baseTable.schema();
  }

  @Override
  public PartitionSpec spec() {
    return baseTable.spec();
  }

  @Override
  public TableIdentifier id() {
    return baseTable.id();
  }

  @Override
  public TableFormat format() {
    return TableFormat.MIXED_ICEBERG;
  }

  @Override
  public PrimaryKeySpec primaryKeySpec() {
    return primaryKeySpec;
  }

  @Override
  public String location() {
    if (StringUtils.isEmpty(this.tableLocation)) {
      return this.baseTable.location();
    } else {
      return this.tableLocation;
    }
  }

  @Override
  public String baseLocation() {
    return baseTable.location();
  }

  @Override
  public String changeLocation() {
    return changeTable.location();
  }

  @Override
  public Map<String, String> properties() {
    long changeWatermark = TablePropertyUtil.getTableWatermark(changeTable.properties());
    long baseWatermark = TablePropertyUtil.getTableWatermark(baseTable.properties());

    Map<String, String> properties = Maps.newHashMap();
    if (changeWatermark > baseWatermark) {
      baseTable
          .properties()
          .forEach(
              (k, v) -> {
                if (!TableProperties.WATERMARK_TABLE.equals(k)) {
                  properties.put(k, v);
                }
              });
      properties.put(TableProperties.WATERMARK_TABLE, String.valueOf(changeWatermark));
    } else {
      properties.putAll(baseTable.properties());
    }
    properties.put(TableProperties.WATERMARK_BASE_STORE, String.valueOf(baseWatermark));
    return ImmutableMap.copyOf(properties);
  }

  @Override
  public AuthenticatedFileIO io() {
    return baseTable.io();
  }

  @Override
  public void refresh() {
    baseTable.refresh();
    if (primaryKeySpec().primaryKeyExisted()) {
      changeTable.refresh();
    }
  }

  @Override
  public BaseTable baseTable() {
    return baseTable;
  }

  @Override
  public ChangeTable changeTable() {
    return changeTable;
  }

  @Override
  public KeyedTableScan newScan() {
    return new BasicKeyedTableScan(this);
  }

  @Override
  public UpdateSchema updateSchema() {
    if (PrimaryKeySpec.noPrimaryKey().equals(primaryKeySpec())) {
      return baseTable().updateSchema();
    }
    return new KeyedSchemaUpdate(this);
  }

  @Override
  public UpdateProperties updateProperties() {
    return new UpdateKeyedTableProperties(this);
  }

  @Override
  public long beginTransaction(String signature) {
    // commit an empty snapshot to ChangeStore, and use the sequence of this empty snapshot as
    // TransactionId
    AppendFiles appendFiles = changeTable.newAppend();
    appendFiles.set(
        SnapshotSummary.TRANSACTION_BEGIN_SIGNATURE, signature == null ? "" : signature);
    appendFiles.commit();
    CreateSnapshotEvent createSnapshotEvent = (CreateSnapshotEvent) appendFiles.updateEvent();
    return createSnapshotEvent.sequenceNumber();
  }

  @Override
  public String toString() {
    return name();
  }

  @Override
  public OverwriteBaseFiles newOverwriteBaseFiles() {
    return new OverwriteBaseFiles(this);
  }

  @Override
  public RewritePartitions newRewritePartitions() {
    return new KeyedPartitionRewrite(this);
  }

  public static class BaseInternalTable extends BasicUnkeyedTable implements BaseTable {

    public BaseInternalTable(
        TableIdentifier tableIdentifier,
        Table baseIcebergTable,
        AuthenticatedFileIO authenticatedFileIO,
        Map<String, String> catalogProperties) {
      super(tableIdentifier, baseIcebergTable, authenticatedFileIO, catalogProperties);
    }
  }

  public static class ChangeInternalTable extends BasicUnkeyedTable implements ChangeTable {

    public ChangeInternalTable(
        TableIdentifier tableIdentifier,
        Table changeIcebergTable,
        AuthenticatedFileIO authenticatedFileIO,
        Map<String, String> catalogProperties) {
      super(tableIdentifier, changeIcebergTable, authenticatedFileIO, catalogProperties);
    }

    @Override
    public ChangeTableIncrementalScan newScan() {
      return new MixedChangeTableScan(this, schema());
    }
  }
}
