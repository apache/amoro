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

package com.netease.arctic.table;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.TableMeta;
import com.netease.arctic.io.ArcticFileIO;
import com.netease.arctic.op.KeyedSchemaUpdate;
import com.netease.arctic.op.OverwriteBaseFiles;
import com.netease.arctic.op.RewritePartitions;
import com.netease.arctic.op.UpdateKeyedTableProperties;
import com.netease.arctic.scan.BaseKeyedTableScan;
import com.netease.arctic.scan.KeyedTableScan;
import com.netease.arctic.trace.AmsTableTracer;
import com.netease.arctic.trace.TracedUpdateProperties;
import com.netease.arctic.trace.TrackerOperations;
import com.netease.arctic.utils.TablePropertyUtil;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.UpdateProperties;
import org.apache.iceberg.UpdateSchema;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.Map;

/**
 * Base implementation of {@link KeyedTable}, wrapping a {@link BaseTable} and a {@link ChangeTable}.
 */
public class BaseKeyedTable implements KeyedTable {
  private final String tableLocation;
  private final PrimaryKeySpec primaryKeySpec;
  protected final AmsClient client;

  protected final TableIdentifier identifier;
  protected final BaseTable baseTable;
  protected final ChangeTable changeTable;
  protected TableMeta tableMeta;

  public BaseKeyedTable(
      TableMeta tableMeta,
      String tableLocation,
      AmsClient client,
      ArcticFileIO arcticFileIO,
      TableOperations baseTableOps,
      TableOperations changeTableOps) {
    this.tableMeta = tableMeta;
    this.tableLocation = tableLocation;
    this.client = client;

    this.identifier = TableIdentifier.of(tableMeta.getTableIdentifier());

    this.baseTable = createBaseTable(baseTableOps, arcticFileIO);
    this.changeTable = createChangeTable(changeTableOps, arcticFileIO);

    PrimaryKeySpec.Builder builder = PrimaryKeySpec.builderFor(baseTable.schema());
    if (tableMeta.getKeySpec() != null &&
        tableMeta.getKeySpec().getFields() != null &&
        tableMeta.getKeySpec().getFields().size() > 0) {
      for (String field : tableMeta.getKeySpec().getFields()) {
        builder.addColumn(field);
      }
    }
    this.primaryKeySpec = builder.build();
  }

  protected BaseTable createBaseTable(TableOperations ops, ArcticFileIO fileIO) {
    return new BaseInternalTable(
        this.identifier, new org.apache.iceberg.BaseTable(ops, this.identifier.getTableName()),
        fileIO, client);
  }

  protected ChangeTable createChangeTable(TableOperations ops, ArcticFileIO fileIO) {
    return new ChangeInternalTable(
        this.identifier, new org.apache.iceberg.BaseTable(ops, this.identifier.getTableName()),
        fileIO, client);
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
    return TableIdentifier.of(tableMeta.getTableIdentifier());
  }

  @Override
  public PrimaryKeySpec primaryKeySpec() {
    return primaryKeySpec;
  }

  @Override
  public String location() {
    return tableLocation;
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
    Map<String, String> props = Maps.newHashMap();
    props.putAll(this.tableMeta.getProperties());
    return props;
  }

  @Override
  public ArcticFileIO io() {
    return baseTable.io();
  }

  @Override
  public void refresh() {
    try {
      this.tableMeta = client.getTable(this.tableMeta.getTableIdentifier());
    } catch (TException e) {
      throw new IllegalStateException("failed refresh table from ams", e);
    }

    baseTable.refresh();
    if (PrimaryKeySpec.noPrimaryKey().equals(primaryKeySpec())) {
      return;
    }
    changeTable.refresh();
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
    return new BaseKeyedTableScan(this);
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
    UpdateProperties updateProperties = new UpdateKeyedTableProperties(this, tableMeta);
    if (client != null) {
      AmsTableTracer tracer = new AmsTableTracer(this, TrackerOperations.UPDATE_PROPERTIES, client);
      return new TracedUpdateProperties(updateProperties, tracer);
    } else {
      return updateProperties;
    }
  }

  @Override
  public long beginTransaction(String signature) {
    try {
      return client.allocateTransactionId(this.tableMeta.getTableIdentifier(), signature);
    } catch (TException e) {
      throw new IllegalStateException("failed begin transaction", e);
    }
  }

  @Override
  public String toString() {
    return name();
  }

  public class BaseInternalTable extends BaseUnkeyedTable implements BaseTable {

    public BaseInternalTable(
        TableIdentifier tableIdentifier, Table baseIcebergTable, ArcticFileIO arcticFileIO,
        AmsClient client) {
      super(tableIdentifier, baseIcebergTable, arcticFileIO, client);
    }

    @Override
    public RewritePartitions newRewritePartition() {
      return new RewritePartitions(this);
    }

    @Override
    public OverwriteBaseFiles newOverwriteBaseFiles() {
      return new OverwriteBaseFiles(BaseKeyedTable.this);
    }

    @Override
    public Map<String, Long> maxTransactionId() {
      String s = properties().get(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID);
      if (s != null) {
        try {
          Map<String, Long> results = Maps.newHashMap();
          Map map = new ObjectMapper().readValue(s, Map.class);
          for (Object key : map.keySet()) {
            results.put(key.toString(), Long.parseLong(map.get(key).toString()));
          }
          return results;
        } catch (JsonProcessingException e) {
          throw new UnsupportedOperationException("Failed to get " + TableProperties.BASE_TABLE_MAX_TRANSACTION_ID, e);
        }
      } else {
        return Collections.emptyMap();
      }
    }

    @Override
    public StructLikeMap<Long> partitionMaxTransactionId() {
      String s = properties().get(TableProperties.BASE_TABLE_MAX_TRANSACTION_ID);
      if (s != null) {
        return TablePropertyUtil.decodePartitionMaxTxId(spec(), s);
      } else {
        return StructLikeMap.create(spec().partitionType());
      }
    }
  }

  public static class ChangeInternalTable extends BaseUnkeyedTable implements ChangeTable {

    public ChangeInternalTable(
        TableIdentifier tableIdentifier, Table changeIcebergTable, ArcticFileIO arcticFileIO,
        AmsClient client) {
      super(tableIdentifier, changeIcebergTable, arcticFileIO, client);
    }
  }
}